/* Regression test for JFXC-913 : Accessing bound variable inside a function causes compiler crash
 *
 * @test
 * @run
 */

import java.lang.System;

var z = bind [1..<11];
function makeData() {
        z;
}
System.out.println(makeData());
