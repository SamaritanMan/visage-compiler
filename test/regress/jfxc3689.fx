/**
 * JFXC-3689 : Compiled bind: Compiled-bind: insert keyvalue into sequence fails.
 *
 * @test
 */
class A { 
   var seq:Object[]; 
} 

class Test { 
     var x:Number; 
     function f(a:A) { 
        def val = 1; 
insert (x => val) into a.seq; 
     } 
} 
