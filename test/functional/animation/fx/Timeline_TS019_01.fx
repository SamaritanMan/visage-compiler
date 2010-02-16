/*
 * Timeline_TS019_01.fx
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

var keepAlive: Timeline = Timeline {
    repeatCount: Timeline.INDEFINITE
    keyFrames: [
    KeyFrame {
        time: 100ms
    }
    ]
};

var dur = 500ms;
var checkAfter = dur.toMillis() * 2;
var t : Timeline = Timeline {
    keyFrames: [
        KeyFrame {time: 100ms},
        KeyFrame {time: dur}
    ]
};

keepAlive.play();
t.play();

runLater(checkAfter, check1);
runLater(checkAfter * 4, finish);

function check1() {
    // check if Timeline stays at its end after normal completion
    check(dur);
    // re-start it backward from the end
    t.rate = -1;
    t.play();
    runLater(checkAfter, check2);
};
function check2() {
    // check if Timeline stays at 0s after normal completion (backward)
    check(0s);
    // re-start it forward from 0s
    t.rate = 1;
    t.play();
    runLater(checkAfter, check3);
};
function check3() {
    // check if Timeline resets to 0s after explicit stop
    t.stop();
    check(0s);
};

function finish () {
    keepAlive.stop();
    t.stop();
};

function check(time: Duration) {
    var tt = t.time;
    if(not t.time.equals(time)) {
        finish();
        throw new AssertionError("FAILED: t.time={tt} should be {time}");
    }
}
