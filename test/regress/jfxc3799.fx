/*
 * Regression :  JFXC-3799 - compiled-bind: backend error if mixee class declaration precedes non-empty mixin class declaration
 *
 * @test
 */

class A extends M {}

mixin class M {
   var s:String;
}
