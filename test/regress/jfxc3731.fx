/**
 * JFXC-3731 : compiled-bind: self-reference in object literal with local bind loops endlessly
 *
 * @test
 * @run
 */

class C {
    var a : Number;
    var b : Number = 1;
}

function foo() {
    var c: C = C {
       override var a = bind c.b on replace {}
    };
}
println(foo().a);
