/*
 * Regression: JFXC-3402 - function bind error.
 *
 * @test
 * @run
 *
 */

class Class1 {
    public var action: function(:String) : Void;
};

function act(str: String): Void {
    println("{str}");
};

var c1 = Class1 { action: bind act };

c1.action("Hello");

