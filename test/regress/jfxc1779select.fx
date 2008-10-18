/*
 * Regression test JFXC-1779 : Result of a bound function is not re-evaluated when a function dependency changes
 *
 * @test
 * @run
 */

class A {
   var fn:function():String
}

var x = A {fn: function():String { "First" }}
var y = A {fn: function():String { "Third" }}
var results = bind x.fn();
println(results);
x.fn = function():String { "Second" };;
println(results);
x = null;
println(results);
x = y;
println(results);
