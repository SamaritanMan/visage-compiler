/**
 * regression test for JFXC119:  support of local variable triggers
 * @test
 * @run
 */

import java.lang.System;

{
    var a: Integer
	on replace { System.out.println("a: =>newV={a}")};
    var x : Integer
        on replace = newV { System.out.println("x: =>newV={x}"); };
    var y : Integer
        on replace oldV = newV { System.out.println("y: => oldV = {oldV}, newV={y}"); };
    var z: Integer
	on replace oldV {System.out.println("z: => oldV = {oldV}")};
    var zz : String = "Ralph" on replace { System.out.println("zz: = {zz}"); }


a = 1;
x = 2;
y = 3;
z = 4;
zz = "Laura";

var seq = [1..10]   
        on replace oldValue[low..high]  = newValue{ System.out.println("seq: lowIndex = {low} highIndex = {high} oldValue => {oldValue} newValue = {newValue}"); };

seq[2] = 0;
seq[4] = 0;
insert 11 into seq;
delete seq[0];
}
