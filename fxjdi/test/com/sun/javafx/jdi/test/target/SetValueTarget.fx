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

class sub {
     public var pubString: String;
     var privString: String;

     public var pubInteger: Integer;
     var privInteger: Integer;

     public var pubNumber: Number;
     var privNumber: Number;

     public var pubBoolean: Boolean;
     var privBoolean: Boolean;

     public var pubDuration: Duration;
     var privDuration: Duration;
     function stopHere() {
     }
}


var initVals = sub{
    pubString:  "pubString1",
    privString: "privString1",
    pubInteger: 1,
    privInteger: 1,
    pubNumber: 1.0,
    privNumber: 1.0,
    pubBoolean: false,
    privBoolean: false,
    pubDuration: 1s
    privDuration: 1s
}

var secondVals = sub{
    pubString:  "pubString2",
    privString: "privString2",
    pubInteger: 2,
    privInteger: 2,
    pubNumber: 2.0,
    privNumber: 2.0,
    pubBoolean: true,
    privBoolean: true,
    pubDuration: 2s
    privDuration: 2s
}

var statString = "statString1";
def defStatString = "defStatString1";


var staticBindee;
var staticBinder = bind staticBindee;

function run() {
   staticBindee = 1;
   initVals.stopHere();
}

