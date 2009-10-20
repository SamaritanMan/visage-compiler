/*
 * Timeline_TS021_01.fx
 *
 * @test
 * @run
 */

/**
 * @author Leonid Popov, Baechul Kim
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

def N = 20;
var golden: Integer[] = [0..N-1];
var out: Integer[];

var keepAlive: Timeline = Timeline {
    repeatCount: Timeline.INDEFINITE
    keyFrames: [
    KeyFrame {
        time: 100ms
    }
    ]
};

var n = 0;
var t : Timeline = Timeline {
        repeatCount: N
        keyFrames: [
            KeyFrame {
                canSkip: false;
                time: 15ms
                action: function() {
                    insert n++ into out;
                    Thread.currentThread().sleep(20);
                }
            }
        ]
    };

keepAlive.play();
t.play();

runLater(2000, check);

function check() {
    t.stop();
	keepAlive.stop();
//	System.out.println("golden = {golden}");
//	System.out.println("out = {out}");
	if(out != golden) {
		throw new AssertionError(" test failed");
	}
}
