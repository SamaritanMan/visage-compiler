/*
 *
 * Regression: JFXC-3910 - Controls at the bottom are missing and the images aren't in the correct positions; regression from alpha1.
 *
 * @test
 * @run
 *
 */
 
class A { 
   var x = 50; 
} 

function validateAndSum() { 
   var sum = 0; 
   println("Validate and sum all a.x"); 
   for (a in aSeq) sum += a.x; 
   x = sum; 
} 

var x = 0 on replace { println("x = {x}"); }

var y = bind x on replace { println("x = {y}"); }

var a = A { x : bind y - 30 }; 

var aSeq = [a] on replace { validateAndSum(); };

println("start"); 

insert A{} into aSeq; 

println(a.x); 

