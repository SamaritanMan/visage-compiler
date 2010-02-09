/*
 * Regression test 
 * JFXC-4085 : Compiled bind optimization: optimize singleton explicit sequences
 *
 * @test
 * @run
 */

class A { var id:String; override function toString() { id } }
var x : A = A{id:"Yes"};
def eb = bind [x] on replace oldValue[a..b] = newValue { 
	println("---- on-replace [{a}..{b}] = {newValue.toString()}");
	println(oldValue);
	println(eb);
}
x = A{id:"No"};
x = null;
x = A{id:"Maybe"};
