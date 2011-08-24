/*
 * Copyright 2008-2009 Sun Microsystems, Inc.  All Rights Reserved.
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

package org.visage.tools.tree;

import org.visage.api.tree.*;
import org.visage.api.tree.Tree.VisageKind;

import com.sun.tools.mjavac.code.Symbol;

/**
 * Visage unary expressions
 *
 * @author Tom Ball
 */
public class VisageUnary extends VisageExpression implements UnaryTree, Tree {

    private JavafxTag opcode;
    public VisageExpression arg;
    public Symbol operator;

    protected VisageUnary(JavafxTag opcode, VisageExpression arg) {
        this.opcode = opcode;
        this.arg = arg;
    }

    @Override
    public void accept(JavafxVisitor v) {
        v.visitUnary(this);
    }

    @Override
    public JavafxTag getFXTag() {
        return opcode;
    }
    
    public int getOperatorTag() {
        return opcode.asOperatorTag();
    }

    public VisageExpression getExpression() {
        return arg;
    }

    public Symbol getOperator() {
        return operator;
    }

    @Override
    public VisageKind getJavaFXKind() {
        switch (getFXTag()) {
            case SIZEOF:
                return VisageKind.SIZEOF;
            case REVERSE:
                return VisageKind.REVERSE;
            default:
                return JavafxTreeInfo.tagToKind(getFXTag());
        }
    }

    //@Override
    public <R, D> R accept(VisageTreeVisitor<R, D> visitor, D data) {
        return visitor.visitUnary(this, data);
    }
}
