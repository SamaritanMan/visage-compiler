/**
 * Regress test for JFXC-3890.
 *
 *  Compiler crashes when multiple return type is used.
 *
 * @test/warning
 */

function f(){
   if (true) {
      return false;
   }
   println("void");
}
