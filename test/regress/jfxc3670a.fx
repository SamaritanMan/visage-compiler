/**
 * Regression test: JFXC-3670 : Compiled bind: bind permeates object literal initializers
 *
 * @test
 * @run
 */

class Baffle { 
  var moe : Integer;
  def id = ++bafCnt;
  init { println("Create {this}") }
  override function toString() : String { "Baffle#{id}:{moe}" }
}
var bafCnt = 0;
var x = 2;
function foo(k : Integer) { println("foo({k})"); 99 }
def z = bind Baffle { moe: foo(x) }
println(z);
++x;
println(z);
