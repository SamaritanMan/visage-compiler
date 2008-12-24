/**
 * should-fail test JFXC-2416 : Parser and tree generation: mixin keyword for classes
 *
 * @test/compile-error
 */

mixin class Sized {
  var width : Number;
  var height : Number;
  public function area() : Number { width * height }
}

var szd = Sized{}

mixin var x = 9;
mixin function foo() { "lop" }

