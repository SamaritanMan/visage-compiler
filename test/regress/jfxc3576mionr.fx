/**
 * Regression test: JFXC-3576 : Compiled bind: bound sequence translation infrastructure and bound range
 *
 * Integer ranges with negative step WITH on-replace
 *
 * @test
 * @run
 */

var x = 1;
var y = 30;
var z = 10;
 
def r = bind [-x..-y step -z] on replace [a..b] = nv { println("[{a}..{b}] = {sizeof nv}") };
// grow, un-step-synced
x = 0;
// grow, step-synced
x = -10;
// shrink, un-step-synced
x = 1;
// shrink, step-synced
x = 11;
// same size, un-step-synced
x = 12;
