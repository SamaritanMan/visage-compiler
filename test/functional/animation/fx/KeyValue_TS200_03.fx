/*
 * KeyValue_TS200_03.fx
 *
 * @test
 * @run
 */

/**
 * @author Baechul Kim
 */

import javafx.animation.*;
import javafx.lang.Duration;
import java.lang.System;
import java.lang.Thread;
import java.lang.AssertionError;
import java.lang.Throwable;
import java.util.concurrent.TimeUnit;
import com.sun.javafx.runtime.Pointer;


class XYZ {
    public var s:String = "abc";
    public var i:Integer = 0;
    public var b:Boolean = true;
    public var n:Number = 1.0;
    public var d:Duration = 1s;
}

var xyz = XYZ{};

var bps = bind Pointer.make(xyz, "s"); 
var ps = bps.unwrap();

var bpi = bind Pointer.make(xyz, "i"); 
var pi = bpi.unwrap();

var bpb = bind Pointer.make(xyz, "b"); 
var pb = bpb.unwrap();

var bpn = bind Pointer.make(xyz, "n"); 
var pn = bpn.unwrap();

var bpd = bind Pointer.make(xyz, "d"); 
var pd = bpd.unwrap();

var t : Timeline = Timeline {
    keyFrames: [		
		KeyFrame {
			time: 1s
			values: [
				KeyValue {
					target: ps 
					value: function() { "def" }
				},
				KeyValue {
					target: pi 
					value: function() { 10 }
				},
				KeyValue {
					target: pb
					value: function() { false }
				},
				KeyValue {
					target: pn 
					value: function() { 10.0 }
				},
				KeyValue {
					target: pd 
					value: function() { 10s }
				}
			]
			action: function() {
				//System.out.println("<After>");
				//System.out.println("String: {xyz.s}");
				//System.out.println("Integer: {xyz.i}");
				//System.out.println("Boolean: {xyz.b}");
				//System.out.println("Number: {xyz.n}");
				//System.out.println("Duration: {xyz.d}");
				
				if(xyz.s != "def") {
					throw new AssertionError("test failed: String");
				}
				if(xyz.i != 10) {
					throw new AssertionError("test failed: Integer");
				}
				if(xyz.b != false) {
					throw new AssertionError("test failed: Boolean");
				}
				if(xyz.n != 10.0) {
					throw new AssertionError("test failed: Number");
				}
				if(xyz.d != 10s) {
					throw new AssertionError("test failed: Duration");
				}
			}
		}
	]
}

//System.out.println("<Before>");
//System.out.println("String: {xyz.s}");
//System.out.println("Integer: {xyz.i}");
//System.out.println("Boolean: {xyz.b}");
//System.out.println("Number: {xyz.n}");
//System.out.println("Duration: {xyz.d}\n");
t.play();
