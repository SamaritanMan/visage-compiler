/*
 * JFXC-4136 : bind-expression fails when object literal with variable initializers is inside if-condition
 *
 * @test
 * @run
 */

class Y {
    var i:Integer;
    override function toString() { "Y{i}" }
}

var boolvar = false;
var y0 = Y { i: 3 };

var y1 = bind if (boolvar) y0 else null;
var y2 = bind if (boolvar) Y { i: 7 } else null;
var y3 = bind if (boolvar) null else y0;
var y4 = bind if (boolvar) null else Y { i: 11 };
var y5 = bind if (boolvar) Y { } else null;
var y6 = bind if (boolvar) null else Y { };

function p(x) { println("{x}: y1={y1} y2={y2} y3={y3} y4={y4} y5={y5} y6={y6}") }

p(1);
boolvar = true;
p(2);
boolvar = false;
p(3);
boolvar = true;
p(4);
