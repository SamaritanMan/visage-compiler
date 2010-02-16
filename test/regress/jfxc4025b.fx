/*
 * Regression test 
 * JFXC-4025 : Duplicate and incorrect invalidations on bound if sequence
 *
 * @test
 * @run
 */

var n = 1;
def bs = bind if(n>2) [0..n] else [0..n*2] on replace oldVal[a..b] = newVal { 
	println("[{a}..{b}]");  
	println("old: {oldVal.toString()}");  
	println("new: {newVal.toString()}")
 };
println("=10");
n = 10;
println("=1");
n = 1;
println("=2");
n = 2;
println("=3");
n = 3;
println("=4");
n = 4;
