/**
 * JFXC-3761 : Compiled bind: cannot find symbol jfx$2pse ; caused by ref to a script level var inside a for inClause
 *
 * @test
 */

class A {
   var x: Integer;
}

var a: A;
for (i in [0..jfxc3761.a.x]) {0};
