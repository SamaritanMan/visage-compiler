/**
 * JFXC-3683 : Compiled-bind: bound object literal in overridden var causes backend failure
 *
 * @test
 */

abstract class Super {}
class Sub extends Super {var x}

class A {
   var bv:Super;
}

mixin class M { var x; }

class B extends A, M {
   override var bv = bind Sub {x:this.x}
}
