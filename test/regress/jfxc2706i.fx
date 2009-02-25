/**
 * JFXC-2706 : AbstractBoundComprehension: NPE during execution
 *
 * Integer ranges
 *
 * @test
 * @run
 */

var x = 1;
var y = 30;
var z = 10;
 
var range = bind for (i in [x..y step z]) i;
println(range);
// grow, un-step-synced
x = 0;
println(range);
// grow, step-synced
x = -10;
println(range);
// shrink, un-step-synced
x = 1;
println(range);
// shrink, step-synced
x = 11;
println(range);
// same size, un-step-synced
x = 12;
println(range);
