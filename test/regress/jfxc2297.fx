/**
 * JFXC-2297 :  Inconsistency in evaluation of variables used in bind expression if it gets changed in different locations.
 *
 * @test
 * @run
 */

var t = 2;
var s:Integer = bind f(t);

function f(x:Integer){
  t = t*2;
  t;
}
println("value of s {s}");

f1();

println("value of t {t}");
println("value of s {s}");

function f1(){
  t = 5;
}
