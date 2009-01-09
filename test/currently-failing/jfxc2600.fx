/*
 * Conversion of Character sequence to other numeric sequence types causes internal compiler error
 *
 * After fixing please uncomment and update corresponding testcases in
 *     test/features/F26-numerics/SeqCastTest.fx
 *
 * @test/fail
 */

var c1: Character = 5 as Character;
var vCharacter : Character[] = [c1, c1+1, c1+2];
var vByte : Byte[] = [1..7];
var vInteger : Integer[] = [256..259];
var vLong : Long[] = [1000000..1000003];
var vShort : Short[] = [16000..16004];
var vFloat : Float[] = [5.5, 6.5,7.25];
var vDouble : Double[] = [6.125..7.875];
var vNumber: Number[]=[35120.5..35125.5];
var res=this;
var gen: Boolean = false;


var xByte: Byte[] = vByte;
var xShort: Short[] = vShort;
var xCharacter: Character[] = vCharacter;
var xInteger: Integer[] = vInteger;
var xLong: Long[] = vLong;
var xFloat: Float[] = vFloat;
var xDouble: Double[] = vDouble;
var xNumber: Number[] = vNumber;

function testCharacterSeqCast() {
    xByte=vCharacter;
    xShort=vCharacter;
    xInteger=vCharacter;
    xLong=vCharacter;
    xFloat=vCharacter;
    xDouble=vCharacter;
    xNumber=vCharacter;
    xCharacter=vCharacter;

    xCharacter=vByte;
    xCharacter=vShort;
    xCharacter=vInteger;
    xCharacter=vLong;
    xCharacter=vFloat;
    xCharacter=vDouble;
    xCharacter=vNumber;
    xCharacter=vCharacter;
}

testCharacterSeqCast();
