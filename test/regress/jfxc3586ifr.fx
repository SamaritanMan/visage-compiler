/**
 * Regression test JFXC-3586 : Compiled bind: bound sequence: for-expression
 *
 * If body over a range.
 *
 * @test
 * @run
 */

var mmm = 3;
def bf = bind for (iii in [1..12]) if (iii mod mmm == 0) [iii..(iii+1)] else [];
println(bf);
mmm = 4;
println(bf);
mmm = 3;
println(bf);
mmm = 1;
println(bf);
