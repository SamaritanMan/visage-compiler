/*
 * Regression test 
 * JFXC-3788 : compiled-bind: recursive onReplace call on bound sequence
 *
 * Original reduced case.
 * Technically, the function call is a lazy/eager issue. 
 * Until it is optimized, bound explicit sequences are eager (even in this case where it need not be)
 *  -- so this test shows this missing update.
 *
 * @test
 * @run
 */

import javafx.util.Sequences;

class Fred {
   var width: Number;
   var height: Number;
}

class Path {
   var elements :Fred[];
}

function calcX(p1: Number) {
// println("jj: calcX = {p1}");
    return p1;
}
var seen : Number[];

function calcY(p1: Number, p2: Number) {
    insert p2 into seen;
    return p2;
}

var pieRadiusX: Number = 20;
var pieRadiusY = bind pieRadiusX * .5;
var clambed: Number = 3.0;

class PP {
    var xx: Number; var yy: Number;
    var side: Path = Path {
        elements: bind [
            Fred{ width: calcX(xx+2) height: calcY(clambed, yy + 1)}
            Fred{ width: calcX(xx+4) height: calcY(clambed, yy + 3)}
        ]
    }
}

var mypp = PP{
        xx: bind pieRadiusX
        yy: bind pieRadiusY
};

var dummy : Integer;
dummy = sizeof mypp.side.elements;
println(Sequences.sort(seen));
pieRadiusX = 60;
dummy = sizeof mypp.side.elements;
println(Sequences.sort(seen));
