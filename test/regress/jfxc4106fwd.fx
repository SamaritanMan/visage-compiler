/**
 * JFXC-4106 : Strict declaration order initialization: bound sequences
 *
 * Forward references
 *
 * @test
 * @run
 */

class jfxc4106fwd {
  var w = this;
  def dw = w;
  var j = 1;
  
  def missing = "*MISSING*";
  var bindsInited = false;
  var repSeq = for (i in [0..16]) missing;
  function report(bi : Integer, msg : String) : Void {
    if (bindsInited) {
      if (repSeq[bi] != missing) {
        println("ERROR: multiple on-replace ({bi})");
        println(repSeq[bi]);
        println(msg);
        println("<<<<<<");
      }
      repSeq[bi] = msg;
    } else {
      println(msg);
    }
  }
  function show() : Void {
    repSeq[0] = "--------------";
    for (msg in repSeq) {
      println(msg)
    }
  }

  var start = 1 on replace { println("... start ...") }

  var blk1 = bind { var tv = val; tv } 	on replace [a..b] = newVal { report( 1, " 1: block1  [{a}..{b}] = {newVal.toString()}"); }
  var blk2 = bind { val } 		on replace [a..b] = newVal { report( 2, " 2: block2  [{a}..{b}] = {newVal.toString()}"); }
  var for1 = bind for (i in val) i 	on replace [a..b] = newVal { report( 3, " 3: for1    [{a}..{b}] = {newVal.toString()}"); }
  var for2 = bind for (i in val) [i] 	on replace [a..b] = newVal { report( 4, " 4: for2    [{a}..{b}] = {newVal.toString()}"); }
  var idt1 = bind val		 	on replace [a..b] = newVal { report( 5, " 5: ident1  [{a}..{b}] = {newVal.toString()}"); }
  var ife1 = bind if (sizeof val < 2) val else val
				 	on replace [a..b] = newVal { report( 6, " 6: if-expr [{a}..{b}] = {newVal.toString()}"); }
  var par1 = bind (val)		 	on replace [a..b] = newVal { report( 7, " 7: parens  [{a}..{b}] = {newVal.toString()}"); }
  var sel1 = bind w.val	 		on replace [a..b] = newVal { report( 8, " 8: select  [{a}..{b}] = {newVal.toString()}"); }
  var emp1 = bind []	 		on replace [a..b] = newVal { report( 9, " 9: empty   [{a}..{b}] = {newVal.toString()}"); }
  var rng1 = bind [j..7] 		on replace [a..b] = newVal { report(10, "10: range   [{a}..{b}] = {newVal.toString()}"); }
  var slc1 = bind val[1..3] 		on replace [a..b] = newVal { report(11, "11: slice   [{a}..{b}] = {newVal.toString()}"); }
  var rev1 = bind reverse val 		on replace [a..b] = newVal { report(12, "12: reverse [{a}..{b}] = {newVal.toString()}"); }
  var inv1 = bind val with inverse	on replace [a..b] = newVal { report(13, "13: invers1 [{a}..{b}] = {newVal.toString()}"); }
  var inv2 = bind dw.val with inverse	on replace [a..b] = newVal { report(14, "14: invers2 [{a}..{b}] = {newVal.toString()}"); }
  var xpl1 = bind [5, 3, j]		on replace [a..b] = newVal { report(15, "15: xplct1  [{a}..{b}] = {newVal.toString()}"); }
  var xpl2 = bind [val, 4] 		on replace [a..b] = newVal { report(16, "16: xplct2  [{a}..{b}] = {newVal.toString()}"); }

  var finish = 1 on replace { println("... bind init complete ...");  bindsInited = true; }

  var val = [0, 1, 2, 3];

  function makeMutable() {
    w = null;
    j = 2;
    val = []
  }
}

jfxc4106fwd{}.show()
