/**
 * JFXC-3546 : Internal bug in compiler: nativearray in bound-for -- cannot find symbol symbol : variable startPos$ 
 *
 * @test
 * @run
 */

var jj: Number[] = bind for(item in "1,2,3".split(",")) Number.parseFloat(item);
println(jj);
