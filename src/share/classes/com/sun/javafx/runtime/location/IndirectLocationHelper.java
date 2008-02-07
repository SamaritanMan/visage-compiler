/*
 * Copyright 2008 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.javafx.runtime.location;

/**
 * Helper classes for indirect locations; maintains separate dependency paths for the static dependencies (passed into
 * the constructor) and the dynamic dependencies (embodied in the returned location from computeLocation()).  All
 * subclasses need to do is provide the computeLocation() method.
 *
 * @author Brian Goetz
 */
public class IndirectLocationHelper<T extends Location> extends ObjectExpression<T> implements ObjectLocation<T> {
    private final IndirectLocation<T> helped;

    public IndirectLocationHelper(IndirectLocation<T> helped, Location... dependencies) {
        super(true, dependencies);
        this.helped = helped;
    }

    public T computeValue() {
        helped.clearDynamicDependencies();
        T location = helped.computeLocationInternal();
        helped.addDynamicDependency(location);
        return location;
    }

    public void invalidate() {
        super.invalidate();
        helped.invalidate();
    }
}
