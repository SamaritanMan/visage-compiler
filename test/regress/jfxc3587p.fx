/**
 * JFXC-3587 : Compiled bind: bound sequence: reverse
 *
 * public instance, no on-replace
 *
 * @test
 * @run
 */

public class jfxc3587p { 
  public var seq = ["a", "b", "c", "d", "e", "f", "g"];
  public def brs = bind reverse seq;
  public def brrs = bind reverse brs;

  function show() {
    println(seq);
    println(brrs);
    println("-");
    println(brs);
    println(reverse seq);
    println("--------");
  }

  function test() {
    show();
    seq[1..3] = "x";
    show();
    insert "z" into seq;
    show();
    seq[0] = "@";
    show();
    delete seq[1];
    show();
  }
}

function run() {
  jfxc3587p{}.test()
}


