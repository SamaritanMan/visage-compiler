/*
 * Regression test JFXC-1331 :  untyped empty sequence is translated to Object
 *
 * @test
 */

var foo = [];
foo = [1,2,3];
for (i in foo) { i }
for (i2 in []) { i2 }

function foox(s : String[]) {}

foox([]);