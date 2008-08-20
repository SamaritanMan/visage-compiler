/*
 * Regression test for JFXC-757 : NPE on non-constant override var default
 *
 * @test
 * @run
 */

import java.lang.System;

class Guts {
  var name : String
}

class Base {
  var gut : Guts = Guts {name: "John Doe"}
}

class Sub extends Base {
  override var gut = Guts {name: "Jane Doe"}
}

var x = new Sub;
System.out.println(x.gut.name)
