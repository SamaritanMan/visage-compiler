/*
 * Timeline_TS006_01.fx
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

function runLater(ms: Number, f: function(): Void): Void {
    Timeline {
        keyFrames: KeyFrame {
            time: Duration.valueOf(ms)
            action: f
        }
    }.play();
} 


// Scenario 1
var count: Integer = 0;
var t1 : Timeline = Timeline {
    keyFrames: [
        KeyFrame {
            time: 0ms
            action: function() {
				//System.out.println("count = {count}");
				count++;
            }
        },
		KeyFrame {
            time: 100ms
            action: function() {
				//System.out.println("count = {count}");
				count++;
            }
        },
		KeyFrame {
            time: 200ms
            action: function() {
				//System.out.println("count = {count}");
				count++;
            }
        }
	]
};

// Scenario 2
var myFrames = [
	KeyFrame {
		time: 0ms
		action: function() {
			//System.out.println("count = {count++}");
		}
	},
	KeyFrame {
		time: 100ms
		action: function() {
			//System.out.println("count = {count++}");
		}
	},
	KeyFrame {
		time: 200ms
		action: function() {
			//System.out.println("count = {count++}");
		}
	}
];

var t2 : Timeline = Timeline {
    keyFrames: myFrames
};

// Scenario 3
var t3 : Timeline = Timeline {
    keyFrames: for (image in [1..3])
        KeyFrame {
            time: 100ms * indexof image
            action: function() {
				//System.out.println("image = {indexof image}");
            }
        }
};

t1.play();
runLater(1000, f1);

function f1() {
	count = 0;
	t2.play();
	runLater(1000, f2);
}

function f2() {
	t3.play();
}
