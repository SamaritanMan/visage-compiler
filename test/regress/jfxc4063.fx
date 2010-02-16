/*
 * @test
 * @run
 */

import javafx.reflect.FXLocal;
import javafx.reflect.FXVarMember;
import javafx.reflect.FXValue;
import javafx.reflect.FXSequenceValue;
import javafx.reflect.FXSequenceType;


var x=26;
public class A {
    var t1:Number[] = bind [x];
}

def b = A{};
def sq = [22, 23, 26];

def cntxt = FXLocal.getContext();

function run() {

    def c = A{t1:sq}; // This is allowed.
    // So should be able to create same object using fx reflection theoretically?

    def cls = cntxt.findClass(A.class.getName());
    def obj = cls.allocate();
    def vr = cls.getVariable("t1");
    def ov = buildSequence(vr, sq);
    obj.initVar(vr,ov);
    obj.initialize();
    def nobj = obj.asObject() as A;
    println("newobj.t1 is {nobj.t1}");
}

function buildSequence(v:FXVarMember, obj:Object[]):FXValue {
    def elementType = (v.getType() as FXSequenceType).getComponentType();
    def builder = cntxt.makeSequenceBuilder(elementType);
    for (o in obj) {
        builder.append(cntxt.mirrorOf(o));
    }
    return builder.getSequence();
}
