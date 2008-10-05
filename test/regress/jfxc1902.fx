/*
 *
 * JFXC-1902: bind with inverse inside object literals doesn't sync up values
 * A bidirectional bind of two instance variables at object creation time (i.e.,inside object literal).
 * @test
 * @run
 * 
 */

import java.lang.System;

class jfxc1902{
    protected var x="uninit";
    protected var y="uninit";

    function show() {
        println("x={x}");
        println("y={y}");
        println("");
    }

    function update() {
        show();
        x = "update";
        show();
        println("-----");
    }
}

public var psbi: jfxc1902 = jfxc1902 {
     x: "psbi";
     y:bind psbi.x with inverse; 
}
 
var sbi: jfxc1902 = jfxc1902 {
     x: "sbi";
     y:bind sbi.x with inverse; 
}
 
public var psb: jfxc1902 = jfxc1902 {
     x: "psb";
     y:bind psb.x; 
}
 
var sb: jfxc1902 = jfxc1902 {
     x: "sb";
     y:bind sb.x; 
}
 
class Foo {
    public var pfbi: jfxc1902 = jfxc1902 {
     x: "pfbi";
     y:bind pfbi.x with inverse; 
    }
 
    var fbi: jfxc1902 = jfxc1902 {
     x: "fbi";
     y:bind fbi.x with inverse; 
    }
 
    public var pfb: jfxc1902 = jfxc1902 {
     x: "pfb";
     y:bind pfb.x; 
    }
 
    var fb: jfxc1902 = jfxc1902 {
     x: "fb";
     y:bind fb.x; 
    }
}

function run(args:String[]){
    psbi.update();
    sbi.update();
    psb.update();
    sb.update();

    var fo = Foo{};
    fo.pfbi.update();
    fo.fbi.update();
    fo.pfb.update();
    fo.fb.update();
} 