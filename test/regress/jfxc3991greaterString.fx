/*
 * Regression test 
 * JFXC-3991 : coalesce bound-for element invalidations into bulk trigger phase update
 *              to prevent on-replace in inconsistent state
 *
 * Conditional for-loop body, String
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
  var exp = for (i in [0..30]) if (i > max) tArm else fArm;
  if (content != exp) {
    println("ERROR: content does not match");
    println(content);
    println(exp);
  }
}

var mirror : String[] = [];
var max = 2;
var tArm = "ww";
var fArm = "zz";
var content = bind for (i in [0..30]) if (i > max) tArm else fArm
      on replace [a..b] = newV {
          mirror[a..b] = newV;
          for(z in content) z; 
      }; 
check();
max = 2;
check();
max = -1;
check();
max = 50;
check();

fArm = "mm";
check();
fArm = "mm";
check();

max = 1;
tArm = "ll";
check();
