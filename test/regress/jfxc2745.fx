/**
 * JFXC-2745 : bind translation cannot always convert from non-sequence to sequence
 *
 * @test
 * @run
 */

var b = true;
var a:String[] = bind "yo";
var c:String[] = bind if (b) "foo" else null;
var d:String[] = bind if (b) "foo" else "boo";

println(a);
println(c);
println(d);
b = false;
println(c);
println(d);
