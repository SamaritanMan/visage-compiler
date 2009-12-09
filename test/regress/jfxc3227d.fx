/**
 * JFXC-3227 : Compiler should always enforce sequence flattening
 *
 * @test
 * @run
 */

function foo(x:Object) {
   var seq:Object[] = for (i in [1,2,3]) x;
   println(seq);
}

foo([1,2,3]);
