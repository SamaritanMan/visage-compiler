/**
 * Return statement inconsistency
 * @test
 */
function f(b1 : Boolean, b2 : Boolean) {    
    if (b1) {
		return "100";
    }
	if (b2) {
		return 101;
	}
    false;
}
var x;
x = "100";
var y = f(true, true);
var z : java.lang.Comparable = y;