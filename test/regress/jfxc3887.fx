/*
 * Regression: JFXC-3887 - var bound to overridden var in superclass is initialized too early.
 *
 * @test
 * @run
 *
 */

class SuperClass { 
   var a1 = 1 on replace {println("a1 = {a1}")}; 
} 

class sub1 extends SuperClass { 
   var b1 = 2 on replace { println("b1 = {b1}");} 
   var b2 = b1 on replace { println("b2 = {b2}");} 

   override var a1 = bind b2; 
} 
var i1 = sub1 {b1: 89}; 
