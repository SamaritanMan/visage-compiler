/**
 * Regression test JFXC-891 : block followed by some keywords gives parser error
 *
 * @test
 * @run
 */

import java.lang.System;

function bip(q : Integer[]) : Integer[] {
  if (sizeof q == 0) {
    return [1, 2]
  }
  reverse q
}

function bop(q : Integer[]) : Integer {
  var k = { 1 + 2 }
  sizeof q
}

function mop(x : Integer) : Integer {
  var k = { x }
  ++k
}

System.out.println(bip([1,2,3]));
System.out.println(bop([1,2,3]));
System.out.println(mop(9));
