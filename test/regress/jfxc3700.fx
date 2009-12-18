/**
 * Regression test for JFXC-3700 : Compiled-bind: assertion error: bound parts should have been converted to overrides
 *
 * @test
 */

class A {
var p : Object[];
}

var x = 1;
var y = A{ p: bind for (m in []) {A{p: bind x}}}