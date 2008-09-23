/*
 *
 * JFXC-1250 : Disallow more than one of public, protected, package
 * @test/compile-error
 *
 */

import java.lang.System;


public class AccessModifiersTest2{
   package protected var months=["Jan","Feb","Mar",
   				"Apr","May","Jun","Jul",
   				"Aug","Sept","Oct","Nov",
   				"Dec"];
   
   package public var a:Integer=13;
   public-init public-read var b:Integer=15;
   package public var s:String="Howdy folks";
   public-read public-init var greet:String="Good Morning friends";
   public-init var fp:function(:Number,:Integer):java.lang.Double;

   public-init def max=100;
   public public-read def min=10;
   
}