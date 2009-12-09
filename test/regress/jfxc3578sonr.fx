/**
 * Regression test: JFXC-3578 : Compiled bind: translation of non-transformative bound sequences: identifier and member select
 *
 *  Bound member select sequence of Object WITH on-replace -- script-level
 *
 * @test
 * @run
 */

class jfxc3578sonr {
  var q : Object[] = ["nope"]
}

var sel = jfxc3578sonr {q: [0..5] };
def bq = bind sel.q on replace [beg..end] = newVal { println("  Replace [{beg}..{end}] = {sizeof newVal}") };
insert "first" into sel.q;
delete 3 from sel.q;
sel = jfxc3578sonr {q: "second"};
sel = jfxc3578sonr {q: [10..100 step 10]};
insert "fourth" into sel.q;
