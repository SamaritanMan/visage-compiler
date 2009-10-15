/*
 * Copyright 2009 Sun Microsystems, Inc.  All Rights Reserved.
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
import com.sun.javafx.api.tree.JavaFXTreeScanner;
import com.sun.javafx.api.tree.InstantiateTree;
import com.sun.javafx.api.tree.UnitTree;

import java.io.File;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests that the 'preverveTrees' flag actually preserves Parens AST nodes during parsing
 *
 * @author mcimadamore
 */
public class JFXC3523Test {

    @Test
    public void testScanVarOverrideInit() throws Exception {
        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(JavafxcTool.class.getClassLoader());
            JavafxcTool tool = JavafxcTool.create();
            MockDiagnosticListener<? super FileObject> dl = new MockDiagnosticListener<FileObject>();
            StandardJavaFileManager fileManager = tool.getStandardFileManager(dl, null, null);
            File file = new File("test/src/com/sun/tools/javafx/api/JFXC3523.fx");
            Iterable<? extends JavaFileObject> fileObjects = fileManager.getJavaFileObjects(file);
            JavafxcTask javafxTask = tool.getTask(null, fileManager, dl, null, fileObjects);
            Iterable<? extends UnitTree> treeList = javafxTask.analyze();

            JavafxcTrees trees = JavafxcTrees.instance(javafxTask);
            UnitTree unit = treeList.iterator().next();

            ObjectLiteralFinder olf = new ObjectLiteralFinder();
            olf.scan(treeList, null);

            assertEquals(true, olf.foundLiteral);
        } finally {
            Thread.currentThread().setContextClassLoader(orig);
        }
    }

    static class ObjectLiteralFinder extends JavaFXTreeScanner<Void,Object> {

        boolean foundLiteral = false;

        @Override
        public Void visitInstantiate(InstantiateTree node, Object p) {
            foundLiteral = true;
            return super.visitInstantiate(node, p);
        }
    }
}
