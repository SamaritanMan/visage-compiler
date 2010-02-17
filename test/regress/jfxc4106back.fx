/**
 * JFXC-4106 : Strict declaration order initialization: bound sequences
 *
 * Normal backward references
 *
 * @test
 * @run
 */

class jfxc4106back {
  var w = this;
  def dw = w;
  var j = 1;
  var val = [0, 1, 2, 3];

  var start = 1 on replace { println("... start ...") }

  var blk1 = bind { var tv = val; tv } 	on replace [a..b] = newVal { println(" 1: block1  [{a}..{b}] = {newVal.toString()}"); }
  var blk2 = bind { val } 		on replace [a..b] = newVal { println(" 2: block2  [{a}..{b}] = {newVal.toString()}"); }
  var for1 = bind for (i in val) i 	on replace [a..b] = newVal { println(" 3: for1    [{a}..{b}] = {newVal.toString()}"); }
  var for2 = bind for (i in val) [i] 	on replace [a..b] = newVal { println(" 4: for2    [{a}..{b}] = {newVal.toString()}"); }
  var idt1 = bind val		 	on replace [a..b] = newVal { println(" 5: ident1  [{a}..{b}] = {newVal.toString()}"); }
  var ife1 = bind if (sizeof val < 2) val else val
				 	on replace [a..b] = newVal { println(" 6: if-expr [{a}..{b}] = {newVal.toString()}"); }
  var par1 = bind (val)		 	on replace [a..b] = newVal { println(" 7: parens  [{a}..{b}] = {newVal.toString()}"); }
  var sel1 = bind w.val	 		on replace [a..b] = newVal { println(" 8: select  [{a}..{b}] = {newVal.toString()}"); }
  var emp1 = bind []	 		on replace [a..b] = newVal { println(" 9: empty   [{a}..{b}] = {newVal.toString()}"); }
  var rng1 = bind [j..7] 		on replace [a..b] = newVal { println("10: range   [{a}..{b}] = {newVal.toString()}"); }
  var slc1 = bind val[1..3] 		on replace [a..b] = newVal { println("11: slice   [{a}..{b}] = {newVal.toString()}"); }
  var rev1 = bind reverse val 		on replace [a..b] = newVal { println("12: reverse [{a}..{b}] = {newVal.toString()}"); }
  var inv1 = bind val with inverse	on replace [a..b] = newVal { println("13: invers1 [{a}..{b}] = {newVal.toString()}"); }
  var inv2 = bind dw.val with inverse	on replace [a..b] = newVal { println("14: invers2 [{a}..{b}] = {newVal.toString()}"); }
  var xpl1 = bind [5, 3, j]		on replace [a..b] = newVal { println("15: xplct1  [{a}..{b}] = {newVal.toString()}"); }
  var xpl2 = bind [val, 4] 		on replace [a..b] = newVal { println("16: xplct2  [{a}..{b}] = {newVal.toString()}"); }

  var finish = 1 on replace { println("... finish ...") }

  function makeMutable() {
    w = null;
    j = 2;
    val = []
  }
}

jfxc4106back{}
