/**
 * JFXC-3354 : Compiler change causing sample app positioning problems
 *
 * Self-reference causes forward-reference causes lack of update on init.
 *
 * @test
 * @run
 */

def GOODNESS = 55.55;

class BIL {
    public var width : Number;
}

class MyText {
    var transX : Number;
    public var bilCopy : BIL;
    public var bil = bind BIL { width: 55.55 };
}

class MyCalc {
    var nt : MyText = MyText {
       bilCopy: bind nt.bil;
    }
}

function run() {
    var mc = MyCalc {}
    if (mc.nt.bilCopy.width != GOODNESS) {
       println("Bad result: {mc.nt.bilCopy.width}");
    }
}
