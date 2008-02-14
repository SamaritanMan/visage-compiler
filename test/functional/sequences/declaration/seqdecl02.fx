import java.lang.System;
import java.lang.Exception;

/**
  * nested sequences are flattened.
 *
 * @test
 * @run
 */

var week_days = ["Mon","Tue","Wed","Thur","Fri"];
var weekend_days = ["Sat","Sun"];
var days = [week_days, weekend_days];
 System.out.println( days );
for(i in [5..6]) {
 System.out.println( "day {i}: {days[i]}");
}

var fib1=                         [1];
var fib2=                  [fib1[0],fib1[0]];
var fib3 =        [ fib2[0], fib2[0]+fib2[1], fib2[1] ] ;
var fib4 = [ fib3[0], fib3[0]+fib3[1], fib3[1]+fib3[2], fib3[2] ] ;
var fibs =[ fib1, fib2, fib3, fib4 ] ;
System.out.println(fibs);
System.out.println("You may expect to see the usual Fibonacci tree, but...");
try{
   for (i in [0..sizeof fibs]){   System.out.println("{i}:  {fibs[i]}");}
} catch( e: Exception )  {
 System.out.println("...fx flattens our sequences of sequences");
}

