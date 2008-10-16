/**
 * Regression test JFXC-2204 : On assignment, object literal init, or initial value, convert literal null to default value
 *
 * @test
 * @run
 */

class A {
  var s : Duration;
}

function run() {
  var a : Duration = null;
  println("init: {a}");
  var b = 77ms;
  b = null;
  println("assign: {b}");
  var f : Duration;
  println("default: {f}");

  var g = new A;
  println("instance default: {g.s}");
  g.s = null;
  println("assign instance var: {g.s}");
  g = A {s: null}
  println("object literal: {g.s}");
}