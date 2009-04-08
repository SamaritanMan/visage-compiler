/**
 * Regression test for JFXC-2296 
 *
 *    Compiler fails to infer return type if 
 *    last statment is a bind expression.
 *
 * @test
 * @run
 */

// original test submitted in issue report
function f():function():String {
    var x= bind function (){
     "returning function value";
   }
} 

println(f()());

// the same with simpler return type
var y = 32;
 
function func():Integer {
   var x = bind y*2;
}

println(func());
