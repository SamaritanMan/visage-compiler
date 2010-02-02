/*
 * Regression test JFXC-4040 : bound explicit sequence: invalidation of elments is piecemeal -- and thus yields inconsistent states
 *
 * non-nullable
 *
 * @test
 * @run
 */

var x = "Yes";
def eb = bind [x, x, x, x, x] on replace oldValue[a..b] = newValue { 
	println("---- on-replace [{a}..{b}] = {newValue.toString()}");
	println(oldValue);
	println(eb);
}
x = "No";
x = "Maybe";
