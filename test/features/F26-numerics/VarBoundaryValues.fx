/*
 * Test boundary values for numeric types {Character, Byte, Long, Short,
 * Float, Double} using cross-level assignments and instance/local
 * differentiations of variables with equal names.
 *
 * @test/fxunit
 * @run
 */

import javafx.fxunit.FXTestCase;

public class VarBoundaryValues extends FXTestCase {
    var cInstance = cScript;
    var bInstance = bScript;
    var lInstance = lScript;
    var sInstance = sScript;
    var fInstance = fScript;
    var dInstance = dScript;

    function testTransitiveAssignmentCharacter() {
        var cLocal = cInstance;
        assertEquals(65535 as Character, cLocal);
        cLocal = 65535;
        assertEquals(65535 as Character, cLocal);
        cLocal = 65535 as Character;
        assertEquals(65535 as Character, cLocal);
    }

    function testTransitiveAssignmentByte() {
        var bLocal = bInstance;
        assertEquals(127 as Byte, bLocal);
        bLocal = 127;
        assertEquals(127 as Byte, bLocal);
        bLocal = 127 as Byte;
        assertEquals(127 as Byte, bLocal);
    }

    function testTransitiveAssignmentLong() {
        var lLocal = lInstance;
        // Compiler error: Integer out of range: 9223372036854775807
        // Uncomment this when issue JFXC-2571 is resolved
 //       assertEquals(9223372036854775807 as Long, lLocal);
        lLocal = 9223372036854775807;
//        assertEquals(9223372036854775807 as Long, lLocal);
//        lLocal = 9223372036854775807 as Long;
//        assertEquals(9223372036854775807 as Long, lLocal);
    }

    function testTransitiveAssignmentShort() {
        var sLocal = sInstance;
        assertEquals(32767 as Short, sLocal);
        sLocal = 32767;
        assertEquals(32767 as Short, sLocal);
        sLocal = 32767 as Long;
        assertEquals(32767 as Short, sLocal);
    }

    function testTransitiveAssignmentFloat() {
        var fLocal = fInstance;
        assertEquals(3.4028234663852886E38 as Float, fLocal, 0);
        fLocal = 3.4028234663852886E38;
        assertEquals(3.4028234663852886E38 as Float, fLocal, 0);
        fLocal = 3.4028234663852886E38 as Float;
        assertEquals(3.4028234663852886E38 as Float, fLocal, 0);
    }

    function testTransitiveAssignmentDouble() {
        var dLocal = dInstance;
        assertEquals(1.7976931348623157E308 as Double, dLocal, 0);
        dLocal = 1.7976931348623157E308;
        assertEquals(1.7976931348623157E308 as Double, dLocal, 0);
        dLocal = 1.7976931348623157E308 as Double;
        assertEquals(1.7976931348623157E308 as Double, dLocal, 0);
    }

    function testInstanceAndLocalCharacter() {
        // Min value
        def cInstance : Character = 0;
        assertEquals(0 as Character, cInstance);
        assertEquals(65535 as Character, this.cInstance);
    }

    function testInstanceAndLocalByte() {
        // Min value
        def bInstance : Byte = -127;
        assertEquals(-127 as Byte, bInstance);
        assertEquals(127 as Byte, this.bInstance);
    }

    function testInstanceAndLocalLong() {
        // Min value
        // Compiler error: Long out of range: -9223372036854775808
        // Uncomment this when issue JFXC-2576 is resolved
//        def lInstance : Long = -9223372036854775808;
//        assertEquals(-9223372036854775808 as Long, lInstance);
//        assertEquals(9223372036854775807 as Long, this.lInstance);
    }

    function testInstanceAndLocalShort() {
        // Min value
        def sInstance : Short = -32768;
        assertEquals(-32768 as Short, sInstance);
        assertEquals(32767 as Short, this.sInstance);
    }

    function testInstanceAndLocalFloat() {
        // Min value
        def fInstance : Float = 1.401298464324817E-45;
        assertEquals(1.401298464324817E-45 as Float, fInstance, 0);
        assertEquals(3.4028234663852886E38 as Float, this.fInstance, 0);
    }

    function testInstanceAndLocalDouble() {
        // Min value
        def dInstance : Double = 4.94065645841246544E-324;
        assertEquals(4.94065645841246544E-324 as Double, dInstance, 0);
        assertEquals(1.7976931348623157E308 as Double, this.dInstance, 0);
    }
}

/* Max values */
var cScript : Character = 65535;
var bScript : Byte = 127;
var lScript : Long = 9223372036854775807;
var sScript : Short = 32767;
var fScript : Float = 3.4028234663852886E38;
var dScript : Double = 1.7976931348623157E308;
