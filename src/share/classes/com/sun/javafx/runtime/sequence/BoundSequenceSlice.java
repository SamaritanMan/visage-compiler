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

package com.sun.javafx.runtime.sequence;

import com.sun.javafx.runtime.TypeInfo;
import com.sun.javafx.runtime.location.IntLocation;
import com.sun.javafx.runtime.location.ChangeListener;
import com.sun.javafx.runtime.location.SequenceLocation;

/**
 * BoundSequenceSlice
 *
 * @author Brian Goetz
 * @author Zhiqun Chen
 */
class BoundSequenceSlice<T> extends AbstractBoundSequence<T> implements SequenceLocation<T> {
    private final SequenceLocation<T> sequenceLoc;
    private final IntLocation lowerLoc;
    private final IntLocation upperLoc;
    private final boolean isExclusive;
    private int lower, upper/*exclusive*/;
    private int size;

    BoundSequenceSlice(boolean lazy, TypeInfo<T, ?> typeInfo, SequenceLocation<T> sequenceLoc, IntLocation lowerLoc, IntLocation upperLoc, boolean isExclusive) {
        super(lazy, typeInfo);
        this.sequenceLoc = sequenceLoc;
        this.lowerLoc = lowerLoc;
        this.upperLoc = upperLoc;
        this.isExclusive = isExclusive;
        if (!lazy)
            setInitialValue(computeValue());
        addTriggers();
    }

    protected Sequence<? extends T> computeValue() {
        computeBounds(true, true);
        return sequenceLoc.get().getSlice(lower, upper);
    }
    
    /**
     * adjust for exclusive upper bound
     */
    private int adjusted(int upperValue) {
        return (isExclusive ? 0 : 1) + upperValue;
    }

    private void computeBounds(boolean updateLower, boolean updateUpper) {
        int seqSize = sequenceLoc.getAsSequence().size();
        
        if (updateLower) {
            lower = lowerLoc.get();
        }
        if (updateUpper) {
            upper = adjusted(upperLoc == null ? seqSize-1 : upperLoc.get());
        }
        
        if (seqSize == 0) {
            size = 0;
        } else {
            int range = ((upper > seqSize)? seqSize:upper) - ((lower<0)? 0: lower);
            size = (range >= 0) ? range : 0;
        }
    }
          
    private void addTriggers() {
        if (lazy) {
            sequenceLoc.addInvalidationListener(new InvalidateMeListener());
            lowerLoc.addInvalidationListener(new InvalidateMeListener());
            if (upperLoc != null)
                upperLoc.addInvalidationListener(new InvalidateMeListener());
        }
        else {
            sequenceLoc.addSequenceChangeListener(new ChangeListener<T>() {

                @Override
                public void onChange(ArraySequence<T> buffer, Sequence<? extends T> oldValue, int startPos, int endPos, Sequence<? extends T> newElements) {
                    int oldSize = size;
                    computeBounds(true, true);

                    Sequence<? extends T> newValue = buffer != null ? buffer : newElements;
                    Sequence<? extends T> newSeq = newValue.getSlice(lower, upper);
                    updateSlice(0, oldSize, newSeq); // FIXME inefficient for whole-sequence replacement
                }
            });
            lowerLoc.addChangeListener(new ChangeListener<Integer>() {

                @Override
                public void onChange(int oldValue, int newValue) {
                    assert oldValue != newValue;
                    int oldSize = size;
                    computeBounds(true, false);

                    if (sequenceLoc.getAsSequence().size() > 0 && size != oldSize) {
                        if (size > oldSize) {
                            updateSlice(
                                    0,
                                    0,
                                    sequenceLoc.getSlice(newValue, (oldSize == 0) ? upper : oldValue));
                        } else {
                            updateSlice(
                                    0,
                                    oldSize - size,
                                    sequenceLoc.getAsSequence().getEmptySequence());
                        }
                    }
                }
            });
            if (upperLoc != null) {
                upperLoc.addChangeListener(new ChangeListener<Integer>() {

                    @Override
                    public void onChange(int oldValue, int newValue) {
                        assert oldValue != newValue;
                        int oldSize = size;
                        computeBounds(false, true);

                        if (sequenceLoc.getAsSequence().size() > 0 && size != oldSize) {
                            if (size > oldSize) {
                                int oldUpper = adjusted(oldValue);
                                updateSlice(
                                        oldSize,
                                        oldSize,
                                        sequenceLoc.getSlice((oldUpper >= lower) ? oldUpper : lower, upper));
                            } else {
                                updateSlice(
                                        size,
                                        oldSize,
                                        sequenceLoc.getAsSequence().getEmptySequence());
                            }
                        }
                    }
                });
            }
        }
    }
}
