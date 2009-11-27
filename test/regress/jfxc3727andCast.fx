/**
 * Regression test JFXC-3727 : Compiled bind: bound and/or must cut-off RHS preface
 *
 * Select on an objlit in right-hand-side of or
 *
 * @test
 * @run
 */

class A { var x = true }
var a : Object = 2;
var b = bind false and (a as A).x;
println(b);
