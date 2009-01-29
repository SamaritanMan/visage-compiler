/*
 * Copyright 2008 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.javafx.runtime.sequence;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.sun.javafx.runtime.location.AbstractLocation;
import com.sun.javafx.runtime.location.ObjectChangeListener;
import com.sun.javafx.runtime.location.SequenceChangeListener;
import com.sun.javafx.runtime.location.SequenceLocation;
import com.sun.javafx.runtime.TypeInfo;

/**
 * Abstract base class for bound sequences.  Subclass constructors are expected to compute the initial value, set up
 * any required triggers on dependent objects, and call the setInitialValue() method to provide a value.  (This places
 * limits on designing subclasses for further inheritance.)  The setInitialValue() method must be called exactly once.
 *
 * @author Brian Goetz
 */
public abstract class AbstractBoundSequence<T> extends AbstractLocation implements SequenceLocation<T> {
    protected final TypeInfo<T, ?> typeInfo;
    private List<SequenceChangeListener<T>> changeListeners;
    private Sequence<T> value;

    // Currently, no support for lazy binding.

    protected AbstractBoundSequence(TypeInfo<T, ?> typeInfo) {
        this.typeInfo = typeInfo;
        this.value = typeInfo.emptySequence;
    }

    protected void setInitialValue(Sequence<T> initialValue) {
        if (isValid())
            throw new IllegalStateException("Cannot call setInitialValue more than once");
        Sequence<T> oldValue = value;
        Sequence<T> newValue = initialValue;
        if (newValue == null)
            newValue = typeInfo.emptySequence;
        value = newValue;
        setValid();
        if (!Sequences.isEqual(oldValue, newValue)) {
            invalidateDependencies();
            notifyListeners(0, Sequences.size(oldValue)-1, newValue, oldValue, newValue);
        }
    }

    protected void updateSlice(int startPos, int endPos, Sequence<? extends T> newValues) {
        Sequence<T> oldValue = value;
        if (changeListeners != null) {
            Sequences.noteShared(newValues);
            Sequences.noteShared(oldValue);
        }
        value = Sequences.replaceSlice(oldValue, startPos, endPos, newValues);
        invalidateDependencies();
        notifyListeners(startPos, endPos, newValues, oldValue, value);
    }

    protected void updateSlice(int startPos, int endPos, Sequence<? extends T> newValues, Sequence<T> newSequence) {
        Sequence<T> oldValue = value;
        value = newSequence;
        invalidateDependencies();
        notifyListeners(startPos, endPos, newValues, oldValue, newSequence);
    }

    protected Sequence<T> getRawValue() {
        return value;
    }

    public Sequence<T> get() {
        return getAsSequence();
    }

    public T get(int position) {
        return getAsSequence().get(position);
    }

    public Sequence<T> getAsSequence() {
        assert(isValid());
        Sequences.noteShared(value);
        return value;
    }

    public TypeInfo<T, ?> getElementType() {
        return typeInfo;
    }

    public Sequence<T> getSlice(int startPos, int endPos) {
        return getAsSequence().getSlice(startPos, endPos);
    }

    public boolean isNull() {
        return Sequences.size(getAsSequence()) == 0;
    }

    public void addChangeListener(final ObjectChangeListener<Sequence<T>> listener) {
        addChangeListener(new SequenceChangeListener<T>() {
            public void onChange(int startPos, int endPos, Sequence<? extends T> newElements, Sequence<T> oldValue, Sequence<T> newValue) {
                listener.onChange(oldValue, newValue);
            }
        });
    }

    public void addChangeListener(SequenceChangeListener<T> listener) {
        if (changeListeners == null)
            changeListeners = new LinkedList<SequenceChangeListener<T>>();
        changeListeners.add(listener);
    }

    public void removeChangeListener(SequenceChangeListener<T> listener) {
        if (changeListeners != null)
            changeListeners.remove(listener);
    }

    private void notifyListeners(final int startPos, final int endPos,
                                 final Sequence<? extends T> newElements,
                                 final Sequence<T> oldValue, final Sequence<T> newValue) {
        if (changeListeners != null) {
            Sequences.noteShared(newElements);
            Sequences.noteShared(oldValue);
            Sequences.noteShared(newValue);
            for (SequenceChangeListener<T> listener : changeListeners)
                listener.onChange(startPos, endPos, newElements, oldValue, newValue);
        }
    }

    public Iterator<T> iterator() {
        return getAsSequence().iterator();
    }

    @Override
    public String toString() {
        return getAsSequence().toString();
    }

    @Override
    public void invalidate() {
        throw new UnsupportedOperationException();
    }

    public void setDefault() {
        throw new UnsupportedOperationException();
    }

    public T set(int position, T value) {
        throw new UnsupportedOperationException();
    }

    public Sequence<T> set(Sequence<T> value) {
        throw new UnsupportedOperationException();
    }

    public Sequence<T> setFromLiteral(Sequence<T> value) {
        throw new UnsupportedOperationException();
    }

    public Sequence<T> setAsSequence(Sequence<? extends T> value) {
        throw new UnsupportedOperationException();
    }

    public Sequence<T> setAsSequenceFromLiteral(Sequence<? extends T> value) {
        throw new UnsupportedOperationException();
    }

    public Sequence<? extends T> replaceSlice(int startPos, int endPos, Sequence<? extends T> newValues) {
        throw new UnsupportedOperationException();
    }

    public void delete(int position) {
        throw new UnsupportedOperationException();
    }

    public void deleteSlice(int startPos, int endPos) {
        throw new UnsupportedOperationException();
    }

    public void deleteAll() {
        throw new UnsupportedOperationException();
    }

    public void deleteValue(T value) {
        throw new UnsupportedOperationException();
    }

    public void delete(SequencePredicate<T> sequencePredicate) {
        throw new UnsupportedOperationException();
    }

    public void insert(T value) {
        throw new UnsupportedOperationException();
    }

    public void insert(Sequence<? extends T> values) {
        throw new UnsupportedOperationException();
    }

    public void insertFirst(T value) {
        throw new UnsupportedOperationException();
    }

    public void insertFirst(Sequence<? extends T> values) {
        throw new UnsupportedOperationException();
    }

    public void insertBefore(T value, int position) {
        throw new UnsupportedOperationException();
    }

    public void insertBefore(T value, SequencePredicate<T> sequencePredicate) {
        throw new UnsupportedOperationException();
    }

    public void insertBefore(Sequence<? extends T> values, int position) {
        throw new UnsupportedOperationException();
    }

    public void insertBefore(Sequence<? extends T> values, SequencePredicate<T> sequencePredicate) {
        throw new UnsupportedOperationException();
    }

    public void insertAfter(T value, int position) {
        throw new UnsupportedOperationException();
    }

    public void insertAfter(T value, SequencePredicate<T> sequencePredicate) {
        throw new UnsupportedOperationException();
    }

    public void insertAfter(Sequence<? extends T> values, int position) {
        throw new UnsupportedOperationException();
    }

    public void insertAfter(Sequence<? extends T> values, SequencePredicate<T> sequencePredicate) {
        throw new UnsupportedOperationException();
    }
}
