/**
 * Regression test for JFXC-3699 : Compiled-bind: NPE with bound for in override var
 *
 * @test
 */

class A {
   var legend:Object[];
}

class B extends A {
   override var legend = bind for (x in [1,2,3]) 1;
}
