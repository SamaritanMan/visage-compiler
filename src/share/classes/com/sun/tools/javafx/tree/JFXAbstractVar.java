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

package com.sun.tools.javafx.tree;

import com.sun.javafx.api.JavafxBindStatus;
import com.sun.javafx.api.tree.*;
import com.sun.javafx.api.tree.OnReplaceTree;

import com.sun.tools.mjavac.util.Name;
import com.sun.tools.mjavac.code.Symbol.VarSymbol;

/**
 *
 * @author Robert Field
 */
public abstract class JFXAbstractVar extends JFXExpression implements JFXBoundMarkable, VariableTree {
    public final Name name;
    private JFXType jfxtype;
    public final JFXModifiers mods;
    private final JFXExpression init;
    private JavafxBindStatus bindStatus;
    private final JFXOnReplace[] triggers;
    
    public VarSymbol sym;

    protected JFXAbstractVar(
            Name name,
            JFXType jfxtype,
            JFXModifiers mods,
            JFXExpression init,
            JavafxBindStatus bindStat,
            JFXOnReplace onReplace,
            JFXOnReplace onInvalidate,
            VarSymbol sym) {
        this.name = name;
        this.jfxtype = jfxtype;
        this.mods = mods;
        this.init = init;
        this.bindStatus = bindStat == null ? JavafxBindStatus.UNBOUND : bindStat;
        this.triggers = new JFXOnReplace[JFXOnReplace.Kind.values().length];
        this.triggers[JFXOnReplace.Kind.ONREPLACE.ordinal()] = onReplace;
        this.triggers[JFXOnReplace.Kind.ONINVALIDATE.ordinal()] = onInvalidate;
        this.sym = sym;
    }

    public abstract boolean deferInit();

    public abstract boolean isOverride();

    public boolean isStatic() {
        return sym.isStatic();
    }

    public void markBound() {
        if (!bindStatus.isBound()) {
            bindStatus = JavafxBindStatus.UNIDIBIND;
        }
    }

    public JFXExpression getInitializer() {
        return init;
    }

    public JFXOnReplace getOnInvalidate() {
        return triggers[JFXOnReplace.Kind.ONINVALIDATE.ordinal()];
    }

    public OnReplaceTree getOnInvalidateTree() {
        return triggers[JFXOnReplace.Kind.ONINVALIDATE.ordinal()];
    }

    public JFXOnReplace getOnReplace() {
        return triggers[JFXOnReplace.Kind.ONREPLACE.ordinal()];
    }

    public OnReplaceTree getOnReplaceTree() {
        return triggers[JFXOnReplace.Kind.ONREPLACE.ordinal()];
    }

    public JFXOnReplace getTrigger(JFXOnReplace.Kind triggerKind) {
        return triggers[triggerKind.ordinal()];
    }

    public OnReplaceTree getTriggerTree(JFXOnReplace.Kind triggerKind) {
        return triggers[triggerKind.ordinal()];
    }

    public VarSymbol getSymbol() {
        return sym;
    }

    public JavafxBindStatus getBindStatus() {
        return bindStatus;
    }

    public boolean isBidiBind() {
        return bindStatus.isBidiBind();
    }

    public boolean isBound() {
        return bindStatus.isBound();
    }

    public boolean isLazy() {
        return bindStatus.isLazy();
    }

    public boolean isUnidiBind() {
        return bindStatus.isUnidiBind();
    }

    public <R, D> R accept(JavaFXTreeVisitor<R, D> visitor, D data) {
        return visitor.visitVariable(this, data);
    }

    public JavaFXKind getJavaFXKind() {
        return JavaFXKind.VARIABLE;
    }

    public Name getName() {
        return name;
    }

    public JFXTree getType() {
        return jfxtype;
    }

    public JFXType getJFXType() {
        return jfxtype;
    }

    public void setJFXType(JFXType type) {
        jfxtype = type;
    }

    public JFXModifiers getModifiers() {
        return mods;
    }
}
