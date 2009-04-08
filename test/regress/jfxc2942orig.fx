/**
 * Regression test JFXC-2942 : Lazy binding: range -- throws divide by zero exception in trigger
 *
 * Original
 *
 * @test
 * @run
 */

function n(x : Integer) : Integer  { println(">>> {x}"); x }

var a = 15;
var b = 20;

def rc = bind lazy [n(a) .. n(b)];
println("----");
println(rc);

