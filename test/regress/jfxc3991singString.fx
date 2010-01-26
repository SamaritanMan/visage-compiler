/*
 * Regression test 
 * JFXC-3991 : coalesce bound-for element invalidations into bulk trigger phase update
 *              to prevent on-replace in inconsistent state
 *
 * Non-sequence for-loop body, String
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
  if (content != seq[0..2]) {
    println("ERROR: content does not match");
    println(content);
    println(seq[0..2]);
  }
}

var mirror : String[] = [];
var seq = ['a', 'b', 'c'];
var content = bind for (i in [1,2,3]) seq[i-1]
      on replace [a..b] = newV {
          mirror[a..b] = newV;
          for(z in content) z; 
      }; 
check();
seq[0] = 'x';
check();
seq[2] = 'z';
check();
seq[1] = 'y';
check();

insert 'w' before seq[0];
check();

seq = ['m', 'm', 'm', 'm'];
check();
