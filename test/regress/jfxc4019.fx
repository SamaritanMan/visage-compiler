/*
 * @test
 * @run
 */

import javafx.reflect.*;

public class A1 extends B1, C1 {
}

public abstract class B1 {
     public var text:String;
}

public mixin class C1 {
    public var text:String;
}

function test1() {
    def a = A1 {};
    def cntxt = FXLocal.getContext(); 
    def ov = cntxt.mirrorOf(a);
    for (v in ov.getClassType().getVariables(true)) {
        if (not v.isStatic() and (v.isPublic() or v.isPublicInit())) {
            println("{v.getName()} in {v.getDeclaringClass()} = {v.getValue(ov).getValueString()}");
        }
    }
}

public class A2 extends B2 {
}

public abstract class B2 extends  C2 {
}

public mixin class C2 {
    public var height:Number;
}

function test2() {
    def cntxt = FXLocal.getContext();
    def btn = A2{height:23.0};
    def ov = cntxt.mirrorOf(btn);
    for (v in ov.getClassType().getVariables(true)) {
            println("{v.getName()}: {v.getType()} = {v.getValue(ov)} ");
    }
}

function run() {
    test1();
    test2();
}
