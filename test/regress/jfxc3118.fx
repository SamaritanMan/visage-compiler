/*
 * Regression: JFXC-3118 - Character sequence compare doesn't work.
 *
 * @test
 * @run
 *
 */

var test1 : Character[] = [60, 61];
if (test1 != [60, 61]) println("FAILED");
