/*
 * Regression: JFXC-4163 - Mixin class static function values are not compiling. Blocking build.
 *
 * @compilefirst jfxc4163super.fx
 * @test
 * @run
 *
 */

public var sg1 = function():String { "jfxc4163 sg1" };

public class jfxc4163sub extends jfxc4163super {
    public var cg1 = function():String { "jfxc4163 cg1" };
}

function run() {
    var x = jfxc4163sub{};
    
    println("{x.sf1()}");
    println("{x.sf2()}");
    println("{x.sf3()}");
    println("{x.sf4()}");
    println("{x.cf1()}");
    println("{x.cf2()}");
    println("{x.sg1()}");
    println("{x.cg1()}");
}