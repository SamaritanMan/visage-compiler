/**
 * JFXC-3692 : Compiled bind: Can't find the receiver of a mixin var: AssertionError: Cannot find owner
 *
 * @test
 */

mixin class jj { 
   public var jjint: Integer; 
} 

bound function f1() { 
    getjj().jjint 
} 

function getjj(): jj { 
   null; 
} 
