/**
 * basic checks of Bits.shiftLeft and shiftRight for longs
 *
 * @test
 * @run
 */

import javafx.util.Bits;

function check(msg:String, i:Integer, j:Integer) {
 if(i!=j){throw new java.lang.Exception("{msg}: {i} !={j}"); }

 }

var i1:Long = 2056;
var i2:Long = 8;

var s0:Integer = 0;
var s1:Integer = 1;
var s2:Integer = 2;
var s3:Integer = 3;
var s5:Integer = 5;
var s32:Integer = 32;
var s60:Integer = 60;
var s127:Integer = 127;
var s256:Integer = 256;
var s514:Integer = 514;
var s8224:Integer = 8224;
var s32736:Integer = 32736;

var i1_2L:Long = Bits.shiftLeft(i1,s2);
var i1_2R:Long = Bits.shiftRight(i1,s2);
check("shift 2056 Left2",i1_2L,s8224);
check("shift 2056 Right2",i1_2R,s514);

var i2_5L:Long = Bits.shiftLeft(i2,s5);
var i2_3R:Long = Bits.shiftRight(i2,s3);  //
check("shift 8 Left5",i2_5L,s256);
check("shift 8 Right3",i2_3R,s1);
//---------------------------------------
var i3:Long = 15;
var i4:Long = 1023;

var i3_2L:Long = Bits.shiftLeft(i3,s2);
var i3_2R:Long = Bits.shiftRight(i3,s2);
check("shift 15 Left2",i3_2L,s60);
check("shift 15 Right2",i3_2R,s3);

var i4_5L:Long = Bits.shiftLeft(i4,s5);
var i4_3R:Long = Bits.shiftRight(i4,s3);  //
check("shift 1023 Left5",i4_5L,s32736);
check("shift 1023 Right3",i4_3R,s127);
//---------------------------------------
var i00:Long = 0;
var i01:Long = 1;

var i00_2L:Long = Bits.shiftLeft(i00,2);
var i00_2R:Long = Bits.shiftRight(i00,2);
check("shift 0 Left2" ,i00_2L,0);
check("shift 0 Right2",i00_2R,0);

var i01_5L:Long = Bits.shiftLeft(i01,5);
var i01_3R:Long = Bits.shiftRight(i01,3);  //
check("shift 1 Left2" ,i01_5L,32);
check("shift 1 Right2",i01_3R,0);
