/**
 * JFXC-4908 : Internal Compiler error , non-static method invalidate$$x$ol$1$$(int) cannot be referenced from a static context
 *
 * @test
 * @compilefirst Main.fx
 */

public var kk = bind Main.stage.width; 
public class MyNode { 
    public var jj = bind Main.stage.width; 
} 

