/*
 * Timeline_TS002_02.fx

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
import java.awt.event.*;

var keepalive: Timeline = Timeline {
    repeatCount: Timeline.INDEFINITE
    keyFrames: [
        KeyFrame {
            time: 1000ms
        }
    ]
}
keepalive.play();

var images = [1..10];
var golden = [0..9];
var id: Integer = 0;

var t : Timeline = Timeline {
    autoReverse: true
    repeatCount: 1
    keyFrames: for (image in images)
        KeyFrame {
            time: 100ms * indexof image
            action: function() {
				//System.out.println("=> {indexof image}");

				// check: The reserve shouldn't happen in the 2nd animation run.  
                if(indexof image != golden[id++]) {
                    t.stop();
					keepalive.stop();
					throw new AssertionError("autoReverse test failed");
                }
            }
        }
}

function runLater(ms: Number, f: function(): Void): Void {
    Timeline {
        keyFrames: KeyFrame {
            time: Duration.valueOf(ms)
            action: f
        }
    }.play();
}

//System.out.println("1st run");
t.play();

// Anim runs 1s. Now let's stop it before it reaches the last frame.
runLater(500, runAgain);	

function runAgain() {	
	if(t.running) {
		t.stop();
	}
	
	id = 0;
	//System.out.println("2nd run");
	t.play();
	runLater(2000, stop);
}

function stop() {
	if(t.running) {
		t.stop();
	}

	if(keepalive.running) {
		keepalive.stop();
	}
}

