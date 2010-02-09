/*
 * Regression test 
 * JFXC-4043 : Bad invalidations when bound-for induction sequence and body update at same time
 *
 * Insert 
 *
 * @test
 * @run
 */

    var items = ["Monday", "Tuesday", "Wednesday"];
    var xx = bind
            for(i in [0..<sizeof items]) {
                items[i]
            }
       on replace [a..b] = newVal{
          println("[{a}..{b}] = {sizeof newVal} -- xx = {sizeof(xx)}");
        };
    

    function run() {
      insert "Thursday" into items;
      insert "Friday" into items;
    }
