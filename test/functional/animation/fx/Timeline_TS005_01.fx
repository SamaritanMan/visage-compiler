/*
 * Timeline_TS005_01.fx
 * 
 * @ test  disabled: see RT-3743 
 * @ run  disabled
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

var count: Integer = 0;

var t: Timeline = Timeline {
    repeatCount: 2
    keyFrames: [
        KeyFrame {
            time: 100ms
			action: function() {
				count++;
				//System.out.println(".count = {count}");
				if(count == 1) {
					t.repeatCount = Timeline.INDEFINITE;
				}
			}
        }
    ]
};

function runLater(ms: Number, f: function(): Void): Void {
    Timeline {
        keyFrames: KeyFrame {
            time: Duration.valueOf(ms)
            action: f
        }
    }.play();
} 

function check() {
	if(not t.running) {
		throw new AssertionError("stop test failed");
	} else {
		t.stop();
	}
}

t.play();
runLater(3000, check);
