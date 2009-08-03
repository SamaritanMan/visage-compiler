/*
 * Copyright 2007-2009 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  
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

package com.sun.tools.javafx.api;

import com.sun.javafx.api.JavafxcTask;

import com.sun.javafx.api.tree.FunctionDefinitionTree;
import com.sun.javafx.api.tree.FunctionInvocationTree;
import com.sun.javafx.api.tree.JavaFXTreePathScanner;
import com.sun.javafx.api.tree.UnitTree;
import com.sun.tools.javac.util.JavacFileManager;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.tools.JavaFileObject;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

/**
 * This test makes sure that the AllTrees.fx file contains all tree constructs
 * from com.sun.javafx.api.tree.Tree.JavaFXKind.values().
 * 
 * @author David Strupl
 */
public class JFXC3382 {

    private static final String SEP = File.pathSeparator;
    private static final String DIR = File.separator;

    private String javafxLibs = "dist/lib/shared";
    private String javafxDeskLibs = "dist/lib/desktop";

    private String inputDir = "test/sandbox/com/sun/tools/javafx/api";

    private JavafxcTrees trees;
    private UnitTree ut;

    @Before
    public void setup() throws IOException {
        JavafxcTool tool = JavafxcTool.create ();
        JavacFileManager manager = tool.getStandardFileManager (null, null, Charset.defaultCharset ());

        ArrayList<JavaFileObject> filesToCompile = new ArrayList<JavaFileObject> ();
        filesToCompile.add (manager.getFileForInput (inputDir + DIR + "FunctionParametersTest.fx"));

        JavafxcTask task = tool.getTask (null, null, null, Arrays.asList ("-XDdisableStringFolding", "-cp",
            javafxLibs + DIR + "javafxc.jar" + SEP + javafxLibs + DIR + "javafxrt.jar" + SEP + javafxDeskLibs + DIR + "javafx-ui-common.jar" + SEP + inputDir
        ), filesToCompile);

        task.parse();
        Iterable analyzeUnits = task.analyze();
        trees = JavafxcTrees.instance(task);
        ut = (UnitTree)analyzeUnits.iterator().next();
    }

    @After
    public void teardown() {
        trees = null;
        ut = null;
    }

    @Test
    public void testFunctionInvocationParameters() throws Exception {
        JavaFXTreePathScanner<Void, Void> scanner = new JavaFXTreePathScanner<Void, Void>() {

            @Override
            public Void visitMethodInvocation(FunctionInvocationTree node, Void p) {
                Element e = trees.getElement(getCurrentPath());
                System.out.println(node);
                assertEquals(1, ((ExecutableElement)e).getTypeParameters().size());
                return super.visitMethodInvocation(node, p);
            }
        };
        scanner.scan(ut, null);
    }

    @Test
    public void testFunctionDefinitionParameters() throws Exception {
        JavaFXTreePathScanner<Void, Void> scanner = new JavaFXTreePathScanner<Void, Void>() {
            @Override
            public Void visitFunctionDefinition(FunctionDefinitionTree node, Void p) {
                Element e = trees.getElement(getCurrentPath());
                System.out.println(node);
                assertEquals(1, ((ExecutableElement)e).getTypeParameters().size());
                return super.visitFunctionDefinition(node, p);
            }


        };
        scanner.scan(ut, null);
    }

    private static File getTmpDir() {
        try {
            File f = File.createTempFile("dummy", "file");
            f.deleteOnExit();
            File tmpdir = f.getParentFile();
            if (tmpdir != null)
                return tmpdir;
        } catch (IOException ex) {
        }
        File f = new File("test-output");
        f.mkdir();
        return f;
    }
    
}
