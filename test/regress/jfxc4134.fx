/*
 * JFXC-4134 : StickyNotes sample: Text in textbox is too high
 *
 * @test
 * @run
 */

class B {var x:Integer;}

class A {
   var init = false;
   var b:B = bind if (init) B{x:12} else B{};
   var c = b;
   var y:Integer = 0;
}

var a:A = A { y: bind a.b.x}
a.init = true;
println(a.y);
