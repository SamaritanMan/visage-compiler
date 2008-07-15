/*
 * Timeline_TS001_02.fx

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
import javax.swing.Timer;
import java.awt.event.*;

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

function runLater(ms: Number, f: function(): Void): Void {
	var timer = new Timer(ms, ActionListener {
		public function actionPerformed(e: ActionEvent) {
			f();
		}
	});
	timer.setRepeats(false);
	timer.start();
}

function pause() {
	t.pause();
}

function resume() {
	t.resume();
}

t.start();

var dummy = bind t.paused on replace {
	//System.out.println("t.paused = {t.paused}");
}

for(ms in [100..3000 step 100]) {
	runLater(ms, if( ((ms/100) mod 2) == 0 ) resume else pause);
}
