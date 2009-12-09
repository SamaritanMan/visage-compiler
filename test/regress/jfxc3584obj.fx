/**
 * Regression test: JFXC-3584 : Compiled bind: bound sequence: explicit
 *
 * Bound explicit sequences of nullable object (no sequences) with on-replace
 *
 * @test
 * @run
 */

var x : Object = "hello";
var y : Object = "stay";
var z : Object = "bye";
def q = bind [x, y, z] on replace [beg..end] = newVal { println("replace {beg} .. {end} -- {sizeof newVal}") };
println(q);
y = "linger";
println(q);
x = null;
println(q);
y = null;
println(q);
x = "happy";
println(q);


