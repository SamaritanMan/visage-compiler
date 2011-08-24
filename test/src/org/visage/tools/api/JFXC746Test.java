/*
 * Copyright 2003-2009 Sun Microsystems, Inc.  All Rights Reserved.
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

package org.visage.tools.api;

import org.visage.api.VisagecTask;
import org.visage.tools.api.VisagecTool;
import org.visage.tools.api.VisagecTrees;
import org.visage.api.tree.UnitTree;
import org.visage.api.tree.ExpressionTree;
import org.visage.api.tree.Tree;
import org.visage.api.tree.SourcePositions;
import java.io.File;
import java.util.List;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Customer-supplied test for JFXC-746 issue.
 */
public class JFXC746Test {
    private static final String testSrc = System.getProperty("test.src.dir", "test/src");

    @Test
    public void testJFXC746() throws Exception {
        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        try {
            /* The javac library uses the context classloader to load the
             * javac implementation. In a NetBeans module, it needs to
             * be loaded by the module's classloader to make sure that the
             * version of javac this compiler requires takes precedence
             * over the JDK's version.
             */
            Thread.currentThread().setContextClassLoader(VisagecTool.class.getClassLoader());
            VisagecTool tool = VisagecTool.create();
            MockDiagnosticListener<? super FileObject> dl = new MockDiagnosticListener<FileObject>();
            StandardJavaFileManager fileManager = tool.getStandardFileManager(dl, null, null);
            File file = new File("test/src/org/visage/tools/api/Test.visage");
            Iterable<? extends JavaFileObject> fileObjects = fileManager.getJavaFileObjects(file);
            VisagecTask visageTask = tool.getTask(null, fileManager, dl, null, fileObjects);
            List<? extends UnitTree> treeList = (List)visageTask.parse();
            assertTrue("AST list size should be 1!", treeList.size() == 1);

            SourcePositions sp = VisagecTrees.instance(visageTask).getSourcePositions();
            UnitTree tree = treeList.iterator().next();
            ExpressionTree pkg = tree.getPackageName();
            long start = sp.getStartPosition(tree, pkg);
            long end = sp.getEndPosition(tree, pkg);
            String pkgName = pkg.toString();
            assertTrue("Package AST end-start <" + (end-start) + "> should be same as pkgName len <" + pkgName.length() + ">", end - start == pkgName.length());
            
            Tree cls = tree.getTypeDecls().iterator().next();
            start = sp.getStartPosition(tree, cls);
            end = sp.getEndPosition(tree, cls);
            String clsDecl = "class Test{}";
            assertTrue("Class AST end-start <"+ (end-start+1) + "> should be same as class length <" + clsDecl.length() + ">", end - start +1  == clsDecl.length());
            // For V3 assertTrue("Class AST end-start <"+ (end-start)+ "> should be same as class length <" + clsDecl.length() + ">", end - start  == clsDecl.length());
        } finally {
            Thread.currentThread().setContextClassLoader(orig);
        }
    }
}
