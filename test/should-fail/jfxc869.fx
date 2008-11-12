/**
 * Should-fail test for JFXC-869 - Visibility of catch-parameter
 * This script should fail during compilation.
 *
 * @test
 * @compile/fail jfxc869.fx
 */

import java.lang.Exception;
import java.lang.System;

try {
   throw new Exception();
}
catch (ex: java.lang.Exception) {
   System.out.println("I'm here {ex} ");
}

// The following line should result in error
System.out.println("I'm here {ex} ");
