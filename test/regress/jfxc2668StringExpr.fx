/**
 * JFXC-2668 : Regression: crash: no bound conversion of instance to sequence of superclass 
 *
 * @test
 */

var x : Object[] = bind "yo{1}";
println(x);
