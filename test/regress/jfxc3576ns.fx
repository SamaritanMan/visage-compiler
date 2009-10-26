/**
 * Regressioin test: JFXC-3576 : Compiled bind: bound sequence translation infrastructure and bound range
 *
 * Number ranges with step -- no on-replace
 *
 * @test
 * @run
 */

var rb = 5.5;
var re = 7.0;
var stp = 0.25;
def range = bind [rb .. re step stp];
println(range);
rb = 3.25;
println(range);
rb = 6.1;
println(range);
rb = 8.5;
println(range);
re = 12.0;
println(range);
stp = 0.5;
println(range);
