/**
 * Regression test JFXC-2017 : Cannot assgin an Integer sequence to a Number sequence
 *
 * @test
 * @run
 */

var objs:Object[];
var ints:Integer[];
var nums:Number[];
var strings:String[];
var threads:java.lang.Thread[];

objs = ints;
objs = nums;
objs = strings;
objs = threads;
