/*
 * Regression test
 * JFXC-3981 : Compiler crash regression in apps/MediaComponent:
 *
 * @test
 */

class jfxc3981a {
  var y: Boolean;
}

class A {
  var y: Boolean;
}

class B {
   function f() {
      A { y: bind y with inverse; }
   }
}
