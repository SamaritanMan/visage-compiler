/*
 * KeyValue_TS200_04.fx
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
import com.sun.javafx.runtime.PointerFactory;


class XYZ {
    public var s:String = "abc";
    public var i:Integer = 0;
    public var b:Boolean = true;
    public var n:Number = 1.0;
    public var d:Duration = 1s;
}

var xyz = XYZ{};

var pf: PointerFactory = PointerFactory {};
var bps = bind pf.make(xyz.s); 
var ps = bps.unwrap();

var bpi = bind pf.make(xyz.i); 
var pi = bpi.unwrap();

var bpb = bind pf.make(xyz.b); 
var pb = bpb.unwrap();

var bpn = bind pf.make(xyz.n); 
var pn = bpn.unwrap();

var bpd = bind pf.make(xyz.d); 
var pd = bpd.unwrap();

var t : Timeline = Timeline {
    keyFrames: [		
		KeyFrame {
			time: 1s
			values: [
				KeyValue {
					target: ps 
					value: function() { "def" }
					interpolate: Interpolator.DISCRETE
				},
				KeyValue {
					target: pi 
					value: function() { 10 }
					interpolate: Interpolator.EASEBOTH
				},
				KeyValue {
					target: pb
					value: function() { false }
					interpolate: Interpolator.EASEIN
				},
				KeyValue {
					target: pn 
					value: function() { 10.0 }
					interpolate: Interpolator.EASEOUT
				},
				KeyValue {
					target: pd 
					value: function() { 10s }
					interpolate: Interpolator.LINEAR
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
