/*
 * Test basic slice trigger behavior; removed insert and delete triggers.
 * I'll work on more functional triggers(triggers that do something) later.
 * TODO: not complete but need to put this back
 *
 * @test
 * @compilefirst ../../TestUtils.fx
 * @run
 */

import java.lang.System;


/* a little different in that it extends the TestUtils class rather than creating a TestUtils object.*/
class FooSlice extends TestUtils {

/** seq1, 0 - 50 x 5's */
attribute seq1 = [0,5,10,15,20,25,30,35,40,50]
on replace oldValue[indx  .. lastIndex]=newElements {	GFT++; insert "{oldValue}" into replacements;		};

/** seq2, 2^0 - 2^8 */
attribute seq2 = [0,1,2,3,4]
on replace oldValue[indx  .. lastIndex]=newElements
  { GFT++; insert "seq_2: replace {newElements} at {indx}\n" into replacements;  };

attribute seq_2p : Integer[]
on replace oldValue[indx  .. lastIndex]=newElements
  { GFT++; insert "seq_2p: replaced {String.valueOf(oldValue)}[{indx}..{lastIndex}] by {String.valueOf(newElements)}\n" into replacements;  };

/** seq3 10 element descending sequence 10 - 1 */
attribute seq3 = [10..1 step -1]
on replace oldValue[indx  .. lastIndex]=newElements
	{  GFT++;
		if(sizeof oldValue  < sizeof newElements) 			{
			if(sizeof oldValue > 0){insert "replacement({sizeof newElements}) is larger than existing({sizeof oldValue})\n" into replacements;}
		    }
   };

/** seq4 4 element descending sequence 101 - 98 */
attribute done: Boolean = false;
attribute seq4 = [101..98 step -1]
on replace oldValue[indx  .. lastIndex]=newElements {	GFT++;
   insert "insert {newElements} or {oldValue} into {seq4}" into replacements;
};

/** seq5, 11 element sequence 100 - 110 */
attribute seq5 = [100..110]
	on replace oldValue[indx  .. lastIndex]=newElements {	GFT++; insert "{oldValue}" into replacements;		};

/**
 * Tests on seq1
 */
function test1() {
   System.out.println("-test1-");
   //this does not trigger a trigger.
	seq1[3] = seq1[3]/5;
   //insert seq1[3]/5 at seq1[3];  //presumably this replacement syntax will.
   printSequence(seq1);
   PrintPassFail( "check insertion into seq1", Success( seq1[3],3) );
}

function pow(b:Integer, e:Integer):Integer {
	var res = 1;
	if(e==0) {return res;}
	if(e<0) { return 0; }
	for(i in [ 1..e]) res = res*b;
	return res;
}

/**
 * Tests on seq1
 */
function test2() {
 System.out.println("-test2-");
 PrintPassFail( "check size of seq2", Success( seq2.size(),5) );
 printSequence(seq2);
  for ( n in seq2 ) 	  {
	  insert pow(2,n) into seq_2p;
  }
  checkIs(seq_2p,[1,2,4,8,16], "seq_2p: check new sequence");
  printSequence(seq_2p);
  insert 7 into seq2;
  insert 10 into seq2;
  insert 8 into seq2;
  checkIs(seq2,[0,1,2,3,4,7,10,8],"seq2: check altered sequence");
  System.out.println("trying to trigger replace trigger");
  seq2[5..6] = [5,6,7];
  checkIs(seq2,[0,1,2,3,4,5,6,7,8],"seq2: check new sequence after replace");

  for ( n in seq2[5..] ) 	  {
	  insert pow(2,n) into seq_2p;
  }
  printSequence(seq_2p);
  checkIs(seq_2p,[1,2,4,8,16,32,64,128,256],"line 95: seq_2p: check new sequence");
  checkB( checkAscendingSequence(seq2,true)==true,"line 96:check for ascending sequence");
 }

function test3() {
  System.out.println("-test3-replacement is larger than the sequence");
  checkI(sizeof seq3,10,"line 101: check size of seq3: {sizeof seq3}");
  var larger_seq3 = [ 20..1 step -1 ];
  seq3[0..10] = larger_seq3;
  checkI( sizeof seq3,20,"line 104: check size of larger seq3: { sizeof seq3}");
  checkB( checkAscendingSequence(seq3,false)==true,"line 105: check for descending sequence of seq3" );

  var larger_seq3b = [ 26..100 ];
  seq3[0..20] = larger_seq3b;
  checkI( sizeof seq3,75,"line 109: check size of larger  seq3: { sizeof seq3}");
  check("contents of seq3: {seq3}");
  checkB( checkAscendingSequence(seq3,true)==true,"line 111: check for ascending sequence of seq3");
}

/** var seq4 = [101..98] */
function test4() {
  System.out.println("-test4-empty-");
  done=false;
  insert 99 into seq4;
  check("contents of seq4 with 99 inserted: {seq4}");
  checkB( checkAscendingSequence(seq4,false)==false,"line 120: check for descending sequence of seq4, should not be descending." );
}

/**
 * Tests on seq5
 */
 function test5() {
    System.out.println("-test5-");
    printSequence(seq5);
    seq5[3] = 88;
    insert 77 into seq5;
    delete 109 from seq5;
    delete seq5[6];
    seq5[4..8] = seq5[5..7];
    checkIs(seq5 , [ 100, 101, 102, 88, 105, 107, 108, 77 ], "line 134: seq5-check end sequence");
    delete seq5;
	 checkB( seq5==[], "line 136: seq5-check for deleted sequence" );
 }
};


var fooslice = new FooSlice;
fooslice.test1();
fooslice.test2();
fooslice.test3();
fooslice.test4();
fooslice.test5();
fooslice.Replacements();
fooslice.report();

