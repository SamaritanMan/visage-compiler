/*
 * jfxc2410 - Predefined type names for {Integer,Double,Float...}
 * @test
 * @run
 */
def bDebug: Boolean=false;
function debugOut(msg:String){if(bDebug)println(msg)}
var c:Character on replace { print(c) };
var b:Byte  on replace { println("b={b}") };
var s:Short  on replace { println("s={s}") };
var s2:Short = 32;
var s3:Short = 512;
var i:Integer on replace { println("i={i}") };
var l:Long  on replace { println("l={l}") };
var d:Double on replace { println("d={d}") };
var f:Float on replace { println("f={f}") };
//*
for(i in [97..122]) c=i;
c=99;
println("");
b=64;
s=2048;
i=4096;
l=32768;
d=16.14;
f=0.64e2; //ie. 64
//*/
class Nclass {
   var c:Character on replace { print(c) };
   var cfoo:Character;
   var b:Byte  on replace { println("nc.b={b}") };
   var s:Short  on replace { println("nc.s={s}") };
   var i:Integer on replace { println("nc.i={i}") };
   var l:Long  on replace { println("nc.l={l}") };
   var d:Double on replace { println("nc.d={d}") };
   var f:Float on replace { println("nc.f={f}") };

   init {
    println("init");
    println("Nclass.c={c}");
    println("Nclass.b={b}");
    println("Nclass.s={s}");
    println("Nclass.i={i}");
    println("Nclass.l={l}");
    println("Nclass.d={d}");
    println("Nclass.f={f}");
    println("reassign");
    for(i in [65..90]) c=i;
    println("");
    b=8;
    s=127;
    i=2147483647;
    l=32768;
    d=3.14;
    f=6.02e23;
   };

   function setc( c1:Character ):Void { cfoo=c1;}
}
var nc : Nclass = new Nclass();

nc.cfoo = 98;
//nc.setc(98); //bug 2535

//simple function into which to pass different types, they could do anything...just need parm types.


function bcheck( b1:Byte, b2:Byte):Void {
 if ( b1 != b2 ) { println("ERROR: {b1} != {b2}"); }
}

function scheck( s1:Short, s2:Short):Void {
 if ( s1 != s2 ) { println("ERROR: {s1} != {s2}"); }
}

function icheck( i1:Integer, i2:Integer):Void {
 if ( i1 != i2 ) { println("ERROR: {i1} != {i2}"); }
}

function lcheck( l1:Long, l2:Long):Void {
 if ( l1 != l2 ) { println("ERROR: {l1} != {l2}"); }
}


function dcheck( d1:Double, d2:Double):Void {
 if ( d1 != d2 ) { println("ERROR: {d1} != {d2}"); }
}

function fcheck( f1:Float, f2:Float):Void {
 if ( f1 != f2 ) { println("ERROR: {f1} != {f2}"); }
}


debugOut("bcheck");
bcheck(b,b);
bcheck(b,i/64);
bcheck(i/64,i/64);
bcheck(b,l/s3);
bcheck(b,s/s2);
//bcheck(b,c);  //error
bcheck(b/4,d);
bcheck(b,f);

scheck(b,b);
scheck(b,i/64);
scheck(i,i);
scheck(b,l/512);
scheck(b*32,s);
//scheck(b,c);//scheck(s1:Integer,s2:Integer) in Main cannot be applied to (Integer,char)
scheck(b,d*4);
scheck(b,f);


debugOut("icheck");
icheck(b,b);
icheck(b,(i as Byte)+64);  // i as Byte == 0
icheck(i,i);
icheck(b,l/512);
icheck(b,s/32);
icheck(b,(c as Double)-35.0);
icheck(b*1.0/4,d);
icheck(b,f);

debugOut("lcheck");
lcheck(b,b);
lcheck(b*64,i);
lcheck(i,i);
lcheck(b*512,l);
lcheck(b,s/32);
lcheck(b,(c as Long)-35);
lcheck(b/4,d);
lcheck(b,f);

//Long and Float do not "fit" into Integer parameter
debugOut("dcheck");
dcheck(b,b);
dcheck(b,i/64);
dcheck(i,i);
dcheck(b,l/512);
dcheck(b*32.0,s);
dcheck( (b+35) as Character,c);
dcheck(b,(d as Integer)*4);
dcheck(b,f);

debugOut("fcheck");
fcheck(b,b);
fcheck(b,i/b);
fcheck(i,i);
fcheck(b,l/512);
fcheck(b,s/32);
fcheck(b, (c as Short) - 35);
fcheck(b,(d as Byte) * 4);
fcheck(b,f);

//all these are okay
println("c={c}");
println("b={b}");
println("s={s}");
println("i={i}");
println("l={l}");
println("d={d}");
println("f={f}");

b=1;
s=2;
i=32;
l=123;
d=3.14;
f=6.23e23;
println("b={b}");
println("s={s}");
println("i={i}");
println("l={l}");
println("d={d}");
println("f={f}");

l=d;
i=l;
s=i;
b=s;
println("b={b}");
println("s={s}");
println("i={i}");
println("l={l}");
println("d={d}");
println("f={f}");


/*
 * jfxc2442 - conversions between numeric sequences
 */
var bs : Byte[];
var ls : Long[];
var fs : Float[];
var ds : Double[];

def bc : Byte[] = [1, 2, 3];
def lc : Long[] = [1234, 5678];
def fc : Float[] = [3.14, 6.23];
def dc : Double[] = [1.23456789, 9.999999991];

bs = dc;
println( bs );
ls = fc;
println( ls );
fs = ds;
println( fs );
fs = bs;
println( fs );
ds = fs;
println( ds );
ds = ls;
println( ds );

