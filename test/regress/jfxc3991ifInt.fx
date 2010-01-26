/*
 * Regression test 
 * JFXC-3991 : coalesce bound-for element invalidations into bulk trigger phase update
 *              to prevent on-replace in inconsistent state
 *
 * Conditional for-loop body, Integer
 *
 * @test
 * @run
 */

function check() {
  if (mirror != content) {
    println("ERROR: mirror does not match");
    println(mirror);
    println(content);
  }
  var exp = for (i in [0..3]) if (cond) tArm else fArm;
  if (content != exp) {
    println("ERROR: content does not match");
    println(content);
    println(exp);
  }
}

var mirror : Integer[] = [];
var cond = false;
var tArm = 1;
var fArm = 2;
var content = bind for (i in [0..3]) if (cond) tArm else fArm
      on replace [a..b] = newV {
          mirror[a..b] = newV;
          for(z in content) z; 
      }; 
check();
cond = false;
check();
cond = true;
check();
cond = false;
check();

fArm = 3;
check();
fArm = 3;
check();

cond = true;
tArm = 4;
check();
