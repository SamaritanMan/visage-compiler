/*
 * JFXC-3916: Compiled-bind: new compiler regression when compiling controls
 *
 * @test
 */

class Foo {
   var m;
}

class A {
    var y:function(x:Number):String;
    function f() {
        var foo:Foo;
        foo = Foo {
            m: bind if (y != null) y(1) else null;
        }
    }
}
