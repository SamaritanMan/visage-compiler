/* 
 * Regression test: JFXC-268: Incorrect error message locations
 *
 * @test/compile-error
 */

import java.lang.*;

var ab:Integer = function(a:Number, b:Number):Integer { a + b; }

System.out.println(ab(23,43));
