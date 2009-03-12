/**
 * Regression test JFXC-2544 : Animation: Zero frame not setup correctly
 * @test
 * @run/fail
 */

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

/**
 * @author bchristi
 */

 // Demonstrate problem w/ setting of initial values
var x = 0;
var y = 0;

var yMon = bind y on replace {
    java.lang.System.out.println("{yMon}");
}

var timeline:Timeline = Timeline {
    keyFrames: [
        KeyFrame {
            time: 0s
            values: [
                x => 50
            ]
        },
        KeyFrame {
            time: 2s
            values: [                
                y => 5
            ]
        }
    ]
}
timeline.playFromStart();
