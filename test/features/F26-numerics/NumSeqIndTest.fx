/*
 * Test sequences indexing with various numeric types
 *
 * @test/fxunit
 * @
 */

import javafx.fxunit.FXTestCase;
 
public class NumSeqIndTest extends FXTestCase {

var res = 0;
var s: String[] = ["0","1","2","3","4","5","6","7","8","9"];

public function testIndByte() {
   var r: String = s[0 as Byte];
   assertEquals("0",r);

   var j: Byte = 0 as Byte;
   assertEquals("0",s[j]);
}

public function testIndShort() {
   var r: String = s[1 as Short];
   assertEquals("1",r);

   var j: Short = 1 as Short;
   assertEquals("1",s[j]);
}

public function testIndCharacter() {
   var r: String = s[2 as Character];
   assertEquals("2",r);

   var j: Character = 2 as Character;
   assertEquals("2",s[j]);
}

public function testIndInteger() {
   var r: String = s[3 as Integer];
   assertEquals("3",r);

   var j: Integer = 3 as Integer;
   assertEquals("3",s[j]);
}

public function testIndLong() {
   var r: String = s[4 as Long];
   assertEquals("4",r);

   var j: Long = 4 as Long;
   assertEquals("4",s[j]);
}

public function testIndFloat() {
   var r: String = s[5.5 as Float];
   assertEquals("5",r);

   var j: Float = 5.5 as Float;
   assertEquals("5",s[j]);
}

public function testIndDouble() {
   var r: String = s[6.25 as Double];
   assertEquals("6",r);

   var j: Double = 6.25 as Double;
   assertEquals("6",s[j]);
}

public function testIndNumber() {
   var r: String = s[7.75 as Number];
   assertEquals("7",r);

   var j: Number = 7.75 as Number;
   assertEquals("7",s[j]);
}


}
