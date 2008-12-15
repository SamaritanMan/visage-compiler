/*
 * Regression test: Problem with vars being accessed before declared
 *
 * @test/warning
 */

function f() {
   return y; //allowed - y is referenced from a different named scope
}
var x = w; //not allowed - w is referenced from the same named scope
var y = z; //allowed, z is a def variable initialized by an object literal
var w = 1;
def z = Foo{a:1}

class Foo {
   var a;
}

Foo { a:v; } //not allowed - v is referenced from the same named scope
var u = function() {x}(); //allowed - v is referenced from a different named scope (lambda)
var v = 1;