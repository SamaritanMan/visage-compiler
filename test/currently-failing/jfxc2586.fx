/*
 * Same values assigned to the var of type Double and to the element of 
 * a sequence of Double are not equal.
 * Please uncomment corresponding lines in test/features/F26-numerics/VarScopes.fx
 * when this issue is resolved.
 *
 * @test
 * @run/fail
 */

var dSeq : Double[] = [ 5555.11..5556.11 ];
var d : Double = 5555.11;
// The result will become expected if 5555.11 is explicitly casted to Double
// var d : Double = 5555.11 as Double; 

// Currently prints: d: 5555.11, dSeq[0]: 5555.10986328125, equal: false 
var log = "d: {d}, dSeq[0]: {dSeq[0]}, equal: {d == dSeq[0]}";
println(log);

if (d != dSeq[0])
    throw new java.lang.Exception("Comparison failure: d != dSeq[0]\n{log}");
