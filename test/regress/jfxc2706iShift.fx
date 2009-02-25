/**
 * JFXC-2706 : AbstractBoundComprehension: NPE during execution
 *
 * Integer shifting ranges 
 *
 * @test
 * @run
 */

var x = 1;
var z = 10;
 
var range = bind for (i in [x..(x+30) step z]) i;
println(range);
// same size, un-step-synced
x = 2;
println(range);
// same size, step-synced
x = 12;
println(range);
