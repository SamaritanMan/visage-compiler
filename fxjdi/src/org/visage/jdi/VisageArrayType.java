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

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Type;
import java.util.List;

/**
 *
 * @author sundar
 */
public class VisageArrayType extends VisageReferenceType implements ArrayType {
    public VisageArrayType(VisageVirtualMachine visagevm, ArrayType underlying) {
        super(visagevm, underlying);
    }

    public String componentSignature() {
        return underlying().componentSignature();
    }

    public VisageType componentType() throws ClassNotLoadedException {
        return VisageWrapper.wrap(virtualMachine(), underlying().componentType());
    }

    public String componentTypeName() {
        return underlying().componentTypeName();
    }

    public VisageArrayReference newInstance(int length) {
        return VisageWrapper.wrap(virtualMachine(), underlying().newInstance(length));
    }

    @Override
    protected ArrayType underlying() {
        return (ArrayType) super.underlying();
    }
}
