/**
 * Regress test for JFXC-3002
 *  on-replace triggers should not be allowed inside bind
 *
 * @test
 * @run
 */

// this is to make sure that we don't 
// break 'normal' on-replace triggers.

var x = 0 on replace {
   println("x changed!");
}

x = 1;
x = 2;
