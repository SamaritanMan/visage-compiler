/**
 * Test javafx.util.Bits functions
 *
 * @test
 * @run
 */

import javafx.util.Bits;
import javafx.util.Sequences;


/**
 * some test check functions
 */
 /** assert check for Boolean true */
function myassert( msg:String, expr:Boolean ) {
	if( expr != true){ throw new java.lang.Exception("{msg}: {expr} != true "); }
}

function testRemoveInt() {
  def   masks : Integer[] = [ -1,0,2,4,8,16,32,1024 ];
  def    nums : Integer[] = [ 1..36];
  def expectedSeq =  [ 0, 0, 1, 0, 1, 2, 3, 0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 0, 1, 2, 3, 4 ];
  var newnums : Integer[];
  for ( num in nums) {
   var _num = num;
   for ( m in masks) {
     if ( Bits.contains(num,m) )
     _num = Bits.remove(num,m);
   }
   insert _num into newnums;
  }
  try {  myassert("testRemove", (expectedSeq==newnums) ); }
  catch(e:java.lang.Exception){println("FAIL; testRemove"); println(expectedSeq);println("!=");println(newnums);}
}

function testRemoveLong() {
  def   masks : Long[] = [ -1,0,2,4,8,16,32,1024 ];
  def    nums : Long[] = [ 1..36];
  def expectedSeq:Long[] =  [ 0, 0, 1, 0, 1, 2, 3, 0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 0, 1, 2, 3, 4 ];
  var newnums : Long[];
  for ( num in nums) {
   var _num = num;
   for ( m in masks) {
     if ( Bits.contains(num,m) )
     _num = Bits.remove(num,m);
   }
   insert _num into newnums;
  }
  try {  myassert("testRemove", (expectedSeq==newnums) ); }
  catch(e:java.lang.Exception){println("FAIL; testRemove"); println(expectedSeq);println("!=");println(newnums);}
}

function testAddInt() {
 def add1 = [-256..256];
 def s = sizeof add1;
 for ( i in [0..s] )
    myassert( "Bits.add({add1[i]},{Bits.complement(add1[i])})={Bits.add(add1[i],Bits.complement(add1[i]))}" , Bits.add(add1[i], Bits.complement(add1[i]) ) == -1);
}

function testAddLong() {
 def add1:Long[] = [-256..256];
 def s = sizeof add1;
 for ( i in [0..s] )
    myassert( "Bits.add({add1[i]},{Bits.complement(add1[i])})={Bits.add(add1[i],Bits.complement(add1[i]))}" , Bits.add(add1[i], Bits.complement(add1[i]) ) == -1);
}


testRemoveInt();
testRemoveLong();
testAddInt();
testAddLong();
