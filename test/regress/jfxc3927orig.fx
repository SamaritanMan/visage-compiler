/*
 * Regression test 
 * JFXC-3927 : SWAT failure : JawbreakerGame compiler crash -- JavafxClassSymbol cannot be cast to JavafxVarSymbol
 *
 * Original extraction
 *
 * @compilefirst jfxc3927origSub.fx
 * @test
 * @run
 */

var tc = bind jfxc3927origSub.timeLimit[0];
println(tc);
