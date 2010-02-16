/*
 * Regression test
 * JFXC-3994 : Missing error message when variable override non-existent symbol whose clashes with an outer var
 *
 * @test/compile-error
 */

class A {
var x;
}

class B extends A {
override var x = 1;
override var x = 2; //?
}

println(B{}.x);
