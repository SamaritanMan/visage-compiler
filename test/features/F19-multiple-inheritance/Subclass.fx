/*
 * @subtest F19
 */

public class Subclass extends Base1, Base2 {
    package var d : Integer;

    //TODO: reinstate
    //  remove as work-around to JFXC-1803
    //function foo() { "{a}{b}{c}{d}" }
    package function wahoo() { d }
}
