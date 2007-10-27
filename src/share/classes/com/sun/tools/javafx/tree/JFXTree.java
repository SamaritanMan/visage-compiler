/*
 * Copyright 1999-2007 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.tools.javafx.tree;

import com.sun.javafx.api.tree.JavaFXTree;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.Visitor;

import com.sun.source.tree.TreeVisitor;

/**
 * The base of the JavaFX AST
 * well... except for things like statement which (at least for now) have to be subclassed
 * off other parts of the JCTree.
 */
public abstract class JFXTree extends JCTree implements JavaFXTree {
    
    /** Initialize tree with given tag.
     */
    protected JFXTree() {
    }
    
    public abstract void accept(JavafxVisitor v);
    
    @Override
    public void accept(Visitor v) {
        if (v instanceof JavafxVisitor) {
            this.accept((JavafxVisitor)v);
        } else {
            v.visitTree(this);
        }
    }
    
    // stuff to ignore
    
    public Kind getKind()  {
        throw new InternalError("not implemented");
    }
    
    @Override
    public <R,D> R accept(TreeVisitor<R,D> v, D d) {
        throw new InternalError("not implemented");
    }
    
    @SuppressWarnings("unchecked")
    public static <T> java.util.List<T> convertList(Class<T> klass, com.sun.tools.javac.util.List<?> list) {
	if (list == null)
	    return null;
	for (Object o : list)
	    klass.cast(o);
        return (java.util.List<T>)list;
    }
}
