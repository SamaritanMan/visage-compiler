/*
 * Regression test for JFXC-3822 : A bi-directionally bound variable is not allowed to participate in another bi-directional binding
 *
 * @test
 * @run
 */

var x:Number = 10;

var a:Number = bind x with inverse on replace {
   println("a: {a}");
}
var b:Number = bind a with inverse on replace {
   println("b: {b}");
}
