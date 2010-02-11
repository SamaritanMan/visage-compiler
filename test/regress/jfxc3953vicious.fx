/*
 * JFXC-3953 : Compiled bind optimization: rewrite bound range for correctness, speed, and static footprint
 *
 * Test bound range sequence against pseudo-random incessant simultaneous changes to lower, upper, and step.
 * Include access during update - from every angle.
 * No output unless error (or verbose turned on).
 *
 * @test
 * @run
 */

import javafx.util.Bits;

var verbose = false;

class vvs {
  var vlow : Integer;
  var vhigh : Integer;
  var vstep : Integer;
  override function toString() { "vvs[{vlow}..{vhigh} step {vstep}]" }
}

class rough {
  var vvv : vvs;
  var bl = bind vvv.vlow;
  var bh = bind vvv.vhigh;
  var bs = bind vvv.vstep;
  var bx = bind [bl .. bh step bs];

  function check(q : String) {
    if (verbose) println("check: {q}: {vvv}");
    def cmp = [vvv.vlow .. vvv.vhigh step vvv.vstep];
    if (sizeof bx != sizeof cmp) println("ERROR: Size mismatch ({sizeof bx} != {sizeof cmp})");
    if (bx != cmp) println("ERROR: Content mismatch ({bx.toString()} != {cmp.toString()})");
    if (bx[-1] != 0) println("ERROR: Small offset not zero (bx[-1] = {bx[-1]}){q}");
    if (bx[sizeof bx] != 0) println("ERROR: Big offset not zero (bx[{sizeof bx}] = {bx[sizeof bx]}){q}");
  }
}

class rv extends rough {
  override var vvv; init { check("rv") }
}

class rl extends rough {
  override var bl on replace { check("rl") }
}

class rh extends rough {
  override var bh on replace { check("rh") }
}

class rs extends rough {
  override var bs on replace { check("rs") }
}

class rx extends rough {
  override var bx on replace { check("rx") }
}

function toVvs(z : Integer) : vvs {
  vvs {
	vlow: Bits.bitAnd(z, 7) - 3
	vhigh: Bits.bitAnd(Bits.shiftRight(z, 3), 7) - 3
	vstep: (Bits.bitAnd(Bits.shiftRight(z, 6), 3) + 1) * (if (Bits.bitAnd(Bits.shiftRight(z, 8), 1)==0) 1 else -1)
  }
}

var munchers = [179, 53, 223, 167, 373, 13, 7];
var movers = [ 37, 5, 109, 41, 11];
var munInx = 0;
var movInx = 0;

function rnd() {
  ++munInx;
  ++movInx;
  if (munInx >= sizeof munchers) munInx = 0;
  if (movInx >= sizeof movers) movInx = 0;
  munchers[munInx] *= movers[movInx];
  munchers[munInx]
}

function manhandle(r : rough) {
  r.check("--");
  r.vvv = toVvs(rnd());
  r.check("--");
  r.vvv = toVvs(rnd());
  r.check("--");
  r.vvv = toVvs(rnd());
  r.check("--");
  r.vvv = toVvs(rnd());
  r.check("--");
  r.vvv = toVvs(rnd());
  r.check("--");
  r.vvv = toVvs(rnd());
  r.check("--");
  r.vvv = toVvs(rnd());
  r.check("--");
  r.vvv = toVvs(rnd());
  r.check("--");
  r.vvv = toVvs(rnd());
  r.check("--");
  r.vvv = toVvs(rnd());
  r.check("--");
  r.vvv = toVvs(rnd());
  r.check("--");
}

for (i in [0 .. 511]) {
  var ini = toVvs(i);
  if (verbose) println("--- Initial: {ini}");
  if (verbose) println("(rv)");
  manhandle( rv{ vvv: ini } );
  if (verbose) println("(rl)");
  manhandle( rl{ vvv: ini } );
  if (verbose) println("(rh)");
  manhandle( rh{ vvv: ini } );
  if (verbose) println("(rs)");
  manhandle( rs{ vvv: ini } );
  if (verbose) println("(rx)");
  manhandle( rx{ vvv: ini } );
}
  