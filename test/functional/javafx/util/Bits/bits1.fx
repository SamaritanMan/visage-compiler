/**
 * basic checks of Bits.shiftLeft and shiftRight
 *
 * @test
 * @run
 */

import javafx.util.Bits;

function check(msg:String, i:Integer, j:Integer) {
 if(i!=j){throw new java.lang.Exception("{msg}: {i} !={j}"); }
 }

var i1 = 2056;
var i2 = 8;

var i1_2L = Bits.shiftLeft(i1,2);
var i1_2R = Bits.shiftRight(i1,2);
check("shift 2056 Left2",i1_2L,8224);
check("shift 2056 Right2",i1_2R,514);

var i2_5L = Bits.shiftLeft(i2,5);
var i2_3R = Bits.shiftRight(i2,3);  //
check("shift 8 Left5",i2_5L,256);
check("shift 8 Right3",i2_3R,1);
//---------------------------------------
var i3 = 15;
var i4 = 1023;

var i3_2L = Bits.shiftLeft(i3,2);
var i3_2R = Bits.shiftRight(i3,2);
check("shift 15 Left2",i3_2L,60);
check("shift 15 Right2",i3_2R,3);

var i4_5L = Bits.shiftLeft(i4,5);
var i4_3R = Bits.shiftRight(i4,3);  //
check("shift 1023 Left5",i4_5L,32736);
check("shift 1023 Right3",i4_3R,127);
//---------------------------------------
var i00 = 0;
var i01 = 1;

var i00_2L = Bits.shiftLeft(i00,2);
var i00_2R = Bits.shiftRight(i00,2);
check("shift 0 Left2" ,i00_2L,0);
check("shift 0 Right2",i00_2R,0);

var i01_5L = Bits.shiftLeft(i01,5);
var i01_3R = Bits.shiftRight(i01,3);  //
check("shift 1 Left2" ,i01_5L,32);
check("shift 1 Right2",i01_3R,0);

