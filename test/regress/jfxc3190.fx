/*
 * Regression: JFXC-3190 - internal error in the OpenJFX compiler (1.2-dev- ali-2009-05-03).
 *
 * @test
 *
 */

class Test{
var x=10;
var y = bind this.foo();

function foo(){ "foo"}
}
