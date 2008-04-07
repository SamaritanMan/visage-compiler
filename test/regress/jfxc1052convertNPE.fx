/**
 * Regression test JFXC-1052 : NPE in bound type conversion to Object
 *
 * @test
 * @run
 */

import java.lang.System;
import java.lang.Object;

bound function foo(x : Object) : Object { x }

var y : Integer = 8;
var z : Object = bind foo(y);
System.out.println(z);

var s : String = "hiho";
var sz : Object = bind foo(s);
System.out.println(sz);

var b : Boolean = true;
var bz : Object = bind foo(b);
System.out.println(bz);

var d : Number = 3.14159;
var dz : Object = bind foo(d);
System.out.println(dz);

