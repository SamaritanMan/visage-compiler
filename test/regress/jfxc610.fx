/**
 * Regression test JFXC-610 : bind update for new
 *
 * @test
 * @run
 */

import java.lang.System; 

class Foo {}
class Bar extends Foo {}
class Baz {
 function getBar() : Bar { new Bar }
 function toFoo(x : Foo) : Integer {
   System.out.println(x instanceof Bar);
   51
 }
 function doit() : Void {
   var z = bind toFoo(getBar());
   System.out.println(z);
 }
}
(new Baz).doit() 
