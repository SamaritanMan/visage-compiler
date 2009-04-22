/*
 * Testing the mixin declarations.
 *
 * @compilefirst MxDecl01Java.java 
 * @compilefirst subpackage/MxDecl01lib.fx
 * @test
 * @run
 */

import subpackage.MxDecl01lib.*;

public mixin class M1 extends MxDecl01Java.Intf1, MxDecl01Java.Intf2, MxDecl01Java.Intf3 { var s1 : String = "m1" }
package mixin class M2 { var s2 = "m2" }
protected mixin class M3 { var i3 : Integer = 3 }
mixin class M4 { var i4 = 4 }
mixin class M5 { var n5 : Number = 5.0 }
mixin class M6 { var n6 = 6.0 }
mixin class M7 { var s7 : String = "m7" }
mixin class M8 { var s8 = "m8" }

mixin class Mx extends M1, M2, M3, M4, M5, M6, M7, M8, subpackage.MxDecl01lib.M9 {}

class Mixee1 extends Mx, Super {}
class Mixee2 extends MxDecl01Java, MxDecl01Java.Intf2, Mx {}

function run() {
    testMixee1();
    testMixee2();
}

function testMixee1() {
    var m = Mixee1 {}
    printMx(m);
    println(m.s);
}

function testMixee2() {
    var m = Mixee2 {}
    printMx(m);
    println(m.j);
}

function printMx(mx : Mx) {
    println(mx.s9);
    println(mx.s8);
    println(mx.s7);
    println(mx.n6);
    println(mx.n5);
    println(mx.i4);
    println(mx.i3);
    println(mx.s2);
    println(mx.s1);
}
