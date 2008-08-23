/*
 * @subtest
 */


import java.lang.System;

public class TestUtils  {
	var pass = 0;
	var fail = 0;
   public var GFT = 0; //Golden_File_Tests; do not show up as pass or fail check
	var bDEBUG = false;
	var failures:String[];
	function debugout(msg: String) { if(bDEBUG) System.out.println( "{msg}" ); }
   public var replacements = [ "REPLACEMENTS:" ];
   public function Replacements() {System.out.println( replacements ); }
   public function printSequence( seq : Integer[] ) { System.out.println( seq ); }
   public function print(msg:String) { System.out.println(msg); }
	/* Increment output if used as comparison line for .EXPECTED file. This way golden file test checks can be accounted for. */
   public function addGFT( n:Integer){ GFT+=n; }
   public function check(description:String) {GFT++; System.out.println("CHECK: {description}");	}
   public function check(output:Boolean, description:String) { pass++; if(not output)System.out.println("CHECK[method has no output]: {description}");	}

	/* Test increment checks. These print msg string only upon failure.  */
   public function checkS(s1:String, s2:String, msg:String) { if(s1==s2){pass++; }else {fail++;print("FAILED: {msg} : {s1} != {s2}");} }
   public function checkI(i1:Integer, i2:Integer, msg:String) { if(i1==i2){pass++; }else {fail++;print("FAILED: {msg} : {i1} != {i2}");} }
   public function checkN(n1:Number, n2:Number, msg:String) { if(n1==n2){pass++; }else {fail++;print("FAILED: {msg} : {n1} != {n2}");} }
   public function checkIs(i1s:Integer[], i2s:Integer[], msg:String) { if(i1s==i2s){pass++; }else {fail++;print("FAILED: {msg} : {i1s} != {i2s}");} }
   public function checkSs(s1s:String[], s2s:String[], msg:String) { if(s1s==s2s){pass++; }else {fail++;print("FAILED: {msg} : {s1s} != {s2s}");} }
   public function checkNs(n1s:Number[], n2s:Number[], msg:String) { if(n1s==n2s){pass++; }else {fail++;print("FAILED: {msg} : {n1s} != {n2s}");} }
   public function checkB(b:Boolean,msg:String) { if(b){pass++;} else{fail++;print("FAILED: {msg}");}}
   public function checknotB(b:Boolean,msg:String) { if(b){fail++;print("FAILED: {msg}");}else{pass++;} }
   /* output report of tests. This output itself should be place in an fx.EXPECTED file */
   public function report() {
	System.out.println("========= results ================");
	System.out.println("Tests:      {pass+fail+GFT}");
	System.out.println("Passed:     {pass}");
	System.out.println("Failed:     {fail}");
	System.out.println("GoldenFile: {GFT} output comparisons");
	System.out.println("==================================");
		/* An idea whose time has not yet come? :) 
		 * Idea is to place string such as:    FAIL: testcase, description  into collection(sequence) and print out 
		 * to show which tests failed. However, most fail checks print this info anyway if properly used.
		System.out.println("=========failed tests ================");
		for ( msg in failures)
			System.out.println(msg);
		 */
	 }

	/*
	 * check for ascendency of an Integer sequence.
	 * Since it check for ascending or descending depending on 'asc', it only returns true/false and does
	 * not increment pass/fail. 
	 * Pass this to checkB() for test incrementing and message upon fail.
	 */
   public function checkAscendingSequence( seq : Integer[], asc: Boolean):Boolean {
		var retval = true;
		var max = seq.size()-2;
		for (i in [0..max] )	{
		   if(asc) { if(seq[i+1] < seq[i]) {retval = false;}	}
   		   else    { if(seq[i] < seq[i+1]) { retval = false;}  }
		}
		return retval;
	}

   public function compare(n1:Integer, n2:Integer):Boolean {
		var retval = false;
		if (n1==n2) { retval = true; }
                return retval;
	}

   public function compare( s1:String[], s2:String[]):Boolean {
		var retval = true;
		if(sizeof s1 > sizeof s2) {	retval = false;  }
		else if (sizeof s1<sizeof s2) { 	retval = false; }
		else {
			for(i in [ 0..sizeof s1]) {
				if(s1[i] != s2[i])	retval = false;
			}
		}
		return retval;
	}

   public function comparei( i1:Integer[], i2:Integer[]):Boolean {
		var retval = true;
		if(sizeof i1 > sizeof i2) {	retval = false;  }
		else if (sizeof i1<sizeof i2) { 	retval = false; }
		else {
			for(i in [ 0..sizeof i1]) {
				if(i1[i] != i2[i])	retval = false;
			}
		}
		return retval;
	}

   public function Success(n1:Integer, n2:Integer):Boolean {
		var retval = false;
		if (n1==n2) { retval = true; pass=pass+1; }
		else { fail = fail+1;System.out.println("FAILED: Success({n1},{n2})"); }
      return retval;
	}
   public function Success(b1:Boolean, b2:Boolean):Boolean {
		var retval = false;
		if (b1==b2) { retval = true; pass=pass+1; }
		else { fail = fail+1;System.out.println("FAILED: Success({b1},{b2})"); }
      return retval;
	}
   public function Failure(n1:Integer, n2:Integer):Boolean {
		var retval = true;
		if (n1==n2) { retval = false; fail = fail+1; System.out.println("FAILED: Failure({n1},{n2})"); }
		else { pass = pass + 1; }
      return retval;
	}

   public function PrintPassFail(bOutcome:Boolean) {
			var success = "FAIL";
         if ( bOutcome==true ) { success="PASS"; }
			System.out.println( "{success}" );
	}
   public function PrintPassFail(description:String, bOutcome:Boolean) {
			var success = "FAIL";
         if ( bOutcome==true )
				{ success="PASS"; }
			else
				{ insert "{success} : {description}" into failures;}
			System.out.println( "{success} : {description}" );
	}

};

