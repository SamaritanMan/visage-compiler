/*
 * Duration_TS002_01.fx

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
import javax.swing.Timer;
import java.awt.event.*;


var t1: Duration = 1h;
var t2: Duration = 30m;

if(t1.add(t2) != 1.5h) {
	throw new AssertionError("duration add test failed");
}

if(t1.sub(t2) != 30m) {
	throw new AssertionError("duration sub test failed");
}

if(t1.div(2) != 30m) {
	throw new AssertionError("duration div test failed");
}

if(t2.mul(2) != 60.0m) {
	throw new AssertionError("duration div test failed");
}
