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
 
package javafx.ui.canvas; 


import com.sun.scenario.scenegraph.SGShape;

/**
 * The <code>Circle</code> class describes a circle that is defined
 * by a center point (cx, cy), and a radius (radius).
 */
public class Circle extends Shape {
    // TODO MARK AS FINAL
    private attribute awtEllipse: java.awt.geom.Ellipse2D.Double;

    /** The x coordinate of the center point of this circle. */
    public attribute cx: Number on replace {
        var x = cx - radius;
        var y = cy - radius;
        var width = 2*radius;
        var height = 2*radius;
        awtEllipse.setFrame(x, y, width, height);
        sgshape.setShape(awtEllipse);
    };
    /** The y coordinate of the center point of this circle. */
    public attribute cy: Number on replace {
        var x = cx - radius;
        var y = cy - radius;
        var width = 2*radius;
        var height = 2*radius;
        awtEllipse.setFrame(x, y, width, height);
        sgshape.setShape(awtEllipse);
    };
    /** The radius of this circle. */
    public attribute radius: Number on replace {
        var x = cx - radius;
        var y = cy - radius;
        var width = 2*radius;
        var height = 2*radius;
        awtEllipse.setFrame(x, y, width, height);
        sgshape.setShape(awtEllipse);
    };

    public function createShape(): SGShape  {
        var x = cx - radius;
        var y = cy - radius;
        var width = 2*radius;
        var height = 2*radius;
        awtEllipse = new java.awt.geom.Ellipse2D.Double(x, y, width, height);
        var sgshape = new SGShape();
        sgshape.setShape(awtEllipse);
        return sgshape;
    }
}

    