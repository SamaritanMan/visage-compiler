/*
 * keywords not used in sqe functional tests as of 3/18/08
 * with, inverse
 *
 * @test
 * @compilefirst ../TestUtils.fx
 * @run
 */

import java.lang.System;

var TU = new TestUtils;

var I1 = 5;
var I2 = bind I1;
var I3 = bind I1 with inverse;

//initial check of check int and bound ints
TU.checkB( I1==5 and I2==5 and I3==5,"check 0 of simple bound variables");
//
I1 = 10;
TU.checkB( I1==10 and I2==10 and I3==10,"check 1 of simple bound variables");
//Cannot assign to bound variable
try {
I2 = 21;
}catch( atbe: java.lang.Exception ){
TU.checkB(atbe.toString().indexOf("AssignToBoundException" )>-1,"check for AssignToBoundException");
}
//unless bidirectional bind is used
I3=30;
TU.checkB( I1==30 and I2==30 and I3==30,"check 2 simple bind with inverse");

// bound strings
var aLabel = "a label";
var aBoundLabel = bind aLabel;
var aBiBoundLabel = bind aLabel with inverse;
TU.checkB( aLabel.compareTo("a label")==0 and aBoundLabel.compareTo("a label")==0 and aBiBoundLabel.compareTo("a label")==0, "check 0 of bound strings");
//check 1-change initial label value
aLabel="Hello World";
TU.checkB( aLabel.compareTo("Hello World")==0 and aBoundLabel.compareTo("Hello World")==0 and aBiBoundLabel.compareTo("Hello World")==0, "check 1 of bound strings");
//Cannot assign to bound variable
try {
aBoundLabel = "WoooHooo";
}catch( atbe2: java.lang.Exception ){
TU.checkB(atbe2.toString().indexOf("AssignToBoundException" )>-1,"check for AssignToBoundException on String");
}
//change value of string bound 'with inverse'
aBiBoundLabel="Good-Bye!";
TU.checkB( aLabel.compareTo("Good-Bye!")==0 and aBoundLabel.compareTo("Good-Bye!")==0 and aBiBoundLabel.compareTo("Good-Bye!")==0, "check 2 of bound strings");

//bound sequences
var fives = [ 5..100 step 5];
var tens = bind for (i in fives where i % 10 == 0) i;
TU.checkIs( tens, [ 10,20,30,40,50,60,70,80,90,100],"bind to sequence before value change");
fives = [ 100..150 step 5 ];
TU.checkIs( tens, [100,110,120,130,140,150],"change value of bound sequence");
TU.report();
