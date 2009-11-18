/**
 * Regression test: JFXC-3670 : Compiled bind: bind permeates object literal initializers
 *
 * Non-final local referenced from function value
 *
 * @test
 * @run
 */

class jfxc3636a {
  var v : function() : String
}

function run() {
  var val = "OK";
  val = "joke";
  var z = jfxc3636a { v: function() { val } }
  println(z.v());
}
