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

package com.sun.visage.jdi.request;

import com.sun.visage.jdi.FXVirtualMachine;
import com.sun.visage.jdi.FXWrapper;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.request.ClassPrepareRequest;

/**
 *
 * @author sundar
 */
public class FXClassPrepareRequest extends FXEventRequest implements ClassPrepareRequest {
    public FXClassPrepareRequest(FXVirtualMachine fxvm, ClassPrepareRequest underlying) {
        super(fxvm, underlying);
    }

    public void addClassExclusionFilter(String arg0) {
        underlying().addClassExclusionFilter(arg0);
    }

    public void addClassFilter(ReferenceType arg0) {
        underlying().addClassFilter(FXWrapper.unwrap(arg0));
    }

    public void addClassFilter(String arg0) {
        underlying().addClassFilter(arg0);
    }

    public void addSourceNameFilter(String arg0) {
        underlying().addSourceNameFilter(arg0);
    }

    @Override
    protected ClassPrepareRequest underlying() {
        return (ClassPrepareRequest) super.underlying();
    }
}
