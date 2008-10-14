/**
 * Regression test JFXC-2140 : 'and' behaving differently when used with bind
 *
 * @test
 * @run
 */

class A {
   public var value = "something";
}
class B extends A {
}

var a = new A;
var b = bind (a instanceof B and (a as B).value == "something");
println(b);

