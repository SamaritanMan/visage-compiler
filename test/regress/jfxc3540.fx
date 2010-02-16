/**
 * Regression test: JFXC-3540 : Compiled bind: Translation not handling bind super.x correctly.
 *
 * @test
 * @run
 */

class A { var x=10; }

class B extends A {
  var y = bind super.x;
}

var b = B{};
println("b.y={b.y}");
