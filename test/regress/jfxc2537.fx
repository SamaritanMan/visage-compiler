/**
 * JFXC-2537 : element access for byte, short, long, double bound sequences
 *
 * @test
 * @run
 */

var bx : Byte[] = [1, 2, 3, 4];
var sx : Short[] = [1, 2, 3, 4];
var lx : Long[] = [1, 2, 3, 4];
var dx : Double[] = [1.25, 2.50, 3.75, 4.0];

var i = 0;

def bb = bind bx[i];
def sb = bind sx[i];
def lb = bind lx[i];
def db = bind dx[i];

println("b: {bb}, s: {sb}, l:{lb}, d: {db}");
i = 2;
println("b: {bb}, s: {sb}, l:{lb}, d: {db}");
bx[2] = 99;
sx[2] = 88;
lx[2] = 77;
dx[2] = 3.125;
println("b: {bb}, s: {sb}, l:{lb}, d: {db}");
