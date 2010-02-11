/**
 * The vicious bound sequence test over Integer
 *
 * @compilefirst ViciousCheckOvWbI.fx
 * @compilefirst ViciousEngineOvWbI.fx
 * @test
 * @run
 */

var unchanging = true;

class A extends ViciousCheckOvWbI {
  override function expect() { if (n == 5) [ 4, 4, n*3] else [ n + 10 ] }
  override var content =  bind if (n == 5) [ 4, 4, n*3] else [ n + 10 ];
}

class B extends ViciousCheckOvWbI {
  override function expect() { if (n == 5) [0..n] else [3] }
  override var content =  bind if (n == 5) [0..n] else [3];
}

class C extends ViciousCheckOvWbI {
  override function expect() { if (n == 5) [3] else [0..n] }
  override var content =  bind if (n == 5) [3] else [0..n];
}

class D extends ViciousCheckOvWbI {
  override function expect() { if (n > 3) [-n .. 2] else [n .. 0 step -2] }
  override var content =  bind if (n > 3) [-n .. 2] else [n .. 0 step -2];
}

class E extends ViciousCheckOvWbI {
  override function expect() { if (n > 2 and n < 7) [ 4, 4, n*3] else [ 0..(n+10) ] }
  override var content =  bind if (n > 2 and n < 7) [ 4, 4, n*3] else [ 0..(n+10) ];
}

class F extends ViciousCheckOvWbI {
  override function expect() { if (n > 4) [ 1, 2, 3, 4 ] else [ 6 ] }
  override var content =  bind if (n > 4) [ 1, 2, 3, 4 ] else [ 6 ];
}

class G extends ViciousCheckOvWbI {
  override function expect() { if (unchanging) [ 4, 4, n*3] else [ n + 10 ] }
  override var content =  bind if (unchanging) [ 4, 4, n*3] else [ n + 10 ];
}

class H extends ViciousCheckOvWbI {
  override function expect() { if (unchanging) [0..n] else [3] }
  override var content =  bind if (unchanging) [0..n] else [3];
}

class I extends ViciousCheckOvWbI {
  override function expect() { if (unchanging) [3] else [0..n] }
  override var content =  bind if (unchanging) [3] else [0..n];
}

class J extends ViciousCheckOvWbI {
  override function expect() { if (unchanging) [-5 .. n] else [n .. 0 step -2] }
  override var content =  bind if (unchanging) [-5 .. n] else [n .. 0 step -2];
}

class K extends ViciousCheckOvWbI {
  override function expect() { if (n > 2 and n < 7) [ n, n, n, n ] else [ 0..(n+10) ] }
  override var content =  bind if (n > 2 and n < 7) [ n, n, n, n ] else [ 0..(n+10) ];
}

class L extends ViciousCheckOvWbI {
  override function expect() { if (n != 5 and n > 2 and n < 8) [ n, n+n, 14, 2, n ] else  [ n*10, n, n*n ] }
  override var content =  bind if (n != 5 and n > 2 and n < 8) [ n, n+n, 14, 2, n ] else  [ n*10, n, n*n ];
}

class M extends ViciousCheckOvWbI {
  override function expect() { if (n != 5 and n < 8) [ n, [0..n], 14, [-n..0], n ] else  [ n*10, n, n*n, n*n ] }
  override var content =  bind if (n != 5 and n < 8) [ n, [0..n], 14, [-n..0], n ] else  [ n*10, n, n*n, n*n ];
}

class N extends ViciousCheckOvWbI {
  override function expect() { if (n > 2 and n < 7) [n*3] else [n] }
  override var content =  bind if (n > 2 and n < 7) [n*3] else [n];
}

class ViciousBSOvWbI1 extends ViciousEngineOvWbI {

  //override var verbose = 2;

  override function tests() {
    [ A{}, B{}, C{}, D{}, E{}, F{}, G{}, H{}, I{}, J{}, K{}, L{}, M{}, N{} ]
  }
}

ViciousBSOvWbI1{}.test()
