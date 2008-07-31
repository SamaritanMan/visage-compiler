/*
 * Copyright 2008 Sun Microsystems, Inc.  All Rights Reserved.
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

package javafx.scene.paint;

// PENDING_DOC_REVIEW
/**
 * This enum defines one of the following methods to use when painting 
 * outside the gradient bounds: {@code  CycleMethod.NO_CYCLE}, 
 * {@code CycleMethod.REFLECT}, or {@code  CycleMethod.REPEAT}.
 *
 * @profile common conditional multigradient
 */      
public enum CycleMethod {

    // PENDING_DOC_REVIEW
    /**
     * Defines the cycle method that uses the terminal colors to fill the remaining area. 
     *
     * @profile common conditional multigradient
     */      
    NO_CYCLE(0), // MultipleGradientPaint.CycleMethod.NO_CYCLE

    // PENDING_DOC_REVIEW
    /**
     * Defines the cycle method that reflects the gradient colors start-to-end, 
     * end-to-start to fill the remaining area.
     *
     * @profile common conditional multigradient
     */      
    REFLECT(1), // MultipleGradientPaint.CycleMethod.REFLECT
    
    // PENDING_DOC_REVIEW
    /**
     * Defines the cycle method that repeats the gradient colors to fill the remaining area.
     *
     * @profile common conditional multigradient
     */      
    REPEAT(2); //MultipleGradientPaint.CycleMethod.REPEAT

    final int toolkitValue;
    
    private CycleMethod(int toolkitValue) {
        this.toolkitValue = toolkitValue;
    }

}