/**
 * JFXC-3469 : compiler crashes at script compilation.
 *
 * @test
 */

class Test {
   var action : function():Void;
}

class Main {
    public function checkPack() : Void {
    };

    public function test() {
        Test {
            action: function() : Void {
                // Used to crash with "cannot find symbol" error.
                this.checkPack();
            }
        }
    };
}
