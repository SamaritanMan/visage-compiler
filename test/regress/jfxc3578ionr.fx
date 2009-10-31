/**
 * Regression test: JFXC-3578 : Compiled bind: translation of non-transformative bound sequences: identifier and member select
 *
 *  Bound member select sequence of Object WITH on-replace -- instance-level
 *
 * @test
 * @run
 */

class bs2 {
  var q : Object[] = ["nope"]
}

class actor {
  var sel = bs2 {q: [0..5] };
  def bq = bind sel.q on replace [beg..end] = newVal { println("  Replace [{beg}..{end}] = {sizeof newVal}") };

  function doit() {
    insert "first" into sel.q;
    delete 3 from sel.q;
    sel = bs2 {q: "second"};
    sel = bs2 {q: [10..100 step 10]};
    insert "fourth" into sel.q;
  }
}

actor{}.doit();
