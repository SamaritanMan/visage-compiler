/**
 * JFXC-3810 : CSSFun sample gets ClassCastException: com.sun.javafx.runtime.sequence.SingletonSequence cannot be cast to javafx.scene.paint.Paint'
 *
 * @test
 * @run
 */

function f1(x:Boolean):Object {
   if (x) [1] else 1;
}

function f2(x:Boolean):Object[] {
   if (x) [1] else 1;
}

function f3(x:Boolean) {
   if (x) [1] else 1;
}


var c1 = false on replace {println("c1 = {c1}");};
var x1 = bind f1(c1);
var x2 = bind f2(c1);
var x3 = bind f3(c1);
println(x1);
println(x2);
println(x3);
c1 = true;
println(x1);
println(x2);
println(x3);

var c2 = false on replace {println("c2 = {c2}");};
var y1:Object = bind if (c2) [1] else 1;
var y2:Object[] = bind if (c2) [1] else 1;
var y3 = bind if (c2) [1] else 1;
println(y1);
println(y2);
println(y3);
c2 = true;
println(y1);
println(y2);
println(y3);
