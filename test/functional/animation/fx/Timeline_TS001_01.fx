/*
 * Timeline_TS001_01.fx

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

var images = [1..16];
var golden = [[0..15], [14..0 step -1]];
var id: Integer = 0;

var t : Timeline = Timeline {
    autoReverse: true
    repeatCount: 2
    keyFrames: for (image in images)
        KeyFrame {
            time: 100ms * indexof image
            action: function() {
				//System.out.println("=> {indexof image}");
                if(indexof image != golden[id++]) {
                    t.stop();
					throw new AssertionError("autoReverse test failed");
                }
            }
        }
}

t.play();
