/*
 * Regression test: jfxc149
 *
 * @test
 * @run
 */

import java.lang.System;
class A {
attribute f: function(:String,:String):String = null;
}

var a = A {
f: function(a:String, b: String) {a.concat(b)}
}

function cat(a:String, b:String) {a.concat(b);}

var a1 = A {
f: cat
}

var f = cat;

System.out.println(a.f("Hello ", "World!"));
System.out.println(a1.f("Hello ", "World!"));
System.out.println(f("Hello ", "World!")); 
