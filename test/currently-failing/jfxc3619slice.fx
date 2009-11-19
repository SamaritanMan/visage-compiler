/**
 *  jfxc-3619: Compiled bind: dependent state/mode -- non-bound initalizers of bound object literal
 *
 *  Assure that new objects are created when a non bound initializer changes, and that
 *  the initted ivar contains the updated value.
 *
 * @test/fail
 * @run
 */
class Foo {
    var xx: Integer;
    var seqx: Integer[];
    var strx: String;
    var foox: Foo;
}

function bar(p1: Integer) : Integer {
   return p1;
}

var aa: Integer = 1;
var bb: Integer = 2;
var hash: Integer;
var test: String;
var seqy = [0..10];

// The following tests should all cause a new Foo instance to be created when
// aa changes.


print("vv1: seqy[0..<aa]");
aa = 1;
def vv1 = bind Foo{seqx:                 seqy[0..<aa]};
hash = java.lang.System.identityHashCode(vv1);
aa = 2;
if (hash == java.lang.System.identityHashCode(vv1)) {
    print(": failed");
}
if (vv1.seqx != seqy[0..<aa]) {
    print(": failed seqx = {vv1.seqx}");
}
println("");


print("vv2: seqy[yy | yy < aa]");
aa = 1;
def vv2 = bind Foo{seqx:                 seqy[yy | yy < aa]};
hash = java.lang.System.identityHashCode(vv2);
aa = 2;
if (hash == java.lang.System.identityHashCode(vv2)) {
    print(": failed");
}
if (vv2.seqx != seqy[yy | yy < aa]) {
    print(": failed seqx = {vv2.seqx}");
}
println("");


println("All Done");
