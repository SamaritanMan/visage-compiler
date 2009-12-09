/**
 * JFXC-3370 : Clean up bound..or and bound..and implementation for memory leaks
 *
 * @test
 * @run
 */

// With JFXC-3370, bound..or, bound..and implementation changed. We want to make
// sure things are fine by these sanity checks.

var x : Boolean = true;
var i : Integer = 100;


// on replace makes these binds be eager
var y = bind x or (i < 10) on replace oldy {
   println("y changed from {oldy} to {y}");
};

var z = bind x and (i < 10) on replace oldz {
   println("z changed from {oldz} to {z}");
};

println("changing x to false");
x = false;


println("changing i to 2");
i = 2;


// try bind..or and bind..and as a non-top-level expression
var a = true;
var b = bind not (a or (i < 10));
println("b is {b}");
var c = bind not (a and (i < 10));
println("c is {c}");
