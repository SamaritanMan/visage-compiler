/**
 * Regression test JFXC-838 : bound string expression
 *
 * @test
 * @run
 */
import java.lang.System; 

var enableBindingOverhaul;

var k = 5;
var z = 3.1415926535;
var s = 'blah';
var b = false;
var bs = bind "k={k} z={%6.3f z} z^2={%e z*z} s={s} {if(b) 'clowns' else '{s}{%6d k}'}";
System.out.println(bs);
k =2;
System.out.println(bs);
z = 1.1;
System.out.println(bs);
s = 'fog';
System.out.println(bs);
b = true;
System.out.println(bs);

