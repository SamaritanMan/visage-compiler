/*
 * Regression test 
 * JFXC-3991 : coalesce bound-for element invalidations into bulk trigger phase update
 *              to prevent on-replace in inconsistent state
 *
 * Index into bound if sequence for-loop body, Integer
 *
 * @test
 * @run
 */

function check(what : String) {
  if (mirror != content) {
    println("ERROR {what}: mirror does not match");
    println(mirror);
    println(content);
  }
  var exp = for (i in [0..30]) (if (i > max) tArm else fArm)[i];
  if (content != exp) {
    println("ERROR {what}: content does not match");
    println(content);
    println(exp);
  }
}

var mirror : Integer[] = [];
var max = 2;
var tArm : Integer[] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9 ];
var fArm : Integer[] = [];
var content = bind for (i in [0..30]) (if (i > max) tArm else fArm)[i]
      on replace [a..b] = newV {
          mirror[a..b] = newV;
          for(z in content) z; 
      }; 
check("init");
max = 2;
check("same");
max = -1;
check("max = -1");
max = 50;
check("max = 50");

fArm = [ 9, 99, 999 ];
check("F");
max = 2;
check("max = 2");
max = -1;
check("max = -1");
max = 50;
check("max = 50");

def chg = [ 77 ];
fArm = chg;
check("fArm = chg");
fArm = chg;
check("repeat");

max = 10;
check("max = 10");

tArm = [];
check("tArm = []");

max = 1;
tArm = 123455;
check("*");
