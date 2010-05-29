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

/*
 * This is a basic test to show that internal instance vars are properly filtered out
 * and that the internal naming scheme for user instance vars is filtered out.
 */

package com.sun.javafx.jdi.test;

import com.sun.jdi.ReferenceType;
import com.sun.javafx.jdi.FXReferenceType;
import com.sun.javafx.jdi.FXObjectReference;
import com.sun.jdi.Field;
import com.sun.jdi.Value;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.event.BreakpointEvent;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import junit.framework.Assert;

public class PublicFetchInvalidTest extends JavafxTestBase {
    ReferenceType targetClass;
    ThreadReference mainThread;

    private static final String targetClassName = "com.sun.javafx.jdi.test.target.PublicFetchInvalidTarget";

    public PublicFetchInvalidTest() {
        super(targetClassName);
    }

    @Test
    public void testFetchInvalid() {
        try {
            startTests();
        } catch (Exception exp) {
            exp.printStackTrace();
            Assert.fail(exp.getMessage());
        }
    }

    /********** test core **********/

    protected void runTests() throws Exception {
        writeActual("stop in = " + targetClassName + "$sam");
        BreakpointEvent bpe = startTo(targetClassName + "$sam", "stopHere", "()V");
        targetClass = bpe.location().declaringType();

        FXReferenceType topClass = (FXReferenceType)vm().classesByName(targetClassName).get(0);
        writeActual("Field values for class = " + topClass);
        writeActual("  value of staticVar = " + topClass.FXGetValue(topClass.fieldByName("staticVar")));
        // note that staticBinder is invalid
        writeActual("  value of staticBinder = " + topClass.getValue(topClass.fieldByName("staticBinder")));

        writeActual("  ReadOnly, Bound, Invalid flags for all fields");
        List<Field>allFields = topClass.allFields();
        for (Field fld: allFields) {
            writeActual("    field = " + fld + ", " + 
                    topClass.isReadOnly(fld) + ", " +
                    topClass.isBound(fld) + ", " +
                    topClass.isInvalid(fld));

        }

        writeActual("  Values of all fields with VoidValue for fields with getters");
        Map<Field, Value>allValues = topClass.getValues(allFields);
        for (Field fld: allFields) {
            writeActual("   field = " + fld + ", value = " + allValues.get(fld));
        }

        // Note that this forces staticBinder to be evaluated and the new value is fetched.
        writeActual("  Values of all fields with forced values for fields with getters");
        allValues = topClass.FXGetValues(allFields);
        for (Field fld: allFields) {
            writeActual("   field = " + fld + ", value = " + allValues.get(fld));
        }

        {
            // check flags after staticBinder has become valid
            writeActual("  ReadOnly, Bound, Invalid flags for staticBinder when it is valid");
            Field fld = topClass.fieldByName("staticBinder");
            writeActual("    field = " + fld + ", " + 
                        topClass.isReadOnly(fld) + ", " +
                        topClass.isBound(fld) + ", " +
                        topClass.isInvalid(fld));
        }

        // Object ivars
        FXObjectReference samObjRef = (FXObjectReference)topClass.FXGetValue(topClass.fieldByName("samObj"));
        ReferenceType samClass = (ReferenceType)samObjRef.type();
        writeActual("\nField values for object = " + samObjRef);
        writeActual("  value of ivar0 = "          + samObjRef.FXGetValue(samClass.fieldByName("ivar0")));
        writeActual("  value of ivar1 = "          + samObjRef.FXGetValue(samClass.fieldByName("ivar1")));
        writeActual("  value of ivarBinder = "     + samObjRef.getValue(samClass.fieldByName("ivarBinder")));

        writeActual(" Readonly, Bound, and Invalid flags for all fields");
        allFields = samClass.allFields();
        for (Field fld: allFields) {
            writeActual("    field = " + fld + ", " + 
                    samObjRef.isReadOnly(fld) + ", " +
                    samObjRef.isBound(fld) + ", " +
                    samObjRef.isInvalid(fld));
        }

        writeActual("  Values of all fields with VoidValue for fields with getters:");
        allValues = samObjRef.getValues(allFields);
        for (Field fld: allFields) {
            writeActual("   field = " + fld + ", value = " + allValues.get(fld));
        }

        writeActual("  Values of all fields with getters called:");
        allValues = samObjRef.FXGetValues(allFields);
        for (Field fld: allFields) {
            writeActual("   field = " + fld + ", value = " + allValues.get(fld));
        }

        {
            // check flags after ivarBinder has become valid
            writeActual("  ReadOnly, Bound, Invalid flags for ivarBinder when it is valid");
            Field fld = samClass.fieldByName("ivarBinder");
            writeActual("    field = " + fld + ", " + 
                        samObjRef.isReadOnly(fld) + ", " +
                        samObjRef.isBound(fld) + ", " +
                        samObjRef.isInvalid(fld));
        }
        
        testFailed = !didTestPass();
        
        /*
         * resume until end
         */
        listenUntilVMDisconnect();
        
        /*
         * deal with results of test
         * if anything has called failure("foo") testFailed will be true
         */
        if (!testFailed) {
            writeActual(testClassName + ": passed");
        } else {
            throw new Exception(testClassName + ": failed");
        }
    }
}
