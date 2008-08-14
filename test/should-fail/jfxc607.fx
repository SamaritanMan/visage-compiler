/**
 *
 * Regression test JFXC-607 : Variables defined using 'def' should be final
 *
 * @test/fail
 */

public def round : String = "Orbital";

class Fuzzy {

   def count = 1234;

   function doit() {
      def snurd = 3.1415926;
  
      round = "Linear";
      count = 4321;
      snurd = 1.41421356;
   }
}
