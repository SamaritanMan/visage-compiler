/**
 * Regression test for JFXC-1043: Last tick in Timeline is not performed
 *
 * @test
 * @run
 */

import javafx.animation.*;
import java.lang.System;

class CLS {
    public var a: Number = 0;
}

var cls = CLS {};

var t: Timeline = Timeline {
   keyFrames: [
       KeyFrame {
           time: 1ms
           values: cls.a => 2.0
           action : function () {
               System.out.println("timeline finished - {cls.a}");
           }
       }
   ]
};

t.play();
