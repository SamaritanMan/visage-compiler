/*
 * Regression test 
 * JFXC-3991 : coalesce bound-for element invalidations into bulk trigger phase update
 *              to prevent on-replace in inconsistent state
 *
 * Index into bound range sequence for-loop body
 *
 * @test
 * @run
 */

var onrSeen = false;

function check(what : String, onrExpected : Boolean) {
  if (mirror != content) {
    println("ERROR {what}: mirror does not match");
    println(mirror);
    println(content);
  }
  var exp = for (i in [0..10]) ([low..high step hop])[i];
  if (content != exp) {
    println("ERROR {what}: content does not match");
    println(content);
    println(exp);
  }
//  if (onrSeen and not onrExpected) {
//    println("ERROR {what}: unexpected on-replace");
//  }
  if (not onrSeen and onrExpected) {
    println("ERROR {what}: missed expected on-replace");
  }
  onrSeen = false;
}

var mirror : Integer[] = [];
var low = 3;
var high = 12;
var hop = 3;
var content = bind for (i in [0..10]) ([low..high step hop])[i]
      on replace [a..b] = newV {
          onrSeen = true;
          mirror[a..b] = newV;
          for(z in content) z; 
      }; 
check("init", true);
low = 3;
high = 12;
hop = 3;
check("same", false);
low = 0;
check("low = 0", true);
high = 13;
check("high = 13", false);
high = 15;
check("high = 15", true);
high = 15;
check("same high = 15", false);
hop = 1;
check("hop = 1", true);
high = 30;
check("overflow high = 30", false);

