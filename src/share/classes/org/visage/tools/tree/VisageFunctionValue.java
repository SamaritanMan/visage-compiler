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

import com.sun.tools.mjavac.util.List;

/**
 *
 * @author bothner
 */
public class VisageFunctionValue extends VisageExpression implements FunctionValueTree {

    public VisageType rettype;
    public List<VisageVar> funParams;
    public VisageBlock bodyExpression;
    public VisageFunctionDefinition definition;

    public VisageFunctionValue(VisageType rettype,
            List<VisageVar> params,
            VisageBlock bodyExpression) {
        this.rettype = rettype;
        this.funParams = params;
        this.bodyExpression = bodyExpression;

        if  (bodyExpression != null) {
            this.pos = bodyExpression.pos;
        }
    }

    public VisageType getVisageReturnType() {
        return rettype;
    }

    public VisageType getType() {
        return rettype;
    }

    public List<VisageVar> getParams() {
        return funParams;
    }

    public java.util.List<? extends VariableTree> getParameters() {
        return (java.util.List) funParams;
    }

    public VisageBlock getBodyExpression() {
        return bodyExpression;
    }

    public void accept(VisageVisitor v) {
        v.visitFunctionValue(this);
    }

    @Override
    public VisageTag getVisageTag() {
        return VisageTag.FUNCTIONEXPRESSION;
    }

    public VisageKind getVisageKind() {
        return VisageKind.FUNCTION_VALUE;
    }

    public <R, D> R accept(VisageTreeVisitor<R, D> visitor, D data) {
        return visitor.visitFunctionValue(this, data);
    }
}
