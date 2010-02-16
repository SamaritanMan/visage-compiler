/**
 * Regression test: JFXC-3578 : Compiled bind: translation of non-transformative bound sequences: identifier and member select
 *
 * Bound member select sequence of String in instance context with on-replace referencing oldValue
 *
 * @test
 * @run
 */

class Y {
  var ref = this;
  var x = ["hi", "low"];
  var y = bind ref.x on replace oldSlice[a..b] = newSlice { 
        println("y: removed {oldSlice[a..b].toString()} and added {newSlice.toString()}"); 
      };

  function doit() {
    insert "bo" into x;
    insert "mo" into x;
    x = ["zorp", "zard"];
    ref = null;
  }
}

Y{}.doit()
