/*
 * Timeline_TS008_01.fx
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

var golden: Integer[] = [0..1500 step 500];
var out: Integer[];

var t1 : Timeline = Timeline {
    keyFrames: [        
		KeyFrame {
            time: 500ms
            action: function() {
				//System.out.println("time = 500ms");
				insert 500 into out;
            }
        },
		KeyFrame {
            time: 1500ms
            action: function() {
				//System.out.println("time = 1500ms");
				insert 1500 into out;
            }
        },
		KeyFrame {
            time: 1000ms
            action: function() {
				//System.out.println("time = 1000ms");
				insert 1000 into out;
            }
        },
		KeyFrame {
            time: 0ms
            action: function() {
				//System.out.println("time = 0ms");
				insert 0 into out;
            }
        },
		// just to ensure the program is alive when "check" is called.
		KeyFrame {
            time: 2500ms
        }
	]
};

t1.play();
runLater(2000, check);

function check() {
	//System.out.println("golden = " + golden);
	//System.out.println("out = " + out);
	if( out != golden ) {
		throw new AssertionError("test failed");
	}
}
