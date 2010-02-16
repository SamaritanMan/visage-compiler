/**
 * Regression test JFXC-3627 : Compiled bind: bound sequence: function invocation
 *
 * @test
 * @run
 */

  function fluff(str : String) : String[] {
    [ "front", str, "back" ]
  }
  var blip = "middle";
  def bx = bind fluff(blip);
  println(bx);
  blip = "slide";
  println(bx);
