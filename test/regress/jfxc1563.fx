/*
 * @test
 */
import javafx.animation.KeyFrame;
import java.lang.System;

var count: Integer = 0 on replace old = newValue {
    System.out.println("count = {newValue}");
};

var t = KeyFrame {
            time: 100ms
            action: function() {
                count++;
            }
        };
