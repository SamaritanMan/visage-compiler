/**
 * Regression test: JFXC-3578 : Compiled bind: translation of non-transformative bound sequences: identifier and member select
 *
 * Bound member select sequence of String without on-replace
 *
 * @test
 * @run
 */

class jfxc3578selString {
  var q = ["nope"];
}

var sel = jfxc3578selString {q: "init"};
def bq = bind sel.q;
println(bq);
insert "first" into sel.q;
println(bq);
delete "init" from sel.q;
println(bq);
sel = jfxc3578selString {q: "second"};
println(bq);
sel = jfxc3578selString {q: "third"};
insert "fourth" into sel.q;
println(bq);
