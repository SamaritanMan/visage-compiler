/* 
 * Regression test: JFXC-1122: Compiler error encountered when using java.text.NumberFormat.parse()
 *
 * @test/compile-error
 */

import java.lang.System;

var nos:String[] = [ "10", "11", "12.34" ];

var fmt = java.text.NumberFormat.getInstance();

//var fields = for (i in [0..2]) fmt.parse(nos[i]).intValue();
var fields:Number[] = for (i in [0..2]) fmt.parse(nos[i]);

for (i in fields) {
     System.out.println("Item [{indexof i}] = [{i}]");
}
