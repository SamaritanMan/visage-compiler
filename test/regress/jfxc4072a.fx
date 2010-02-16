/*
 * Regression test: Forward reference check overhaul
 *
 * @test
 */

class A {
    var a:Integer = b = 1;
    var b:Integer = this.c = 1;
    var c:Integer = A.d = 1;
    var d:Integer = jfxc4072a.A.e = 1;
    var e:Integer = e = 1;
    var f:Integer = f = 1;
    var g:Integer = A.g = 1;
    var h:Integer = jfxc4072a.A.h = 1;
    var i:Integer = { l = 1; A.l = 1; jfxc4072a.A.l = 1; this.l = 1; i = 1; A.i = 1; jfxc4072a.A.i = 1; this.i = 1; };
    var l:Integer = 42;


    function func() {
        var a:Integer =  b = 1;
        var b:Integer = b = 1;
        var c:Integer = { c = 1; d = 1 };
        var d:Integer = 42;
    }
}

var a:Integer = b = 1;
var b:Integer = jfxc4072a.c = 1;
var c:Integer = c = 1;
var d:Integer = jfxc4072a.d = 1;
var e:Integer = { e = 1; jfxc4072a.e = 1; f = 1; jfxc4072a.f = 1 };
var f:Integer = 42;

function func() {
    var a:Integer =  b = 1;
    var b:Integer = b = 1;
    var c:Integer = { c = 1; d = 1 };
    var d:Integer = 42;
}
