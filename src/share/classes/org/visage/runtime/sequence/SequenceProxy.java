/*
 * Copyright 2009 Sun Microsystems, Inc.  All Rights Reserved.
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

package org.visage.runtime.sequence;
import org.visage.runtime.*;

/**
 * @author Robert Field
 */
public class SequenceProxy<T> extends AbstractSequence<T> {
    VisageObject instance;
    int varNum;

    public SequenceProxy(TypeInfo<T> ti, VisageObject instance, int varNum) {
        super(ti);
        this.instance = instance;
        this.varNum = varNum;
    }

    public VisageObject instance() {
        return instance;
    }

    public int varNum() {
        return varNum;
    }
    
    public int size() {
        return instance.size$(varNum);
    }

    public T get(int position) {
        return (T) instance.elem$(varNum, position);
    }

    @Override
    public boolean getAsBoolean(int position) {
        return instance.getAsBoolean$(varNum, position);
    }

    @Override
    public char getAsChar(int position) {
        return instance.getAsChar$(varNum, position);
    }

    @Override
    public byte getAsByte(int position) {
        return instance.getAsByte$(varNum, position);
    }

    @Override
    public short getAsShort(int position) {
        return instance.getAsShort$(varNum, position);
    }

    @Override
    public int getAsInt(int position) {
        return instance.getAsInt$(varNum, position);
    }

    @Override
    public long getAsLong(int position) {
        return instance.getAsLong$(varNum, position);
    }

    @Override
    public float getAsFloat(int position) {
        return instance.getAsFloat$(varNum, position);
    }

    @Override
    public double getAsDouble(int position) {
        return instance.getAsDouble$(varNum, position);
    }
}
