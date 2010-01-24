/*
 * Regression test 
 * JFXC-4011 : All invalidation must maintain matched invalidation-phase / trigger-phase call pairs.
 *
 * bound if sequence -- assure no false indirect on-replace
 *
 * @test
 * @run
 */

var i = 3;
var max = 10;
var big = ["a", "b", "c", "d"];
var little = ["x", "y"];
def bif = bind if (i > max) big else little;
def bel = bind bif[1] on replace { println("onr: '{bel}'") };

println("no change");
i = 4;
max = 20;
println("now");
i = 30; 
println("no change");
max = 10;
i = 20;
println("back");
i = 1; 
println("zap");
little = [];
