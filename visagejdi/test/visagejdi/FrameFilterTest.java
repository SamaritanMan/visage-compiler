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

package visagejdi;


import org.visage.jdi.VisageStackFrame;
import org.visage.jdi.VisageVirtualMachine;
import org.visage.jdi.VisageWrapper;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.StackFrame;
import com.sun.jdi.event.BreakpointEvent;
import org.junit.Test;
import junit.framework.Assert;

/**
 *
 * @author sundar
 */
public class FrameFilterTest extends JdbBase {

// @BeginTest FrameFilter.visage
// public var xx;
//
// function run() {
//     xx = 3;
// }
// @EndTest

    @Test(timeout=5000)
    public void testFilterFrames() {
        return;
        /*
         // FIXME: Test disabled -- till we decide on synthetic/internal methods.

        try {
            compile("FrameFilter.visage");
            stop("in FrameFilter.onReplace$xx");

            visagerun();

            BreakpointEvent bkpt = waitForBreakpointEvent();
            VisageStackFrame frame = (VisageStackFrame) bkpt.thread().frame(0);

            // onReplace$xx is internal visage method and so should not show up.
            Assert.assertEquals("visage$run$", frame.location().method().name());

            // onReplace$xx is internal visage method and so should show up in underlying
            // (java JDI) frames.
            StackFrame jframe = VisageWrapper.unwrap(bkpt.thread()).frame(0);
            Assert.assertEquals("onReplace$xx", jframe.location().method().name());
            
            cont();
            quit();
        } catch (Exception exp) {
            exp.printStackTrace();
            Assert.fail(exp.getMessage());
        }*/
    }
}
