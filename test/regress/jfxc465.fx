/* Regression test: JFXC-465:Cannot access for var when used in init block
 * @test
 * @run
 */
class jfxc465 { 
    function boo() : Void {
        for(rr in [0..4]) { 
            var r = rr; 
        } 
    }

    init { 
        for(ii in [0..4]) { 
            var i = ii; 
        } 
    } 
}
