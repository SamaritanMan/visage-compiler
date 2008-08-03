/*
 * @test
 * @run
 */

import java.lang.System;
import java.lang.Object;
import java.lang.Exception;

/**
 * Port of qsort.java, a javac test.
 * For comparision, compile qsort.java and run (take a second to sort 3000+ items), while this
 * fx form take a minute or more to sort just 1000.
 *  The partition() function is very slow.
 * This FAILS for smaller sequences
 */

public class qsort
{
    attribute bPrint: Boolean = true;
    attribute arraysize = 0;
    attribute Int_sequence:Integer[];
    attribute compare_count = 0;
    attribute swap_count = 0;
    function PrintOff(b: Boolean) { bPrint = not b; }

     //////// debugging function //////////////////////////
	 attribute debug = false;
	 function debugout(msg:String) { if (debug)	 {System.out.println( msg ); } }

    /** Return length of internal array */
	 function length():Integer { return sizeof Int_sequence; }

    /** Print routines controlled by Boolean bPrint */
     function println( msg: String ) { if (bPrint) System.out.println(msg); }
     function print( msg: String  )  { if (bPrint) System.out.print(msg);   }

    /** Print array preceded by message msg  */
    function printArray(msg: String)
    {
      if (bPrint) {
        System.out.print(msg);
		  System.out.println(Int_sequence);
		}
    }

    /** Swap object in Int_sequence with indexes i and j  */
    function swap( i:Integer, j:Integer)
    {   debugout( "swap({i},{j})" );
		  ++swap_count;
        var temp = Int_sequence[i];
        Int_sequence[i] = Int_sequence[j];
        Int_sequence[j] = temp;
    }

    function Compare(i1:Integer, i2:Integer):Integer
    {
       ++compare_count;
		 return (i1 - i2);
    }

    /** Find pivot point in Int_sequence  */
    function findPivot(i:Integer, j:Integer):Integer
    {
		 var ret;
       var firstkey = Int_sequence[i];
		  for(k in [i+1..j]) {
          if( Compare(Int_sequence[k],firstkey) > 0) { return k; } //ret =k; return ret; }
          else if( Compare( Int_sequence[k],firstkey) < 0 )  { return i; } //ret = i; return i; }
       }
       //should not get here unless sequence until sequence is sorted.
       return -1;
    }

	 /** Partition array around pivot point 'pivot'	  */
    function partition(i:Integer, j:Integer, pivot:Integer): Integer
    {
        debugout("start partition({i},{j})......");
        var l = i;
        var r = j;
          while(l<=r)
          {
              swap(l,r);
              while( Compare(Int_sequence[l],pivot)<0 )
				     {l = l+1;}
              while( Compare(Int_sequence[r],pivot)>-1 )
				     {r = r-1;}
          }
		 debugout(".....finish partition({i},{j})");
		  return l;
    }

    /**
     * Recursive quick sort algorithm
     *   While pivot index is >=0(ie., j>i)
     *   1. Find pivot index.
     *   2. Partition array around index
     *   3. Sort each partition
     */
    function sort(i:Integer,j:Integer):Boolean {
        var k =0;
        var pivotindex = 0;
        var pivot = 0;
        pivotindex = findPivot(i,j);
          if(pivotindex >=0)
          {
              pivot = Int_sequence[pivotindex];
              k = partition(i,j,pivot);
              sort(i,k-1);
              sort(k,j);
          }
		  return true;
    }


function test1() {
//create an array
// >=302 sorts, but <302 crashes in runtime.
var arraysize = 302;
var multiplier = 100;
var modnum = 151;
var totalSortedItems = 0;
var totalCompares = 0;
var totalSwaps = 0;
System.out.print("START...");
println("----Integer array-----------------");
for( j in [0..arraysize-1]) {
   insert ((j*multiplier + 1) %modnum) into Int_sequence;
}
printArray("Unsorted");
sort(0,arraysize-1);
totalSortedItems += arraysize;
totalCompares += compare_count;
totalSwaps += swap_count;
printArray("Sorted");
	System.out.println("DONE!");
   System.out.println("Items sorted: {totalSortedItems}  Swaps: {totalSwaps}  compares: {totalCompares}") ;
   System.out.println("PASS QSORT");
}
}

var QSortTest = new qsort;
QSortTest.PrintOff(false);
QSortTest.test1();


/*
qsort.println("----Double array-----------------");
      Double Darray[] = new Double[arraysize];
      for(Integer j = 0; j<arraysize; j++)
         Darray[j] = new Double( ((j*multiplier + 1) %modnum)*1.235 );
      qsort<Double> qD = new qsort<Double>(Darray);
      String type = qD.Int_sequence[0].getClass().getName();
qsort.println("type of Object array[1]: " + type);
      qD.printArray("Unsorted:");
      qD.sort(0,qD.length()-1);
      totalSortedItems += qD.length();
      totalCompares += qD.compare_count;
      totalSwaps += qD.swap_count;
      qD.printArray("Sorted:");

      //see note for constructor for primitive type arrays
qsort.println("----Integer array-----------------");
      Integer iarray[]  = new Integer[arraysize];
      for(Integer j = 0; j<arraysize; j++)
         iarray[j] = (j*multiplier + 1) %modnum;
      qsort<Integer> q = new qsort<Integer>(iarray);
      q.printArray("Unsorted");
      q.sort(0,q.length()-1);
      totalSortedItems += q.length();
      totalCompares    += q.compare_count;
      totalSwaps       += q.swap_count;
      //force use of enhanced for loop in printArray(T[])
      q.printArray ( q.getSortedArray() );

qsort.println("----String array-----------------");
      String Sarray[] ={"cat", "dog","aligator","Zebra","Monkey","elephant","snake","lizard"};
      qsort<String> qS = new qsort<String>(Sarray);
      qS.printArray("Unsorted:");
      qS.sort(0,qS.length()-1);
      System.out.println("DONE!");
      totalSortedItems += qS.length();
      totalCompares    += qS.compare_count;
      totalSwaps       += qS.swap_count;
      qS.printArray("Sorted:");
      System.out.println("Items sorted: " + totalSortedItems  + "  Swaps: " + totalSwaps + "  compares: " + totalCompares) ;
      System.out.println("PASS QSORT");
    }
//*/

