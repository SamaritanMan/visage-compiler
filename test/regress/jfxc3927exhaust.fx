/*
 * Regression test 
 * JFXC-3927 : SWAT failure : JawbreakerGame compiler crash -- JavafxClassSymbol cannot be cast to JavafxVarSymbol
 *
 * Exhaustive test of references to external script-level var
 *
 * @compilefirst jfxc3927exhaustSub.fx
 * @test
 * @run
 */

//assign
jfxc3927exhaustSub.i = 7;
jfxc3927exhaustSub.ibi = 7;
jfxc3927exhaustSub.o = 7;
jfxc3927exhaustSub.obi = 7;
jfxc3927exhaustSub.s = 7;
jfxc3927exhaustSub.sbi = 7;

//assign op
jfxc3927exhaustSub.i += 7;
jfxc3927exhaustSub.ibi += 7;

// binary
var x1 = jfxc3927exhaustSub.i + jfxc3927exhaustSub.id + jfxc3927exhaustSub.ib + jfxc3927exhaustSub.ibi;
var x1b = bind jfxc3927exhaustSub.i + jfxc3927exhaustSub.id + jfxc3927exhaustSub.ib + jfxc3927exhaustSub.ibi;

// for
var x2 = for (i in jfxc3927exhaustSub.sbi where i <= jfxc3927exhaustSub.i) { "burp" };
var x2b = bind for (i in jfxc3927exhaustSub.sbi where i <= jfxc3927exhaustSub.i) { "burp" };

// if
var x3 = if (jfxc3927exhaustSub.b) jfxc3927exhaustSub.i else jfxc3927exhaustSub.id;
var x3b = bind if (jfxc3927exhaustSub.b) jfxc3927exhaustSub.i else jfxc3927exhaustSub.id;;
var x3bs = bind if (jfxc3927exhaustSub.b) jfxc3927exhaustSub.s else jfxc3927exhaustSub.sd;;

// instanceof
var x4 = jfxc3927exhaustSub.o instanceof Integer;
var x4b = bind jfxc3927exhaustSub.o instanceof Integer;

// member select
var x5 = jfxc3927exhaustSub.c.x;
var x5b = bind jfxc3927exhaustSub.c.x;
var x5bb = bind jfxc3927exhaustSub.cb.x;
var x5bi = bind jfxc3927exhaustSub.c.x with inverse;

// sequence delete
delete jfxc3927exhaustSub.id from jfxc3927exhaustSub.s;
delete jfxc3927exhaustSub.s[0];
delete jfxc3927exhaustSub.s;

// sequence explicit
var x6 = [jfxc3927exhaustSub.id, jfxc3927exhaustSub.odb, jfxc3927exhaustSub.c.x, jfxc3927exhaustSub.sd];
var x6b = bind [jfxc3927exhaustSub.id, jfxc3927exhaustSub.odb, jfxc3927exhaustSub.c.x, jfxc3927exhaustSub.sd];

// sequence indexed
var x7 = jfxc3927exhaustSub.sd[jfxc3927exhaustSub.id];
var x7b = bind jfxc3927exhaustSub.sd[jfxc3927exhaustSub.id];

// sequence insert
insert jfxc3927exhaustSub.id into jfxc3927exhaustSub.s;

// sequence range
var x8 = [jfxc3927exhaustSub.id .. jfxc3927exhaustSub.i step jfxc3927exhaustSub.id];
var x8b = bind [jfxc3927exhaustSub.id .. jfxc3927exhaustSub.i step jfxc3927exhaustSub.id];

// sequence slice
var x9 = jfxc3927exhaustSub.sd[jfxc3927exhaustSub.id .. jfxc3927exhaustSub.i];
var x9b = bind jfxc3927exhaustSub.sd[jfxc3927exhaustSub.id .. jfxc3927exhaustSub.i];

// unary
var xa = reverse jfxc3927exhaustSub.sdb;
var xab = bind reverse jfxc3927exhaustSub.sdb;
var xax = ++jfxc3927exhaustSub.i;
var xax2 = jfxc3927exhaustSub.i--;
var xan = not jfxc3927exhaustSub.b;

function foo() {
//assign
jfxc3927exhaustSub.i = 7;
jfxc3927exhaustSub.ibi = 7;
jfxc3927exhaustSub.o = 7;
jfxc3927exhaustSub.obi = 7;
jfxc3927exhaustSub.s = 7;
jfxc3927exhaustSub.sbi = 7;

//assign op
jfxc3927exhaustSub.i += 7;
jfxc3927exhaustSub.ibi += 7;

// binary
var v1 = jfxc3927exhaustSub.i + jfxc3927exhaustSub.id + jfxc3927exhaustSub.ib + jfxc3927exhaustSub.ibi;
var v1b = bind jfxc3927exhaustSub.i + jfxc3927exhaustSub.id + jfxc3927exhaustSub.ib + jfxc3927exhaustSub.ibi;

// for
var v2 = for (i in jfxc3927exhaustSub.sbi where i <= jfxc3927exhaustSub.i) { "burp" };
var v2b = bind for (i in jfxc3927exhaustSub.sbi where i <= jfxc3927exhaustSub.i) { "burp" };

// if
var v3 = if (jfxc3927exhaustSub.b) jfxc3927exhaustSub.i else jfxc3927exhaustSub.id;
var v3b = bind if (jfxc3927exhaustSub.b) jfxc3927exhaustSub.i else jfxc3927exhaustSub.id;;
var v3bs = bind if (jfxc3927exhaustSub.b) jfxc3927exhaustSub.s else jfxc3927exhaustSub.sd;;

// instanceof
var v4 = jfxc3927exhaustSub.o instanceof Integer;
var v4b = bind jfxc3927exhaustSub.o instanceof Integer;

// member select
var v5 = jfxc3927exhaustSub.c.x;
var v5b = bind jfxc3927exhaustSub.c.x;
var v5bb = bind jfxc3927exhaustSub.cb.x;
var v5bi = bind jfxc3927exhaustSub.c.x with inverse;

// sequence delete
delete jfxc3927exhaustSub.id from jfxc3927exhaustSub.s;
delete jfxc3927exhaustSub.s[0];
delete jfxc3927exhaustSub.s;

// sequence explicit
var v6 = [jfxc3927exhaustSub.id, jfxc3927exhaustSub.odb, jfxc3927exhaustSub.c.x, jfxc3927exhaustSub.sd];
var v6b = bind [jfxc3927exhaustSub.id, jfxc3927exhaustSub.odb, jfxc3927exhaustSub.c.x, jfxc3927exhaustSub.sd];

// sequence indexed
var v7 = jfxc3927exhaustSub.sd[jfxc3927exhaustSub.id];
var v7b = bind jfxc3927exhaustSub.sd[jfxc3927exhaustSub.id];

// sequence insert
insert jfxc3927exhaustSub.id into jfxc3927exhaustSub.s;

// sequence range
var v8 = [jfxc3927exhaustSub.id .. jfxc3927exhaustSub.i step jfxc3927exhaustSub.id];
var v8b = bind [jfxc3927exhaustSub.id .. jfxc3927exhaustSub.i step jfxc3927exhaustSub.id];

// sequence slice
var v9 = jfxc3927exhaustSub.sd[jfxc3927exhaustSub.id .. jfxc3927exhaustSub.i];
var v9b = bind jfxc3927exhaustSub.sd[jfxc3927exhaustSub.id .. jfxc3927exhaustSub.i];

// unary
var va = reverse jfxc3927exhaustSub.sdb;
var vab = bind reverse jfxc3927exhaustSub.sdb;
var vax = ++jfxc3927exhaustSub.i;
var vax2 = jfxc3927exhaustSub.i--;
var van = not jfxc3927exhaustSub.b;

println(jfxc3927exhaustSub.i);
println(jfxc3927exhaustSub.id);
println(jfxc3927exhaustSub.ib);
println(jfxc3927exhaustSub.idb);
println(jfxc3927exhaustSub.ibi);

println(jfxc3927exhaustSub.o);
println(jfxc3927exhaustSub.od);
println(jfxc3927exhaustSub.ob);
println(jfxc3927exhaustSub.odb);
println(jfxc3927exhaustSub.obi);

println(jfxc3927exhaustSub.s);
println(jfxc3927exhaustSub.sd);
println(jfxc3927exhaustSub.sb);
println(jfxc3927exhaustSub.sdb);
println(jfxc3927exhaustSub.sbi);

println(jfxc3927exhaustSub.b);

println(v1);
println(v1b);
println(v2);
println(v2b);
println(v3);
println(v3b);
println(v3bs);
println(v4);
println(v4b);
println(v5);
println(v5b);
println(v5bb);
println(v5bi);
println(v6);
println(v6b);
println(v7b);
println(v8);
println(v8b);
println(v9);
println(v9b);
println(va);
println(vab);
println(vax);
println(vax2);
println(van);

}


println(x1);
println(x1b);
println(x2);
println(x2b);
println(x3);
println(x3b);
println(x3bs);
println(x4);
println(x4b);
println(x5);
println(x5b);
println(x5bb);
println(x5bi);
println(x6);
println(x6b);
println(x7b);
println(x8);
println(x8b);
println(x9);
println(x9b);
println(xa);
println(xab);
println(xax);
println(xax2);
println(xan);

foo();