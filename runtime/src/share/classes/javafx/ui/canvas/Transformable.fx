/*
 * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
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
import java.awt.geom.AffineTransform;
/** 
 * Interface for graphic objects that can be transformed (translated,
 * scaled, skewed, or rotated).
 */
public abstract class Transformable {
    /** A list of transformation functions that will be performed on this object. */
    public attribute transform: Transform[]
        on insert  [indx] (newValue) {
            newValue.transformable = this;
            updateTransform();   
        }
        on delete  [indx] (oldValue)  {
            if (oldValue.transformable == this) {
                oldValue.transformable = null;
            }
            updateTransform();  
        }
        on replace [indx] (oldValue) {
            if (oldValue.transformable == this) {
                oldValue.transformable = null;
            }
            transform[indx].transformable = this;
            updateTransform();   
        };
    
    protected attribute affineTransform: AffineTransform;
    
    protected function updateTransform() {
        var t = new AffineTransform();
        foreach (i in transform) {
            if (i.transform <> null) {
                t.concatenate(i.transform);
            }
        }
        if (affineTransform <> t) {
            affineTransform = t;
            //TODO this makes no sense, as you just assgined affineTransform to t
            // but this was the way it was in the interpreter.
            if (affineTransform == t) {
                if(onTransformChanged <> null) {
                    onTransformChanged(affineTransform);
                }
            }
        }
    }
    protected attribute  onTransformChanged: function(:AffineTransform):Void;
}


