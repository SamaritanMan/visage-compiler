/*
 * Regression test 
 * JFXC-4025 : Duplicate and incorrect invalidations on bound if sequence
 *
 * @test
 * @run
 */

var n = 1;
def bs = bind if(n>2) [0..n] else [0..n*2] on replace [a..b] = newVal { println("[{a}..{b}] = {sizeof newVal}") };
n = 10;
n = 1;
