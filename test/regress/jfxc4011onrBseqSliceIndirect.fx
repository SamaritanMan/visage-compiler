/*
 * Regression test 
 * JFXC-4011 : All invalidation must maintain matched invalidation-phase / trigger-phase call pairs.
 *
 * bound slice -- assure no false indirect on-replace
 *
 * @test
 * @run
 */

var low = 1;
var high = 5;
def q1 = [0..8];
def q2 = [0..100 step 10];
var seq = q1;
def bif = bind seq[low..high];
def bel = bind bif[1] on replace { println(bel) };

println("no change");
low = 1;
high = 5;
seq = q1;
println("now");
low = 0;
high = 6;
seq = q2;
println("no change");
low = 0;
high = 6;
seq = q2;
println("done");
