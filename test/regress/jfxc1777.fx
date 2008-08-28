/**
 * Regression test for JFXC-1777 : java.lang.Object should be auto-imported
 *
 * @test
 * @run
 */

import java.lang.System;

var x : Object;

x = "you";
System.out.println(x);

x = 1;
System.out.println(x);
