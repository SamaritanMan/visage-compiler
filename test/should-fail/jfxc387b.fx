/*
 * Regression test: JFXC-387: Compiler says boolean/int/double/java.lang.String when it means Boolean/Integer/Double/String
 *
 * @test/compile-error
 *
 * This should produce:
 *
 *     jfxc387b.fx:29: incompatible types
 *     found   : Integer
 *     required: String
 *     var bar : String = 10;
 *                        ^
 *     1 error
 *     
 * instead of
 *
 *     jfxc387b.fx:29: incompatible types
 *     found   : int
 *     required: java.lang.String
 *     var bar : String = 10;
 *                        ^
 *     1 error
 *
 * There doesn't seem to be a way to provide .EXPECTED files 
 * for tests that are expected to fail compilation.
 *
 */

var bar : String = 10;
