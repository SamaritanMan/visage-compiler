/*
 * JFXC-4147: non-bound sequence object literal initializer for var bound in class is broken
 *
 * @test
 * @run
 */

public class A {
  public var n = "Wilbur";
  public var seq = bind [n]
}

class B {
  var a = A {
    seq: ["Orr"]
  }
}

function run() {
  println(B{}.a.seq[0])
}
