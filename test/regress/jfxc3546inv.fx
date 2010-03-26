/**
 * JFXC-3546 : Internal bug in compiler: nativearray in bound-for -- cannot find symbol symbol : variable startPos$ 
 *
 * Check invalidation
 *
 * @test
 * @run
 */

var str = "gee hello there";
var arr = str.split(" ");
def ba = bind arr as String[] on invalidate { println("Inv") };
println(ba);
str = "wow man";
arr = str.split(" ");
arr = str.split("w");
println("---");
println(ba);
