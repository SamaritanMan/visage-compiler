/*
 * Testing mixins with "instanceof" operator.
 *
 * @test/fxunit
 */

import javafx.fxunit.FXTestCase;

mixin class M1 {}
mixin class M2 extends M1 {}
mixin class M3 {}
mixin class M4 extends M2, M3 {}

class Mixee1 extends M1 {}
class Mixee2 extends M2 {}
class Mixee3 extends M4 {}

public class MxInstof01 extends FXTestCase {

    function testLevel1() {
        var m = Mixee1 {};
        assertTrue(m instanceof M1);
        assertTrue(m instanceof Mixee1);
        assertFalse(m instanceof M2);
        assertFalse(m instanceof M3);
        assertFalse(m instanceof M4);
    }

    function testLevel2() {
        var m = new Mixee2();
        assertTrue(m instanceof M1);
        assertTrue(m instanceof M2);
        assertTrue(m instanceof Mixee2);
        assertFalse(m instanceof M3);
        assertFalse(m instanceof M4);
    }

    function testLevel3() {
        var m = new Mixee3();
        assertTrue(m instanceof M1);
        assertTrue(m instanceof M2);
        assertTrue(m instanceof M3);
        assertTrue(m instanceof M4);
        assertTrue(m instanceof Mixee3);
    }
}
