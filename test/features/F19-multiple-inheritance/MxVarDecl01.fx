/*
 * variable declarations in mixin
 *
 * @test/fxunit
 * @run
 */
import javafx.fxunit.FXTestCase;

mixin class A {
  var v_  = 1;
  var nv_ ;
  var v_String: String = "ABC";
  var nv_String: String;
  var v_Boolean: Boolean = true;
  var nv_Boolean: Boolean;
  var v_Duration: Duration = 1s;
  var nv_Duration: Duration;
  var v_Byte: Byte = 120;
  var nv_Byte: Byte;
  var v_Short: Short = 30000;
  var nv_Short: Short;
  var v_Character: Character = 64;
  var nv_Character: Character;
  var v_Integer: Integer = 1000000;
  var nv_Integer: Integer;
  var v_Long: Long = 1000000000;
  var nv_Long: Long;
  var v_Float: Float = 10.5;
  var nv_Float: Float;
  var v_Double: Double = 1.25e4;
  var nv_Double: Double;
  var v_Number: Number = 100;
  var nv_Number: Number;
}

class B extends A {
}

class C extends B {
  override var v_  = 12;
  override var v_String  = "ABC2";
  override var v_Boolean  = false;
  override var v_Duration  = 12s;
  override var v_Byte  = 122;
  override var v_Short  = 30002;
  override var v_Character  = 62;
  override var v_Integer  = 1000002;
  override var v_Long  = 1000000002;
  override var v_Float  = 10.2;
  override var v_Double  = 1.25e2;
  override var v_Number  = 102;
}

class D extends A {
  function get_()  { return v_; }
  function get_String(): String { return v_String; }
  function get_Boolean(): Boolean { return v_Boolean; }
  function get_Duration(): Duration { return v_Duration; }
  function get_Byte(): Byte { return v_Byte; }
  function get_Short(): Short { return v_Short; }
  function get_Character(): Character { return v_Character; }
  function get_Integer(): Integer { return v_Integer; }
  function get_Long(): Long { return v_Long; }
  function get_Float(): Float { return v_Float; }
  function get_Double(): Double { return v_Double; }
  function get_Number(): Number { return v_Number; }
}

public class MxVarDecl01 extends FXTestCase {

  public function testA01() {
    var x= B{};
    assertEquals(1, x.v_);
    assertEquals("ABC" as String, x.v_String);
    assertEquals(true as Boolean, x.v_Boolean);
    assertEquals(1s as Duration, x.v_Duration);
    assertEquals(120 as Byte, x.v_Byte);
    assertEquals(30000 as Short, x.v_Short);
    assertEquals(64 as Character, x.v_Character);
    assertEquals(1000000 as Integer, x.v_Integer);
    assertEquals(1000000000 as Long, x.v_Long);
    //assertEquals(10.5 as Float, x.v_Float);
    //assertEquals(1.25e4 as Double, x.v_Double);
    //assertEquals(100 as Number, x.v_Number);
  }
  public function testA02() {
    var x= C{};
    assertEquals(12, x.v_);
    assertEquals("ABC2" as String, x.v_String);
    assertEquals(false as Boolean, x.v_Boolean);
    assertEquals(12s as Duration, x.v_Duration);
    assertEquals(122 as Byte, x.v_Byte);
    assertEquals(30002 as Short, x.v_Short);
    assertEquals(62 as Character, x.v_Character);
    assertEquals(1000002 as Integer, x.v_Integer);
    assertEquals(1000000002 as Long, x.v_Long);
    //assertEquals(10.2 as Float, x.v_Float);
    //assertEquals(1.25e2 as Double, x.v_Double);
    //assertEquals(102 as Number, x.v_Number);
  }
  public function testA03() {
    var x= D{};
    assertEquals(1, x.get_());
    assertEquals("ABC" as String, x.get_String());
    assertEquals(true as Boolean, x.get_Boolean());
    assertEquals(1s as Duration, x.get_Duration());
    assertEquals(120 as Byte, x.get_Byte());
    assertEquals(30000 as Short, x.get_Short());
    assertEquals(64 as Character, x.get_Character());
    assertEquals(1000000 as Integer, x.get_Integer());
    assertEquals(1000000000 as Long, x.get_Long());
    //assertEquals(10.5 as Float, x.get_Float());
    //assertEquals(1.25e4 as Double, x.get_Double());
    //assertEquals(100 as Number, x.get_Number());
  }
}
