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

import org.visage.api.VisageBindStatus;
import org.visage.api.tree.*;

import com.sun.tools.mjavac.code.Type;

public abstract class VisageExpression extends VisageTree implements ExpressionTree, VisageBoundMarkable {
    
    private VisageBindStatus bindStatus;

    /** Initialize tree.
     */
    protected VisageExpression() {
        this.bindStatus = VisageBindStatus.UNBOUND;
    }

    protected VisageExpression(VisageBindStatus bindStatus) {
        this.bindStatus = bindStatus == null ? VisageBindStatus.UNBOUND : bindStatus;
    }


    @Override
    public VisageExpression setType(Type type) {
        super.setType(type);
        return this;
    }

    @Override
    public VisageExpression setPos(int pos) {
        super.setPos(pos);
        return this;
    }

    public void markBound(VisageBindStatus bindStatus) {
        this.bindStatus = bindStatus;
    }

    public VisageBindStatus getBindStatus() {
        return bindStatus;
    }

    public boolean isBound() {
        return bindStatus.isBound();
    }

    public boolean isUnidiBind() {
        return bindStatus.isUnidiBind();
    }

    public boolean isBidiBind() {
        return bindStatus.isBidiBind();
    }
}
