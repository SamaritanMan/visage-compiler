/*
 * JFXC-2795: Compiler givces ambiguity error when type seems it can be inferred
 *
 * @test
 * @run
 */

var x : java.lang.Float = 5;
var y : java.lang.Float = 10;
println(x + y); //15
println(x+=y); //15
println(x++); //15
println(++x); //17