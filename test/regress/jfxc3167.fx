/**
 * JFXC-3167 - class literals lead to an error in backend compilation.
 *
 * @test
 * @run
 */

// the following used to crash in the back-end
println(jfxc3167.class);
