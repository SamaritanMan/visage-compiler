/**
 * Regress test for JFXC-1169 - Compiler crashes on a Bind to a fx class instance creation with disallowed constructor args
 *
 * @test/compile-error
 */

class InvalidConstArgTest{
var x:Integer;
var y:Integer;
}
var x =bind new InvalidConstArgTest(1); // Invalid call