/**
 * JFXC-3405 : CoolFX demo compiles with 1.2 but not with 1.2.1.
 *
 * @test
 * @run
 */

public class jfxc3405 {
    function wait(str: String) {
        println("wait called with {str}");
    }

    var f on replace  {
        hide(function () {
           wait("hello");
        });
    }

    function hide(action: function(): Void): Void {
        action();
    }
}

function run() {
   jfxc3405 {}
}

