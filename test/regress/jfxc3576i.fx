/**
 * Regressioin test: JFXC-3576 : Compiled bind: bound sequence translation infrastructure and bound range
 *
 * Integer ranges at script-leve -- no step, no on-replace
 *
 * @test
 * @run
 */

var rb = 1;
var re = 10;
def range = bind [rb .. re];

println(range);
re = 20;
println(range);
rb = 5;
println(range);
re = 10;
println(range);
re = -10;
println(range);
rb = -12;
println(range);
rb = -8;
println(range);
