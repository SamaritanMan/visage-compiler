import java.lang.System;
import java.text.DecimalFormat;

/*
 * @test
 * @compilefirst ../TestUtils.fx
 * @run
 */

/*
Expressions may be factored into subroutines called functions, for example:
*/
var TU = new TestUtils;

function z(a:Number, b:Number):Number {
     var x = a + b;
     var y = a - b;
     return sq(x) / sq (y);
}

function sq(n:Number): Number {n * n;}

/** some formatters */
function formatNumber(n:Number):String {
   var df:DecimalFormat = new DecimalFormat("###,###.##");
   df.setMinimumIntegerDigits(1);
	df.setMinimumFractionDigits(0);
   return df.format(n);
}

/*
A function takes the form:
function name (parameterName : parameterType, ...): returnType body
where body can be any expression.
*/
function m   (               ) {}
function m0  ( i : Integer   ) {}
function m02 ( s : String    ) {}
function m1  ( i : Integer   ): Integer{ return i; }
function m11 ( i : Integer   ): String { return i.toString(); }
function m12 ( s : String    ): String { return s;}
function m2  ( ia: Integer[] ): Integer[] { return ia; }
function m21 ( ia: Integer[] ): String[] { var sary:String[]; for(i in ia) {insert i.toString() into sary;} return sary; }
function m22 ( sa: String[]  ): String[] { return sa; }
function m3  ( i : String   ): Integer{ return java.lang.Integer.valueOf(i); }
function m4  ( ia: Integer[] ): Integer[] { return ia; }
function m5  ( s : String) { System.out.println({s}); }
function m6  ( i : Integer, s : String) : String { return "{s}{i}"; }
function m7  ( i : Integer, s : String) : String { return " {i.toString()}{s}"; }
function m8  ( i : Integer, s : String) : String {return s + i.toString(); }

/* basic function calls */
TU.check(false,"method with no parms, no return"); m();
TU.check(false,"method takes Integer, no return"); m0(1);
TU.check(false,"method takes String, no return");m02("hello");
TU.check(false,"method takes String-var.toString()-no return"); m02( "{m11(5).toString()}");

/* Other basic functions */
//System.out.print( "{m12( "{m11(5).toString()}")} - "); //prints "5 - "
TU.checkS({m12( "{m11(5).toString()}")}, "5","nested function takes int returns string passed as string");
//System.out.print( "{m6(2,"U")} - ");          //prints "U2 - "
TU.checkS( {m6(2,"U")},"U2","function takes integer,string and returns string of two variables");
//System.out.print( "{m8(2,"U")} - ");          //prints "U2 - "
TU.checkS( {m6(2,"U")},"U2","function takes integer,string and returns string using '+' to concat string and i.toString()");

/* Several ways to nest functions as parameters */
//System.out.print( "{ m3(m11(m1(55)))} -");    //prints "55 - "
TU.checkI({ m3(m11(m1(55)))},55,"nested functions 2 deep, outside one returns Integer");
//System.out.println( "{m1( m3(m11(m1(55))))}");  //prints "55 - "
TU.checkI({m1( m3(m11(m1(55))))},55,"nested functions 3 deep, outside one returns Integer");
//System.out.print( m2([{m1( m3(m11(m1(1))))},{m1( m3(m11(m1(2))))},{m1( m3(m11(m1(3))))}])); //prints [ 1, 2, 3 ]
TU.checkIs(m2([{m1( m3(m11(m1(1))))},{m1( m3(m11(m1(2))))},{m1( m3(m11(m1(3))))}]),[1,2,3],"nested function as sequence passed to function that take Integer sequence");
//System.out.println( m21(m2([{m1( m3(m11(m1(1))))},{m1( m3(m11(m1(2))))},{m1( m3(m11(m1(3))))}]))); //prints [ 1, 2, 3 ]
TU.checkSs(m21(m2([{m1( m3(m11(m1(1))))},{m1( m3(m11(m1(2))))},{m1( m3(m11(m1(3))))}])),["1","2","3"],"nested functions returning Integer passed to function which returns them as sequnece of strings.");
//m5( m6(101,"Hwy ") + m7(99," dead baboons ") + m8(8,"  That's a prime"));
TU.checkS(m12( m6(101,"Hwy ") + m7(99," dead baboons ") + m8(8,"  That's a prime")),"Hwy 101 99 dead baboons   That's a prime8","concatenated string of methods returning strings pass to method taking a string");
System.out.println();

/*Functions are first-class objects (they can, for example,  be assigned to variables, or */
var squares1:Number[];
for(x in [2..12]) {
	var xx = sq(x);
   insert xx into squares1;
}
TU.checkNs(squares1,[ 4.0, 9.0, 16.0, 25.0, 36.0, 49.0, 64.0, 81.0, 100.0, 121.0, 144.0 ],"functions assigned to variables");
//System.out.println( {squares1} );

/*passed as parameters to other functions.)*/
var squares2:String[];
for(x in [10..30 step 5]) {
	var xx = formatNumber( sq(x) );
	insert xx into squares2;
}
TU.checkSs(squares2,[ "100", "225", "400", "625", "900" ],"squares:functions passed as parameters to other functions");
//System.out.println({squares2});

/*Functions may be anonymous:*/
var timestable:String[];
var mult = function(a:Number, b:Number):Number { a * b; }
for(a1 in [ 1..10], a2 in [ 1..10]){
	insert "{a1} x {a2} = {formatNumber( mult(a1, a2) )}" into timestable;
//	System.out.println("{a1} x {a2} = {formatNumber( mult(a1, a2) )}");
}
TU.checkSs(timestable,["1 x 1 = 1", "1 x 2 = 2", "1 x 3 = 3", "1 x 4 = 4", "1 x 5 = 5", "1 x 6 = 6", "1 x 7 = 7", "1 x 8 = 8", "1 x 9 = 9", "1 x 10 = 10", "2 x 1 = 2", "2 x 2 = 4", "2 x 3 = 6", "2 x 4 = 8", "2 x 5 = 10", "2 x 6 = 12", "2 x 7 = 14", "2 x 8 = 16", "2 x 9 = 18", "2 x 10 = 20", "3 x 1 = 3", "3 x 2 = 6", "3 x 3 = 9", "3 x 4 = 12", "3 x 5 = 15", "3 x 6 = 18", "3 x 7 = 21", "3 x 8 = 24", "3 x 9 = 27", "3 x 10 = 30", "4 x 1 = 4", "4 x 2 = 8", "4 x 3 = 12", "4 x 4 = 16", "4 x 5 = 20", "4 x 6 = 24", "4 x 7 = 28", "4 x 8 = 32", "4 x 9 = 36", "4 x 10 = 40", "5 x 1 = 5", "5 x 2 = 10", "5 x 3 = 15", "5 x 4 = 20", "5 x 5 = 25", "5 x 6 = 30", "5 x 7 = 35", "5 x 8 = 40", "5 x 9 = 45", "5 x 10 = 50", "6 x 1 = 6", "6 x 2 = 12", "6 x 3 = 18", "6 x 4 = 24", "6 x 5 = 30", "6 x 6 = 36", "6 x 7 = 42", "6 x 8 = 48", "6 x 9 = 54", "6 x 10 = 60", "7 x 1 = 7", "7 x 2 = 14", "7 x 3 = 21", "7 x 4 = 28", "7 x 5 = 35", "7 x 6 = 42", "7 x 7 = 49", "7 x 8 = 56", "7 x 9 = 63", "7 x 10 = 70", "8 x 1 = 8", "8 x 2 = 16", "8 x 3 = 24", "8 x 4 = 32", "8 x 5 = 40", "8 x 6 = 48", "8 x 7 = 56", "8 x 8 = 64", "8 x 9 = 72", "8 x 10 = 80", "9 x 1 = 9", "9 x 2 = 18", "9 x 3 = 27", "9 x 4 = 36", "9 x 5 = 45", "9 x 6 = 54", "9 x 7 = 63", "9 x 8 = 72", "9 x 9 = 81", "9 x 10 = 90", "10 x 1 = 10", "10 x 2 = 20", "10 x 3 = 30", "10 x 4 = 40", "10 x 5 = 50", "10 x 6 = 60", "10 x 7 = 70", "10 x 8 = 80", "10 x 9 = 90", "10 x 10 = 100" ],"Functions can be anonymous.");

//recursive function to return fibonacci numbers
function fib(n:Integer):Integer {
	if (n<2) {return 1;}
	else {return (fib(n-1) + fib(n-2));}
}

//descending sequence lends itself to inverting a sequence
function flipSequence(iseq:Integer[]):Integer[] {
	  var newseq:Integer[];
	  for(i in [sizeof iseq-1 .. 0 step -1]) { insert iseq[i] into newseq; }
	  return newseq;
}

//sample Integer stack
public class TestIntStack {
  attribute thestack:Integer[]
	  	   on replace oldValue[indx  .. lastIndex]=newElements { }//System.out.println("replaced {String.valueOf(oldValue)}[{indx}..{lastIndex}] by {String.valueOf(newElements)}")};
  function getStack():Integer[] { return thestack; }
  function getTop():Integer { var t = sizeof thestack-1; return t; }
  function push(s:Integer)    { insert s into thestack; }
  function push(ss:Integer[]) { insert ss into thestack;}
  function flip(){ thestack = reverse thestack; }
  public function pop():Integer  {
    var retval = thestack[sizeof thestack-1];
	 delete thestack[ sizeof thestack-1 ];
    return retval;
  }

  //find index of first Integer==s
  public function search( s:Integer ):Integer  {
		var pos = -1;
		for( idx in [sizeof thestack-1 .. 0 step -1 ]){
			if(thestack[idx]==s) {	pos = idx;	return pos;	}
		}
    return pos;
  }
  public function print() {		System.out.println(thestack);  }
}

function check( msg:String,f:Integer, ExpectedValue:Integer) {
	if(f != ExpectedValue) { System.out.println("FAILED: {msg}; {f} != {ExpectedValue}"); }
}

System.out.println("a simple Stack application");
var tis: TestIntStack = new TestIntStack;
//regular object as arg
tis.push(5);
tis.push(8);
tis.push(22);
tis.push(11);
tis.push(95);
TU.checkIs(tis.getStack(),[5,8,22,11,95],"getStack; return Integer[] after inserts");
//sequence as arg
tis.push( [10,20,30]);
TU.checkIs(tis.getStack(),[5,8,22,11,95,10,20,30],"getStack; return Integer[] after insert of another sequence");

for(i in [1..4]) {tis.pop()}; //pop "top" four items off stack
TU.checkIs(tis.getStack(),[5,8,22,11],"pop top four items off stack");

// function returning sequence as arg
//System.out.println("descending sequence lends itself to inverting a sequence, push flip of 5,10,15");
tis.push(flipSequence([ 5,10,15]));
TU.checkIs(tis.getStack(),[5,8,22,11,15,10,5],"push flipped sequence");
TU.checkI(tis.search(22),2,"tis.search(22)");
//System.out.println("...flip the stack with 'reverse'..");
tis.flip();
TU.checkIs(tis.getStack(),[5,10,15,11,22,8,5],"flip sequence with reverse keyword");

//System.out.println("Recursive functions MUST have a return value specified.");
var fibs:Integer[];
for(i in [0..20]){  insert fib(i) into fibs; }
TU.checkIs(fibs,[1,1,2,3,5,8,13,21,34,55,89,144,233,377,610,987,1597,2584,4181,6765,10946],"recursive function: fibonacci numbers");

TU.report();

