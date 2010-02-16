/*
 * Regression test for JFXC-4041
 * Compiled bind optimization: use element type, not sequence type, where possible for explicit sequence parts
 *
 * @test
 * @run
 */

class A {
  override function toString() { "A" }
}

var cond = false;
var a = A{}
def bseq1 = bind [
  if (cond) a else null,
  if (cond) a else null,
  if (cond) a else null
];
println(bseq1);
cond = true;
println(bseq1);
cond = false;
def bseq2 = bind [
  if (cond) "Hello!" else null,
  if (cond) "Hello!" else null,
  if (cond) "Hello!" else null
];
println(bseq2);
cond = true;
println(bseq2);
cond = false;
def bseq3:A[] = bind [
  if (cond) a else null,
  if (cond) a else null,
  if (cond) a else null
];
println(bseq3);
cond = true;
println(bseq3);
cond = false;
def bseq4:String[] = bind [
  if (cond) "Hello!" else null,
  if (cond) "Hello!" else null,
  if (cond) "Hello!" else null
];
println(bseq4);
cond = true;
println(bseq4);
