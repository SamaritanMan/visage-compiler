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

package com.sun.javafx.jdi.request;

import com.sun.javafx.jdi.FXThreadReference;
import com.sun.javafx.jdi.FXVirtualMachine;
import com.sun.javafx.jdi.FXWrapper;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.request.StepRequest;

/**
 *
 * @author sundar
 */
public class FXStepRequest extends FXEventRequest implements StepRequest {
    public FXStepRequest(FXVirtualMachine fxvm, StepRequest underlying) {
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

    public void addInstanceFilter(ObjectReference ref) {
        underlying().addInstanceFilter(FXWrapper.unwrap(ref));
    }

    public int depth() {
        return underlying().depth();
    }

    public int size() {
        return underlying().size();
    }

    public FXThreadReference thread() {
        return FXWrapper.wrap(virtualMachine(), underlying().thread());
    }

    @Override
    protected StepRequest underlying() {
        return (StepRequest) super.underlying();
    }
}
