/**
 * regression test: JFXC-1261 Conversion of singleton sequence on slice replacement
 * @test
 * @run
 */

import java.lang.System;

var ints:Integer[] = [1..10];
ints[1..3] = 22;
System.out.println(ints[0]);
System.out.println(ints[1]);
System.out.println(ints[2]);
