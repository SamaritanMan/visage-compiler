/*
 * Regression test: JFXC-2914 - Two warning messages when converting Number[] to Integer[]
 *
 * @test/warning
 */

var b1 = [1.1, 2.2, 3.3];
var b2: Integer[] = b1;

function f():Integer[] {
  return b1;
}