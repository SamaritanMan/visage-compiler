/* regression test for JFXC-1171
 *
 * @test
 * @run
 */

import java.lang.System;
import java.util.UnknownFormatConversionException;

// This should print "Success".
try {
    System.out.println(##"{%D 10}");
    System.out.println("Failed");
} catch (e: UnknownFormatConversionException) {
    System.out.println("Success");
}
