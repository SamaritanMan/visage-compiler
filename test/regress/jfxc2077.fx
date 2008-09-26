/**
 * regression test: JFXC-2077 : Compiler confused with assigning member variables on Java classes
 *
 * @test
 * @run
 */

class Klass {
     var v: Integer on replace {
         var d = new java.awt.Dimension();
         d.width = v;
         println(d.getWidth());
     }
}

Klass {v: 1234}

 