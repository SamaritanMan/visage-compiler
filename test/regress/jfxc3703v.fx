/**
 * Regression test JFXC-3586 : Compiled bind: ABORT not implemented JFXIndexof - non-bound-for with bound-index-of
 *
 * Value test
 *
 * @test
 * @run
 */

class Path {
  var fill: Integer;
  override function toString() { " ({fill})" }
}

var rng = [0..3];

var vv = for (xx in rng) {
    Path {
        fill: bind indexof xx;
    }
}
println(vv);
