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

/**
 *
 * @author Robert Field
 */
public class VisageSequenceEmpty extends VisageAbstractSequenceCreator implements SequenceEmptyTree {

    public VisageSequenceEmpty() {
    }

    public void accept(VisageVisitor v) {
        v.visitSequenceEmpty(this);
    }

    @Override
    public VisageTag getVisageTag() {
        return VisageTag.SEQUENCE_EMPTY;
    }

    public VisageKind getVisageKind() {
        return VisageKind.SEQUENCE_EMPTY;
    }

    public <R, D> R accept(VisageTreeVisitor<R, D> visitor, D data) {
        return visitor.visitSequenceEmpty(this, data);
    }
}
