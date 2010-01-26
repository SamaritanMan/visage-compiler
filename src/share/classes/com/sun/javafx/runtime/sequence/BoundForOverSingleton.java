/*
 * Copyright 2010 Sun Microsystems, Inc.  All Rights Reserved.
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
import com.sun.javafx.runtime.FXObject;

public abstract class BoundForOverSingleton<T, PT> extends BoundFor<T, PT> {

    private boolean inWholesaleUpdate = true; // ignore initial individual updates
    private int lowestInvalidPart;            // lowest part index that is invalid
    private int highestInvalidPart = -1;      // highest part index that is invalid, negative means none
    protected int pendingTriggers = 0;        // number of invalidations seen minus number of triggers seen
    private boolean inPartChange = false;     // adding, removing, changing parts
    private int sizeAtLastTrigger = 0;        // size the previous time we did trigger phase invalidate

    private static final boolean DEBUG = false;

    public BoundForOverSingleton(FXObject container, int forVarNum, int inductionSeqVarNum, boolean dependsOnIndex) {
        super(container, forVarNum, inductionSeqVarNum, dependsOnIndex);
    }

    // Called by invalidate when the result of a part changes.
    @Override
    public void update$(FXObject src, final int varNum, int startPos, int endPos, int newLength, final int phase) {
        if (uninitialized || inWholesaleUpdate)
            return;
        int ipart = ((FXForPart) src).getIndex$();
        if ((phase & PHASE_TRANS$PHASE) == PHASE$INVALIDATE) {
            if (DEBUG) System.err.println("inv update$ id: " + forVarNum + ", ipart: " + ipart + ", " + lowestInvalidPart + " ... " + highestInvalidPart);
            if (highestInvalidPart < 0) {
                // No outstanding invalid region, mark this as the beginning and end of region
                // and send a blanket invalidation
                highestInvalidPart = lowestInvalidPart = ipart;
                pendingTriggers = 1;
                blanketInvalidationOfBoundFor();
            } else {
                // Already have invalid parts, encompass ours
                ++pendingTriggers;
                if (ipart < lowestInvalidPart) {
                    lowestInvalidPart = ipart;
                }
                if (ipart > highestInvalidPart) {
                    highestInvalidPart = ipart;
                }
            }
            return;
        }
        --pendingTriggers;
        if (DEBUG) System.err.println("+trig update$ id: " + forVarNum + ", ipart: " + ipart + ", " + lowestInvalidPart + " ... " + highestInvalidPart);

        if (pendingTriggers > 0) {
            // Trigger when all the part triggers have come in
            return;
        }
        assert pendingTriggers == 0;

        if (inPartChange) {
            // Part change will handle update
            return;
        }
        if (DEBUG) System.err.println(".trig update$ id: " + forVarNum + ", ipart: " + ipart + ", " + lowestInvalidPart + " ... " + highestInvalidPart);

        // Do invalidation for all currently invalid parts
        int invStartPos = lowestInvalidPart;
        int invEndPos = highestInvalidPart + 1;

        // Set-up to lazily update lengths
        sizeAtLastTrigger = numParts;

        // Calculate the inserted length (in the new parts)
        int insertedLength = invEndPos - invStartPos;

        //TODO: This maintains existing semantics -- remove for better performance
        for (int i = invStartPos; i < invEndPos; ++i) {
            get(i);
        }

        highestInvalidPart = -1;

        // Send invalidation
        if (DEBUG) System.err.println("-trig update$ id: " + forVarNum + ", ipart: " + ipart + ", oldStart: " + invStartPos + ", oldEndPos: " + invEndPos + ", insertedLength: " + insertedLength);
        container.invalidate$(forVarNum, invStartPos, invEndPos, insertedLength, FXObject.PHASE_TRANS$CASCADE_TRIGGER);
    }

    void showStates(String label) {
            for (int ips = 0; ips < numParts; ++ips) {
                System.err.print(getPart(ips).getFlags$(partResultVarNum) & VFLGS$STATE_MASK);
            }
            System.err.println(" - " + label);
    }

    // Called by invalidate when the input sequence changes.
    public void replaceParts(int startPart, int endPart, int insertedParts, int phase) {
        if (uninitialized)
            return;
        if ((phase & PHASE_TRANS$PHASE) == PHASE$INVALIDATE) {
            if (DEBUG) System.err.println("inv replaceParts id: " + forVarNum + ", inPartChange: " + inPartChange);
            if (!inPartChange) {
                inPartChange = true;
                blanketInvalidationOfBoundFor();
            }
            return;
        }
        if (DEBUG) System.err.println("+trig replaceParts id: " + forVarNum + ", startPart: " + startPart + ", endPart: " + endPart + ", insertedParts: " + insertedParts);
        boolean outstandingInvalidations = highestInvalidPart >= 0;

        if (startPart < 0) {
            // This is a no-change trigger
            if (outstandingInvalidations) {
                // We collected part updates during this no-change invalidation, proceed using them
                startPart = lowestInvalidPart;
                endPart = highestInvalidPart;
                insertedParts = highestInvalidPart - lowestInvalidPart;
            } else {
                // Pass on this no-change trigger
                container.invalidate$(forVarNum, SequencesBase.UNDEFINED_MARKER_INT, SequencesBase.UNDEFINED_MARKER_INT, 0, FXObject.PHASE_TRANS$CASCADE_INVALIDATE);
                return;
            }
        }

        int newEndPart = startPart + insertedParts;
        int deltaParts = newEndPart - endPart;
        int newNumParts = numParts + deltaParts;

        int trailingLength;

        // Don't generate individual updates
        inWholesaleUpdate = true;

        if (parts == null) {
            assert startPart == 0;
            assert endPart == 0;
            trailingLength = 0;

            // Allocate the new elements
            FXForPart<PT>[] newParts = (FXForPart<PT>[]) new FXForPart[newNumParts];

            // Install new parts
            parts = newParts;
            numParts = newNumParts;

            // Fill in the new parts
            buildParts(0, insertedParts);
        } else {
            trailingLength = numParts - endPart;

            if (DEBUG) System.err.println(".trig replaceParts id: " + forVarNum + ", parts.len: " + parts.length + ", start: " + startPart + ", end: " + endPart +
                        ", newNumParts: " + newNumParts + ", s+i: " + (startPart + insertedParts) + ", trail: " + trailingLength);

            int endPartCopy = (newEndPart < endPart)? newEndPart : endPart;

            // In-place modification.  Update reused induction vars (if any) Invalidation from parts.
            for (int ips = startPart; ips < endPartCopy; ++ips) {
                syncInductionVar(ips);
            }

            if (newNumParts != numParts) {
                for (int ips = endPartCopy; ips < endPart; ++ips) {
                    removeDependent$(parts[ips], partResultVarNum, this);
                }

                // Allocate the new elements
                FXForPart<PT>[] newParts = (FXForPart<PT>[]) new FXForPart[newNumParts];

                // Copy the existing parts
                System.arraycopy(parts, 0, newParts, 0, endPartCopy);
                System.arraycopy(parts, endPart, newParts, newEndPart, trailingLength);

                // Install new parts
                parts = newParts;
                numParts = newNumParts;

                // Fill in the new parts (if any)
                buildParts(endPartCopy, newEndPart);
            }
        }

        // Update the trailing indices -- need indices for internal bookkeeping, always update
        assert startPart + insertedParts + trailingLength == numParts;
        for (int ips = newEndPart; ips < numParts; ++ips) {
            getPart(ips).adjustIndex$(deltaParts);
        }

        // Set-up to lazily update lengths
        int previousSize = sizeAtLastTrigger;
        sizeAtLastTrigger = numParts;

        // Invalidation parameters
        int invStartPos;
        int invEndPos;
        int newEndPos;

        if (outstandingInvalidations) {
            // Trying to change parts and do individual update at the same time -- invalidate everything
            invStartPos = 0;
            invEndPos = previousSize;
            newEndPos = sizeAtLastTrigger;
        } else if (dependsOnIndex) {
            // We depend on indices, everything after the start point is invalid
            invStartPos = startPart;
            invEndPos = previousSize;
            newEndPos = sizeAtLastTrigger;
        } else {
            // Calculate the inserted length (in the new parts)
            invStartPos = startPart;
            invEndPos = endPart;
            newEndPos = newEndPart;
        }

        //TODO: This maintains existing semantics -- remove for better performance
        for (int i = invStartPos; i < newEndPos; ++i) {
            get(i);
        }

        if (DEBUG) System.err.println("-trig replaceParts id: " + forVarNum + ", invStartPos: " + invStartPos + ", invEndPos: " + invEndPos + ", len: " + (newEndPos - invStartPos));

        container.invalidate$(forVarNum,
                invStartPos,
                invEndPos,
                newEndPos - invStartPos,
                FXObject.PHASE_TRANS$CASCADE_TRIGGER);

        inPartChange = false;
        inWholesaleUpdate = false;
        highestInvalidPart = -1;
    }

    public int size() {
        initializeIfNeeded();
        return numParts;
    }

    public T get(int index) {
        initializeIfNeeded();
        if (index < 0 || index >= numParts) {
            return null;
        } else {
            FXForPart part = getPart(index);
            T res = (T) part.get$(partResultVarNum);
            return res;
        }
    }

}

