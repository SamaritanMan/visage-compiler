/*
 * Regression: JFXC-1933 - Pre-user-default value of String and Duration local var is null (which is invalid).
 *
 * @test
 * @run
 *
 */

var str = "Hello" on replace old { println("old={old}") }
