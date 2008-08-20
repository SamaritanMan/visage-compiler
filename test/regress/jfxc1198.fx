/**
 * regression test: JFXC-1198 : Passing null or empty sequence arg to a bound function throws method ambiguity error
 *
 * @test
 * @run
 */

import java.lang.System;

class A{
        var name :String;
        bound function doIt (x:String[]):String[] {
              ["Fx","Rocks"];
        }


}
var obj:A = A{name :"Test"};

System.out.println( obj.doIt(null) );
System.out.println( obj.doIt([]) );

