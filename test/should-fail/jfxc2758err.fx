/**
 * Should-Fail test JFXC-2758 : Bad test for alternate radix long literals
 *
 * @test/compile-error
 */

var x : Integer = 0xCFFFFFFFF;
var y : Integer = 0xAA00000000;
var z : Integer = 070000000000;
