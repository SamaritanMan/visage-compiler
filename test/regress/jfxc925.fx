/*
 * @test
 * @run
 */

import java.lang.System;

var enableBindingOverhaul;

let fs = for (k in [1..5]) if (k < 3) [k, k] else [];
System.out.println("fx: {for (f in fs) " {f}"}.");

let fsb = bind for (k in [1..5]) if (k < 3) [k, k] else []; 

class Foo {};
var v : Foo = bind null;
var w : Foo = bind if (v != null) new Foo else null;
var x : Foo = bind if (v == null) null else new Foo;

var w0 = bind if (v != null) new Foo else null;
var x0 = bind if (v == null) null else new Foo; 
