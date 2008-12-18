/*
 * Feature test for lazy bind: bind to if
 *
 * @test
 * @run
 */

var b : Boolean = true;
var x : Integer = 1
        on replace ov { println("x {ov} -> {x}") };
var y : Integer = 2
        on replace ov { println("y {ov} -> {y}") };
var z : Integer = bind lazy if (b) then x else y
        on replace ov { println("z {ov} -> {z}") };

println("Starting");
pz();
pz();
println("modify x");
x = 9;
pz();
println("modify y");
y = 3;
pz();
println("modify b");
b = false;
pz();

function pz() { println("reading z"); println("z:{z}") }
