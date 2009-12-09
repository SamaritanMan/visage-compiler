/*
 * JFXC-3709 : Compiled bind: object literal, here a bind, there a bind => Cannot find ctor: xxxx(boolean)
 *
 * @test
 *
 */

public class jfxc3709 {
    var itemsFiltered = true;
    def upArrowMenuItem = ArrowCustomMenuItem {
        disable: bind true;
    }
}

class ArrowCustomMenuItem {
   public var disable: Boolean;
   var visible = bind itemsFiltered;
}