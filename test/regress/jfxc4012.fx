/**
 * Regress test for JFXC-4012.
 *
 *  Break interrupts outer loop instead of inner
 *
 * @test
 * @run
 */

for(c in [1,2,3,4,5]) {
    var i = 5;
    println("c = {c}");
    while (i > 0) {
        i--;
        if (i == 3) {
            break;
        }
    }
}
