/*
 * Regression test: for JFXC-3827: compiled-bind: javafx controls don't respond to mouse
 *
 * @test
 * @run
 */

class A {
   var x = "old!";
   var y = bind x on replace { println("y is {y}"); }
}

class B extends A {
   var k = bind x;
}

var a:A = B{};
println("start");
a.x = "new!";
