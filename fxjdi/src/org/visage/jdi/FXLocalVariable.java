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

package org.visage.jdi;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.StackFrame;

/**
 *
 * @author sundar
 */
public class FXLocalVariable extends FXMirror implements LocalVariable {
    public FXLocalVariable(FXVirtualMachine fxvm, LocalVariable underlying) {
        super(fxvm, underlying);
    }

    public String genericSignature() {
        return underlying().genericSignature();
    }

    public boolean isArgument() {
        return underlying().isArgument();
    }

    public boolean isVisible(StackFrame frame) {
        return underlying().isVisible(FXWrapper.unwrap(frame));
    }

    public String name() {
        return underlying().name();
    }

    public String signature() {
        return underlying().signature();
    }

    public FXType type() throws ClassNotLoadedException {
        return FXWrapper.wrap(virtualMachine(), underlying().type());
    }

    public String typeName() {
        return underlying().typeName();
    }

    public int compareTo(LocalVariable o) {
        return underlying().compareTo(FXWrapper.unwrap(o));
    }

    @Override
    protected LocalVariable underlying() {
        return (LocalVariable) super.underlying();
    }
}
