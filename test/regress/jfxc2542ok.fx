/**
 * Regression test: JFXC-2542 : Proper literal conversions
 *
 * @test
 * @run
 */

def a : Float = 0.0;
def b : Float = 4.3;

def k : Byte = 127;
def l : Short = 789;

println(a);
println(b);
println(k);
println(l);
