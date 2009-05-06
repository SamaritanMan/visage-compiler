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

import java.util.BitSet;

import com.sun.javafx.runtime.TypeInfo;

/**
 * Helper methods for modifying sequences and notifying sequence change listeners.  The helper methods only call the
 * sequence trigger methods; if the underlying sequence is modified then the caller is responsible for
 * calling the onChanged() method.
 *
 * @author Brian Goetz
 */
public class SequenceMutator {

    public interface Listener<T> {
        public void onReplaceSlice(int startPos, int endPos, Sequence<? extends T> newElements, Sequence<? extends T> oldValue, Sequence<? extends T> newValue);
        public void onReplaceElement(int startPos, int endPos, T newElement, Sequence<? extends T> oldValue, Sequence<? extends T> newValue);
    }

    // Inhibit instantiation
    private SequenceMutator() {
    }

    /**
     * The core sequence mutation operation is slice replacement.  A slice is represented by (start, end) indexes, which
     * are _inclusive_.  The length of a slice is end-start+1.
     *
     * A one-element slice at position p is represented (p, p).   The empty slice before element p is represented
     * by (p, p-1).  The empty slice after the last element is represented by (len, len-1) where len is the length
     * of the sequence.
     *
     * Inserting elements into the sequence is performed by replacing an empty slice with a non-empty slice.
     * Deleting elements from the sequence is performed by replacing a non-empty slice with an empty slice.
     * Replacing elements in the sequence is performed by replacing a non-empty slice with another non-empty one.
     *
     * @param target The sequence in which the slice is being replaced
     * @param listener A sequence listener which will be notified, may be null
     * @param startPos Starting position of the slice, inclusive, may be 0..size
     * @param endPos Ending position of the slice, inclusive, may be start-1..size
     * @param newValues Values to be inserted, may be null
     * @return The new sequence.
     */
  public static <T> Sequence<? extends T> replaceSlice(TypeInfo<T,?> elementType, Sequence<? extends T> target, Listener<T> listener,
                                               int startPos, int endPos, Sequence<? extends T> newValues) {
        Sequence<? extends T> result;
        final int size = Sequences.size(target);

        if (startPos > size || startPos < 0)
            return target;

        if (endPos < startPos-1)
            endPos = startPos-1;
        else if (endPos > size)
            endPos = size;

        if (startPos == 0 && endPos == size-1) {
            result = (newValues != null) ? Sequences.upcast(newValues) : target.getEmptySequence();
        }
        else if (startPos == endPos + 1) {
            // Insertion at startPos
            if (Sequences.size(newValues) == 0)
                result = target;
            else if (startPos == 0)
                result = Sequences.concatenate(elementType, newValues, target);
            else if (startPos == size)
                result = Sequences.concatenate(elementType, target, newValues);
            else
                result = Sequences.replace(target, startPos, startPos, newValues);
        }
        else if (Sequences.size(newValues) == 0) {
            if (newValues == null)
                newValues = target.getEmptySequence();
            // Deletion from startPos to endPos inclusive
            if (endPos == startPos-1)
                result = target;
            else if (endPos >= size-1)
                result = Sequences.subsequence(target, 0, startPos);
            else if (startPos == 0)
                result = Sequences.subsequence(target, endPos+1, size);
            else {
                result = Sequences.replace(target, startPos, endPos+1, elementType.emptySequence);
            }
        }
        else if (startPos <= endPos) {
            // @@@ OPT: Special-case for replacing leading or trailing slices
            result = Sequences.replace(target, startPos, endPos+1, newValues);
        }
        else
            throw new IllegalArgumentException();

        if (result != target && listener != null)
            listener.onReplaceSlice(startPos, endPos, newValues, target, result);
        return result;
    }

    /**
     * Optimized version of replaceSlice where sizeof newValues == 1.
     * @param target The sequence in which the slice is being replaced
     * @param listener A sequence listener which will be notified, may be null
     * @param startPos Starting position of the slice, inclusive, may be 0..size
     * @param endPos Ending position of the slice, inclusive, may be start-1..size-1
     * @param newValue The single replement value - null is treated like deletion
     * @return The new sequence.
       */
  public static <T> Sequence<? extends T> replaceSlice(TypeInfo<T,?> typeInfo, Sequence<? extends T> target, Listener<T> listener,
                                               int startPos, int endPos, T newValue) {
        final int size = Sequences.size(target);
        if (startPos > size || startPos < 0 || endPos >= size)
            return target;
        if (newValue == null)
            return replaceSlice(typeInfo, target, listener, startPos, endPos, target.getEmptySequence());

        Sequence<? extends T> result;
        if (startPos == endPos) {
            result = Sequences.replace(target, startPos, newValue);
        } else {
            result = Sequences.replace(target, startPos, endPos+1, newValue);
        }
        if (listener != null) {
            listener.onReplaceElement(startPos, endPos, newValue, target, result);
        }
        return result;
    }

    /**
     * Modify the element at the specified position.  If the position is out of range, the sequence is not
     * modified.
     */
    public static <T> Sequence<? extends T> set(TypeInfo<T,?> typeInfo, Sequence<? extends T> target, Listener<T> listener, int position, T value) {
        return replaceSlice(typeInfo, target, listener, position, position, value);
    }

    /**
     * Delete the element at the specified position.  If the position is out of range, the sequence is not modified.
     */
    public static <T> Sequence<? extends T> delete(TypeInfo<T,?> typeInfo, Sequence<? extends T> target, Listener<T> listener, int position) {
        return replaceSlice(typeInfo, target, listener, position, position, target.getEmptySequence());
    }

    /**
     * Insert the specified value at the end of the sequence
     */
    public static <T> Sequence<? extends T> insert(TypeInfo<T,?> typeInfo, Sequence<? extends T> target, Listener<T> listener, T value) {
        int tsize = target.size();
        return replaceSlice(typeInfo, target, listener, tsize, tsize-1, value);
    }

    /**
     * Insert the specified values at the end of the sequence
     */
    public static <T> Sequence<? extends T> insert(TypeInfo<T,?> typeInfo, Sequence<? extends T> target, Listener<T> listener, Sequence<? extends T> values) {
        int tsize = target.size();
        return replaceSlice(typeInfo, target, listener, tsize, tsize-1, values);
    }

    /**
     * Insert the specified value before the specified position.  If the position is negative, it is inserted before
     * element zero; if it is greater than or equal to the size of the sequence, it is inserted at the end.
     */
    public static <T> Sequence<? extends T> insertBefore(TypeInfo<T,?> typeInfo, Sequence<? extends T> target, Listener<T> listener,
                                               T value, int position) {
        if (position < 0)
            position = 0;
        else {
            int size = target.size();
            if (position > size)
                position = size;
        }
        return replaceSlice(typeInfo, target, listener, position, position-1, value);
    }

    /**
     * Insert the specified values before the specified position.  If the position is negative, they are inserted before
     * element zero; if it is greater than or equal to the size of the sequence, they are inserted at the end.
     */
    public static <T> Sequence<? extends T> insertBefore(TypeInfo<T,?> typeInfo, Sequence<? extends T> target, Listener<T> listener,
                                               Sequence<? extends T> values, int position) {
        if (position < 0)
            position = 0;
        else {
            int size = target.size();
            if (position > size)
                position = size;
        }
        return replaceSlice(typeInfo, target, listener, position, position-1, values);
    }

    /**
     * Delete the elements matching the specified predicate.
     */
    public static <T> Sequence<? extends T> delete(TypeInfo<T,?> typeInfo, Sequence<? extends T> target, Listener<T> listener,
                                         SequencePredicate<? super T> predicate) {
        BitSet bits = target.getBits(predicate);
        if (bits.cardinality() == 0)
            return target;
        bits.flip(0, target.size());
        Sequence<? extends T> result = Sequences.filter(target, bits);
        if (listener != null) {
            Sequence<? extends T> lastValue = target;
            BitSet partialBits = new BitSet(target.size());
            partialBits.flip(0, target.size());
            for (int i = target.size() - 1; i >= 0; i--) {
                // @@@ OPT: Collapse adjacent bits into ranges
                if (!bits.get(i)) {
                    partialBits.flip(i);
                    Sequence<? extends T> nextValue = Sequences.filter(target, partialBits);
                    listener.onReplaceSlice(i, i, target.getEmptySequence(), lastValue, nextValue);
                    lastValue = nextValue;
                }
            }
        }
        return result;
    }
}
