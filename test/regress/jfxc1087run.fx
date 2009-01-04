/*
 * Regression test JFXC-1087 : bound interpolator
 *
 * @test
 * @run
 */

import javafx.animation.*;

var op:Number = -1.0;               
var kf = bind KeyFrame {
            time: 600ms
            values: op => 9.9 tween Interpolator.EASEOUT
         }
var vs = kf.values[0];
println(vs.target.get());
println(vs.value());
println(vs.interpolate == Interpolator.EASEOUT);
