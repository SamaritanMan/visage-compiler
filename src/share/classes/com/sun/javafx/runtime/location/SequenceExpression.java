/*
 * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
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

import com.sun.javafx.runtime.sequence.Sequence;
import com.sun.javafx.runtime.sequence.Sequences;

import java.util.Iterator;

/**
 * SequenceExpression represents an integer-value bound expression.  Associated with an SequenceExpression is an expression
 * that is used to recalculate the value, and a list of dependencies (locations).  If any of the dependencies are
 * changed, the expression is recomputed.  SequenceExpressions are created with the make() and makeLazy() factories; the
 * locations are created in an initially invalid state, so that their evaluation can be deferred until an appropriate
 * time.
 *
 * @author Brian Goetz
 */
public class SequenceExpression<T> extends AbstractSequenceLocation<T> implements SequenceLocation<T> {
    private final SequenceBindingExpression<T> expression;

    /**
     * Create an SequenceExpression with the specified expression and dependencies.
     */
    public static <T> SequenceLocation<T> make(Class<T> clazz, SequenceBindingExpression<T> exp, Location... dependencies) {
        SequenceExpression<T> loc = new SequenceExpression<T>(clazz, false, exp);
        exp.location = loc;
        loc.addDependencies(dependencies);
        return loc;
    }

    /**
     * Create a lazy SequenceExpression with the specified expression and dependencies.
     */
    public static <T> SequenceLocation<T> makeLazy(Class<T> clazz, SequenceBindingExpression<T> exp, Location... dependencies) {
        SequenceExpression<T> loc = new SequenceExpression<T>(clazz, true, exp);
        exp.location = loc;
        loc.addDependencies(dependencies);
        return loc;
    }

    private SequenceExpression(Class<T> clazz, boolean lazy, SequenceBindingExpression<T> expression) {
        super(clazz, false, lazy);
        this.expression = expression;
    }

    @Override
    public void update() {
        if (!isValid()) {
            Sequence<T> v = Sequences.upcast(clazz, expression.get());
            if (!equals(v, previousValue))
                replaceValue(v);
            setValid(false);
            previousValue = null;
        }
    }

    private void ensureValid() {
        if (!isValid())
            update();
    }

    @Override
    public String toString() {
        ensureValid();
        return super.toString();
    }

    @Override
    public Iterator<T> iterator() {
        ensureValid();
        return super.iterator();
    }

    @Override
    public T get(int position) {
        ensureValid();
        return super.get(position);
    }

    @Override
    public Sequence<T> get() {
        ensureValid();
        return super.get();
    }

    @Override
    public void invalidate() {
        if (isValid())
            previousValue = value;
        super.invalidate();
    }
}
