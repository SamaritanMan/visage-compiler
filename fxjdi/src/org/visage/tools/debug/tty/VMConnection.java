/*
 * Copyright 1998-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package org.visage.tools.debug.tty;

import org.visage.jdi.VisageBootstrap;
import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ThreadStartRequest;
import com.sun.jdi.request.ThreadDeathRequest;

import java.util.*;
import java.util.regex.*;
import java.io.*;

class VMConnection {

    private final Env env;
    private VirtualMachine vm;
    private Process process = null;
    private int outputCompleteCount = 0;

    private final Connector connector;
    private final Map<String, com.sun.jdi.connect.Connector.Argument> connectorArgs;
    private final int traceFlags;

    synchronized void notifyOutputComplete() {
        outputCompleteCount++;
        notifyAll();
    }

    synchronized void waitOutputComplete() {
        // Wait for stderr and stdout
        if (process != null) {
            while (outputCompleteCount < 2) {
                try {wait();} catch (InterruptedException e) {}
            }
        }
    }

    private Connector findConnector(String name) {
        for (Connector connector :
                 VisageBootstrap.virtualMachineManager().allConnectors()) {
            if (connector.name().equals(name)) {
                return connector;
            }
        }
        return null;
    }

    private Map <String, com.sun.jdi.connect.Connector.Argument> parseConnectorArgs(Connector connector, String argString) {
        Map<String, com.sun.jdi.connect.Connector.Argument> arguments = connector.defaultArguments();

        /*
         * We are parsing strings of the form:
         *    name1=value1,[name2=value2,...]
         * However, the value1...valuen substrings may contain
         * embedded comma(s), so make provision for quoting inside
         * the value substrings. (Bug ID 4285874)
         */
        String regexPattern =
            "(quote=[^,]+,)|" +           // special case for quote=.,
            "(\\w+=)" +                   // name=
            "(((\"[^\"]*\")|" +           //   ( "l , ue"
            "('[^']*')|" +                //     'l , ue'
            "([^,'\"]+))+,)";             //     v a l u e )+ ,
        Pattern p = Pattern.compile(regexPattern);
        Matcher m = p.matcher(argString);
        while (m.find()) {
            int startPosition = m.start();
            int endPosition = m.end();
            if (startPosition > 0) {
                /*
                 * It is an error if parsing skips over any part of argString.
                 */
                throw new IllegalArgumentException
                    (MessageOutput.format("Illegal connector argument",
                                          argString));
            }

            String token = argString.substring(startPosition, endPosition);
            int index = token.indexOf('=');
            String name = token.substring(0, index);
            String value = token.substring(index + 1,
                                           token.length() - 1); // Remove comma delimiter

            Connector.Argument argument = arguments.get(name);
            if (argument == null) {
                throw new IllegalArgumentException
                    (MessageOutput.format("Argument is not defined for connector:",
                                          new Object [] {name, connector.name()}));
            }
            argument.setValue(value);

            argString = argString.substring(endPosition); // Remove what was just parsed...
            m = p.matcher(argString);                     //    and parse again on what is left.
        }
        if ((! argString.equals(",")) && (argString.length() > 0)) {
            /*
             * It is an error if any part of argString is left over,
             * unless it was empty to begin with.
             */
            throw new IllegalArgumentException
                (MessageOutput.format("Illegal connector argument", argString));
        }
        return arguments;
    }

    VMConnection(Env env, String connectSpec, int traceFlags) {
        String nameString;
        String argString;
        int index = connectSpec.indexOf(':');
        if (index == -1) {
            nameString = connectSpec;
            argString = "";
        } else {
            nameString = connectSpec.substring(0, index);
            argString = connectSpec.substring(index + 1);
        }

        this.env = env;
        this.connector = findConnector(nameString);
        if (connector == null) {
            throw new IllegalArgumentException
                (MessageOutput.format("No connector named:", nameString));
        }

        this.connectorArgs = parseConnectorArgs(connector, argString);
        this.traceFlags = traceFlags;
    }

    synchronized VirtualMachine open() {
        if (connector instanceof LaunchingConnector) {
            vm = launchTarget();
        } else if (connector instanceof AttachingConnector) {
            vm = attachTarget();
        } else if (connector instanceof ListeningConnector) {
            vm = listenTarget();
        } else {
            throw new InternalError
                (env.messageOutput().format("Invalid connect type"));
        }
        vm.setDebugTraceMode(traceFlags);
        if (vm.canBeModified()){
            setEventRequests(vm);
            resolveEventRequests();
        }
        /*
         * Now that the vm connection is open, fetch the debugee
         * classpath and set up a default sourcepath.
         * (Unless user supplied a sourcepath on the command line)
         * (Bug ID 4186582)
         */
        if (env.getSourcePath().length() == 0) {
            if (vm instanceof PathSearchingVirtualMachine) {
                PathSearchingVirtualMachine psvm =
                    (PathSearchingVirtualMachine) vm;
                env.setSourcePath(psvm.classPath());
            } else {
                env.setSourcePath(".");
            }
        }

        return vm;
    }

    boolean setConnectorArg(String name, String value) {
        /*
         * Too late if the connection already made
         */
        if (vm != null) {
            return false;
        }

        Connector.Argument argument = connectorArgs.get(name);
        if (argument == null) {
            return false;
        }
        argument.setValue(value);
        return true;
    }

    String connectorArg(String name) {
        Connector.Argument argument = connectorArgs.get(name);
        if (argument == null) {
            return "";
        }
        return argument.value();
    }

    public synchronized VirtualMachine vm() {
        if (vm == null) {
            throw new VMNotConnectedException();
        } else {
            return vm;
        }
    }

    boolean isOpen() {
        return (vm != null);
    }

    boolean isLaunch() {
        return (connector instanceof LaunchingConnector);
    }

    public void disposeVM() {
        try {
            if (vm != null) {
                vm.dispose();
                vm = null;
            }
        } finally {
            if (process != null) {
                process.destroy();
                process = null;
            }
            waitOutputComplete();
        }
    }

    private void setEventRequests(VirtualMachine vm) {
        EventRequestManager erm = vm.eventRequestManager();

        // Normally, we want all uncaught exceptions.  We request them
        // via the same mechanism as Commands.commandCatchException()
        // so the user can ignore them later if they are not
        // interested.
        // FIXME: this works but generates spurious messages on stdout
        //        during startup:
        //          Set uncaught java.lang.Throwable
        //          Set deferred uncaught java.lang.Throwable
        Commands evaluator = new Commands(env);
        evaluator.commandCatchException
            (new StringTokenizer("uncaught java.lang.Throwable"));

        ThreadStartRequest tsr = erm.createThreadStartRequest();
        tsr.enable();
        ThreadDeathRequest tdr = erm.createThreadDeathRequest();
        tdr.enable();
    }

    private void resolveEventRequests() {
        env.getSpecList().resolveAll();
    }

    private void dumpStream(InputStream stream) throws IOException {
        BufferedReader in =
            new BufferedReader(new InputStreamReader(stream));
        int i;
        try {
            while ((i = in.read()) != -1) {
                   env.messageOutput().printDirect((char)i);// Special case: use
                                                      //   printDirect()
            }
        } catch (IOException ex) {
            String s = ex.getMessage();
            if (!s.startsWith("Bad file number")) {
                  throw ex;
            }
            // else we got a Bad file number IOException which just means
            // that the debuggee has gone away.  We'll just treat it the
            // same as if we got an EOF.
        }
    }

    /**
     *  Create a Thread that will retrieve and display any output.
     *  Needs to be high priority, else debugger may exit before
     *  it can be displayed.
     */
    private void displayRemoteOutput(final InputStream stream) {
        Thread thr = new Thread("output reader") {
            public void run() {
                try {
                    dumpStream(stream);
                } catch (IOException ex) {
                    env.fatalError("Failed reading output");
                } finally {
                    notifyOutputComplete();
                }
            }
        };
        thr.setPriority(Thread.MAX_PRIORITY-1);
        thr.start();
    }

    private void dumpFailedLaunchInfo(Process process) {
        try {
            dumpStream(process.getErrorStream());
            dumpStream(process.getInputStream());
        } catch (IOException e) {
            env.messageOutput().println("Unable to display process output:",
                                  e.getMessage());
        }
    }

    /* launch child target vm */
    private VirtualMachine launchTarget() {
        LaunchingConnector launcher = (LaunchingConnector)connector;
        try {
            VirtualMachine vm = launcher.launch(connectorArgs);
            process = vm.process();
            displayRemoteOutput(process.getErrorStream());
            displayRemoteOutput(process.getInputStream());
            return vm;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            env.fatalError("Unable to launch target VM.");
        } catch (IllegalConnectorArgumentsException icae) {
            icae.printStackTrace();
            env.fatalError("Internal debugger error.");
        } catch (VMStartException vmse) {
            env.messageOutput().println("vmstartexception", vmse.getMessage());
            env.messageOutput().println();
            dumpFailedLaunchInfo(vmse.process());
            env.fatalError("Target VM failed to initialize.");
        }
        return null; // Shuts up the compiler
    }

    /* attach to running target vm */
    private VirtualMachine attachTarget() {
        AttachingConnector attacher = (AttachingConnector)connector;
        try {
            return attacher.attach(connectorArgs);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            env.fatalError("Unable to attach to target VM.");
        } catch (IllegalConnectorArgumentsException icae) {
            icae.printStackTrace();
            env.fatalError("Internal debugger error.");
        }
        return null; // Shuts up the compiler
    }

    /* listen for connection from target vm */
    private VirtualMachine listenTarget() {
        ListeningConnector listener = (ListeningConnector)connector;
        try {
            String retAddress = listener.startListening(connectorArgs);
            env.messageOutput().println("Listening at address:", retAddress);
            vm = listener.accept(connectorArgs);
            listener.stopListening(connectorArgs);
            return vm;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            env.fatalError("Unable to attach to target VM.");
        } catch (IllegalConnectorArgumentsException icae) {
            icae.printStackTrace();
            env.fatalError("Internal debugger error.");
        }
        return null; // Shuts up the compiler
    }
}
