/*
 * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.tools.javafx.script;

import com.sun.javafx.api.*;
import java.io.IOException;
import java.io.Writer;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.tools.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.io.File;
import java.io.LineNumberReader;
import java.io.StringReader;
/**
 * Simple interface to the JavaFX Script compiler using JSR 199 Compiler API, 
 * based on https://scripting.dev.java.net's JavaCompiler by A. Sundararajan.
 */
public class JavaFXScriptCompiler {    
    private JavafxcTool tool;
    private StandardJavaFileManager stdManager;
    private ClassLoader parentClassLoader;

    public JavaFXScriptCompiler(ClassLoader parent) {
	parentClassLoader = parent;
        tool = JavafxcTool.create();
    }

    public Map<String, byte[]> compile(String source, String fileName) {
        PrintWriter err = new PrintWriter(System.err);
        return compile(source, fileName, err, null, null);
    }

    public Map<String, byte[]> compile(String fileName, String source, 
                                    Writer err) {
        return compile(fileName, source, err, null, null);
    }

    public Map<String, byte[]> compile(String fileName, String source, 
                                    Writer err, String sourcePath) {
        return compile(fileName, source, err, sourcePath, null);
    }

    public Map<String, byte[]> compile(String fileName, String source, 
                                    Writer err, String sourcePath, 
                                    String classPath) {
        return compile(fileName, source, err, sourcePath, classPath,
                new DiagnosticCollector<JavaFileObject>(), true);
    }

    /**
     * compile given String source and return bytecodes as a Map.
     *
     * @param fileName source fileName to be used for error messages etc.
     * @param source Java source as String
     * @param err error writer where diagnostic messages are written
     * @param sourcePath location of additional .java source files
     * @param classPath location of additional .class files
     * @param diagnostics error and warning collector
     * @param printDiagnostics true if diagnostics should be displayed
     */
    public Map<String, byte[]> compile(String fileName, String source, 
                    Writer err, String sourcePath, String classPath,
                    DiagnosticListener<JavaFileObject> diagnostics,
                    boolean printDiagnostics) {

        // create a new memory JavaFileManager
	if (stdManager == null) {
	    stdManager = tool.getStandardFileManager(diagnostics, null, null);
	}
        MemoryFileManager manager = new MemoryFileManager(stdManager, parentClassLoader);

        // prepare the compilation unit
        List<JavaFileObject> compUnits = new ArrayList<JavaFileObject>(1);
        compUnits.add(manager.makeStringSource(fileName, source));

        // javac options
        List<String> options = new ArrayList<String>();
        options.add("-Xlint:all-unchecked");
        options.add("-g");
        options.add("-deprecation");
        if (sourcePath != null) {
            options.add("-sourcepath");
            options.add(sourcePath);
        }
        if (classPath != null) {
            options.add("-classpath");
            options.add(classPath);
        }
        options.add("-target");
        options.add(isJava6() ? "1.6" : "1.5");
        
        options.add("-XDdumpfx=/tmp");
        
        // create a compilation task
        JavafxcTask task = tool.getTask(err, manager, diagnostics, options, compUnits);

        if (task.call() == false) {
            if (printDiagnostics) {
		if (diagnostics instanceof DiagnosticCollector) {
		    printDiagnostics((DiagnosticCollector)diagnostics, err);
		}
            }
            return null;
        }

        Map<String, byte[]> classBytes = manager.getClassBytes();
        try {
            manager.close();
        } catch (IOException exp) {
        }

        return classBytes; 
    }
    
    private static boolean isJava6() {
        try {
            Class.forName("java.util.ServiceLoader");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    static void printDiagnostics(DiagnosticCollector<JavaFileObject> diagnostics, Writer err) {
        // install customized diagnostic message formats, which don't print script name
        com.sun.tools.javac.util.Context context = new com.sun.tools.javac.util.Context();
        com.sun.tools.javac.util.Options options =
                com.sun.tools.javac.util.Options.instance(context);
        options.put("diags", "%l: %t%m|%p%m");

        PrintWriter perr = new PrintWriter(err);
        RedirectedLog log = new RedirectedLog(context, perr);
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
            log.report((com.sun.tools.javac.util.JCDiagnostic)diagnostic);
        }
    }
    
    private static class RedirectedLog extends com.sun.tools.javac.util.Log {
        RedirectedLog(com.sun.tools.javac.util.Context context, PrintWriter writer) {
            super(context, writer);
        }
    }
}
