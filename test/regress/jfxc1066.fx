/*
 * Regression test: for JFXC-1066 :  KeyFrame Literal support 
 *
 * @test
 * @run
 */


import java.lang.System;
import javafx.animation.*;

var x = 0;
var y = 0;
var sx = 0;
var sy = 0;
 
var ax = Timeline {
        // x
        keyFrames:
        [at (0s) {
            x => 0;
        },
        at (10s) {
            x => 700 tween Interpolator.LINEAR;
        }]
        autoReverse: true
        repeatCount: Timeline.INDEFINITE 
    }
    var ay = Timeline {
        // y
        repeatCount: Timeline.INDEFINITE
        keyFrames:
        [at (0s) {
            y => 0;
        },
        at (2.2s) {
            y => 375 tween Interpolator.SPLINE(0, 0, .5, 0);
        },
        at (2.25s) {
            y => 375;
        },
        at (4.5s) {
            y => 0 tween Interpolator.SPLINE(0, 0, 0, 0.5);
        }]
    }
    
    var sxy =  Timeline {
        // scale x y
        repeatCount: Timeline.INDEFINITE
        
        keyFrames:
        [at (2.15s) {
            sx => 1;
            sy => 1;
        },
        at (2.25s) {
            sx => 1.2 tween Interpolator.LINEAR;
            sy => .7 tween Interpolator.LINEAR;
        },
        at (2.5s) {
            sy => 1 tween Interpolator.LINEAR;
            sx => 1 tween Interpolator.LINEAR;
        },
        at (4.5s) {
            sx => 1;
            sy => 1;
        }]
    }
var txy = Timeline {
    repeatCount: Timeline.INDEFINITE
        
    keyFrames: at (3.15s) {
            sx => 1;
            sy => 1;
        }
}

var zxy = Timeline {
	keyFrames: at ( 4.15s) {
	}
}
System.out.println(ax.keyFrames[0].values[0].value);
System.out.println(ay.keyFrames[1].values[0].value);
System.out.println(sxy.keyFrames[2].values[1].value);
