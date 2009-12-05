/**
 * Regression test JFXC-3772 : Samples: .IllegalArgumentException: Children sequence: duplicate children detected: parent=Scene$Root
 *
 * @test
 * @run
 */

class A { 
   var x on replace { 
      for (elem in seq) elem.x = x; 
   } 
   def const = "const"; 
   var seq:A[] on replace { println("def const is: {const}"); } 
} 

var a = A{}; 
a.seq = A{}
