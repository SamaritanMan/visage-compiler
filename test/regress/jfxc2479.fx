/**
 * JFXC-2479 : Arithmetic operations on Duration by new numerics
 *
 * @test
 * @run
 */

{
var a = 2s * 9;
var x :Float = 1.0;
var b = 3s * x;
var c = 4s * 4.4;
var d = 5s / 2.2;
var e = 6s / x;
var dbl : Double = 1.1;
var f = 7s / dbl;
var g = 8s * dbl;
println( a );
println( b );
println( c );
println( d );
println( e );
println( f );
println( g );
}