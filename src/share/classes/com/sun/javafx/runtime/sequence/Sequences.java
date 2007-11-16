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

package com.sun.javafx.runtime.sequence;

import java.util.BitSet;
import java.util.List;

/**
 * Sequences -- static helper methods for constructing derived sequences. Implements heuristics for reducing time and
 * space overhead, such as flattening complicated sequence trees where appropriate and ignoring null transformations
 * (such as appending an empty sequence). These methods are generally preferable to the constructors for
 * CompositeSequence, FilterSequence, SubSequence, etc, because they implement heuristics for sensible time-space
 * tradeoffs.
 * 
 * @author Brian Goetz
 */
public final class Sequences {

    public static final Integer INTEGER_ZERO = 0;
    public static final Double DOUBLE_ZERO = 0.0;
    public static final Boolean BOOLEAN_ZERO = false;

    // Inhibit instantiation
    private Sequences() { }

    /** Factory for simple sequence generation */
    public static<T> Sequence<T> make(Class<T> clazz, T... values) {
        // OPT: for small sequences, just copy the elements
        return new ArraySequence<T>(clazz, values);
    }

    /** Factory for simple sequence generation */
    public static<T> Sequence<T> make(Class<T> clazz, T[] values, int size) {
        // OPT: for small sequences, just copy the elements
        return new ArraySequence<T>(clazz, values, size);
    }

    /** Factory for simple sequence generation */
    public static<T> Sequence<T> make(Class<T> clazz, List<T> values) {
        // OPT: for small sequences, just copy the elements
        return new ArraySequence<T>(clazz, values);
    }

    /** Concatenate two sequences into a new sequence.  */
    public static<T> Sequence<T> concatenate(Class<T> clazz, Sequence<? extends T> first, Sequence<? extends T> second) {
        // OPT: for small sequences, just copy the elements
        if (first.size() == 0)
            return upcast(clazz, second);
        else if (second.size() == 0)
            return upcast(clazz, first);
        else
            return new CompositeSequence<T>(clazz, first, second);
    }

    /** Concatenate zero or more sequences into a new sequence.  */
    public static<T> Sequence<T> concatenate(Class<T> clazz, Sequence<? extends T>... seqs) {
        // OPT: for small sequences, just copy the elements
        return new CompositeSequence<T>(clazz, seqs);
    }

    /** Create an Integer range sequence ranging from lower to upper inclusive. */
    public static Sequence<Integer> range(int lower, int upper) {
        return new IntRangeSequence(lower, upper);
    }

    /** Create an Integer range sequence ranging from lower to upper inclusive, incrementing by the specified step. */
    public static Sequence<Integer> range(int lower, int upper, int step) {
        return new IntRangeSequence(lower, upper, step);
    }

    /** Create an Integer range sequence ranging from lower to upper exclusive. */
    public static Sequence<Integer> rangeExclusive(int lower, int upper) {
        return new IntRangeSequence(lower, upper, true);
    }

    /** Create an Integer range sequence ranging from lower to upper exnclusive, incrementing by the specified step. */
    public static Sequence<Integer> rangeExclusive(int lower, int upper, int step) {
        return new IntRangeSequence(lower, upper, step, true);
    }

    /** Create a double range sequence ranging from lower to upper inclusive, incrementing by the specified step. */
    public static Sequence<Double> range(double lower, double upper, double step) {
        return new NumberRangeSequence(lower, upper, step);
    }

    /** Create a double range sequence ranging from lower to upper exnclusive, incrementing by the specified step. */
    public static Sequence<Double> rangeExclusive(double lower, double upper, double step) {
        return new NumberRangeSequence(lower, upper, step, true);
    }

    /** Create a filtered sequence.  A filtered sequence contains some, but not necessarily all, of the elements
     * of another sequence, in the same order as that sequence.  If bit n is set in the BitSet, then the element
     * at position n of the original sequence appears in the filtered sequence.  */
    public static<T> Sequence<T> filter(Sequence<T> seq, BitSet bits) {
        // OPT: for small sequences, just copy the elements
        if (bits.cardinality() == seq.size() && bits.nextClearBit(0) == seq.size())
            return seq;
        else if (bits.cardinality() == 0)
            return EmptySequence.get(seq.getElementType());
        else
            return new FilterSequence<T>(seq, bits);
    }

    /** Extract a subsequence from the specified sequence, starting as the specified start position, and up to but
     * not including the specified end position.  If the start position is negative it is assumed to be zero; if the
     * end position is greater than seq.size() it is assumed to be seq.size().  */
    public static<T> Sequence<T> subsequence(Sequence<T> seq, int start, int end) {
        // OPT: for small sequences, just copy the elements
        if (start >= end)
            return EmptySequence.get(seq.getElementType());
        else if (start <= 0 && end >= seq.size())
            return seq;
        else
            return new SubSequence<T>(seq, start, end);
    }

    /** Create a sequence containing a single element, the specified value */
    public static<T> Sequence<T> singleton(Class<T> clazz, T t) {
        return new SingletonSequence<T>(clazz, t);
    }

    /** Create an empty sequence */
    public static<T> Sequence<T> emptySequence(Class<T> clazz) {
        return EmptySequence.get(clazz);
    }

    /** Reverse an existing sequence */
    public static<T> Sequence<T> reverse(Sequence<T> sequence) {
        return new ReverseSequence<T>(sequence);
    }

    /** Create a new sequence that is the result of applying a mapping function to each element */
    public static<T,U> Sequence<U> map(Class<U> clazz, Sequence<T> sequence, SequenceMapper<T, U> mapper) {
        // OPT: for small sequences, do the mapping eagerly
        return new MapSequence<T,U>(clazz, sequence, mapper);
    }

    /** Upcast a sequence of T to a sequence of superclass-of-T */
    public static<T> Sequence<T> upcast(Class<T> clazz, Sequence<? extends T> sequence) {
        return new UpcastSequence<T>(clazz, sequence);
    }
}
