/**
 * Should-fail test JFXC-1904 : Misleading error messages when a run() function is present
 *
 * @test/compile-error
 */

import java.lang.*;

var value = Math.PI;
System.out.println("Startup");

public function xxx() {
    System.out.println("Value is {value}");
}