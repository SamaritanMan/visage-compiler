/*
 * Interpolator_TS304_02.fx
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
import com.sun.javafx.runtime.PointerFactory;

function runLater(ms: Number, f: function(): Void): Void {
	var timer = new Timer(ms, ActionListener {
		public function actionPerformed(e: ActionEvent) {
			f();
		}
	});
	timer.setRepeats(false);
	timer.start();
}

var keepAlive : Timeline = Timeline {
	repeatCount: Timeline.INDEFINITE
    keyFrames: KeyFrame {
		time: 100ms
	}
};

var count: Integer = 0;
var pf: PointerFactory = PointerFactory {};
var d: Duration = 0s on replace old = newValue {
	//System.out.println("{old} => {newValue}");
	count++;
}
var bpd = bind pf.make(d); 
var pd = bpd.unwrap();

var keyValue = KeyValue {
	target: pd
	value: 100s
}

var t = Timeline {
    keyFrames: [
		KeyFrame {
            time: 1s
            values: keyValue
        }
   ]
}

//System.out.println("\nInterpolator.LINEAR:");
keyValue.interpolate = Interpolator.LINEAR;
keepAlive.start();
t.start();
runLater(2000, rerun1);

function rerun1() {	
	//System.out.println("count = {count}");
	if(count != 2) {
		//throw new AssertionError("test failed");
	}
	//System.out.println("\nInterpolator.EASEIN:");
	keyValue.interpolate = Interpolator.EASEIN;
	count = 0;
	t.start();
	runLater(2000, rerun2);
}

function rerun2() {
	//System.out.println("count = {count}");
        end();
	//System.out.println("\nInterpolator.EASEOUT:");
	keyValue.interpolate = Interpolator.EASEOUT;
	count = 0;
	t.start();
	runLater(2000, rerun3);
}

function rerun3() {
	//System.out.println("count = {count}");
        end();
	//System.out.println("\nInterpolator.EASEBOTH:");
	keyValue.interpolate = Interpolator.EASEBOTH;
	count = 0;
	t.start();
	runLater(2000, rerun4);
}

function rerun4() {
	//System.out.println("count = {count}");
        end();
	//System.out.println("\nInterpolator.DISCRETE:");
	keyValue.interpolate = Interpolator.DISCRETE;
	count = 0;
	t.start();
	runLater(2000, end);
}

function end() {
	keepAlive.stop();
	//System.out.println("count = {count}");
	if(count != 2) {
		throw new AssertionError("test failed");
	}
}