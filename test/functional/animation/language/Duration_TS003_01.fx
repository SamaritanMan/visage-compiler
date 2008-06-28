/*
 * Duration_TS003_01.fx

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


var t1: Duration = 1m;
var t2: Duration = 10s;

if(not t1.gt(t2)) {
	throw new AssertionError("duration gt test failed");
}

if(not t1.ge(t2)) {
	throw new AssertionError("duration ge test failed");
}

if(not t1.ge(60000ms)) {
	throw new AssertionError("duration ge test failed");
}

if(t1.lt(t2)) {
	throw new AssertionError("duration lt test failed");
}

if(t1.le(t2)) {
	throw new AssertionError("duration le test failed");
}

if(not t1.le(60000ms)) {
	throw new AssertionError("duration le test failed");
}
