/*
 * Optimization: JFXC-4001 - Compiled bind optimization: Enumerate dependencies to speed up large update$.'
 *
 * @test
 * @run
 *
 */

var a;

mixin class M {
    var b = bind a on replace { println("b from M"); }
}

mixin class N extends M {
    var c = bind a on replace { println("c from N"); }
}

class A extends M {
    var d = bind a on replace { println("d from A"); }
}

class B extends A {
    var e = bind a on replace { println("e from B"); }
}

class C extends N {
    var f = bind a on replace { println("f from C"); }
}

mixin class O {
    var g = bind a on replace { println("g from O"); }
}

class D extends B, O {
    var h = bind a on replace { println("h from D"); }
}

println("begin");

println("iA = A");
var iA = A{};
println("iB = B");
var iB = B{};
println("iC = C");
var iC = C{};
println("iD = D");
var iD = D{};

println("a = 10");
a = 10;
println("a = 20");
a = 20;
println("a = 30");
a = 30;
println("done");




