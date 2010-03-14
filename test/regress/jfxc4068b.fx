/*
 * JFXC-4068 : PrimitiveShapes - transition from LinearGradient to RadialGradient
 *
 * If-expression
 *
 * @test
 * @run
 */

class A {
   var name:String;
   var x:Number;
}

var a1:A =  bind A {
   name: "A1"
   x: variable
}

var a2:A =  bind A {
   name:"A2"
   x: variable
}

var variable = 0;

var a = bind if (variable mod 2 == 1) a1.name else a2.x 
                     on replace { println("on-replace  a: {a}"); }

println("START");
for (x in [0..10]) {
   println("x = {x}");
   variable = x;
}

