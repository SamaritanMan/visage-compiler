/**
 * Regression test: JFXC-3610 : Compiled-bind: bugs in handling embedded bound sequences
 *
 * Handling of bound implicit sequence conversion and bound [] 
 *
 * @test
 * @run
 */

var obj  = bind "yo";
def res = bind [] != obj;
def res2 = bind null != obj;
def res3 = bind null == [];
println(res);
println(res2);
println(res3);
