/*
 * Regression test for bug JFXC-580: 
 *    In a bind context, function arguments consisting of calls to 
 *    Java methods cause compilation failure
 * @test
 */

function foo(x : Integer) : Integer {
  x
}

var y = bind foo("abc".length());
