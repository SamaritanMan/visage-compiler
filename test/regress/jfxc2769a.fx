/**
 * JFXC-2769 : crash compiling bound function with for/if/else embedded within string expression
 *
 * most simplified test case
 *
 * @test
 * @run
 */

var states = [true];
def bs = bind  "{for (state in states) "--" }";
println(bs);
