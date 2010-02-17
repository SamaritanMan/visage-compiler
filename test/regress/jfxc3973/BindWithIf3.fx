/**
 * JFXC-3973 : Bound variable not reset to initial object literal.
 *
 * @test
 * @run
 */

// This test is same as BindWithIf2.fx exception for animation removal.

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

    val = 0;
    val = 1;
    val = 2;
    val = 3;
}
