/**
 * JFXC-3441 : extends grammar to support on invalidate triggers
 *
 * @test/compile-error
 */

var y = 1;
var x1 = bind y on replace {} on invalidate {} on replace {} on invalidate {};
var x2 = bind y on invalidate {} on replace {} on invalidate {} on replace {};
