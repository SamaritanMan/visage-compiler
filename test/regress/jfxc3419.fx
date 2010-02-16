/**
 * JFXC-3419 : Compiler crash with bound if for function type.
 *
 * @test
 * @run
 */

class Foo { 
    var x = "hoo"; 
    public function hoo():Void { 
        println(x);
    }
}

var foo = new Foo();
def f:function():Void = bind foo.hoo; 
f();

foo = Foo { x : "hoo moo" };
f();
