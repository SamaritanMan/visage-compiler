/*
 * Regression test for JFXC-3995 : Compiled-bind: type error in the backend when compiling AT
 *
 * @test
 */

var y = 1;

function f() {
  if (false) {
     var x = bind y;
     return "";
  }
  return null;
}
