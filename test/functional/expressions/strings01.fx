import java.lang.System;
import java.util.Date;
/*
 * @test
 * @compilefirst ../TestUtils.fx
 * @run
 */

var TU = new TestUtils;
function print(msg:String) { System.out.println( msg ); }
/*
 * String Literals and String Expressions
 * 
 * Basic string declarations "specified" in language reference.
 */

// A literal character string is specified with single quotes, e.g.
var s1 = 'Hello';
TU.checkB(s1 instanceof String,"Single quote is a String");
TU.checkS(s1,"Hello","A literal character string is specified with single quotes");
// or with double quotes:
var s2 = "Hello";
TU.checkB(s2 instanceof String,"Double quote is a String");
TU.checkS(s2,"Hello","A literal character string can be specified with double quotes");

//In the latter case, expressions may be embedded using {}, e.g
var name = 'Joe';
var comma = ',';
var s3 = "Hello{comma} {name}"; // s = 'Hello Joe'
TU.checkS(s3,"Hello, Joe","expressions may be embedded using \{\}");

//    * For this to work, the compiler infers a type for the 
//      embedded expression that it can coerce to a String.
TU.checkB(s3 instanceof String,"embedded string vars are  \"coerced\" to type String");

//    * The embedded expression may itself contain quoted strings (which, 
//      in turn, may contain further embedded expressions), e.g
var answer = true;
var s4 = "The answer is {if (answer) "Yes" else "No"}"; // s = 'The answer is Yes'
TU.checkS(s4,"The answer is Yes","may contain quoted strings (which may contain further embedded expressions");
var i:Integer = 101;
TU.checkB("{i} Dalmations" instanceof String, "embedded integers coerced to String");
TU.checkS("{i} Dalmations", "101 Dalmations","Integer var embedded in String becomes part of String");
//    * Just like the Java programming language, double-quoted String literals cannot contain newlines
//      but can contain escapes.

var s5 = "This\ncontains\nnew lines";
System.out.println(s5);

var s6 = "Hello";
var s7 = ", ";
var s8 = "World!";
var hw = "{s6}{s7}{s8}";
TU.checkS(hw,"Hello, World!","string var can be concatentated with +");
//TU.checkI(i,5,"strings are java strings");


hw = "Hello, World!";
TU.checkS(hw,"Hello, World!","string literals can be concatentated with +");

var EXCL = "!";
hw = "{s6}{comma} World{EXCL}";
TU.checkS(hw,"Hello, World!","concatenations and coercions can be mixed freely");

/*
 * String vars are mostly java.lang.String
 */
var str1 = 'abc';
var str2 = "abc";
TU.checkB( str2.compareTo(str1)==0, "eq equality with compareTo()" );
str1 = 'ab';
TU.checkB( str2.compareTo(str1)>0, "gt equality with compareTo()" );
str1 = 'abcd';
TU.checkB( str2.compareTo(str1)<0, "lt equality with compareTo()" );

/*
 * some String methods may not return what you expect, or you may 
 * not know exactly what to expect.
 */
var str2Bytes = str2.getBytes();
var ibytes = [97,98,99];
TU.checkIs( ibytes,str2Bytes,"compare return of getBytes with Integer[]");

/* Random String methods:
 * endsWith(), toUpperCase(), trim()
 */
var poem = "Mary had a little lamb";
var dog = "dog";
var cat = "   cat   ";
TU.checkB ( poem.endsWith("lamb"), "String.endsWith(); true case");
TU.checkB ( poem.endsWith("dog")==false, "String.endsWith(); false case");
/* String.toUpperCase() */
TU.checkS( dog.toUpperCase(),"DOG","String.toUpperCase()");
TU.checkS( cat.trim(), "cat","String.trim()");

function removewhitespace( s:String):String {
  var news = "";
  var seq = s.split(' ');
  for ( l in seq ){     news = "{news}{l}";}
 return news;
}
TU.checkS(removewhitespace(poem),"Maryhadalittlelamb", "String.split method returns String array, not a String sequence");

/* An oddity about FX is that literals, such as strings, are also objects */
var literal_to_seq = "In FX literals are also objects".split(' ');
TU.checkSs( literal_to_seq, [ "In", "FX", "literals", "are", "also", "objects" ], "Literals are objects");

TU.report();
