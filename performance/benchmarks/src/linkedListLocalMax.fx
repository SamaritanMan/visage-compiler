
/*
 * this test is adapted from jfxc3315LocalMax.fx
 */
public class LinkedListLocalMax extends cbm {
    override public function test():Number {
        var top : Links = Links {};
        for (m in [1..10]) {
            for (n in [1..1000]) {
                var current = top;
                top = Links {
                    next: top
                    furb: bind current.furb
                }
                0;
            }
        }
        top = null;
        return 1;
    }
}
class Links {
    public var next : Links;
    public var ool  : Boolean;
    public var furb : Number;
}

public function run(args:String[]) {
    var bs = new bsort();
    cbm.runtest(args,bs)
}