/*
 * Regression test: JFXC-393:functions aren't checked for return -- CPU goes to 100%
 *
 * @test
 * @run
 */
class Bar {
    public function setSize(w:Number, h:Number):Void {
        w = h;
    }

}