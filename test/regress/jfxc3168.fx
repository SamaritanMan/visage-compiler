/*
 * Regression: JFXC-3168 - Compiler crash: named outer access.
 *
 * @test
 * @run
 *
 */

abstract class d {
  abstract function bar() : Integer;
}

class xxx {
  public var x = 1;
  var y : String;
  function foo() {
    d {
      override function bar() {
         xxx.x + x
       }
    }
  }
}

var xx = xxx{}
println(xx.foo().bar());
