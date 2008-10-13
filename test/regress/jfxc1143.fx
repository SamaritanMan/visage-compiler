/*
 * toggle test
 *
 * @test/nocompare
 * @run
 */

import javafx.animation.*;
import java.lang.System;
import java.lang.AssertionError;

var ids: Integer[] = [0..15];
var golden: Integer[] = [0,1,2,3,4,5, 4,3,2,1,0];
var out: Integer[];

function runLater(ms: Duration, f: function(): Void): Void {
    Timeline {
        keyFrames: KeyFrame {
            time: ms
            action: f
        }
    }.play();
}

var keepAlive : Timeline = Timeline {
    repeatCount: Timeline.INDEFINITE
    keyFrames: KeyFrame {
        time: 100ms
    }
};

var t : Timeline = Timeline {
    repeatCount: 1
    keyFrames: [for (id in ids)
        KeyFrame {
            time: 100ms * indexof id
            action: function() {
                //System.out.println("id: {id}");
                insert id into out;
                if(id == 5) {
                    t.rate *= -1;
                }
            }
        },
    ]
};

keepAlive.play();
t.play();

runLater(2000ms, check);
function check() {
    keepAlive.stop();
    if(t.running) {
        t.stop();
        throw new AssertionError("test failed: t is still running");
    }

    if(out != golden) {
        throw new AssertionError("test failed: {out} != {golden}");
    }
    System.out.println("pass");
}

