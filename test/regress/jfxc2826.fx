/*
 * Regression: JFXC-2826 - internal error in the OpenJFX compiler ( 1.1.0 ) when using on replace with sequence.
 *
 * @test
 * @run
 *
 */

var t =[ 1,2,"RED"] on replace test10 [a..b]=newElements {
    println("change made from {a} to {b}");
    println("Actual:{t}");
};
