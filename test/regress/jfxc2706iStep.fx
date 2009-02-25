/**
 * JFXC-2706 : AbstractBoundComprehension: NPE during execution
 *
 * Integer ranges - step goes to one
 *
 * @test
 * @run
 */

def lower = [1, 2, 29];
def stepsize = [10, 1, 1];
var idx = 0;

var range = bind for (i in [lower[idx]..30 step stepsize[idx]]) i;
println(range);
// step size changes to sync appearing while lower change un-step-synced
idx = 1;
println(range);
idx = 0;
println(range);
idx = 2;
println(range);
