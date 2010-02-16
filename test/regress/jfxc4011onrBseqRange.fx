/*
 * Regression test 
 * JFXC-4011 : All invalidation must maintain matched invalidation-phase / trigger-phase call pairs.
 *
 * bound range, on-replace -- assure no false on-replace
 *
 * @test
 * @run
 */

var low = 1;
var high = 5;
var hop = 1;
def bif = bind [low..high step hop] on replace [a..b] = newVal {
             println("[{a}..{b}] = {sizeof newVal}")
}

println("no change");
low = 1;
high = 5;
hop = 1;
println("now");
low = 0;
high = 6;
hop = 2;
println("no change");
low = 0;
high = 6;
hop = 2;
println("done");
