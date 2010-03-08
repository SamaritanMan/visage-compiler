/**
 * JFXC-3615 :  No exception when writing to a bidirectionally bound variable whose bindee is bound to an expression
 *
 * @test
 * @run
 */

import com.sun.javafx.runtime.AssignToBoundException;

println("");
println("----------------------------------------------------");

var x1: Number;
var a1: Number = bind x1 on replace { println("trigger: a1={a1}"); }

x1 = 10;

println("try to write to bound var");
try {
    a1 = 5;
    println("*** FAILURE: Did not catch the expected exception");
    if (a1 == 5) println("*** ERROR: a1={a1}  x1={x1}");
} catch (e:AssignToBoundException) {
    println("SUCCESS: {e}");
}

///////////////////////////////////////////////////////////////////////////

println("");
println("----------------------------------------------------");

var x2: Number[];
var a2: Number[] = bind x2 on replace { println("trigger: a2={a2}"); }

x2 = [10, 11];

println("try to write to bound seq var");
try {
    delete 10 from a2;
    println("*** FAILURE: Did not catch the expected exception");
    if (a2[0] == 11) println("*** ERROR: a2={a2}  x2={x2}");
} catch (e:AssignToBoundException) {
    println("SUCCESS: {e}");
}

///////////////////////////////////////////////////////////////////////////

println("");
println("----------------------------------------------------");

def y3: Number = 10;
var a3: Number = bind x3 with inverse on replace { println("trigger: a3={a3}"); }
var x3: Number = bind y3 on replace { println("trigger: x3={x3}"); }

println("try to write to doubly bound var");
try {
    a3 = 5;
    println("*** FAILURE: Did not catch the expected exception");
    if (a3 == 5) println("*** ERROR: a3={a3}  x3={x3}  y3={y3}");
} catch (e:AssignToBoundException) {
    println("SUCCESS: {e}");
}

///////////////////////////////////////////////////////////////////////////

println("");
println("----------------------------------------------------");

def y4: Number[] = [10, 11];
var a4: Number[] = bind x4 with inverse on replace { println("trigger: a4={a4}"); }
var x4: Number[] = bind y4 on replace { println("trigger: x4={x4}"); }

println("try to write to bound seq var");
try {
    delete 10 from a4;
    println("*** FAILURE: Did not catch the expected exception");
    if (a4[0] == 11) println("*** ERROR: a4={a4}  x4={x4}  y4={y4}");
} catch (e:AssignToBoundException) {
    println("SUCCESS: {e}");
}
