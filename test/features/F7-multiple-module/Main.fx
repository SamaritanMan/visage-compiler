/*
 * Feature test #7 -- multiple modules
 *
 * @test
 * @compile Moo.fx
 * @compile ModuleTestA.fx
 * @run
 */

var a1 = ModuleTestA {
    vs: "Ramparts"
    vi: 1956
    vn: 3.125
    vb: true
};

println("{ a1.vs } { a1.vi } { a1.vn } { a1.vb }");

a1.vs = "Bulwark";
a1.vi = 7654321;
a1.vn = 2.0;
a1.vb = false;

println("{ a1.vs } { a1.vi } { a1.vn } { a1.vb }");

var ls = "Fabrication";
var l1 = 2007;
var ln = 0.125;
var lb = false;

var a2 = ModuleTestA {
    vs: bind ls
    vi: bind l1
    vn: bind ln
    vb: bind lb
};

println("{ a2.vs } { a2.vi } { a2.vn } { a2.vb }");

ls = "Recycling";
l1 = 1906;
ln = -1.5;
lb = true;

println("{ a2.vs } { a2.vi } { a2.vn } { a2.vb }");

var b = new ModuleTestA.ModuleTestB;

println(b.bar);
 
var m = Moo { x: 1, y: 2 };
m.mooprintln();
