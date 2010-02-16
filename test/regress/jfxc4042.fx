/*
 * Regression test for JFXC-4042
 * Compiled bind optimization: use element type, not sequence type, where possible for for-expression bodies
 *
 * @test
 * @run
 */

class A {
  override function toString() { "A" }
}

var cond = false;
var a = A{}
def bseq1 = bind for (i in [1..3]) if (cond) a else null;
println(bseq1);
cond = true;
println(bseq1);
cond = false;
def bseq2 = bind for (i in [1..3]) if (cond) "Hello!" else null;
println(bseq2);
cond = true;
println(bseq2);
cond = false;
def bseq3:A[] = bind for (i in [1..3]) if (cond) a else null;
println(bseq3);
cond = true;
println(bseq3);
cond = false;
def bseq4:String[] = bind for (i in [1..3]) if (cond) "Hello!" else null;
println(bseq4);
cond = true;
println(bseq4);
