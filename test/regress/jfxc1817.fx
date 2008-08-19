/**
 * Regression test for JFXC-1817 : non-writable doesn't allow init
 *
 * @test
 * @run
 */

import java.lang.System;

class Test {
    public non-writable var foo: Integer;
}

var x = Test{foo: 3};
System.out.println(x.foo);
