/**
 * JFXC-3973 : Bound variable not reset to initial object literal.
 *
 * @test
 * @run
 */

/**
 * @author bchristi
 */

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

class Named {
    var id:String;
}

public class BindWithIf2 {
    public var memberVar:Named on replace oldValue{
        println("memberVar was {oldValue.id}, is now {memberVar.id}");
    }
}

public function run(args:String[]):Void {
    var val1 = Named {id: "val1"};
    var val2 = Named {id: "val2"};

    var val = 0 on replace {
        println("val={val}");
    };

    var bwi = BindWithIf2 {
        memberVar: bind if (val == 1) val1
                   else if (val == 2) val2
                   else Named {
                       id: "ObjLiteral"
                   };
    }

    var tl = Timeline {
        keyFrames: [
            KeyFrame {
                time: 0s,
                values: val => 0
            },
            KeyFrame {
                time: 3s,
                values: val => 3
            },
        ]
    }

    tl.playFromStart();
}
