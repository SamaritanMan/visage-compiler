/*
 * KeyValue_TS200_02.fx
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

var s:String = "abc";
var bps = bind Pointer.make(KeyValue_TS200_02.class, "s"); 
var ps = bps.unwrap();

var i:Integer = 0;
var bpi = bind Pointer.make(KeyValue_TS200_02.class, "i");
var pi = bpi.unwrap();

var b:Boolean = true;
var bpb = bind Pointer.make(KeyValue_TS200_02.class, "b");
var pb = bpb.unwrap();

var n: Number = 1.0; 
var bpn = bind Pointer.make(KeyValue_TS200_02.class, "n"); 
var pn = bpn.unwrap();

var d:Duration = 1s;
var bpd = bind Pointer.make(KeyValue_TS200_02.class, "d"); 
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
				////System.out.println("After:");
				////System.out.println("String: {s}");
				////System.out.println("Integer: {i}");
				////System.out.println("Boolean: {b}");
				////System.out.println("Number: {n}");
				////System.out.println("Duration: {d}");
				
				if(s != "def") {
					throw new AssertionError("test failed: String");
				}
				if(i != 10) {
					throw new AssertionError("test failed: Integer");
				}
				if(b != false) {
					throw new AssertionError("test failed: Boolean");
				}
				if(n != 10.0) {
					throw new AssertionError("test failed: Number");
				}
				if(d != 10s) {
					throw new AssertionError("test failed: Duration");
				}
			}
		}
	]
}

////System.out.println("Before:");
////System.out.println("String: {s}");
////System.out.println("Integer: {i}");
////System.out.println("Boolean: {b}");
////System.out.println("Number: {n}");
////System.out.println("Duration: {d}\n");
t.play();

