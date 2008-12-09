/*
 * SimpleInterpolator_TS401_01.fx
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
import javax.swing.Timer;
import java.awt.event.*;
import java.util.concurrent.TimeUnit;
import com.sun.scenario.animation.Interpolators;
import java.lang.Object;

class GoldenData extends Interpolatable {
	public var value: Number = 0;

	override function ofTheWay(endVal: Object, t: Number): Object {
		var ev = endVal as GoldenData;
		GoldenData {
			value: value + (ev.value - value) * t;
		}
	}

	override function toString(): String {
		 "{value}";
	}
}

class MyInterpolator extends SimpleInterpolator {
	public var i:com.sun.scenario.animation.Interpolator 
		= Interpolators.getEasingInstance(0.25 as Float, 0.34 as Float);
	
	override function curve(t: Number): Number {
		i.interpolate(t.floatValue())
	}
}

var myInterpolator = MyInterpolator{};

var start1 = GoldenData {value: 0}
var end1 = GoldenData {value: 10}
var golden = myInterpolator.interpolate(start1, end1, 0.5);
//System.out.println("golden = {golden}");

var start2 = 0.0;
var end2 = 10.0;
var out = myInterpolator.interpolate(start2, end2, 0.5);
//System.out.println("out = {out}");

if(out != (golden as GoldenData).value) {
	throw new AssertionError("test failed.");
}

