/*
 * Regression : JFXC-4341 -  Function called twice.
 *
 * @test
 * @run
 *
 */

class A {
    public var l:String[];
};

function dup(n:String):String[] {
    println("called");
    [n, n, n, n, n, n, n, n]
}

var a = A{};

var n:String on replace { 
        a.l=dup(n); 
    }; 
