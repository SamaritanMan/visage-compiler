/**
 * Regression test JFXC-2466 : Lazy Binding: Operators -- runtime
 *
 * @test
 * @run
 */

var by : Byte = 1;
var sh : Short = 10;
var ii : Integer = 100;
var lo : Long = 2147383648;
var fl : Float = 1234.5;
var db : Double = 6.12E40;
function tby(x : Byte) : Byte { println("by = {x}"); x };
function tsh(x : Short) : Short { println("sh = {x}"); x };
function tii(x : Integer) : Integer { println("ii = {x}"); x };
function tlo(x : Long) : Long { println("lo = {x}"); x };
function tfl(x : Float) : Float { println("fl = {x}"); x };
function tdb(x : Double) : Double { println("db = {x}"); x };
def by_by_plus = bind lazy tby(by) + tby(by);
def by_by_minus = bind lazy tby(by) - tby(by);
def by_by_mult = bind lazy tby(by) * tby(by);
def by_by_div = bind lazy tby(by) / tby(by);
def by_by_mod = bind lazy tby(by) mod tby(by);
def by_sh_plus = bind lazy tby(by) + tsh(sh);
def by_sh_minus = bind lazy tby(by) - tsh(sh);
def by_sh_mult = bind lazy tby(by) * tsh(sh);
def by_sh_div = bind lazy tby(by) / tsh(sh);
def by_sh_mod = bind lazy tby(by) mod tsh(sh);
def by_ii_plus = bind lazy tby(by) + tii(ii);
def by_ii_minus = bind lazy tby(by) - tii(ii);
def by_ii_mult = bind lazy tby(by) * tii(ii);
def by_ii_div = bind lazy tby(by) / tii(ii);
def by_ii_mod = bind lazy tby(by) mod tii(ii);
def by_lo_plus = bind lazy tby(by) + tlo(lo);
def by_lo_minus = bind lazy tby(by) - tlo(lo);
def by_lo_mult = bind lazy tby(by) * tlo(lo);
def by_lo_div = bind lazy tby(by) / tlo(lo);
def by_lo_mod = bind lazy tby(by) mod tlo(lo);
def by_fl_plus = bind lazy tby(by) + tfl(fl);
def by_fl_minus = bind lazy tby(by) - tfl(fl);
def by_fl_mult = bind lazy tby(by) * tfl(fl);
def by_fl_div = bind lazy tby(by) / tfl(fl);
def by_fl_mod = bind lazy tby(by) mod tfl(fl);
def by_db_plus = bind lazy tby(by) + tdb(db);
def by_db_minus = bind lazy tby(by) - tdb(db);
def by_db_mult = bind lazy tby(by) * tdb(db);
def by_db_div = bind lazy tby(by) / tdb(db);
def by_db_mod = bind lazy tby(by) mod tdb(db);
def sh_by_plus = bind lazy tsh(sh) + tby(by);
def sh_by_minus = bind lazy tsh(sh) - tby(by);
def sh_by_mult = bind lazy tsh(sh) * tby(by);
def sh_by_div = bind lazy tsh(sh) / tby(by);
def sh_by_mod = bind lazy tsh(sh) mod tby(by);
def sh_sh_plus = bind lazy tsh(sh) + tsh(sh);
def sh_sh_minus = bind lazy tsh(sh) - tsh(sh);
def sh_sh_mult = bind lazy tsh(sh) * tsh(sh);
def sh_sh_div = bind lazy tsh(sh) / tsh(sh);
def sh_sh_mod = bind lazy tsh(sh) mod tsh(sh);
def sh_ii_plus = bind lazy tsh(sh) + tii(ii);
def sh_ii_minus = bind lazy tsh(sh) - tii(ii);
def sh_ii_mult = bind lazy tsh(sh) * tii(ii);
def sh_ii_div = bind lazy tsh(sh) / tii(ii);
def sh_ii_mod = bind lazy tsh(sh) mod tii(ii);
def sh_lo_plus = bind lazy tsh(sh) + tlo(lo);
def sh_lo_minus = bind lazy tsh(sh) - tlo(lo);
def sh_lo_mult = bind lazy tsh(sh) * tlo(lo);
def sh_lo_div = bind lazy tsh(sh) / tlo(lo);
def sh_lo_mod = bind lazy tsh(sh) mod tlo(lo);
def sh_fl_plus = bind lazy tsh(sh) + tfl(fl);
def sh_fl_minus = bind lazy tsh(sh) - tfl(fl);
def sh_fl_mult = bind lazy tsh(sh) * tfl(fl);
def sh_fl_div = bind lazy tsh(sh) / tfl(fl);
def sh_fl_mod = bind lazy tsh(sh) mod tfl(fl);
def sh_db_plus = bind lazy tsh(sh) + tdb(db);
def sh_db_minus = bind lazy tsh(sh) - tdb(db);
def sh_db_mult = bind lazy tsh(sh) * tdb(db);
def sh_db_div = bind lazy tsh(sh) / tdb(db);
def sh_db_mod = bind lazy tsh(sh) mod tdb(db);
def ii_by_plus = bind lazy tii(ii) + tby(by);
def ii_by_minus = bind lazy tii(ii) - tby(by);
def ii_by_mult = bind lazy tii(ii) * tby(by);
def ii_by_div = bind lazy tii(ii) / tby(by);
def ii_by_mod = bind lazy tii(ii) mod tby(by);
def ii_sh_plus = bind lazy tii(ii) + tsh(sh);
def ii_sh_minus = bind lazy tii(ii) - tsh(sh);
def ii_sh_mult = bind lazy tii(ii) * tsh(sh);
def ii_sh_div = bind lazy tii(ii) / tsh(sh);
def ii_sh_mod = bind lazy tii(ii) mod tsh(sh);
def ii_ii_plus = bind lazy tii(ii) + tii(ii);
def ii_ii_minus = bind lazy tii(ii) - tii(ii);
def ii_ii_mult = bind lazy tii(ii) * tii(ii);
def ii_ii_div = bind lazy tii(ii) / tii(ii);
def ii_ii_mod = bind lazy tii(ii) mod tii(ii);
def ii_lo_plus = bind lazy tii(ii) + tlo(lo);
def ii_lo_minus = bind lazy tii(ii) - tlo(lo);
def ii_lo_mult = bind lazy tii(ii) * tlo(lo);
def ii_lo_div = bind lazy tii(ii) / tlo(lo);
def ii_lo_mod = bind lazy tii(ii) mod tlo(lo);
def ii_fl_plus = bind lazy tii(ii) + tfl(fl);
def ii_fl_minus = bind lazy tii(ii) - tfl(fl);
def ii_fl_mult = bind lazy tii(ii) * tfl(fl);
def ii_fl_div = bind lazy tii(ii) / tfl(fl);
def ii_fl_mod = bind lazy tii(ii) mod tfl(fl);
def ii_db_plus = bind lazy tii(ii) + tdb(db);
def ii_db_minus = bind lazy tii(ii) - tdb(db);
def ii_db_mult = bind lazy tii(ii) * tdb(db);
def ii_db_div = bind lazy tii(ii) / tdb(db);
def ii_db_mod = bind lazy tii(ii) mod tdb(db);
def lo_by_plus = bind lazy tlo(lo) + tby(by);
def lo_by_minus = bind lazy tlo(lo) - tby(by);
def lo_by_mult = bind lazy tlo(lo) * tby(by);
def lo_by_div = bind lazy tlo(lo) / tby(by);
def lo_by_mod = bind lazy tlo(lo) mod tby(by);
def lo_sh_plus = bind lazy tlo(lo) + tsh(sh);
def lo_sh_minus = bind lazy tlo(lo) - tsh(sh);
def lo_sh_mult = bind lazy tlo(lo) * tsh(sh);
def lo_sh_div = bind lazy tlo(lo) / tsh(sh);
def lo_sh_mod = bind lazy tlo(lo) mod tsh(sh);
def lo_ii_plus = bind lazy tlo(lo) + tii(ii);
def lo_ii_minus = bind lazy tlo(lo) - tii(ii);
def lo_ii_mult = bind lazy tlo(lo) * tii(ii);
def lo_ii_div = bind lazy tlo(lo) / tii(ii);
def lo_ii_mod = bind lazy tlo(lo) mod tii(ii);
def lo_lo_plus = bind lazy tlo(lo) + tlo(lo);
def lo_lo_minus = bind lazy tlo(lo) - tlo(lo);
def lo_lo_mult = bind lazy tlo(lo) * tlo(lo);
def lo_lo_div = bind lazy tlo(lo) / tlo(lo);
def lo_lo_mod = bind lazy tlo(lo) mod tlo(lo);
def lo_fl_plus = bind lazy tlo(lo) + tfl(fl);
def lo_fl_minus = bind lazy tlo(lo) - tfl(fl);
def lo_fl_mult = bind lazy tlo(lo) * tfl(fl);
def lo_fl_div = bind lazy tlo(lo) / tfl(fl);
def lo_fl_mod = bind lazy tlo(lo) mod tfl(fl);
def lo_db_plus = bind lazy tlo(lo) + tdb(db);
def lo_db_minus = bind lazy tlo(lo) - tdb(db);
def lo_db_mult = bind lazy tlo(lo) * tdb(db);
def lo_db_div = bind lazy tlo(lo) / tdb(db);
def lo_db_mod = bind lazy tlo(lo) mod tdb(db);
def fl_by_plus = bind lazy tfl(fl) + tby(by);
def fl_by_minus = bind lazy tfl(fl) - tby(by);
def fl_by_mult = bind lazy tfl(fl) * tby(by);
def fl_by_div = bind lazy tfl(fl) / tby(by);
def fl_by_mod = bind lazy tfl(fl) mod tby(by);
def fl_sh_plus = bind lazy tfl(fl) + tsh(sh);
def fl_sh_minus = bind lazy tfl(fl) - tsh(sh);
def fl_sh_mult = bind lazy tfl(fl) * tsh(sh);
def fl_sh_div = bind lazy tfl(fl) / tsh(sh);
def fl_sh_mod = bind lazy tfl(fl) mod tsh(sh);
def fl_ii_plus = bind lazy tfl(fl) + tii(ii);
def fl_ii_minus = bind lazy tfl(fl) - tii(ii);
def fl_ii_mult = bind lazy tfl(fl) * tii(ii);
def fl_ii_div = bind lazy tfl(fl) / tii(ii);
def fl_ii_mod = bind lazy tfl(fl) mod tii(ii);
def fl_lo_plus = bind lazy tfl(fl) + tlo(lo);
def fl_lo_minus = bind lazy tfl(fl) - tlo(lo);
def fl_lo_mult = bind lazy tfl(fl) * tlo(lo);
def fl_lo_div = bind lazy tfl(fl) / tlo(lo);
def fl_lo_mod = bind lazy tfl(fl) mod tlo(lo);
def fl_fl_plus = bind lazy tfl(fl) + tfl(fl);
def fl_fl_minus = bind lazy tfl(fl) - tfl(fl);
def fl_fl_mult = bind lazy tfl(fl) * tfl(fl);
def fl_fl_div = bind lazy tfl(fl) / tfl(fl);
def fl_fl_mod = bind lazy tfl(fl) mod tfl(fl);
def fl_db_plus = bind lazy tfl(fl) + tdb(db);
def fl_db_minus = bind lazy tfl(fl) - tdb(db);
def fl_db_mult = bind lazy tfl(fl) * tdb(db);
def fl_db_div = bind lazy tfl(fl) / tdb(db);
def fl_db_mod = bind lazy tfl(fl) mod tdb(db);
def db_by_plus = bind lazy tdb(db) + tby(by);
def db_by_minus = bind lazy tdb(db) - tby(by);
def db_by_mult = bind lazy tdb(db) * tby(by);
def db_by_div = bind lazy tdb(db) / tby(by);
def db_by_mod = bind lazy tdb(db) mod tby(by);
def db_sh_plus = bind lazy tdb(db) + tsh(sh);
def db_sh_minus = bind lazy tdb(db) - tsh(sh);
def db_sh_mult = bind lazy tdb(db) * tsh(sh);
def db_sh_div = bind lazy tdb(db) / tsh(sh);
def db_sh_mod = bind lazy tdb(db) mod tsh(sh);
def db_ii_plus = bind lazy tdb(db) + tii(ii);
def db_ii_minus = bind lazy tdb(db) - tii(ii);
def db_ii_mult = bind lazy tdb(db) * tii(ii);
def db_ii_div = bind lazy tdb(db) / tii(ii);
def db_ii_mod = bind lazy tdb(db) mod tii(ii);
def db_lo_plus = bind lazy tdb(db) + tlo(lo);
def db_lo_minus = bind lazy tdb(db) - tlo(lo);
def db_lo_mult = bind lazy tdb(db) * tlo(lo);
def db_lo_div = bind lazy tdb(db) / tlo(lo);
def db_lo_mod = bind lazy tdb(db) mod tlo(lo);
def db_fl_plus = bind lazy tdb(db) + tfl(fl);
def db_fl_minus = bind lazy tdb(db) - tfl(fl);
def db_fl_mult = bind lazy tdb(db) * tfl(fl);
def db_fl_div = bind lazy tdb(db) / tfl(fl);
def db_fl_mod = bind lazy tdb(db) mod tfl(fl);
def db_db_plus = bind lazy tdb(db) + tdb(db);
def db_db_minus = bind lazy tdb(db) - tdb(db);
def db_db_mult = bind lazy tdb(db) * tdb(db);
def db_db_div = bind lazy tdb(db) / tdb(db);
def db_db_mod = bind lazy tdb(db) mod tdb(db);
by = 4;
sh = 4;
ii = 4;
lo = 4;
fl = 4;
db = 4;
by = 72;
sh = 72;
ii = 72;
lo = 72;
fl = 72;
db = 72;
by = 1;
sh = 10;
ii = 100;
lo = 2147383648;
fl = 1234.5;
db = 6.12E40;
println( "by plus by = {by_by_plus}" );
println( "by minus by = {by_by_minus}" );
println( "by mult by = {by_by_mult}" );
println( "by div by = {by_by_div}" );
println( "by mod by = {by_by_mod}" );
println( "by plus sh = {by_sh_plus}" );
println( "by minus sh = {by_sh_minus}" );
println( "by mult sh = {by_sh_mult}" );
println( "by div sh = {by_sh_div}" );
println( "by mod sh = {by_sh_mod}" );
println( "by plus ii = {by_ii_plus}" );
println( "by minus ii = {by_ii_minus}" );
println( "by mult ii = {by_ii_mult}" );
println( "by div ii = {by_ii_div}" );
println( "by mod ii = {by_ii_mod}" );
println( "by plus lo = {by_lo_plus}" );
println( "by minus lo = {by_lo_minus}" );
println( "by mult lo = {by_lo_mult}" );
println( "by div lo = {by_lo_div}" );
println( "by mod lo = {by_lo_mod}" );
println( "by plus fl = {by_fl_plus}" );
println( "by minus fl = {by_fl_minus}" );
println( "by mult fl = {by_fl_mult}" );
println( "by div fl = {by_fl_div}" );
println( "by mod fl = {by_fl_mod}" );
println( "by plus db = {by_db_plus}" );
println( "by minus db = {by_db_minus}" );
println( "by mult db = {by_db_mult}" );
println( "by div db = {by_db_div}" );
println( "by mod db = {by_db_mod}" );
println( "sh plus by = {sh_by_plus}" );
println( "sh minus by = {sh_by_minus}" );
println( "sh mult by = {sh_by_mult}" );
println( "sh div by = {sh_by_div}" );
println( "sh mod by = {sh_by_mod}" );
println( "sh plus sh = {sh_sh_plus}" );
println( "sh minus sh = {sh_sh_minus}" );
println( "sh mult sh = {sh_sh_mult}" );
println( "sh div sh = {sh_sh_div}" );
println( "sh mod sh = {sh_sh_mod}" );
println( "sh plus ii = {sh_ii_plus}" );
println( "sh minus ii = {sh_ii_minus}" );
println( "sh mult ii = {sh_ii_mult}" );
println( "sh div ii = {sh_ii_div}" );
println( "sh mod ii = {sh_ii_mod}" );
println( "sh plus lo = {sh_lo_plus}" );
println( "sh minus lo = {sh_lo_minus}" );
println( "sh mult lo = {sh_lo_mult}" );
println( "sh div lo = {sh_lo_div}" );
println( "sh mod lo = {sh_lo_mod}" );
println( "sh plus fl = {sh_fl_plus}" );
println( "sh minus fl = {sh_fl_minus}" );
println( "sh mult fl = {sh_fl_mult}" );
println( "sh div fl = {sh_fl_div}" );
println( "sh mod fl = {sh_fl_mod}" );
println( "sh plus db = {sh_db_plus}" );
println( "sh minus db = {sh_db_minus}" );
println( "sh mult db = {sh_db_mult}" );
println( "sh div db = {sh_db_div}" );
println( "sh mod db = {sh_db_mod}" );
println( "ii plus by = {ii_by_plus}" );
println( "ii minus by = {ii_by_minus}" );
println( "ii mult by = {ii_by_mult}" );
println( "ii div by = {ii_by_div}" );
println( "ii mod by = {ii_by_mod}" );
println( "ii plus sh = {ii_sh_plus}" );
println( "ii minus sh = {ii_sh_minus}" );
println( "ii mult sh = {ii_sh_mult}" );
println( "ii div sh = {ii_sh_div}" );
println( "ii mod sh = {ii_sh_mod}" );
println( "ii plus ii = {ii_ii_plus}" );
println( "ii minus ii = {ii_ii_minus}" );
println( "ii mult ii = {ii_ii_mult}" );
println( "ii div ii = {ii_ii_div}" );
println( "ii mod ii = {ii_ii_mod}" );
println( "ii plus lo = {ii_lo_plus}" );
println( "ii minus lo = {ii_lo_minus}" );
println( "ii mult lo = {ii_lo_mult}" );
println( "ii div lo = {ii_lo_div}" );
println( "ii mod lo = {ii_lo_mod}" );
println( "ii plus fl = {ii_fl_plus}" );
println( "ii minus fl = {ii_fl_minus}" );
println( "ii mult fl = {ii_fl_mult}" );
println( "ii div fl = {ii_fl_div}" );
println( "ii mod fl = {ii_fl_mod}" );
println( "ii plus db = {ii_db_plus}" );
println( "ii minus db = {ii_db_minus}" );
println( "ii mult db = {ii_db_mult}" );
println( "ii div db = {ii_db_div}" );
println( "ii mod db = {ii_db_mod}" );
println( "lo plus by = {lo_by_plus}" );
println( "lo minus by = {lo_by_minus}" );
println( "lo mult by = {lo_by_mult}" );
println( "lo div by = {lo_by_div}" );
println( "lo mod by = {lo_by_mod}" );
println( "lo plus sh = {lo_sh_plus}" );
println( "lo minus sh = {lo_sh_minus}" );
println( "lo mult sh = {lo_sh_mult}" );
println( "lo div sh = {lo_sh_div}" );
println( "lo mod sh = {lo_sh_mod}" );
println( "lo plus ii = {lo_ii_plus}" );
println( "lo minus ii = {lo_ii_minus}" );
println( "lo mult ii = {lo_ii_mult}" );
println( "lo div ii = {lo_ii_div}" );
println( "lo mod ii = {lo_ii_mod}" );
println( "lo plus lo = {lo_lo_plus}" );
println( "lo minus lo = {lo_lo_minus}" );
println( "lo mult lo = {lo_lo_mult}" );
println( "lo div lo = {lo_lo_div}" );
println( "lo mod lo = {lo_lo_mod}" );
println( "lo plus fl = {lo_fl_plus}" );
println( "lo minus fl = {lo_fl_minus}" );
println( "lo mult fl = {lo_fl_mult}" );
println( "lo div fl = {lo_fl_div}" );
println( "lo mod fl = {lo_fl_mod}" );
println( "lo plus db = {lo_db_plus}" );
println( "lo minus db = {lo_db_minus}" );
println( "lo mult db = {lo_db_mult}" );
println( "lo div db = {lo_db_div}" );
println( "lo mod db = {lo_db_mod}" );
println( "fl plus by = {fl_by_plus}" );
println( "fl minus by = {fl_by_minus}" );
println( "fl mult by = {fl_by_mult}" );
println( "fl div by = {fl_by_div}" );
println( "fl mod by = {fl_by_mod}" );
println( "fl plus sh = {fl_sh_plus}" );
println( "fl minus sh = {fl_sh_minus}" );
println( "fl mult sh = {fl_sh_mult}" );
println( "fl div sh = {fl_sh_div}" );
println( "fl mod sh = {fl_sh_mod}" );
println( "fl plus ii = {fl_ii_plus}" );
println( "fl minus ii = {fl_ii_minus}" );
println( "fl mult ii = {fl_ii_mult}" );
println( "fl div ii = {fl_ii_div}" );
println( "fl mod ii = {fl_ii_mod}" );
println( "fl plus lo = {fl_lo_plus}" );
println( "fl minus lo = {fl_lo_minus}" );
println( "fl mult lo = {fl_lo_mult}" );
println( "fl div lo = {fl_lo_div}" );
println( "fl mod lo = {fl_lo_mod}" );
println( "fl plus fl = {fl_fl_plus}" );
println( "fl minus fl = {fl_fl_minus}" );
println( "fl mult fl = {fl_fl_mult}" );
println( "fl div fl = {fl_fl_div}" );
println( "fl mod fl = {fl_fl_mod}" );
println( "fl plus db = {fl_db_plus}" );
println( "fl minus db = {fl_db_minus}" );
println( "fl mult db = {fl_db_mult}" );
println( "fl div db = {fl_db_div}" );
println( "fl mod db = {fl_db_mod}" );
println( "db plus by = {db_by_plus}" );
println( "db minus by = {db_by_minus}" );
println( "db mult by = {db_by_mult}" );
println( "db div by = {db_by_div}" );
println( "db mod by = {db_by_mod}" );
println( "db plus sh = {db_sh_plus}" );
println( "db minus sh = {db_sh_minus}" );
println( "db mult sh = {db_sh_mult}" );
println( "db div sh = {db_sh_div}" );
println( "db mod sh = {db_sh_mod}" );
println( "db plus ii = {db_ii_plus}" );
println( "db minus ii = {db_ii_minus}" );
println( "db mult ii = {db_ii_mult}" );
println( "db div ii = {db_ii_div}" );
println( "db mod ii = {db_ii_mod}" );
println( "db plus lo = {db_lo_plus}" );
println( "db minus lo = {db_lo_minus}" );
println( "db mult lo = {db_lo_mult}" );
println( "db div lo = {db_lo_div}" );
println( "db mod lo = {db_lo_mod}" );
println( "db plus fl = {db_fl_plus}" );
println( "db minus fl = {db_fl_minus}" );
println( "db mult fl = {db_fl_mult}" );
println( "db div fl = {db_fl_div}" );
println( "db mod fl = {db_fl_mod}" );
println( "db plus db = {db_db_plus}" );
println( "db minus db = {db_db_minus}" );
println( "db mult db = {db_db_mult}" );
println( "db div db = {db_db_div}" );
println( "db mod db = {db_db_mod}" );

/** gen above and expected 

var by : Byte = 1;
var sh : Short = 10;
var ii : Integer = 100;
var lo : Long = 2147383648;
var fl : Float = 1234.5;
var db : Double = 6.12e40;

class P {
  var n: String;
  var r: Object;
  var v: Double;
  var t: String;
  var isReal = false;
}

def vs = [
	P {n: 'by' r: by v: by t: 'Byte'},
	P {n: 'sh' r: sh v: sh t: 'Short'},
	P {n: 'ii' r: ii v: ii t: 'Integer'},
	P {n: 'lo' r: lo v: lo t: 'Long'},
	P {n: 'fl' r: fl v: fl t: 'Float'   isReal: true},
	P {n: 'db' r: db v: db t: 'Double'  isReal: true}
];

class O {
  var n: String;
  var m: String;
  var f: function(:Double,:Double):Double;
  var fl: function(:Long,:Long):Long;
}

def os = [
	O {n: '+' m: 'plus'  f: function(x,y) {x+y}      fl: function(x,y) {x+y}}
	O {n: '-' m: 'minus' f: function(x,y) {x-y}      fl: function(x,y) {x-y}}
	O {n: '*' m: 'mult'  f: function(x,y) {x*y}      fl: function(x,y) {x*y}}
	O {n: '/' m: 'div'   f: function(x,y) {x/y}      fl: function(x,y) {x/y}}
	O {n: 'mod' m: 'mod' f: function(x,y) {x mod y}  fl: function(x,y) {x mod y}}
];

for (v1 in vs) {
  println('var {v1.n} : {v1.t} = {v1.r};');
}

for (v1 in vs) {
  println('function t{v1.n}(x : {v1.t}) : {v1.t} \{ println("{v1.n} = \{x\}"); x \};');
}

for (v1 in vs, v2 in vs, o in os) {
  println('def {v1.n}_{v2.n}_{o.m} = bind lazy t{v1.n}({v1.n}) {o.n} t{v2.n}({v2.n});');
}

for (v1 in vs) {
  println('{v1.n} = 4;');
}

for (v1 in vs) {
  println('{v1.n} = 72;');
}

for (v1 in vs) {
  println('{v1.n} = {v1.r};');
}

for (v1 in vs, v2 in vs, o in os) {
  println('println( "{v1.n} {o.m} {v2.n} = \{{v1.n}_{v2.n}_{o.m}\}" );');
}

println('---------------------------------------');

function display(o : O, v1 : P, v2 : P) : String {
  return if (v1.isReal or v2.isReal)
    if (v1.n == 'db' or v2.n == 'db')
      '{o.f(v1.v, v2.v)}'
    else
      '{o.f(v1.v as Float, v2.v as Float) as Float}'
  else
    '{o.fl(v1.v as Long, v2.v as Long)}'
}

for (v1 in vs, v2 in vs, o in os) {
  println('{v1.n} = {v1.r}');
  println('{v2.n} = {v2.r}');
  println('{v1.n} {o.m} {v2.n} = {display(o, v1, v2)}');
}

****/
