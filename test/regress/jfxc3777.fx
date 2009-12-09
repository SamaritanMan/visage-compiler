/**
 * JFXC-3777 : Compiled bind: prevent duplicate initial on-replace in bound select of explicit sequence
 *
 * @test
 * @run
 */

class A {
  var si : Integer[] on replace [a..b] = newVal { print("on replace [{a}..{b}] = "); println(newVal) }
}

class jfxc3777 {
  var bz = bind [1, 2, 3];

  var bi = A { si : bind bz }
}

println(jfxc3777{}.bi.si)
