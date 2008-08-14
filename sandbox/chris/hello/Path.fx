
/*
 * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
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

package hello.canvas;

import java.awt.geom.GeneralPath;
import com.sun.scenario.scenegraph.SGShape;

/**
 * The <code>Path</code> class represents a geometric path 
 * constructed from straight lines, and quadratic and cubic
 * (B&eacute;zier) curves.
 */
class Path extends Shape {
    attribute UNSET: Integer = java.lang.Integer.MIN_VALUE;
    attribute locked: Boolean on replace  {
        if (not locked) {
            buildPath();
        }
    };
    //TODO 
    public attribute lastMoveToX: Number = UNSET;
    public attribute lastMoveToY: Number = UNSET;
    public attribute xCenter: Number = UNSET;
    public attribute yCenter: Number = UNSET;
    protected function buildPath() {
        if (sgshape != null and not locked) {
            var gp = new GeneralPath(fillRule.id.intValue(), sizeof d);
            for (i in d) {
                i.addTo(gp);
            }
            sgshape.setShape(gp);
        }
    }

    /**
     * Path Data<br></br>
     * A list of commands and/or shapes which define this path.
     */
    public attribute d: AbstractPathElement[]
        on insert [ndx] (p) {
            p.path = this;
            this.buildPath();
        }
        on delete [ndx] (oldValue) {
            this.buildPath();
        }
        on replace [ndx] (oldValue) {
            d[ndx].path = this;
            this.buildPath();
        };

    public function toPath(): Path {
        return this;
    }

    protected function createShape(): SGShape {
        sgshape = new SGShape();
        this.buildPath();
        return sgshape;
    }

    attribute fillRule = FillRule.NON_ZERO;
}





