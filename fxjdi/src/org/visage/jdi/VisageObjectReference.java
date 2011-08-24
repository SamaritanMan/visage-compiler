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

import org.visage.jdi.event.VisageEventQueue;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import com.sun.jdi.ShortValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sundar
 */
public class VisageObjectReference extends VisageValue implements ObjectReference {
    public VisageObjectReference(VisageVirtualMachine fxvm, ObjectReference underlying) {
        super(fxvm, underlying);
    }

    public List<ObjectReference> referringObjects(long count) {
        return VisageWrapper.wrapObjectReferences(virtualMachine(), underlying().referringObjects(count));
    }

    public void disableCollection() {
        underlying().disableCollection();
    }

    public void enableCollection() {
        underlying().enableCollection();
    }

    public int entryCount() throws IncompatibleThreadStateException {
        return underlying().entryCount();
    }

    public int getFlagWord(Field field) {
        VisageReferenceType clazz = (VisageReferenceType)referenceType();
        // could this be a java field inherited by an visage class??
        if (!clazz.isVisageType()) {
            return 0;
        }
        Field jdiField = VisageWrapper.unwrap(field); 
        String jdiFieldName = jdiField.name();
        String vflgFieldName = "VFLG" + jdiFieldName;

        Field  vflgField = clazz.fieldByName(vflgFieldName);
        if (vflgField == null) {
            // not all fields have a VFLG, eg, a private field that isn't accessed
            return 0;
        }
        Value vflgValue = underlying().getValue(VisageWrapper.unwrap(vflgField));
        return((ShortValue)vflgValue).value();
    }

    private boolean areFlagBitsSet(Field field, int mask) {
        return (getFlagWord(field) & mask) == mask;
    }

    /**
     * JDI addition: Determines if a field of this object can be modified.  For example,
     * a field declared with a bind cannot be modified.
     *
     * @return <code>true</code> if the specified field is read only; false otherwise.
     */
    public boolean isReadOnly(Field field) {
        return areFlagBitsSet(field, virtualMachine().VisageReadOnlyFlagMask());
    }

    /**
     * JDI addition: Determines if the value of a field of this object is valid.  A value
     * is invalid if a new value has been specified for the field, but not yet
     * stored into the field, for example, because the field is lazily bound.
     *
     * @return <code>true</code> if the value of the specified field is invalid; false otherwise.
     */
    public boolean isInvalid(Field field) {
        return areFlagBitsSet(field, virtualMachine().VisageInvalidFlagMask());
    }

    /**
     * JDI addition: Determines if a field was declared with a bind clause.
     *
     * @return <code>true</code> if the specified field was declared with a bind clause; false otherwise.
     */
    public boolean isBound(Field field) {
        return areFlagBitsSet(field, virtualMachine().VisageBoundFlagMask());
    }

    /**
     * JDI extension:  This will call the get function for the field if one exists via invokeMethod.
     * The call to invokeMethod is preceded by a call to {@link VisageEventQueue#setEventControl(boolean)} passing true
     * and is followed by a call to {@link VisageEventQueue#setEventControl(boolean)} passing false.
     *
     * If an invokeMethod Exception occurs, it is saved and can be accessed by calling 
     * {@link VisageVirtualMachine#lastFieldAccessException()}. In this case,
     * the default value for the type of the field is returned for a PrimitiveType,
     * while null is returned for a non PrimitiveType.
     */
    public Value getValue(Field field) {
        virtualMachine().setLastFieldAccessException(null);
        Field jdiField = VisageWrapper.unwrap(field);
        VisageReferenceType wrappedClass = (VisageReferenceType)referenceType();
        if (!wrappedClass.isVisageType()) {
            return VisageWrapper.wrap(virtualMachine(), underlying().getValue(jdiField));
        }

        //get$xxxx methods exist for fields except private fields which have no binders
        ReferenceType unwrappedClass = VisageWrapper.unwrap(referenceType());
        List<Method> mth = unwrappedClass.methodsByName("get" + jdiField.name());
        if (mth.size() == 0) {
            return VisageWrapper.wrap(virtualMachine(), underlying().getValue(jdiField));
        }
        Exception theExc = null;
        VisageEventQueue eq = virtualMachine().eventQueue();
        try {
            eq.setEventControl(true);
            return invokeMethod(virtualMachine().uiThread(), mth.get(0), new ArrayList<Value>(0), ObjectReference.INVOKE_SINGLE_THREADED);
        } catch(InvalidTypeException ee) {
            theExc = ee;
        } catch(ClassNotLoadedException ee) {
            theExc = ee;
        } catch(IncompatibleThreadStateException ee) {
            theExc = ee;
        } catch(InvocationException ee) {
            theExc = ee;
        } finally {
            eq.setEventControl(false);
        }
        // We don't have to catch IllegalArgumentException.  It is an unchecked exception for invokeMethod
        // and for getValue

        virtualMachine().setLastFieldAccessException(theExc);
        try {
            return virtualMachine().defaultValue(field.type());
        } catch(ClassNotLoadedException ee) {
            // The type has to be a ReferenceType for which we return null;
            return null;
        }
    }

    /**
     * JDI extension:  This will call the get function for a field if one exists via invokeMethod.
     * The call to invokeMethod is preceded by a call to {@link VisageEventQueue#setEventControl(boolean)} passing true
     * and is followed by a call to {@link VisageEventQueue#setEventControl(boolean)} passing false.
     *
     * If an invokeMethod Exception occurs, it is saved and can be accessed by calling 
     * {@link VisageVirtualMachine#lastFieldAccessException()}. In this case,
     * the default value for the type of the field is returned for a PrimitiveType,
     * while null is returned for a non PrimitiveType.
     */
    public Map<Field, Value> getValues(List<? extends Field> wrappedFields) {
        virtualMachine().setLastFieldAccessException(null);

        // We will find fields which have no getters, and call the underlying
        // getValues to get values for all of them in one fell swoop.
        Map<Field, Field> unwrappedToWrappedMap = new HashMap<Field, Field>();
        List<Field> noGetterUnwrappedFields = new ArrayList<Field>();    // fields that don't have getters

        // But first, for fields that do have getters, call invokeMethod
        // or we will call VisageGetValue for each, depending on doInvokes
        Map<Field, Value> result = new HashMap<Field, Value>();
        VisageReferenceType wrappedClass = (VisageReferenceType)referenceType();
        ReferenceType unwrappedClass = VisageWrapper.unwrap(wrappedClass);

        // Create the above Maps and lists
        for (Field wrappedField : wrappedFields) {
            Field unwrapped = VisageWrapper.unwrap(wrappedField);
            if (wrappedClass.isVisageType()) {
                List<Method> mth = unwrappedClass.methodsByName("get" + unwrapped.name());
                if (mth.size() == 0) {
                    // No getter
                    unwrappedToWrappedMap.put(unwrapped, wrappedField);
                    noGetterUnwrappedFields.add(unwrapped);
                } else {
                    // Field has a getter
                    result.put(wrappedField, getValue(wrappedField));
                }
            } else {
                // Java type
                unwrappedToWrappedMap.put(unwrapped, wrappedField);
                noGetterUnwrappedFields.add(unwrapped);
            }                
        }

        // Get values for all the noGetter fields.  Note that this gets them in a single JDWP trip
        Map<Field, Value> unwrappedFieldValues = underlying().getValues(noGetterUnwrappedFields);

        // for each input Field, create a result map entry with that field as the
        // key, and the value returned by getValues, or null if the field is invalid.

        // Make a pass over the unwrapped no getter fields and for each, put its
        // wrapped version, and wrapped value into the result Map.
        for (Map.Entry<Field, Field> unwrappedEntry: unwrappedToWrappedMap.entrySet()) {
            Field wrappedField = unwrappedEntry.getValue();
            Value resultValue = VisageWrapper.wrap(virtualMachine(), 
                                             unwrappedFieldValues.get(unwrappedEntry.getKey()));
            result.put(wrappedField, resultValue);
        }
        return result;
    }

    public VisageValue invokeMethod(ThreadReference thread, Method method, List<? extends Value> values, int options)
            throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException {
        Value value =
                underlying().invokeMethod(
                    VisageWrapper.unwrap(thread), VisageWrapper.unwrap(method),
                    VisageWrapper.unwrapValues(values), options);
        return VisageWrapper.wrap(virtualMachine(), value);
    }

    public boolean isCollected() {
        return underlying().isCollected();
    }

    public VisageThreadReference owningThread() throws IncompatibleThreadStateException {
        return VisageWrapper.wrap(virtualMachine(), underlying().owningThread());
    }

    public VisageReferenceType referenceType() {
        return VisageWrapper.wrap(virtualMachine(), underlying().referenceType());
    }

    /**
     * JDI extension:  This will call the set function if one exists via invokeMethod.
     * The call to invokeMethod is preceded by a call to {@link VisageEventQueue#setEventControl(boolean)} passing true
     * and is followed by a call to {@link VisageEventQueue#setEventControl(boolean)} passing false.
     *
     * If an invokeMethod Exception occurs, it is saved and can be accessed by calling 
     * {@link VisageVirtualMachine#lastFieldAccessException()}.
     */
    public void setValue(Field field, Value value) throws
        InvalidTypeException, ClassNotLoadedException {
        virtualMachine().setLastFieldAccessException(null);
        Field jdiField = VisageWrapper.unwrap(field);
        Value jdiValue = VisageWrapper.unwrap(value);
        VisageReferenceType clazz = (VisageReferenceType)referenceType();
        if (!clazz.isVisageType()) {
            underlying().setValue(jdiField, jdiValue);
            return;
        }
        if (isReadOnly(field)) {
            throw new IllegalArgumentException("Error: Cannot set value of a read-only field: " + field);
        } 
        if (isBound(field)) {
            throw new IllegalArgumentException("Error: Cannot set value of a bound field: " + field);
        }

        //get$xxxx methods exist for fields except private fields which have no binders
        List<Method> mth = clazz.methodsByName("set" + jdiField.name());
        if (mth.size() == 0) {
            // there is no setter
            underlying().setValue(jdiField, jdiValue);
            return;
        }
        // there is a setter
        ArrayList<Value> args = new ArrayList<Value>(1);
        args.add(jdiValue);
        Exception theExc = null;
        VisageEventQueue eq = virtualMachine().eventQueue();
        try {
            eq.setEventControl(true);
            invokeMethod(virtualMachine().uiThread(), mth.get(0), args, ObjectReference.INVOKE_SINGLE_THREADED);
        } catch(InvalidTypeException ee) {
            theExc = ee;
        } catch(ClassNotLoadedException ee) {
            theExc = ee;
        } catch(IncompatibleThreadStateException ee) {
            theExc = ee;
        } catch(InvocationException ee) {
            theExc = ee;
        } finally {
            eq.setEventControl(false);
        }
        // We don't have to catch IllegalArgumentException.  It is an unchecked exception for invokeMethod
        // and for getValue

        virtualMachine().setLastFieldAccessException(theExc);
    }

    public long uniqueID() {
        return underlying().uniqueID();
    }

    public List<ThreadReference> waitingThreads() throws IncompatibleThreadStateException {
        return VisageWrapper.wrapThreads(virtualMachine(), underlying().waitingThreads());
    }

    @Override
    protected ObjectReference underlying() {
        return (ObjectReference) super.underlying();
    }
}
