/**
 * Regression test: JFXC-3584 : Compiled bind: bound sequence: explicit
 *
 * Bound explicit sequences of non-nullable and sequences
 *
 * @test
 * @run
 */

class jfxc3584seq {
  var bv = 1111;
  var sv = [1..8];
  var ev = 2222;
  var srb1 = -9;
  var srb2 = -4;
  var eb = 9999;
  def qqq = bind [bv, sv, ev, [srb1..srb2], eb];

  function doit() {
    println(qqq);
    eb = 99999;
    println(qqq);
    srb2 = -1;
    println(qqq);
    srb1 = -5;
    println(qqq);
    srb1 = -8;
    println(qqq);
    ev = 22222;
    println(qqq);
    sv = [];
    println(qqq);
    sv = [44, 55];
    println(qqq);
    bv = 11111;
    println(qqq);
  }
}

jfxc3584seq{}.doit()
