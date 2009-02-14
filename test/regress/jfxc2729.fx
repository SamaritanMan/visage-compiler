/**
 * JFXC-2729 : Implement sequence type overhaul
 *
 * @test
 * @run
 */

function f(x:Object) {}
function f2(x:Object, y:Object) {}

var x:Object = [1,2,3];
println(x);

f([1,2,3]);
f2([1,2,3], "Hello!");

var z:Object[] = [1,["a","b","c"]];
println(z);

x = if (true) 1 else ["a", "b", "c"];
println(x);

class A {
var x:Object;
function m():Object {return [];}
}

class B extends A {
override function m():Object[] {return [];}
}

var a = A{x:[1,2,3]};
println(a.m());

var b = B{x:[1,2,3]};
println(b.m());