/**
 * Regression test for JFXC-2445 : Float array not properly type-inferenced, breaks GUI
 *
 * @test
 * @run
 */

var rgbo = java.awt.Color.cyan.getRGBComponents(null); 