/**
 * regression test: JFXC-2953 : Presence of the same var in a base class and two mixin classes on the extends line crashes the compiler
 * @test
 */

class B { var i:Integer; }
mixin class M1 { var i:Integer; }
mixin class M2 { var i:Integer; }
class D extends B, M1, M2 {}
