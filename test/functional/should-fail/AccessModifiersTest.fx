/*
 *
 * This is a negative test for access modifiers. It basically tests the following conditions
 *
 * For subclasses outside the package, the protected member can be
 * accessed only through inheritance.
 *
 * package members can't be accessed from classes outside the package
 * Members without modifiers have default (script-only) access
 * public-init member has script-only write access
 * public-read member has script-only write access
 * Functions can't be declared public-init or public-read
 * Local variables can't have access modifiers
 *
 *
 * @compilefirst pack1/AccessData.fx
 * @test/compile-error
 *
 *
 */


import java.lang.System;
import pack1.AccessData;

public class AccessModifiersTest extends AccessData
{
	var fullMonth=["January","February"];
	var x2:String;
	public function multiply(a1:Number,a2:Integer):java.lang.Double{
	  public-read var result=a1*a2;
	  return result;
        }

	public-read function subtract(a1:Number,a2:Integer):java.lang.Double{
	  a1-a2;
        }
 }

 var sub:AccessModifiersTest=AccessModifiersTest{
  	months:["JANUARY","FEBRUARY"];
  	s:"Hi there !";
  	a:35;
	b:43;
	greet:"readonly";

 };


 function run(){
	 sub.a=27;
         sub.b=29;
	 var sup=new AccessData;

	System.out.println(sub.months);
	System.out.println(sub.s);
	System.out.println(sub.x());
	System.out.println(sub.b);
        System.out.println(sub.max);
        System.out.println(sub.greet);
	var seq1:Integer[]=[30..40];
	System.out.println(sub.flip(seq1));
	var nonBindF=sub.fn()();
	var nonBindG=sub.g(10);
	var bindF=bind sub.fn()();
	System.out.println(sub.x());

	System.out.println("nonBindF = {nonBindF}");
	System.out.println("nonBindG = {nonBindG}");
	System.out.println("bindF = {bindF}");
	System.out.println("bindG = {bindG}");

	System.out.println(sup.months); // months has package access
	System.out.println(sup.s);
	System.out.println(sup.greet);
	System.out.println(sup.fn());
	var seq2:Integer[]=[70..80];
	System.out.println(sup.flip(seq2));
	System.out.println(sup.x()); //  x has package access
	}
