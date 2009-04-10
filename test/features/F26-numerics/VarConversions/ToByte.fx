/*
 * Assigning compatible values to a variable of the type Byte.
 * The code is expected to compile and run successfully. Warnings
 * are not checked.
 *
 * @test/fxunit
 * @run
 */

import javafx.fxunit.FXTestCase;

/* --------------------------------------------------------------
 *            This block could live in a common fx file
 *            if (at)compilefirst worked with (at)test/fxunit
 * -------------------------------------------------------------- */
public def bb : Byte = 127;
public def cc : Character = 65535;
public def ii : Integer = 2147483647;
public def ll : Long = 9223372036854775807;
public def ss : Short = 32767;
public def ff : Float = 3.4028234663852886E38;
public def dd : Double = 1.7976931348623157E308;
public def nn : Number = 3.1415926535;
public def dduu : Duration = 600s;
public def bboo : Boolean = true;
public def sstt : String = "Hello, Java FX!";
public def nul = null;
public def iSeq : Integer[] = [ 1, 2, 3 ];
public def fSeq : Float[] = [ 1.11, 2.22, 3.33 ];
/* ------------------------------------------------------------ */


var b : Byte;

public class ToByte extends FXTestCase {

    function testToByte() {
        b = bb;
        assertEquals127();
        b = 127;
        assertEquals127();
        b = 127 as Byte;
        assertEquals127();

        b = cc;
        b = 127 as Character;
        assertEquals127();

        b = ff;
        b = java.lang.Float.NaN;
        b = 127.0 as Float;
        assertEquals127();
        b = 127.45 as Float;
        assertEquals127();

        b = dd;
        b = java.lang.Double.NaN;
        b = 127.0 as Double;
        assertEquals127();
        b = 127.45 as Double;
        assertEquals127();

        b = nn;
        b = 127.0 as Number;
        assertEquals127();

        b = ii;
        b = 127 as Integer;
        assertEquals127();

        b = ll;
        b = 127 as Long;
        assertEquals127();

        b = ss;
        b = 127 as Short;
        assertEquals127();

        var seq1 = [ 125..3240 ];
        b = seq1[2];
        assertEquals127();

        var seq2 = [ 121.11, 1750.35, 127.0, 1434.9 ];
        b = seq2[2];
        assertEquals127();
    }

    function assertEquals127() {
        assertEquals(127 as Byte, b);
    }
}

