/**
 * Regression test JFXC-2017 : Cannot assgin an Integer sequence to a Number sequence
 *
 * @test
 * @run
 */

var nums:Number[];
var ints:Integer[];
nums = ints;  // fails to compile
