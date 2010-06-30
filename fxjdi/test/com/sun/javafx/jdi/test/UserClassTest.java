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
import com.sun.jdi.ThreadReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ClassType;
import com.sun.javafx.jdi.FXReferenceType;
import com.sun.jdi.event.BreakpointEvent;
import java.util.List;
import org.junit.Test;
import junit.framework.Assert;

public class UserClassTest extends JavafxTestBase {
    ReferenceType targetClass;
    ThreadReference mainThread;

    private static final String targetClassName = "com.sun.javafx.jdi.test.target.UserClassTarget";

    public UserClassTest() {
        super(targetClassName);
    }

    @Test
    public void testUserClass() {
        try {
            startTests();
        } catch (Exception exp) {
            exp.printStackTrace();
            Assert.fail(exp.getMessage());
        }
    }

    /********** test core **********/

    void printUser(StackFrame frame, int count) {
        ReferenceType rt = frame.location().declaringType();
        ClassType userClass = ((FXReferenceType)rt).javaFXUserClass();
        writeActual("userClass " + count + " = " + rt.name() + ", " + 
                    (userClass == null? null: userClass.name()));
    }

    protected void runTests() throws Exception {
        startToMain();
        BreakpointEvent bpe = resumeTo("javafx.lang.Builtins", "println", "(Ljava/lang/Object;)V");
        mainThread = bpe.thread();

        targetClass = bpe.location().declaringType();
        // We are stopped under the on replace for sv2
            
        List<StackFrame> frames =  bpe.thread().frames(0,10);
        for (StackFrame frame: frames) {
            printUser(frame, 1);
        }

        bpe = resumeTo("javafx.lang.Builtins", "println", "(Ljava/lang/Object;)V");
        // stopped in the on replace for override1
        printUser(bpe.thread().frame(2), 2);
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
