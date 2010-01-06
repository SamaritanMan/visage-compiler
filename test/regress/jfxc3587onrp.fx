/**
 * JFXC-3587 : Compiled bind: bound sequence: reverse
 *
 * public instance, on-replace test
 *
 * @test
 * @run
 */

public class jfxc3587onrp { 
  var seqa : Integer;
  var seqb : Integer;
  var seql : Integer;
  var brrsa : Integer;
  var brrsb : Integer;
  var brrsl : Integer;
  public var seq = ["a", "b", "c", "d", "e", "f", "g"] on replace oldV[a..b] = newV { seqa = a; seqb = b; seql = sizeof newV };
  public def brs = bind reverse seq on replace oldV[a..b] = newV { println("[{a}..{b}] = {sizeof newV}") };
  public def brrs = bind reverse brs on replace oldV[a..b] = newV { brrsa = a; brrsb = b; brrsl = sizeof newV };

  function check() {
    if (seqa != brrsa or seqb != brrsb or seql != brrsl) {
      println("[{brrsa}..{brrsb}] = {brrsl} -- got");
      println("[{seqa}..{seqb}] = {seql} -- expected");
      println(seq);
      println(brs);
      println("--------");
    }
  }

  function test() {
    check();
    seq[1..3] = "x";
    check();
    insert "z" into seq;
    check();
    seq[0] = "@";
    check();
    delete seq[1];
    check();
  }
}

function run() {
  jfxc3587onrp{}.test()
}
