/**
 * Regression test JFXC-1372 : Problem with Integer-var instanceof java.lang.Object
 *
 * @test
 * @run
 */
import java.lang.*;
var x = 23;
System.out.println("{x instanceof java.lang.Object}");
