/**
 * JFXC-3585 : Compiled bind: bound sequence: slice
 *
 * Basic functionality, no on-replace
 *
 * @test
 * @run
 */

var seq = ['a', 'b', 'c', 'd', 'e', 'f', 'g'];
var a = 2;
var b = 4;
def bsi = bind seq[a..b];
def bse = bind seq[a..<b];
println(bsi);
println(bse);
b = 6;
println(bsi);
println(bse);
b = 5;
println(bsi);
println(bse);
a = 4; 
println(bsi);
println(bse);
a = 0;
println(bsi);
println(bse);
a = -4;
println(bsi);
println(bse);
b = -3;
println(bsi);
println(bse);
b = 20;
println(bsi);
println(bse);
