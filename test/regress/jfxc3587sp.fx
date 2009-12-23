/**
 * JFXC-3587 : Compiled bind: bound sequence: reverse
 *
 * script-private script-level, no on-replace
 *
 * @test
 * @run
 */

function show() {
  println(seq);
  println(brrs);
  println("-");
  println(brs);
  println(reverse seq);
  println("--------");
}

var seq = ["a", "b", "c", "d", "e", "f", "g"];
def brs = bind reverse seq;
def brrs = bind reverse brs;
show();
seq[1..3] = "x";
show();
insert "z" into seq;
show();
seq[0] = "@";
show();
delete seq[1];
show();
