/*
 * Regression test 
 * JFXC-3991 : coalesce bound-for element invalidations into bulk trigger phase update
 *              to prevent on-replace in inconsistent state
 *
 * Conditional for-loop body, Object
 *
 * @test
 * @run
 */

class Thing {}

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

var mirror : Thing[] = [];
var cond = false;
var tArm = new Thing;
var fArm = new Thing;
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

def chg = new Thing;
fArm = chg;
check();
fArm = chg;
check();

cond = true;
tArm = new Thing;
check();
