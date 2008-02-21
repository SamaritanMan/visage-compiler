import java.lang.System;
import java.lang.Math;
import java.text.DecimalFormat;


/**
  * Any expression of any complexity may be bound - including
  * conditionals, loops, blocks,
  * function calls, and even
  * calls to methods and constructors written in the Java programming language.
  *
  * @test
  * @run
  */

//simple bind of variable to variable expression
class binder {

 private attribute df:DecimalFormat = new DecimalFormat("###,###.##");
/** some formatters */
 function formatNumber(n:Number):String { 
   df.setMinimumIntegerDigits(1);
	df.setMinimumFractionDigits(3);
   return df.format(n);
 }
 function formatNumber(n:Number, id:Integer, fd:Integer):String { 
   df.setMinimumIntegerDigits(id);
	df.setMinimumFractionDigits(fd);
   return df.format(n);
 }

function test1() {
System.out.println("test1: simple bind");
var x = 100;
var y = bind x + 100;
for( a in [5..50 step 5]){
  x = a;
  System.out.println( "x={x}, y={y}");
}
}

function test2() {
  System.out.println("test2: bind to java method");
  var d = 0;
  var x2 = bind Math.cos(Math.toRadians(d));
  for( r in [0..360 step 60]) {
    d = r.intValue();
    System.out.println("d={d}, x2= {formatNumber(x2)}"); // prints -1.0;
  }
}

function test3() {
  System.out.println("test3: sequence binding, list comprehension");
  var j = 50;
  var xs = bind for (i in [1..100] where i < j) i;
  j = 15;
  System.out.println(xs);
  j = 25;
  System.out.println(xs);
  j = 125;
  System.out.println(xs);
  j = 5; 
  System.out.println(xs);

var jj = 1;
var max = 100;
//var xxs = bind for (i in [0..10] )  i; //jfxc-729 this simple form does not compile, also variations
//var xxxs = bind for (ii in [ 0..max step 1]) ii;    //this get different missing symbol
var xxs = bind for (i in [0..max step jj] where i < max )  i;
  jj = 5;
  System.out.println(xxs);
  jj = 8;
  System.out.println(xxs);
}

/** simple nonrecursive functions*/
function summ( seq : Integer[]):Integer {
	var sum = 0;
	for (num in seq) {sum = sum + num;}
	return sum;
}

function factorial( num: Integer ):Integer {
	var prod = 1;
	for ( f in [1..num] ) {prod = prod*f;}
	return prod;
}

function test4() {
System.out.println("test4: bind to sequence defined by function"); //ie. function binding
//check functions
 for( n in [1..10]) System.out.println("{n}!= {formatNumber( factorial(n),1,0) } "); 
 System.out.println("Sum 1..10  : { formatNumber(  summ( [1..10]),1,0)}");  //check functions
 System.out.println("Sum 20..30: { formatNumber(  summ( [20..30]),1,0)}");  //check functions

for (i in [1..10  ] where i > 5 )  System.out.println("Sum 1..{i}: : { formatNumber(  summ( [1..i]),1,0)}");
//jfxc-107 Binding to function arguments fails
//I assume this is why this does not work, it compiles but functions incorrectly
var x = bind for (i in [1..10  ] where i < 10 )  summ( [0..i] ) ;
System.out.println(  x ); //prints [ 1, 1, 1, 1, 1..., 1 ] ???
}

function test5() {
	System.out.println("test5: bind readonly attributes, series of binds to attributes");
var C = new circles;
System.out.println("RAD     DIA   CIRCUM  AREA");
System.out.println("----    ----  ------  -------");
for( cycles in [1..5]) 
{
   for ( rad in [ 1 .. 4 ] ) {    C.setRadius(rad);  }
   for ( rad in [ 5 .. 2 step -1 ] ) {    C.setRadius(rad);  }
}
}

/** lazy bind */
function test6() {
System.out.println("test6: simple lazy bind");
 var x1 = X {
      a: 1
      b: 2   // X.b is now 2 is printed
      c: 3   // X.c is now 3 is printed
 };

 var x2 = X {
      a:  x1.a       // eager, non-incremental
      b:  bind x1.b // eager, incremental (X.b is now 2 is printed)
      c:  bind lazy x1.c  // lazy, incremental (nothing is printed yet)
};

System.out.println(x2.a); // prints 1
System.out.println(x2.b); // prints 2
System.out.println(x2.c); // prints X.c is now 3, then prints 3
x1.a = 5;
x1.b = 5; // prints X.b is now 5, twice
x1.c = 5; // prints X.c is now 5, twice
System.out.println(x2.a); // prints 1
System.out.println(x2.b); // prints 5
System.out.println(x2.c); // prints 5
}

/** to other various loops */
function test7() {}


}

class circles {
    readonly attribute pi : Number = 3.14157;
    attribute radius : Number = 1;
	 private readonly attribute MID:Integer=2;
    readonly attribute diameter:Number = bind radius * 2;
    readonly attribute circumference : Number = bind diameter * pi;
    readonly attribute area : Number =  bind (radius)*(radius)*pi;
	 private attribute df:DecimalFormat = new DecimalFormat("###,###,######.##");
    init {		 System.out.println("test5: multiple binds to attribute");	 }
	 function formatNumber(n:Number):String {
		 df.setMinimumIntegerDigits(MID);
		 df.setMinimumFractionDigits(2);
        return df.format(n);
    }

    function setRadius( r : Number ) {
            radius = r;
            System.out.println("{formatNumber(radius)}  {formatNumber(diameter)}  {formatNumber(circumference)}  {formatNumber(area)}");
    }
}


 class X {
       attribute a: Number;
       attribute b: Number on replace (old) {    System.out.println("X.b is now {b}");   };
       attribute c: Number on replace (old) {    System.out.println("X.c is now {c}");    };
 }


var b = new binder;
b.test1();
b.test2();
b.test3();
b.test4();
b.test5();
b.test6();
