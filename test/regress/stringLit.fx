/*
 * @test
 * @run
 */
import java.lang.System;

var golda = "Ten10done";
var a1 = "Ten{ 5 + 5 }done";
var a2 = 'Ten{ 5 + 5 }done';
System.out.println(a1);

var goldb = "Two2Three3.0done";
var b1 = "Two{1 + 1 }Three{ 1.5 *2}done";
var b2 = 'Two{1 + 1 }Three{ 1.5 *2}done';
System.out.println(b1);

var goldc = "Welcome to CAFE JavaFX";
var c1 = "Welcome to { %X ((12*16 + 10)*16 + 15)*16 + 14 } JavaFX";
var c2 = 'Welcome to { %X ((12*16 + 10)*16 + 15)*16 + 14 } JavaFX';
System.out.println(c1);

var ok =
	a1.equals(golda) and 
	a2.equals(golda) and 
	b1.equals(goldb) and 
	b2.equals(goldb) and
	c1.equals(goldc) and 
	c2.equals(goldc) ;

System.out.println(if (ok) "PASS!" else "FAIL!");
	
