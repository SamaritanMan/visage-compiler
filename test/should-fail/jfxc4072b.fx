/*
 * Regression test: Forward reference check overhaul
 *
 * @test/warning
 */

class Base {
   var a:Integer;
   var b:Integer;
   var c:Integer;
   var d:Integer;
   var e:Integer;
   var f:Integer;
   var g:function():Integer;
   var h:Integer;
   var i:Object[];
   var l:Integer;
   var m:Integer;
   var n:Integer;
   var o:Integer;
   var p:Integer;
   var q:Integer;
   var r:Integer;
}

class A extends Base {
   override var a = b; //warn
   override var b = this.c; //warn
   override var c = A.d; //warn
   override var d = jfxc4072b.A.e; //warn
   override var e = 1 on replace { f; super.f; this.f; A.f; jfxc4072b.A.f; e; super.e; this.e; A.e; jfxc4072b.A.e};
   override var f = 1 on invalidate { g; super.g; this.g; A.g; jfxc4072b.A.g; };
   override var g = function() { h; /* super.h; */ this.h; A.h; jfxc4072b.A.h; g; /* super.g; */ this.g; A.g; jfxc4072b.A.g; 1 }
   override var h = { i; super.i; this.i; A.i; jfxc4072b.A.i; h; super.h; this.h; A.h; jfxc4072b.A.h; } //warn
   override var i = [a => 1, b => this.l, c => A.l, d => jfxc4072b.A.l/*, e => super.l*/];
   override var l = 42;
   override var m = m; //warn
   override var n = this.n; //warn
   override var o = A.o; //warn
   override var p = jfxc4072b.A.p; //warn
   override var q = super.r; //warn
   override var r = super.r; //warn
}
