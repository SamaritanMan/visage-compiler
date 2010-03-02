/**
 * JFXC-4176 :  A RuntimeException occurs in a particular use case of Vanoc on Soma b20
 *
 * @test
 * @run
 */

import javafx.animation.Timeline;
import javafx.animation.KeyFrame;

class Line {
    var startX : Integer;
}

var line:Line = null;
var tl = Timeline {
    keyFrames: KeyFrame {
        time: 100ms
        values: line.startX => 100
    }
}
tl.playFromStart();

println("animation done");
