/**
 * Regression test JFXC-2541 : Number->Float: bound range sequences crash for new numerics
 *
 * @test
 * @run
 */

function run() {
  var a : Double = 1.0;
  var b : Double = 5.0;
  var c : Double = 2.5;
  var seq1 : Double[] = bind [a..b];
  println(seq1);
  var seq2 : Float[] = bind [a..b];
  println(seq2);
  var seq3 : Long[] = bind [a..b];
  println(seq3);
  var seq4 : Byte[] = bind [a..b step c];
  println(seq4);
  var seq5 : Double[] = bind [a..b step c];
  println(seq5);
}
