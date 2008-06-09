/**
 * regression test: JFXC-1111 Attempt to set Integer[] element with double fails with back-end error
 * @test
 * @run
 */

import java.lang.System;

var ints:Integer[] = [1..10];
ints[3] = 12.34;
System.out.println(ints[3]);
