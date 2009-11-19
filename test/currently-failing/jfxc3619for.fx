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
    var seqx: Integer[];
}

function bar(p1: Integer) : Integer {
   return p1;
}

var aa: Integer = 1;
var hash: Integer;


print("vv1: for (ii in [aa..2]) ii");
aa = 1;
def vv1 = bind Foo{seqx:               for (ii in [aa..2]) ii};
hash = java.lang.System.identityHashCode(vv1);
aa = 1;
if (hash != java.lang.System.identityHashCode(vv1)) {
    print(": ********* failed !=");
}
aa = 2;
if (hash == java.lang.System.identityHashCode(vv1)) {
    print(": ********* failed ==");
}
if (vv1.seqx != ( for (ii in [aa..2]) ii)) {
    print(": ********* failed seqx = {vv1.seqx}");
}
println("");


print("vv2: for (ii in [aa..2] where ii == 2) ii");
aa = 1;
def vv2 = bind Foo{seqx:               for (ii in [aa..2] where ii == 2) ii};
hash = java.lang.System.identityHashCode(vv2);
aa = 1;
if (hash != java.lang.System.identityHashCode(vv2)) {
    print(": ********* failed !=");
}
aa = 2;
if (hash == java.lang.System.identityHashCode(vv2)) {
    print(": ********* failed ==");
}
if (vv2.seqx != (for (ii in [aa..2] where ii == 2) ii)) {
    print(": ********* failed seqx = {vv2.seqx}");
}
println("");


print("vv3: for (ii in [0..2] where ii == aa) ii");
aa = 1;
def vv3 = bind Foo{seqx:               for (ii in [0..2] where ii == aa) ii};
hash = java.lang.System.identityHashCode(vv3);
aa = 1;
if (hash != java.lang.System.identityHashCode(vv3)) {
    print(": ********* failed !=");
}
aa = 2;
if (hash == java.lang.System.identityHashCode(vv3)) {
    print(": ********* failed ==");
}
if (vv3.seqx != (for (ii in [0..2] where ii == aa) ii)) {
    print(": ********* failed seqx = {vv3.seqx}");
}
println("");


print("vv4: for (ii in [0..2] where ii == 2) aa");
aa = 1;
def vv4 = bind Foo{seqx:               for (ii in [0..2] where ii == 2) aa};
hash = java.lang.System.identityHashCode(vv4);
aa = 1;
if (hash != java.lang.System.identityHashCode(vv4)) {
    print(": ********* failed !=");
}
aa = 2;
if (hash == java.lang.System.identityHashCode(vv4)) {
    print(": ********* failed ==");
}
if (vv4.seqx != (for (ii in [0..2] where ii == 2) aa)) {
    print(": ********* failed seqx = {vv4.seqx}");
}
println("");


// this translates into a for
print("vv5: seqy[yy | yy < aa]");
var seqy = [0..10];
aa = 1;
def vv5 = bind Foo{seqx:               seqy[yy | yy < aa]};
hash = java.lang.System.identityHashCode(vv5);
aa = 1;
if (hash != java.lang.System.identityHashCode(vv5)) {
    print(": ********* failed !=");
}
aa = 2;
if (hash == java.lang.System.identityHashCode(vv5)) {
    print(": ********* failed ==");
}
if (vv5.seqx != (seqy[yy | yy < aa])) {
    print(": ********* failed seqx = {vv5.seqx}");
}
println("");

println("All Done");

