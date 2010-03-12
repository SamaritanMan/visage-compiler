/**
 * JFXC-3946 :  Compiled-bind: inflation is generating try/block expression in unsupported positions (non empty stack).
 *
 * @test
 */

var v = for (x in [1,2,3]) {
   var y = bind x;
   // used to result in error here..
   if (false) break;
   y;
}

println(v); 
