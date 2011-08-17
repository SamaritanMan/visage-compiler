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
package com.sun.tools.visage.api;

import com.sun.visage.api.JavafxcTask;
import com.sun.visage.api.tree.IdentifierTree;

import com.sun.visage.api.tree.JavaFXTreePathScanner;
import com.sun.visage.api.tree.SourcePositions;
import com.sun.visage.api.tree.Tree;
import com.sun.visage.api.tree.UnitTree;
import com.sun.visage.api.tree.VariableTree;
import com.sun.tools.visage.comp.JavafxEnter;
import com.sun.tools.visage.comp.JavafxEnv;
import com.sun.tools.visage.tree.JFXClassDeclaration;
import com.sun.tools.visage.tree.JFXFunctionDefinition;
import com.sun.tools.visage.tree.JFXScript;
import com.sun.tools.visage.tree.JFXTree;
import com.sun.tools.visage.tree.JFXVar;
import com.sun.tools.visage.tree.JavafxTreeScanner;
import com.sun.tools.mjavac.code.Symbol;
import com.sun.tools.mjavac.util.Context;
import com.sun.tools.mjavac.util.JavacFileManager;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

/**
 * Regression test for JFXC-4258
 *
 */
public class JFXC4258Test {

    private static final String SEP = File.pathSeparator;
    private static final String DIR = File.separator;
    private String visageLibs = "dist/lib/shared";
    private String visageDeskLibs = "dist/lib/desktop";
    private String inputDir = "test/src/com/sun/tools/visage/api";
    private JavafxcTrees trees;
    private UnitTree ut;
    private SourcePositions sp;
    private Context ctx;
    private Elements elements;

    private static class DeclScanner extends JavafxTreeScanner {
        Symbol sym;

        public DeclScanner(Symbol sym) {
            this.sym = sym;
        }

        JFXTree result = null;

        public void scan(JFXTree tree) {
            if (tree != null && result == null) {
                tree.accept(this);
            }
        }

        public void visitScript( JFXScript that) {
            if (that.packge == sym) {
                result = that;
            } else {
                super.visitScript(that);
            }
        }

        public void visitClassDeclaration( JFXClassDeclaration that) {
            if (that.sym == sym) {
                result = that;
            } else {
                super.visitClassDeclaration(that);
            }
        }

        public void visitFunctionDefinition( JFXFunctionDefinition that) {
            if (that.sym == sym) {
                result = that;
            } else {
                super.visitFunctionDefinition(that);
            }
        }


        public void visitVar( JFXVar that) {
            if (that.sym == sym) {
                result = that;
            } else {
                super.visitVar(that);
            }
        }
    }

    private Tree getTree(Element e) {
        DeclScanner ds = new DeclScanner((Symbol)e);

        Symbol sym = (Symbol) e;
        JavafxEnter enter = JavafxEnter.instance(ctx);
        JavafxEnv env = enter.getEnv(sym.enclClass());
        if (env == null) {
            return null;
        }

        env.tree.accept(ds);
        return ds.result;
    }
    
    @Before
    public void setup() throws IOException {
        doSetup();
    }

    private void doSetup() throws IOException {
        JavafxcTool tool = JavafxcTool.create();
        JavacFileManager manager = tool.getStandardFileManager(null, null, Charset.defaultCharset());

        ArrayList<JavaFileObject> filesToCompile = new ArrayList<JavaFileObject>();
        filesToCompile.add(manager.getFileForInput(inputDir + DIR + "GetScope.visage"));

        JavafxcTask task = tool.getTask(null, null, null, Arrays.asList("-XDdisableStringFolding", "-XDpreserveTrees", "-Xjcov", "-cp",
                visageLibs + DIR + "visagec.jar" + SEP + visageLibs + DIR + "visagert.jar" + SEP + visageDeskLibs + DIR + "visage-ui-common.jar" + SEP + inputDir), filesToCompile);

        task.parse();
        Iterable analyzeUnits = task.analyze();
        trees = JavafxcTrees.instance(task);

        ut = (UnitTree) analyzeUnits.iterator().next();
        sp = trees.getSourcePositions();
        ctx = ((JavafxcTaskImpl) task).getContext();
        elements = ((JavafxcTaskImpl) task).getElements();
    }

    @After
    public void teardown() {
        trees = null;
        ut = null;
    }

    @Test
    public void testInClausePosition() throws Exception {
        final Tree[] t = new Tree[2];
        final Symbol[] sym = new Symbol[0];

        JavaFXTreePathScanner<Void, Void> defTreeResolver = new JavaFXTreePathScanner<Void, Void>() {
            @Override
            public Void visitIdentifier(IdentifierTree node, Void p) {
                Element e = trees.getElement(getCurrentPath());
                if (e != null && e.getSimpleName().contentEquals("aaa")) {
                    t[1] = getTree(e);
                }
                return super.visitIdentifier(node, p);
            }

            @Override
            public Void visitVariable(VariableTree node, Void p) {
                Element e = trees.getElement(getCurrentPath());
                if (e != null) {
                    if (e.getSimpleName().contentEquals("aaa")) {
                        t[0] = node;
                    }
                }
                return super.visitVariable(node, p);
            }
        };

        JavaFXTreePathScanner<Void, Void> accessScope = new JavaFXTreePathScanner<Void, Void>() {
            @Override
            public Void visitVariable(VariableTree node, Void p) {
                Element e = trees.getElement(getCurrentPath());
                if (e != null) {
                    if (e.getSimpleName().contentEquals("aaa")) {
                        trees.getScope(getCurrentPath());
                    }
                }
                return super.visitVariable(node, p);
            }
        };

        defTreeResolver.scan(ut, null);
        assertEquals(t[0], t[1]);

        accessScope.scan(ut, null);
        defTreeResolver.scan(ut, null);
        assertEquals(t[0], t[1]);
    }
}