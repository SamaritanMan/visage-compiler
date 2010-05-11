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

class FilterVarsTarget {
    var baseivar1: Number;    // this is declared private because there are no refs
                              // to it in this script, outside this class
    var baseivar2: Number;    // These three are refd in the subclass below which
    var baseivar3: Number;    // causes them to be declared as public, with name 
    var ivarOverride: Number; // prefix $filterVarsTarget
}


mixin class Mixin1 {
    var mixivar1: Number;
}

class FilterVarsTargetSub extends Mixin1, FilterVarsTarget {
    var subivar1: Number;  
    override var ivarOverride: Number;
    function stopHere() {
        baseivar2;
        javafx.lang.Builtins.println("Hi There");
    }
    function getInt() {
        return 22;
    }
}

var obj = FilterVarsTargetSub{};    // this is static in FilterVarsTarget
obj.baseivar2;
//obj.baseivar3;    // fails - see jfxc-4355

obj.stopHere();
var bb = 89;

// this creates var $$ivar1$ol$0
var binder1 = FilterVarsTarget{baseivar1: bind bb};

// this creates $binder2$_$2
var binder2 = bind if (bb == 89) then obj.baseivar1 else obj.getInt();

var binder3 = for (ii in [bb .. bb + 10]) {
                ii*2
              };
