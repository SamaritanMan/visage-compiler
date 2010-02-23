/**
 * JFXC-4157 : Compiler fails with an internal error when compiling code using bound function
 *
 * @test
 * @run
 */

var f:function():Object = null;
var m = bind f on replace { println(f) };
f = function() {1};
f = function() {2};
