/*
 * Compiler reports an error for passing Character value to Byte/Short parameter.
 *
 * After fixing please uncomment and update corresponding testcases in
 *     test/features/F26-numerics/NumFuncTest.fx
 *
 * @test/compile-error
 */

function foobyte(x: Byte): String {"{x}"}
foobyte(50 as Character);
function fooshort(x: Short): String {"{x}"}
fooshort(50 as Character); 
