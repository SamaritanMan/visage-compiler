/**
 * Should-fail test for JFXC-2314:
 * error message has no file name, line number, or errant line of code
 *
 * @test/compile-error
 */

var is2:Integer[] = [1,2,3];
var i2 = i;
for( is2.indexOf(i2) == -1) {
  insert i2 into is2;
}
