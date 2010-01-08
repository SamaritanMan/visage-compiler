/**
 * JFXC-3921 : Compiled-bind: inflation and loops with break/continue
 *
 * @test
 */

function localFunc1() {
   while (true) {
      var a = 1;
      var b = bind a;
      if (a ==1) continue;
   }
}

function localFunc2() {
   for (x in [1,2,3]) {
      var a = 1;
      var b = bind a;
      if (a ==1) continue;
   }
}

function localFunc3() {
   while (true) {
      var a = 1;
      var b = bind a;
      if (a ==1) break;
   }
}

function localFunc4() {
   for (x in [1,2,3]) {
      var a = 1;
      var b = bind a;
      if (a ==1) break;
   }
}
