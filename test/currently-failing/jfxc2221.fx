/**
 * Regression test JFXC-2221 : Overloading fails on sequence to array argument
 *
 * @test/fail
 */

import java.util.Arrays;

println(Arrays.binarySearch([12], 12))
