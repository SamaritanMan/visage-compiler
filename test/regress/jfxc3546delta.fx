/**
 * JFXC-3546 : Internal bug in compiler: nativearray in bound-for -- cannot find symbol symbol : variable startPos$ 
 *
 * Changing out the nativearray
 *
 * @test
 * @run
 */

var str = "5.2 7 9.99";
var jj: Number[] = bind for(item in str.split(" ")) Number.parseFloat(item);
println(jj);

str = "1.1 66";
println(jj);
