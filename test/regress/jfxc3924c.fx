/*
 * Regression test
 * JFXC-3924 : Unexpected type assertion in JavafxTreeMaker.java
 *
 * @test
 */

function f1():Object {
   for (f in [1,2,3]) {
      if (false) return 1;
      var x = 1;
      var y = bind x;
      null;
   }
}

function f2():Object {
   for (f in [1,2,3]) {
      if (false) return 1;
      var x = 1;
      var y = bind x;
      [];
   }
}