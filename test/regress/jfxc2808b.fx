/**
 * JFXC-2808 : Regression: bind to a field with a trigger causes the trigger to get fired twice
 *
 * @test
 * @run
 */

function b(st : String, v : Boolean) : Boolean { print(st); print(" : "); println(v); v }

var cond = false;
var r = false;
var s = false;

def bc = bind if (b("cond", cond)) b("then", r) else b("else", s);

bc;
println("-Then");
r = true;
bc;
println("Cond+");
cond = true;
bc;
println("-Else");
s = true;    // cond is true, so this should not cause the else to be recalced.
bc;
println("Cond-");
cond = false;
bc;
println("-Then");
r = false;
bc;
println("Cond+");
cond = true;
bc;
println("-Else");
s = false;
bc;
println("+Then");
r = true;
bc;
println("Cond-");
cond = false;
bc;
println("-Then");
r = false;
bc;
println("+Else");
s = true;
bc;
