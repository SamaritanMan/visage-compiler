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

package com.sun.javafx.jdi.test.target;

// Note that these script vars are in the 'script' inner class
public def defStaticVar = 22.3;
public var staticVar: Number;
public var staticBinder = bind staticVar;


// Nested class
public class sam {
    public def pubivarDef = 33.0;
    public var ivar0: Number = 89.4;
    public var ivar1: Number;          // this is NOT named sam$ivar1 because it is public
    public var ivarBinder = bind ivar1;
    public function stopHere(){
    }
};

public var samObj = sam{};

function run() {
    staticVar = 89.0;
    // staticBinder is now invalid

    samObj.ivar1 = 22.6;
    // samObj.samBinder is now invalid
    samObj.stopHere();
}
