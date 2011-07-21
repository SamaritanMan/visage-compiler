/*
 * Copyright (c) 2010-2011, Visage Project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name Visage nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.sun.visage.runtime;

import com.sun.visage.animation.AnimationProvider;
import com.sun.visage.functions.Function0;
import com.sun.visage.runtime.sequence.Sequence;
import com.sun.visage.runtime.sequence.Sequences;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Queue;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Entry point into Visage applications
 * 
 * @author Stephen Chin <steveonjava@gmail.com>
 */
public class Entry {

    private static RuntimeProvider runtimeProvider;
    private static boolean noAnimationProvider;
    private static AnimationProvider animationProvider;
    private static NamedArgumentProvider namedArgProvider;
    private static String[] commandLineArgs;

    public static void start(final Class<?> app, String[] args) throws Throwable {
        if (args != null) {
            CommandLineNamedArgumentProvider clnap = new CommandLineNamedArgumentProvider(args);
            setNamedArgumentProvider(clnap);
            commandLineArgs = clnap.getValues();
        }
        final Method main = app.getMethod(entryMethodName(), Sequence.class);
        Object argSeq = Sequences.make(TypeInfo.String, args);
        try {
            AccessController.doPrivileged(
                    new PrivilegedAction<Void>() {

                        @Override
                        public Void run() {
                            CodeSource codesource = app.getProtectionDomain().getCodeSource();
                            if (codesource != null) {
                                SystemProperties.setFXProperty(SystemProperties.codebase, codesource.getLocation().toString());
                            }
                            main.setAccessible(true);
                            return null;
                        }
                    });
        } catch (AccessControlException e) {
            // security issue in applet or jnlp -- ignore
        }
        if (getRuntimeProvider().usesRuntimeLibrary(app)) {
            getRuntimeProvider().run(main, args);
        } else {
            try {
                main.invoke(null, argSeq);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }

    public static void deferAction(Runnable function) {
        getRuntimeProvider().deferAction(function);
    }

    public static void deferAction(final Function0<Void> function) {
        deferAction(new Runnable() {

            @Override
            public void run() {
                function.invoke$(null, null, null);
            }
        });
    }

    public static void exit() {
        getRuntimeProvider().exit();
    }

    private static RuntimeProvider getRuntimeProvider() {
        if (runtimeProvider == null) {
            try {
                runtimeProvider = AccessController.doPrivileged(
                        new PrivilegedAction<RuntimeProvider>() {

                            @Override
                            public RuntimeProvider run() {
                                try {
                                    return ServiceLoader.load(RuntimeProvider.class, Thread.currentThread().getContextClassLoader()).iterator().next();
                                } catch (ServiceConfigurationError e) {
                                } catch (NoSuchElementException e) {
                                }
                                return null;
                            }
                        });
            } catch (Exception e) {
                // ignore all exceptions and assign the runtime default (finally block)
            } finally {
                if (runtimeProvider == null) {
                    runtimeProvider = new NoRuntimeDefault();
                }
            }
        }
        return runtimeProvider;
    }

    public static AnimationProvider getAnimationProvider() {
        if (!noAnimationProvider && (animationProvider == null)) {
            try {
                animationProvider = AccessController.doPrivileged(
                        new PrivilegedAction<AnimationProvider>() {

                            @Override
                            public AnimationProvider run() {
                                try {
                                    return ServiceLoader.load(AnimationProvider.class, Thread.currentThread().getContextClassLoader()).iterator().next();
                                } catch (ServiceConfigurationError e) {
                                } catch (NoSuchElementException e) {
                                }
                                return null;
                            }
                        });
            } finally {
                noAnimationProvider = animationProvider == null;
            }
        }
        return animationProvider;
    }

    public static void setNamedArgumentProvider(NamedArgumentProvider provider) {
        namedArgProvider = provider;
    }

    public static Sequence<? extends String> getArguments() {
        return Sequences.make(TypeInfo.String, commandLineArgs);
    }

    private static Object getArgument(int argument) {
        if (commandLineArgs == null || argument < 0 || argument >= commandLineArgs.length) {
            return null;
        }
        return commandLineArgs[argument];
    }

    public static Object getArgument(String key) {
        if (namedArgProvider != null) {
            Object value = namedArgProvider.get(key);
            if (value != null) {
                return value;
            }
        }
        // treat as a numeric index
        try {
            return getArgument(Integer.parseInt(key));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String entryMethodName() {
        return "visage$run$";
    }

    private static class NoRuntimeDefault extends Thread implements RuntimeProvider {

        private final Queue<Runnable> tasks = new ConcurrentLinkedQueue<Runnable>();

        @Override
        public boolean usesRuntimeLibrary(Class application) {
            return true;
        }

        @Override
        public Object run(final Method entryPoint, final String... args) throws Throwable {
            /*
             * Add the Script invokation to the Queue
             */
            tasks.add(new Runnable() {

                @Override
                public void run() {
                    try {
                        main(entryPoint, args);
                    } catch (Throwable t) {
                        t.printStackTrace(System.err);
                    }
                }
            });
            this.start();
            return null;
        }

        private boolean hasActiveAnimation() {
            AnimationProvider animationProvider = getAnimationProvider();
            return animationProvider != null && animationProvider.hasActiveAnimation();
        }

        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public void run() {
            try {
                while (!tasks.isEmpty() || hasActiveAnimation()) {
                    if (!tasks.isEmpty()) {
                        tasks.remove().run();
                    } else {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            break;
                        }
                    }
                }
                visage.lang.FX.exit(); // implicit exit after timeline is complete
            } catch (FXExit fxe) {
                return; // trap FXExit exception from bubbling up
            }
        }

        private Object main(Method entryPoint, String... args) throws Throwable {
            try {
                return entryPoint.invoke(null, Sequences.make(TypeInfo.String, args));
            } catch (InvocationTargetException ite) {
                Throwable cause = ite.getCause();
                if (cause instanceof FXExit) { // explicit exit
                    return null;
                }
                throw cause;
            }
        }

        @Override
        public void deferAction(Runnable runnable) {
            tasks.add(runnable);
        }

        @Override
        public void exit() {
            try {
                System.exit(0);
            } catch (SecurityException se) {
                // ignore
            }
        }
    }

    private static class CommandLineNamedArgumentProvider implements NamedArgumentProvider {

        private static final char SEPARATOR = '=';
        private Properties namedArguments = new Properties();
        private String[] values;

        private CommandLineNamedArgumentProvider(String[] args) {
            values = new String[args.length];
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                int index = arg.indexOf(SEPARATOR);
                if (index > 0) {
                    String value = arg.substring(index + 1);
                    namedArguments.setProperty(arg.substring(0, index), value);
                    values[i] = value;
                } else {
                    values[i] = arg;
                }
            }
        }

        @Override
        public Object get(String name) {
            return namedArguments.getProperty(name);
        }

        public String[] getValues() {
            return values;
        }
    }
}
