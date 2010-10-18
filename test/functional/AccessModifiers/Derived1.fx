/*
 *
 * This test verifies the behavior of protected,public,public-read and public-init access modifiers:
 *
 * 1. protected members can be accessed by other classes in the same package, plus subclasses regardless of package.
 * 
 * 2. public members can be accessed(read/initialize/write) from anywhere
 *
 * 3. public-read members can be read from anywhere.
 *
 * 4. public-init can be read/initialized from anywhere, writable from within the script only.
 *
 * @test
 * @compilefirst pack1/Base1.fx
 * @run 
 * 
 */

import java.lang.System;
import pack1.Base1;

class Derived1 extends Base1
{
	var fullMonth=["January","February"];
	var x2:String;
	override protected bound function g (parameter: Integer): Integer {   
    	 return this.a + parameter; 
       };
 }
 
 var sub:Derived1=Derived1{
  	months:["JANUARY","FEBRUARY"];  	  		
  	s:"Hi there !";
  	a:35;  		
	b:83; // public-init can be initialized from anywhere
  	
 }; 
 
 
 function run(){
	 sub.a=27;
	 // sub.b=29;   //  b has script only (default) write access in pack1.Base1
	 var sup=new Base1;
 	 sub.fp=sub.add;	

	System.out.println((new Derived1).months);
	System.out.println(sub.months);
	System.out.println(sub.s);
	System.out.println(sub.x());
	System.out.println(sub.fv);
	System.out.println(sub.b);
        System.out.println(sub.max);
        System.out.println(sub.greet);
        System.out.println(sub.fp(80.0,30));
	var seq1:Integer[]=[30..40];
	System.out.println(sub.flip(seq1));
	var nonBindF=sub.fn()();
	var nonBindG=sub.g(10);
	var bindF=bind sub.fn()();
	var bindG=bind sub.g(10);
	System.out.println(sub.x());
	
	System.out.println("nonBindF = {nonBindF}");
	System.out.println("nonBindG = {nonBindG}");
	System.out.println("bindF = {bindF}");
	System.out.println("bindG = {bindG}");


	//System.out.println(sup.months); // months has protected access 
	System.out.println(sup.s);
	System.out.println(sup.greet);
	var seq2:Integer[]=[70..80];
	System.out.println(sup.flip(seq2));

	//System.out.println(sup.x()); //  x has protected access in pack2.Foo2
	}
