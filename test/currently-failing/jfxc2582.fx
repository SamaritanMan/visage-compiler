/*
 * Compilation error when converting a sequence element to another type.
 * Please uncomment corresponding lines in 
 * test/features/F26-numerics/VarConversions/{ToByte.fx,ToCharacter.fx,
 *                                            ToLong.fx,ToShort.fx}
 * when this issue is resolved.
 *
 * @test/fail
 */

def iSeq : Integer[] = [ 100..200 ];
def fSeq : Float[] = [ 100.5..200.5 ];

// inconvertible types: found java.lang.Integer  required byte
var b1 : Byte = iSeq[0];
// inconvertible types: found java.lang.Float  required byte
var b2 : Byte = fSeq[0];

// inconvertible types: found java.lang.Float  required long
var l1 : Long = fSeq[0];

// inconvertible types: found java.lang.Integer  required char
var c1 : Character = iSeq[0];
// inconvertible types: found java.lang.Float  required char
var c2 : Character = fSeq[0];

// inconvertible types: found java.lang.Integer  required short
var s1 : Short = iSeq[0];
// inconvertible types: found java.lang.Float  required short
var s2 : Short = fSeq[0];