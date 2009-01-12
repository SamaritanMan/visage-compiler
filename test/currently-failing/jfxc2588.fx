/*
 * Each of the below sequnce declarations leads to 
 * the following compilation errors:
 *
 * 1. range start must be Integer or Number
 * 2. range end must be Integer or Number
 * 3. range step must be Integer or Number
 *
 * Please uncomment corresponding lines in test/features/F26-numerics/Sequences.fx
 * when this issue is resolved.
 *
 * @test/fail
 */

def b1 : Byte = 10;
def b2 : Byte = 20;
def b3 : Byte = 2;
def c1 : Character = 10;
def c2 : Character = 20;
def c3 : Character = 2;
def s1 : Short = 10;
def s2 : Short = 20;
def s3 : Short = 2;
def l1 : Long = 10;
def l2 : Long = 20;
def l3 : Long = 2;


var bSeq : Byte[] = [ b1..b2 step b3];
var cSeq : Character[] = [ c1..c2 step c3];
var sSeq : Short[] = [ s1..s2 step s3];
var lSeq : Long[] = [ l1..l2 step l3];
