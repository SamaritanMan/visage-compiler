/**
 * JFXC-3684 : Compiled bind: for ( x in java.util.List) .... break causes  undefined label: synth_for$0
 *
 * @test
 */

import java.util.List;
function func() {
    def pickedNodes: List = null;
    for (target in pickedNodes) {
        if (true) {
            break;
        }
    }
}
