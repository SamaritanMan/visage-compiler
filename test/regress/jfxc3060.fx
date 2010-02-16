/**
 * JFXC-3060 - remove intermediate Locations from bind translation -- binary / unary operators.
 *
 * @test
 * @run
 */

// JFXC-3060 is an optimization issue. We want to make sure
// whatever is affected by this optimization still works correctly.

var x = bind 33;
println(x);

var a = 3;
var y = bind a + 4;
println(y);
a = 5;
println(y);

var z = bind a + 6 + x;
println(z);
a = 8;
println(z);

var i = bind  3*x;
println(i);
var j = bind x/3;
println(j);
