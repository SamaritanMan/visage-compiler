/**
 * Regression test: JFXC-3634 : Compiled bind: Compiler crash on assigning to a local var that is refd in a nested function
 *
 * @test
 * @run
 */

class Class1 {
     public var callback : function()
}
function test() : Void {
     var strx: String;
     strx = "boo";
     def inst = Class1 {
             callback: function() {
                 strx;
             }
     }
 }
test(); 
