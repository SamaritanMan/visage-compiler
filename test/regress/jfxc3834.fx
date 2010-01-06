/**
 * JFXC-3834 : TextBox text is shifted up
 *
 * @test
 * @run
 */

class G {
   var a = A{g:this};
   var b = B{g:this};
   var c = C{g:this};
}

class A {
   var g:G;
   var x = 1;
}

class B {
   var g:G;
   var y1 = bind lazy g.a.x;
   var y2 = bind lazy g.a.x;
}

class C {
   var g:G;
   var z = bind g.b.y1 + g.b.y2 on replace {println("replacing C.z"); };
}

var g = G{};
g.c.z;
println("start");
g.a.x = 2; //trigger fired here
println("resetting again");
g.a.x = 6; //no trigger here
