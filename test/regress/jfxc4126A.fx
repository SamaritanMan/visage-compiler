/*
 * Regression: JFXC-4126 - Compiled bind optimization: construct a single invoke$ method per class to handle function.
 *
 * @test
 * @run
 *
 */

mixin class M {
    public var x5 = function(x:Integer, y:Integer):Integer { 5 }
    public var y5 = x5(10, 20);
    
    public var x6 = function(x:Number, y:Number):Number { 6 }
    public var y6 = x6(10, 20);
}

class A {
    public var x1 = function(x:Integer, y:Integer):Integer { 1 }
    public var y1 = x1(10, 20);
    
    public var x2 = function(x:Number, y:Number):Number { 2 }
    public var y2 = x2(10, 20);
}


class B extends A, M {
    public var x3 = function(x:Integer, y:Integer):Integer { 3 }
    public var y3 = x3(10, 20);
    
    public var x4 = function(x:Number, y:Number):Number { 4 }
    public var y4 = x4(10, 20);
}

var b = B{};

println("{b.x1} {b.x2} {b.x3} {b.x4} {b.x5} {b.x6}");
println("{b.y1} {b.y2} {b.y3} {b.y4} {b.y5} {b.y6}");
