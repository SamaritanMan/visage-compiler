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

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Value;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sundar
 */
public class VisageStackFrame extends VisageMirror implements StackFrame {
    public VisageStackFrame(VisageVirtualMachine visagevm, StackFrame underlying) {
        super(visagevm, underlying);
    }

    // Is this frame executing Visage code?
    public boolean isVisageFrame() {
        return location().declaringType().isVisageType();
    }

    public List<Value> getArgumentValues() {
        return VisageWrapper.wrapValues(virtualMachine(), underlying().getArgumentValues());
    }

    public VisageValue getValue(LocalVariable var) {
        if (isVisageSyntheticLocalVar(var.name())) {
            throw new IllegalArgumentException("invalid var: " + var.name());
        }
        return VisageWrapper.wrap(virtualMachine(), underlying().getValue(VisageWrapper.unwrap(var)));
    }

    public Map<LocalVariable, Value> getValues(List<? extends LocalVariable> vars) {
        Map<LocalVariable, LocalVariable> fieldMap = new HashMap<LocalVariable, LocalVariable>();
        List<LocalVariable> unwrappedLocalVariables = new ArrayList<LocalVariable>();
        for (LocalVariable var : vars) {
            LocalVariable unwrapped = VisageWrapper.unwrap(var);
            if (isVisageSyntheticLocalVar(unwrapped.name())) {
                throw new IllegalArgumentException("invalid var: " + var.name());
            }
            unwrappedLocalVariables.add(unwrapped);
            fieldMap.put(unwrapped, var);
        }
        Map<LocalVariable, Value> fieldValues = underlying().getValues(unwrappedLocalVariables);
        Map<LocalVariable, Value> result = new HashMap<LocalVariable, Value>();
        for (Map.Entry<LocalVariable, Value> entry: fieldValues.entrySet()) {
            result.put(fieldMap.get(entry.getKey()), entry.getValue());
        }
        return result;
    }

    public VisageLocation location() {
        return VisageWrapper.wrap(virtualMachine(), underlying().location());
    }

    public void setValue(LocalVariable var, Value value) throws InvalidTypeException, ClassNotLoadedException {
        if (isVisageSyntheticLocalVar(var.name())) {
            throw new IllegalArgumentException("invalid var: " + var.name());
        }
        underlying().setValue(VisageWrapper.unwrap(var), VisageWrapper.unwrap(value));
    }

    public VisageObjectReference thisObject() {
        return VisageWrapper.wrap(virtualMachine(), underlying().thisObject());
    }

    public VisageThreadReference thread() {
        return VisageWrapper.wrap(virtualMachine(), underlying().thread());
    }

    public VisageLocalVariable visibleVariableByName(String name) throws AbsentInformationException {
        if (isVisageSyntheticLocalVar(name)) {
            return null;
        } else {
            return VisageWrapper.wrap(virtualMachine(), underlying().visibleVariableByName(name));
        }
    }

    public List<LocalVariable> visibleVariables() throws AbsentInformationException {
        List<LocalVariable> locals = underlying().visibleVariables();
        List<LocalVariable> result = new ArrayList<LocalVariable>();
        for (LocalVariable var : locals) {
            if (! isVisageSyntheticLocalVar(var.name())) {
                result.add(VisageWrapper.wrap(virtualMachine(), var));
            }
        }
        return result;
    }

    @Override
    protected StackFrame underlying() {
        return (StackFrame) super.underlying();
    }

    public boolean isVisageSyntheticLocalVar(String name) {
        // FIXME: can we have a better test for Visage synthetic local var?
        return isVisageFrame() && name.indexOf('$') != -1;
    }
}
