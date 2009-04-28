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

package com.sun.javafx.runtime.location;

import com.sun.javafx.runtime.sequence.Sequence;
import com.sun.javafx.runtime.sequence.SequencePredicate;
import com.sun.javafx.runtime.TypeInfo;

/**
 * A sequence-valued Location.  Exposes analogues of the mutative methods from Sequence, which modify the sequence
 * value and notify the appropriate change listeners.  
 *
 * @author Brian Goetz
 */
public interface SequenceLocation<T> extends Location, Iterable<T>, ObjectLocation<Sequence<T>> {
    
    T get(int position);

    Sequence<T> getAsSequence();

    public TypeInfo<T, ?> getElementType();

    public void addSequenceChangeListener(ChangeListener<T> listener);

    public void removeSequenceChangeListener(ChangeListener<T> listener);

    public Sequence<T> setAsSequence(Sequence<? extends T> value);

    public T set(int position, T value);

    public Sequence<T> getSlice(int startPos, int endPos);

    public Sequence<? extends T> replaceSlice(int startPos, int endPos, Sequence<? extends T> newValues);

    public void delete(int position);

    public void deleteSlice(int startPos, int endPos);

    public void deleteAll();

    public void deleteValue(T value);

    public void delete(SequencePredicate<T> predicate);

    public void insert(T value);

    public void insert(Sequence<? extends T> values);

    public void insertFirst(T value);

    public void insertFirst(Sequence<? extends T> values);

    public void insertBefore(T value, int position);

    public void insertBefore(T value, SequencePredicate<T> predicate);

    public void insertBefore(Sequence<? extends T> values, int position);

    public void insertBefore(Sequence<? extends T> values, SequencePredicate<T> predicate);

    public void insertAfter(T value, int position);

    public void insertAfter(T value, SequencePredicate<T> predicate);

    public void insertAfter(Sequence<? extends T> values, int position);

    public void insertAfter(Sequence<? extends T> values, SequencePredicate<T> predicate);
}
