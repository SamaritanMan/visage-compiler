/*
 * Regression test for JFXC-1151 : "x is already defined" is detected too late
 *
 * @test/compile-error
 */

function f(x:Object) {
var x:Object;
}
