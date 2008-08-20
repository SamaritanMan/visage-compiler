/*
 * Regression test JFXC-1100 : Using Number as a Sequence Index Causes a Compile Error
 *
 * @test
 * @run
 */

import java.lang.System;

class BugTest {
    var status: Number;
    var codes: Number[] = [0, 1, 2, 3];
    var ubtest: Number = codes[status];
    var btest: Number = bind codes[status];
}

var bt = BugTest { status: 2.2 }
System.out.println(bt.ubtest);
System.out.println(bt.btest);
