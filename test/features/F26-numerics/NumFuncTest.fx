/*
 * Test numeric conversions for parameters and returns 
 *
 * @test/fxunit
 * @
 */ 

import javafx.fxunit.FXTestCase;
 
public class NumFuncTest extends FXTestCase {

var res = this;
var gen: Boolean = false;
var s: String[] = ["0","1","2","3","4","5","6","7","8","9"];

    function paramByte(x: Byte): String {"{x as Long}"}
    function retvarByteActByte(): Byte {var x: Byte = 120 as Byte ; x}
    function retvalByteActByte(): Byte { 120 as Byte }
    function testParamByteActByte() {
       var r_val: String = paramByte(120 as Byte);
       if (gen)javafx.lang.FX.print("\nvar resParamByteActByte_val: Byte={r_val};");       
       assertEquals("{res.resParamByteActByte_val as Long}", "{r_val}");

       var par: Byte = 120 as Byte;
       var r_var: String = paramByte(par);
       if (gen)javafx.lang.FX.print("\nvar resParamByteActByte_var: Byte={r_var};\n//");       
       assertEquals("{res.resParamByteActByte_var as Long}", "{r_var}");
    }

    function testRetByteActByte() {
       var r_val: Byte = retvalByteActByte();
       if (gen)javafx.lang.FX.print("\nvar resRetByteActByte_val: Byte={r_val};");       
       assertEquals("{res.resRetByteActByte_val as Long}", "{r_val as Long}");

       var r_var: Byte = retvarByteActByte();
       if (gen)javafx.lang.FX.print("\nvar resRetByteActByte_var: Byte={r_var};\n//");       
       assertEquals("{res.resRetByteActByte_var as Long}", "{r_var as Long}");
    }

    function retvarByteActShort(): Byte {var x: Short = 30000 as Short ; x}
    function retvalByteActShort(): Byte { 30000 as Short }
    function testParamByteActShort() {
       var r_val: String = paramByte(30000 as Short);
       if (gen)javafx.lang.FX.print("\nvar resParamByteActShort_val: Byte={r_val};");       
       assertEquals("{res.resParamByteActShort_val as Long}", "{r_val}");

       var par: Short = 30000 as Short;
       var r_var: String = paramByte(par);
       if (gen)javafx.lang.FX.print("\nvar resParamByteActShort_var: Byte={r_var};\n//");       
       assertEquals("{res.resParamByteActShort_var as Long}", "{r_var}");
    }

    function testRetByteActShort() {
       var r_val: Byte = retvalByteActShort();
       if (gen)javafx.lang.FX.print("\nvar resRetByteActShort_val: Byte={r_val};");       
       assertEquals("{res.resRetByteActShort_val as Long}", "{r_val as Long}");

       var r_var: Byte = retvarByteActShort();
       if (gen)javafx.lang.FX.print("\nvar resRetByteActShort_var: Byte={r_var};\n//");       
       assertEquals("{res.resRetByteActShort_var as Long}", "{r_var as Long}");
    }

    function retvarByteActCharacter(): Byte {var x: Character = 64 as Character ; x}
    function retvalByteActCharacter(): Byte { 64 as Character }
    function testParamByteActCharacter() {
       //FAIL var r_val: String = paramByte(64 as Character);
       //FAIL if (gen)javafx.lang.FX.print("\nvar resParamByteActCharacter_val: Byte={r_val};");       
       ////FAIL assertEquals("{res.resParamByteActCharacter_val as Long}", "{r_val}");

       //FAIL var par: Character = 64 as Character;
       //FAIL var r_var: String = paramByte(par);
       //FAIL if (gen)javafx.lang.FX.print("\nvar resParamByteActCharacter_var: Byte={r_var};\n//");       
       ////FAIL assertEquals("{res.resParamByteActCharacter_var as Long}", "{r_var}");
    }

    function testRetByteActCharacter() {
       var r_val: Byte = retvalByteActCharacter();
       if (gen)javafx.lang.FX.print("\nvar resRetByteActCharacter_val: Byte={r_val};");       
       assertEquals("{res.resRetByteActCharacter_val as Long}", "{r_val as Long}");

       var r_var: Byte = retvarByteActCharacter();
       if (gen)javafx.lang.FX.print("\nvar resRetByteActCharacter_var: Byte={r_var};\n//");       
       assertEquals("{res.resRetByteActCharacter_var as Long}", "{r_var as Long}");
    }

    function retvarByteActInteger(): Byte {var x: Integer = 1000000 as Integer ; x}
    function retvalByteActInteger(): Byte { 1000000 as Integer }
    function testParamByteActInteger() {
       var r_val: String = paramByte(1000000 as Integer);
       if (gen)javafx.lang.FX.print("\nvar resParamByteActInteger_val: Byte={r_val};");       
       assertEquals("{res.resParamByteActInteger_val as Long}", "{r_val}");

       var par: Integer = 1000000 as Integer;
       var r_var: String = paramByte(par);
       if (gen)javafx.lang.FX.print("\nvar resParamByteActInteger_var: Byte={r_var};\n//");       
       assertEquals("{res.resParamByteActInteger_var as Long}", "{r_var}");
    }

    function testRetByteActInteger() {
       var r_val: Byte = retvalByteActInteger();
       if (gen)javafx.lang.FX.print("\nvar resRetByteActInteger_val: Byte={r_val};");       
       assertEquals("{res.resRetByteActInteger_val as Long}", "{r_val as Long}");

       var r_var: Byte = retvarByteActInteger();
       if (gen)javafx.lang.FX.print("\nvar resRetByteActInteger_var: Byte={r_var};\n//");       
       assertEquals("{res.resRetByteActInteger_var as Long}", "{r_var as Long}");
    }

    function retvarByteActLong(): Byte {var x: Long = 1000000000 as Long ; x}
    function retvalByteActLong(): Byte { 1000000000 as Long }
    function testParamByteActLong() {
       var r_val: String = paramByte(1000000000 as Long);
       if (gen)javafx.lang.FX.print("\nvar resParamByteActLong_val: Byte={r_val};");       
       assertEquals("{res.resParamByteActLong_val as Long}", "{r_val}");

       var par: Long = 1000000000 as Long;
       var r_var: String = paramByte(par);
       if (gen)javafx.lang.FX.print("\nvar resParamByteActLong_var: Byte={r_var};\n//");       
       assertEquals("{res.resParamByteActLong_var as Long}", "{r_var}");
    }

    function testRetByteActLong() {
       var r_val: Byte = retvalByteActLong();
       if (gen)javafx.lang.FX.print("\nvar resRetByteActLong_val: Byte={r_val};");       
       assertEquals("{res.resRetByteActLong_val as Long}", "{r_val as Long}");

       var r_var: Byte = retvarByteActLong();
       if (gen)javafx.lang.FX.print("\nvar resRetByteActLong_var: Byte={r_var};\n//");       
       assertEquals("{res.resRetByteActLong_var as Long}", "{r_var as Long}");
    }

    function retvarByteActFloat(): Byte {var x: Float = 10.5 as Float ; x}
    function retvalByteActFloat(): Byte { 10.5 as Float }
    function testParamByteActFloat() {
       var r_val: String = paramByte(10.5 as Float);
       if (gen)javafx.lang.FX.print("\nvar resParamByteActFloat_val: Byte={r_val};");       
       assertEquals("{res.resParamByteActFloat_val as Long}", "{r_val}");

       var par: Float = 10.5 as Float;
       var r_var: String = paramByte(par);
       if (gen)javafx.lang.FX.print("\nvar resParamByteActFloat_var: Byte={r_var};\n//");       
       assertEquals("{res.resParamByteActFloat_var as Long}", "{r_var}");
    }

    function testRetByteActFloat() {
       var r_val: Byte = retvalByteActFloat();
       if (gen)javafx.lang.FX.print("\nvar resRetByteActFloat_val: Byte={r_val};");       
       assertEquals("{res.resRetByteActFloat_val as Long}", "{r_val as Long}");

       var r_var: Byte = retvarByteActFloat();
       if (gen)javafx.lang.FX.print("\nvar resRetByteActFloat_var: Byte={r_var};\n//");       
       assertEquals("{res.resRetByteActFloat_var as Long}", "{r_var as Long}");
    }

    function retvarByteActDouble(): Byte {var x: Double = 1.25e4 as Double ; x}
    function retvalByteActDouble(): Byte { 1.25e4 as Double }
    function testParamByteActDouble() {
       var r_val: String = paramByte(1.25e4 as Double);
       if (gen)javafx.lang.FX.print("\nvar resParamByteActDouble_val: Byte={r_val};");       
       assertEquals("{res.resParamByteActDouble_val as Long}", "{r_val}");

       var par: Double = 1.25e4 as Double;
       var r_var: String = paramByte(par);
       if (gen)javafx.lang.FX.print("\nvar resParamByteActDouble_var: Byte={r_var};\n//");       
       assertEquals("{res.resParamByteActDouble_var as Long}", "{r_var}");
    }

    function testRetByteActDouble() {
       var r_val: Byte = retvalByteActDouble();
       if (gen)javafx.lang.FX.print("\nvar resRetByteActDouble_val: Byte={r_val};");       
       assertEquals("{res.resRetByteActDouble_val as Long}", "{r_val as Long}");

       var r_var: Byte = retvarByteActDouble();
       if (gen)javafx.lang.FX.print("\nvar resRetByteActDouble_var: Byte={r_var};\n//");       
       assertEquals("{res.resRetByteActDouble_var as Long}", "{r_var as Long}");
    }

    function retvarByteActNumber(): Byte {var x: Number = 100 as Number ; x}
    function retvalByteActNumber(): Byte { 100 as Number }
    function testParamByteActNumber() {
       var r_val: String = paramByte(100 as Number);
       if (gen)javafx.lang.FX.print("\nvar resParamByteActNumber_val: Byte={r_val};");       
       assertEquals("{res.resParamByteActNumber_val as Long}", "{r_val}");

       var par: Number = 100 as Number;
       var r_var: String = paramByte(par);
       if (gen)javafx.lang.FX.print("\nvar resParamByteActNumber_var: Byte={r_var};\n//");       
       assertEquals("{res.resParamByteActNumber_var as Long}", "{r_var}");
    }

    function testRetByteActNumber() {
       var r_val: Byte = retvalByteActNumber();
       if (gen)javafx.lang.FX.print("\nvar resRetByteActNumber_val: Byte={r_val};");       
       assertEquals("{res.resRetByteActNumber_val as Long}", "{r_val as Long}");

       var r_var: Byte = retvarByteActNumber();
       if (gen)javafx.lang.FX.print("\nvar resRetByteActNumber_var: Byte={r_var};\n//");       
       assertEquals("{res.resRetByteActNumber_var as Long}", "{r_var as Long}");
    }

    function paramShort(x: Short): String {"{x as Long}"}
    function retvarShortActByte(): Short {var x: Byte = 120 as Byte ; x}
    function retvalShortActByte(): Short { 120 as Byte }
    function testParamShortActByte() {
       var r_val: String = paramShort(120 as Byte);
       if (gen)javafx.lang.FX.print("\nvar resParamShortActByte_val: Short={r_val};");       
       assertEquals("{res.resParamShortActByte_val as Long}", "{r_val}");

       var par: Byte = 120 as Byte;
       var r_var: String = paramShort(par);
       if (gen)javafx.lang.FX.print("\nvar resParamShortActByte_var: Short={r_var};\n//");       
       assertEquals("{res.resParamShortActByte_var as Long}", "{r_var}");
    }

    function testRetShortActByte() {
       var r_val: Short = retvalShortActByte();
       if (gen)javafx.lang.FX.print("\nvar resRetShortActByte_val: Short={r_val};");       
       assertEquals("{res.resRetShortActByte_val as Long}", "{r_val as Long}");

       var r_var: Short = retvarShortActByte();
       if (gen)javafx.lang.FX.print("\nvar resRetShortActByte_var: Short={r_var};\n//");       
       assertEquals("{res.resRetShortActByte_var as Long}", "{r_var as Long}");
    }

    function retvarShortActShort(): Short {var x: Short = 30000 as Short ; x}
    function retvalShortActShort(): Short { 30000 as Short }
    function testParamShortActShort() {
       var r_val: String = paramShort(30000 as Short);
       if (gen)javafx.lang.FX.print("\nvar resParamShortActShort_val: Short={r_val};");       
       assertEquals("{res.resParamShortActShort_val as Long}", "{r_val}");

       var par: Short = 30000 as Short;
       var r_var: String = paramShort(par);
       if (gen)javafx.lang.FX.print("\nvar resParamShortActShort_var: Short={r_var};\n//");       
       assertEquals("{res.resParamShortActShort_var as Long}", "{r_var}");
    }

    function testRetShortActShort() {
       var r_val: Short = retvalShortActShort();
       if (gen)javafx.lang.FX.print("\nvar resRetShortActShort_val: Short={r_val};");       
       assertEquals("{res.resRetShortActShort_val as Long}", "{r_val as Long}");

       var r_var: Short = retvarShortActShort();
       if (gen)javafx.lang.FX.print("\nvar resRetShortActShort_var: Short={r_var};\n//");       
       assertEquals("{res.resRetShortActShort_var as Long}", "{r_var as Long}");
    }

    function retvarShortActCharacter(): Short {var x: Character = 64 as Character ; x}
    function retvalShortActCharacter(): Short { 64 as Character }
    function testParamShortActCharacter() {
       //FAIL var r_val: String = paramShort(64 as Character);
       //FAIL if (gen)javafx.lang.FX.print("\nvar resParamShortActCharacter_val: Short={r_val};");       
       ////FAIL assertEquals("{res.resParamShortActCharacter_val as Long}", "{r_val}");

       //FAIL var par: Character = 64 as Character;
       //FAIL var r_var: String = paramShort(par);
       //FAIL if (gen)javafx.lang.FX.print("\nvar resParamShortActCharacter_var: Short={r_var};\n//");       
       ////FAIL assertEquals("{res.resParamShortActCharacter_var as Long}", "{r_var}");
    }

    function testRetShortActCharacter() {
       var r_val: Short = retvalShortActCharacter();
       if (gen)javafx.lang.FX.print("\nvar resRetShortActCharacter_val: Short={r_val};");       
       assertEquals("{res.resRetShortActCharacter_val as Long}", "{r_val as Long}");

       var r_var: Short = retvarShortActCharacter();
       if (gen)javafx.lang.FX.print("\nvar resRetShortActCharacter_var: Short={r_var};\n//");       
       assertEquals("{res.resRetShortActCharacter_var as Long}", "{r_var as Long}");
    }

    function retvarShortActInteger(): Short {var x: Integer = 1000000 as Integer ; x}
    function retvalShortActInteger(): Short { 1000000 as Integer }
    function testParamShortActInteger() {
       var r_val: String = paramShort(1000000 as Integer);
       if (gen)javafx.lang.FX.print("\nvar resParamShortActInteger_val: Short={r_val};");       
       assertEquals("{res.resParamShortActInteger_val as Long}", "{r_val}");

       var par: Integer = 1000000 as Integer;
       var r_var: String = paramShort(par);
       if (gen)javafx.lang.FX.print("\nvar resParamShortActInteger_var: Short={r_var};\n//");       
       assertEquals("{res.resParamShortActInteger_var as Long}", "{r_var}");
    }

    function testRetShortActInteger() {
       var r_val: Short = retvalShortActInteger();
       if (gen)javafx.lang.FX.print("\nvar resRetShortActInteger_val: Short={r_val};");       
       assertEquals("{res.resRetShortActInteger_val as Long}", "{r_val as Long}");

       var r_var: Short = retvarShortActInteger();
       if (gen)javafx.lang.FX.print("\nvar resRetShortActInteger_var: Short={r_var};\n//");       
       assertEquals("{res.resRetShortActInteger_var as Long}", "{r_var as Long}");
    }

    function retvarShortActLong(): Short {var x: Long = 1000000000 as Long ; x}
    function retvalShortActLong(): Short { 1000000000 as Long }
    function testParamShortActLong() {
       var r_val: String = paramShort(1000000000 as Long);
       if (gen)javafx.lang.FX.print("\nvar resParamShortActLong_val: Short={r_val};");       
       assertEquals("{res.resParamShortActLong_val as Long}", "{r_val}");

       var par: Long = 1000000000 as Long;
       var r_var: String = paramShort(par);
       if (gen)javafx.lang.FX.print("\nvar resParamShortActLong_var: Short={r_var};\n//");       
       assertEquals("{res.resParamShortActLong_var as Long}", "{r_var}");
    }

    function testRetShortActLong() {
       var r_val: Short = retvalShortActLong();
       if (gen)javafx.lang.FX.print("\nvar resRetShortActLong_val: Short={r_val};");       
       assertEquals("{res.resRetShortActLong_val as Long}", "{r_val as Long}");

       var r_var: Short = retvarShortActLong();
       if (gen)javafx.lang.FX.print("\nvar resRetShortActLong_var: Short={r_var};\n//");       
       assertEquals("{res.resRetShortActLong_var as Long}", "{r_var as Long}");
    }

    function retvarShortActFloat(): Short {var x: Float = 10.5 as Float ; x}
    function retvalShortActFloat(): Short { 10.5 as Float }
    function testParamShortActFloat() {
       var r_val: String = paramShort(10.5 as Float);
       if (gen)javafx.lang.FX.print("\nvar resParamShortActFloat_val: Short={r_val};");       
       assertEquals("{res.resParamShortActFloat_val as Long}", "{r_val}");

       var par: Float = 10.5 as Float;
       var r_var: String = paramShort(par);
       if (gen)javafx.lang.FX.print("\nvar resParamShortActFloat_var: Short={r_var};\n//");       
       assertEquals("{res.resParamShortActFloat_var as Long}", "{r_var}");
    }

    function testRetShortActFloat() {
       var r_val: Short = retvalShortActFloat();
       if (gen)javafx.lang.FX.print("\nvar resRetShortActFloat_val: Short={r_val};");       
       assertEquals("{res.resRetShortActFloat_val as Long}", "{r_val as Long}");

       var r_var: Short = retvarShortActFloat();
       if (gen)javafx.lang.FX.print("\nvar resRetShortActFloat_var: Short={r_var};\n//");       
       assertEquals("{res.resRetShortActFloat_var as Long}", "{r_var as Long}");
    }

    function retvarShortActDouble(): Short {var x: Double = 1.25e4 as Double ; x}
    function retvalShortActDouble(): Short { 1.25e4 as Double }
    function testParamShortActDouble() {
       var r_val: String = paramShort(1.25e4 as Double);
       if (gen)javafx.lang.FX.print("\nvar resParamShortActDouble_val: Short={r_val};");       
       assertEquals("{res.resParamShortActDouble_val as Long}", "{r_val}");

       var par: Double = 1.25e4 as Double;
       var r_var: String = paramShort(par);
       if (gen)javafx.lang.FX.print("\nvar resParamShortActDouble_var: Short={r_var};\n//");       
       assertEquals("{res.resParamShortActDouble_var as Long}", "{r_var}");
    }

    function testRetShortActDouble() {
       var r_val: Short = retvalShortActDouble();
       if (gen)javafx.lang.FX.print("\nvar resRetShortActDouble_val: Short={r_val};");       
       assertEquals("{res.resRetShortActDouble_val as Long}", "{r_val as Long}");

       var r_var: Short = retvarShortActDouble();
       if (gen)javafx.lang.FX.print("\nvar resRetShortActDouble_var: Short={r_var};\n//");       
       assertEquals("{res.resRetShortActDouble_var as Long}", "{r_var as Long}");
    }

    function retvarShortActNumber(): Short {var x: Number = 100 as Number ; x}
    function retvalShortActNumber(): Short { 100 as Number }
    function testParamShortActNumber() {
       var r_val: String = paramShort(100 as Number);
       if (gen)javafx.lang.FX.print("\nvar resParamShortActNumber_val: Short={r_val};");       
       assertEquals("{res.resParamShortActNumber_val as Long}", "{r_val}");

       var par: Number = 100 as Number;
       var r_var: String = paramShort(par);
       if (gen)javafx.lang.FX.print("\nvar resParamShortActNumber_var: Short={r_var};\n//");       
       assertEquals("{res.resParamShortActNumber_var as Long}", "{r_var}");
    }

    function testRetShortActNumber() {
       var r_val: Short = retvalShortActNumber();
       if (gen)javafx.lang.FX.print("\nvar resRetShortActNumber_val: Short={r_val};");       
       assertEquals("{res.resRetShortActNumber_val as Long}", "{r_val as Long}");

       var r_var: Short = retvarShortActNumber();
       if (gen)javafx.lang.FX.print("\nvar resRetShortActNumber_var: Short={r_var};\n//");       
       assertEquals("{res.resRetShortActNumber_var as Long}", "{r_var as Long}");
    }

    function paramCharacter(x: Character): String {"{x as Long}"}
    function retvarCharacterActByte(): Character {var x: Byte = 120 as Byte ; x}
    function retvalCharacterActByte(): Character { 120 as Byte }
    function testParamCharacterActByte() {
       //FAIL var r_val: String = paramCharacter(120 as Byte);
       //FAIL if (gen)javafx.lang.FX.print("\nvar resParamCharacterActByte_val: Character={r_val};");       
       ////FAIL assertEquals("{res.resParamCharacterActByte_val as Long}", "{r_val}");

       //FAIL var par: Byte = 120 as Byte;
       //FAIL var r_var: String = paramCharacter(par);
       //FAIL if (gen)javafx.lang.FX.print("\nvar resParamCharacterActByte_var: Character={r_var};\n//");       
       ////FAIL assertEquals("{res.resParamCharacterActByte_var as Long}", "{r_var}");
    }

    function testRetCharacterActByte() {
       var r_val: Character = retvalCharacterActByte();
       if (gen)javafx.lang.FX.print("\nvar resRetCharacterActByte_val: Character={r_val as Integer};");       
       assertEquals("{res.resRetCharacterActByte_val as Long}", "{r_val as Long}");

       var r_var: Character = retvarCharacterActByte();
       if (gen)javafx.lang.FX.print("\nvar resRetCharacterActByte_var: Character={r_var as Integer};\n//");       
       assertEquals("{res.resRetCharacterActByte_var as Long}", "{r_var as Long}");
    }

    function retvarCharacterActShort(): Character {var x: Short = 30000 as Short ; x}
    function retvalCharacterActShort(): Character { 30000 as Short }
    function testParamCharacterActShort() {
       //FAIL var r_val: String = paramCharacter(30000 as Short);
       //FAIL if (gen)javafx.lang.FX.print("\nvar resParamCharacterActShort_val: Character={r_val};");       
       ////FAIL assertEquals("{res.resParamCharacterActShort_val as Long}", "{r_val}");

       //FAIL var par: Short = 30000 as Short;
       //FAIL var r_var: String = paramCharacter(par);
       //FAIL if (gen)javafx.lang.FX.print("\nvar resParamCharacterActShort_var: Character={r_var};\n//");       
       ////FAIL assertEquals("{res.resParamCharacterActShort_var as Long}", "{r_var}");
    }

    function testRetCharacterActShort() {
       var r_val: Character = retvalCharacterActShort();
       if (gen)javafx.lang.FX.print("\nvar resRetCharacterActShort_val: Character={r_val as Integer};");       
       assertEquals("{res.resRetCharacterActShort_val as Long}", "{r_val as Long}");

       var r_var: Character = retvarCharacterActShort();
       if (gen)javafx.lang.FX.print("\nvar resRetCharacterActShort_var: Character={r_var as Integer};\n//");       
       assertEquals("{res.resRetCharacterActShort_var as Long}", "{r_var as Long}");
    }

    function retvarCharacterActCharacter(): Character {var x: Character = 64 as Character ; x}
    function retvalCharacterActCharacter(): Character { 64 as Character }
    function testParamCharacterActCharacter() {
       var r_val: String = paramCharacter(64 as Character);
       if (gen)javafx.lang.FX.print("\nvar resParamCharacterActCharacter_val: Character={r_val};");       
       assertEquals("{res.resParamCharacterActCharacter_val as Long}", "{r_val}");

       var par: Character = 64 as Character;
       var r_var: String = paramCharacter(par);
       if (gen)javafx.lang.FX.print("\nvar resParamCharacterActCharacter_var: Character={r_var};\n//");       
       assertEquals("{res.resParamCharacterActCharacter_var as Long}", "{r_var}");
    }

    function testRetCharacterActCharacter() {
       var r_val: Character = retvalCharacterActCharacter();
       if (gen)javafx.lang.FX.print("\nvar resRetCharacterActCharacter_val: Character={r_val as Integer};");       
       assertEquals("{res.resRetCharacterActCharacter_val as Long}", "{r_val as Long}");

       var r_var: Character = retvarCharacterActCharacter();
       if (gen)javafx.lang.FX.print("\nvar resRetCharacterActCharacter_var: Character={r_var as Integer};\n//");       
       assertEquals("{res.resRetCharacterActCharacter_var as Long}", "{r_var as Long}");
    }

    function retvarCharacterActInteger(): Character {var x: Integer = 1000000 as Integer ; x}
    function retvalCharacterActInteger(): Character { 1000000 as Integer }
    function testParamCharacterActInteger() {
       //FAIL var r_val: String = paramCharacter(1000000 as Integer);
       //FAIL if (gen)javafx.lang.FX.print("\nvar resParamCharacterActInteger_val: Character={r_val};");       
       ////FAIL assertEquals("{res.resParamCharacterActInteger_val as Long}", "{r_val}");

       //FAIL var par: Integer = 1000000 as Integer;
       //FAIL var r_var: String = paramCharacter(par);
       //FAIL if (gen)javafx.lang.FX.print("\nvar resParamCharacterActInteger_var: Character={r_var};\n//");       
       ////FAIL assertEquals("{res.resParamCharacterActInteger_var as Long}", "{r_var}");
    }

    function testRetCharacterActInteger() {
       var r_val: Character = retvalCharacterActInteger();
       if (gen)javafx.lang.FX.print("\nvar resRetCharacterActInteger_val: Character={r_val as Integer};");       
       assertEquals("{res.resRetCharacterActInteger_val as Long}", "{r_val as Long}");

       var r_var: Character = retvarCharacterActInteger();
       if (gen)javafx.lang.FX.print("\nvar resRetCharacterActInteger_var: Character={r_var as Integer};\n//");       
       assertEquals("{res.resRetCharacterActInteger_var as Long}", "{r_var as Long}");
    }

    function retvarCharacterActLong(): Character {var x: Long = 1000000000 as Long ; x}
    function retvalCharacterActLong(): Character { 1000000000 as Long }
    function testParamCharacterActLong() {
       //FAIL var r_val: String = paramCharacter(1000000000 as Long);
       //FAIL if (gen)javafx.lang.FX.print("\nvar resParamCharacterActLong_val: Character={r_val};");       
       ////FAIL assertEquals("{res.resParamCharacterActLong_val as Long}", "{r_val}");

       //FAIL var par: Long = 1000000000 as Long;
       //FAIL var r_var: String = paramCharacter(par);
       //FAIL if (gen)javafx.lang.FX.print("\nvar resParamCharacterActLong_var: Character={r_var};\n//");       
       ////FAIL assertEquals("{res.resParamCharacterActLong_var as Long}", "{r_var}");
    }

    function testRetCharacterActLong() {
       var r_val: Character = retvalCharacterActLong();
       if (gen)javafx.lang.FX.print("\nvar resRetCharacterActLong_val: Character={r_val as Integer};");       
       assertEquals("{res.resRetCharacterActLong_val as Long}", "{r_val as Long}");

       var r_var: Character = retvarCharacterActLong();
       if (gen)javafx.lang.FX.print("\nvar resRetCharacterActLong_var: Character={r_var as Integer};\n//");       
       assertEquals("{res.resRetCharacterActLong_var as Long}", "{r_var as Long}");
    }

    function retvarCharacterActFloat(): Character {var x: Float = 10.5 as Float ; x}
    function retvalCharacterActFloat(): Character { 10.5 as Float }
    function testParamCharacterActFloat() {
       //FAIL var r_val: String = paramCharacter(10.5 as Float);
       //FAIL if (gen)javafx.lang.FX.print("\nvar resParamCharacterActFloat_val: Character={r_val};");       
       ////FAIL assertEquals("{res.resParamCharacterActFloat_val as Long}", "{r_val}");

       //FAIL var par: Float = 10.5 as Float;
       //FAIL var r_var: String = paramCharacter(par);
       //FAIL if (gen)javafx.lang.FX.print("\nvar resParamCharacterActFloat_var: Character={r_var};\n//");       
       ////FAIL assertEquals("{res.resParamCharacterActFloat_var as Long}", "{r_var}");
    }

    function testRetCharacterActFloat() {
       var r_val: Character = retvalCharacterActFloat();
       if (gen)javafx.lang.FX.print("\nvar resRetCharacterActFloat_val: Character={r_val as Integer};");       
       assertEquals("{res.resRetCharacterActFloat_val as Long}", "{r_val as Long}");

       var r_var: Character = retvarCharacterActFloat();
       if (gen)javafx.lang.FX.print("\nvar resRetCharacterActFloat_var: Character={r_var as Integer};\n//");       
       assertEquals("{res.resRetCharacterActFloat_var as Long}", "{r_var as Long}");
    }

    function retvarCharacterActDouble(): Character {var x: Double = 1.25e4 as Double ; x}
    function retvalCharacterActDouble(): Character { 1.25e4 as Double }
    function testParamCharacterActDouble() {
       //FAIL var r_val: String = paramCharacter(1.25e4 as Double);
       //FAIL if (gen)javafx.lang.FX.print("\nvar resParamCharacterActDouble_val: Character={r_val};");       
       ////FAIL assertEquals("{res.resParamCharacterActDouble_val as Long}", "{r_val}");

       //FAIL var par: Double = 1.25e4 as Double;
       //FAIL var r_var: String = paramCharacter(par);
       //FAIL if (gen)javafx.lang.FX.print("\nvar resParamCharacterActDouble_var: Character={r_var};\n//");       
       ////FAIL assertEquals("{res.resParamCharacterActDouble_var as Long}", "{r_var}");
    }

    function testRetCharacterActDouble() {
       var r_val: Character = retvalCharacterActDouble();
       if (gen)javafx.lang.FX.print("\nvar resRetCharacterActDouble_val: Character={r_val as Integer};");       
       assertEquals("{res.resRetCharacterActDouble_val as Long}", "{r_val as Long}");

       var r_var: Character = retvarCharacterActDouble();
       if (gen)javafx.lang.FX.print("\nvar resRetCharacterActDouble_var: Character={r_var as Integer};\n//");       
       assertEquals("{res.resRetCharacterActDouble_var as Long}", "{r_var as Long}");
    }

    function retvarCharacterActNumber(): Character {var x: Number = 100 as Number ; x}
    function retvalCharacterActNumber(): Character { 100 as Number }
    function testParamCharacterActNumber() {
       //FAIL var r_val: String = paramCharacter(100 as Number);
       //FAIL if (gen)javafx.lang.FX.print("\nvar resParamCharacterActNumber_val: Character={r_val};");       
       ////FAIL assertEquals("{res.resParamCharacterActNumber_val as Long}", "{r_val}");

       //FAIL var par: Number = 100 as Number;
       //FAIL var r_var: String = paramCharacter(par);
       //FAIL if (gen)javafx.lang.FX.print("\nvar resParamCharacterActNumber_var: Character={r_var};\n//");       
       ////FAIL assertEquals("{res.resParamCharacterActNumber_var as Long}", "{r_var}");
    }

    function testRetCharacterActNumber() {
       var r_val: Character = retvalCharacterActNumber();
       if (gen)javafx.lang.FX.print("\nvar resRetCharacterActNumber_val: Character={r_val as Integer};");       
       assertEquals("{res.resRetCharacterActNumber_val as Long}", "{r_val as Long}");

       var r_var: Character = retvarCharacterActNumber();
       if (gen)javafx.lang.FX.print("\nvar resRetCharacterActNumber_var: Character={r_var as Integer};\n//");       
       assertEquals("{res.resRetCharacterActNumber_var as Long}", "{r_var as Long}");
    }

    function paramInteger(x: Integer): String {"{x as Long}"}
    function retvarIntegerActByte(): Integer {var x: Byte = 120 as Byte ; x}
    function retvalIntegerActByte(): Integer { 120 as Byte }
    function testParamIntegerActByte() {
       var r_val: String = paramInteger(120 as Byte);
       if (gen)javafx.lang.FX.print("\nvar resParamIntegerActByte_val: Integer={r_val};");       
       assertEquals("{res.resParamIntegerActByte_val as Long}", "{r_val}");

       var par: Byte = 120 as Byte;
       var r_var: String = paramInteger(par);
       if (gen)javafx.lang.FX.print("\nvar resParamIntegerActByte_var: Integer={r_var};\n//");       
       assertEquals("{res.resParamIntegerActByte_var as Long}", "{r_var}");
    }

    function testRetIntegerActByte() {
       var r_val: Integer = retvalIntegerActByte();
       if (gen)javafx.lang.FX.print("\nvar resRetIntegerActByte_val: Integer={r_val};");       
       assertEquals("{res.resRetIntegerActByte_val as Long}", "{r_val as Long}");

       var r_var: Integer = retvarIntegerActByte();
       if (gen)javafx.lang.FX.print("\nvar resRetIntegerActByte_var: Integer={r_var};\n//");       
       assertEquals("{res.resRetIntegerActByte_var as Long}", "{r_var as Long}");
    }

    function retvarIntegerActShort(): Integer {var x: Short = 30000 as Short ; x}
    function retvalIntegerActShort(): Integer { 30000 as Short }
    function testParamIntegerActShort() {
       var r_val: String = paramInteger(30000 as Short);
       if (gen)javafx.lang.FX.print("\nvar resParamIntegerActShort_val: Integer={r_val};");       
       assertEquals("{res.resParamIntegerActShort_val as Long}", "{r_val}");

       var par: Short = 30000 as Short;
       var r_var: String = paramInteger(par);
       if (gen)javafx.lang.FX.print("\nvar resParamIntegerActShort_var: Integer={r_var};\n//");       
       assertEquals("{res.resParamIntegerActShort_var as Long}", "{r_var}");
    }

    function testRetIntegerActShort() {
       var r_val: Integer = retvalIntegerActShort();
       if (gen)javafx.lang.FX.print("\nvar resRetIntegerActShort_val: Integer={r_val};");       
       assertEquals("{res.resRetIntegerActShort_val as Long}", "{r_val as Long}");

       var r_var: Integer = retvarIntegerActShort();
       if (gen)javafx.lang.FX.print("\nvar resRetIntegerActShort_var: Integer={r_var};\n//");       
       assertEquals("{res.resRetIntegerActShort_var as Long}", "{r_var as Long}");
    }

    function retvarIntegerActCharacter(): Integer {var x: Character = 64 as Character ; x}
    function retvalIntegerActCharacter(): Integer { 64 as Character }
    function testParamIntegerActCharacter() {
       var r_val: String = paramInteger(64 as Character);
       if (gen)javafx.lang.FX.print("\nvar resParamIntegerActCharacter_val: Integer={r_val};");       
       assertEquals("{res.resParamIntegerActCharacter_val as Long}", "{r_val}");

       var par: Character = 64 as Character;
       var r_var: String = paramInteger(par);
       if (gen)javafx.lang.FX.print("\nvar resParamIntegerActCharacter_var: Integer={r_var};\n//");       
       assertEquals("{res.resParamIntegerActCharacter_var as Long}", "{r_var}");
    }

    function testRetIntegerActCharacter() {
       var r_val: Integer = retvalIntegerActCharacter();
       if (gen)javafx.lang.FX.print("\nvar resRetIntegerActCharacter_val: Integer={r_val};");       
       assertEquals("{res.resRetIntegerActCharacter_val as Long}", "{r_val as Long}");

       var r_var: Integer = retvarIntegerActCharacter();
       if (gen)javafx.lang.FX.print("\nvar resRetIntegerActCharacter_var: Integer={r_var};\n//");       
       assertEquals("{res.resRetIntegerActCharacter_var as Long}", "{r_var as Long}");
    }

    function retvarIntegerActInteger(): Integer {var x: Integer = 1000000 as Integer ; x}
    function retvalIntegerActInteger(): Integer { 1000000 as Integer }
    function testParamIntegerActInteger() {
       var r_val: String = paramInteger(1000000 as Integer);
       if (gen)javafx.lang.FX.print("\nvar resParamIntegerActInteger_val: Integer={r_val};");       
       assertEquals("{res.resParamIntegerActInteger_val as Long}", "{r_val}");

       var par: Integer = 1000000 as Integer;
       var r_var: String = paramInteger(par);
       if (gen)javafx.lang.FX.print("\nvar resParamIntegerActInteger_var: Integer={r_var};\n//");       
       assertEquals("{res.resParamIntegerActInteger_var as Long}", "{r_var}");
    }

    function testRetIntegerActInteger() {
       var r_val: Integer = retvalIntegerActInteger();
       if (gen)javafx.lang.FX.print("\nvar resRetIntegerActInteger_val: Integer={r_val};");       
       assertEquals("{res.resRetIntegerActInteger_val as Long}", "{r_val as Long}");

       var r_var: Integer = retvarIntegerActInteger();
       if (gen)javafx.lang.FX.print("\nvar resRetIntegerActInteger_var: Integer={r_var};\n//");       
       assertEquals("{res.resRetIntegerActInteger_var as Long}", "{r_var as Long}");
    }

    function retvarIntegerActLong(): Integer {var x: Long = 1000000000 as Long ; x}
    function retvalIntegerActLong(): Integer { 1000000000 as Long }
    function testParamIntegerActLong() {
       var r_val: String = paramInteger(1000000000 as Long);
       if (gen)javafx.lang.FX.print("\nvar resParamIntegerActLong_val: Integer={r_val};");       
       assertEquals("{res.resParamIntegerActLong_val as Long}", "{r_val}");

       var par: Long = 1000000000 as Long;
       var r_var: String = paramInteger(par);
       if (gen)javafx.lang.FX.print("\nvar resParamIntegerActLong_var: Integer={r_var};\n//");       
       assertEquals("{res.resParamIntegerActLong_var as Long}", "{r_var}");
    }

    function testRetIntegerActLong() {
       var r_val: Integer = retvalIntegerActLong();
       if (gen)javafx.lang.FX.print("\nvar resRetIntegerActLong_val: Integer={r_val};");       
       assertEquals("{res.resRetIntegerActLong_val as Long}", "{r_val as Long}");

       var r_var: Integer = retvarIntegerActLong();
       if (gen)javafx.lang.FX.print("\nvar resRetIntegerActLong_var: Integer={r_var};\n//");       
       assertEquals("{res.resRetIntegerActLong_var as Long}", "{r_var as Long}");
    }

    function retvarIntegerActFloat(): Integer {var x: Float = 10.5 as Float ; x}
    function retvalIntegerActFloat(): Integer { 10.5 as Float }
    function testParamIntegerActFloat() {
       var r_val: String = paramInteger(10.5 as Float);
       if (gen)javafx.lang.FX.print("\nvar resParamIntegerActFloat_val: Integer={r_val};");       
       assertEquals("{res.resParamIntegerActFloat_val as Long}", "{r_val}");

       var par: Float = 10.5 as Float;
       var r_var: String = paramInteger(par);
       if (gen)javafx.lang.FX.print("\nvar resParamIntegerActFloat_var: Integer={r_var};\n//");       
       assertEquals("{res.resParamIntegerActFloat_var as Long}", "{r_var}");
    }

    function testRetIntegerActFloat() {
       var r_val: Integer = retvalIntegerActFloat();
       if (gen)javafx.lang.FX.print("\nvar resRetIntegerActFloat_val: Integer={r_val};");       
       assertEquals("{res.resRetIntegerActFloat_val as Long}", "{r_val as Long}");

       var r_var: Integer = retvarIntegerActFloat();
       if (gen)javafx.lang.FX.print("\nvar resRetIntegerActFloat_var: Integer={r_var};\n//");       
       assertEquals("{res.resRetIntegerActFloat_var as Long}", "{r_var as Long}");
    }

    function retvarIntegerActDouble(): Integer {var x: Double = 1.25e4 as Double ; x}
    function retvalIntegerActDouble(): Integer { 1.25e4 as Double }
    function testParamIntegerActDouble() {
       var r_val: String = paramInteger(1.25e4 as Double);
       if (gen)javafx.lang.FX.print("\nvar resParamIntegerActDouble_val: Integer={r_val};");       
       assertEquals("{res.resParamIntegerActDouble_val as Long}", "{r_val}");

       var par: Double = 1.25e4 as Double;
       var r_var: String = paramInteger(par);
       if (gen)javafx.lang.FX.print("\nvar resParamIntegerActDouble_var: Integer={r_var};\n//");       
       assertEquals("{res.resParamIntegerActDouble_var as Long}", "{r_var}");
    }

    function testRetIntegerActDouble() {
       var r_val: Integer = retvalIntegerActDouble();
       if (gen)javafx.lang.FX.print("\nvar resRetIntegerActDouble_val: Integer={r_val};");       
       assertEquals("{res.resRetIntegerActDouble_val as Long}", "{r_val as Long}");

       var r_var: Integer = retvarIntegerActDouble();
       if (gen)javafx.lang.FX.print("\nvar resRetIntegerActDouble_var: Integer={r_var};\n//");       
       assertEquals("{res.resRetIntegerActDouble_var as Long}", "{r_var as Long}");
    }

    function retvarIntegerActNumber(): Integer {var x: Number = 100 as Number ; x}
    function retvalIntegerActNumber(): Integer { 100 as Number }
    function testParamIntegerActNumber() {
       var r_val: String = paramInteger(100 as Number);
       if (gen)javafx.lang.FX.print("\nvar resParamIntegerActNumber_val: Integer={r_val};");       
       assertEquals("{res.resParamIntegerActNumber_val as Long}", "{r_val}");

       var par: Number = 100 as Number;
       var r_var: String = paramInteger(par);
       if (gen)javafx.lang.FX.print("\nvar resParamIntegerActNumber_var: Integer={r_var};\n//");       
       assertEquals("{res.resParamIntegerActNumber_var as Long}", "{r_var}");
    }

    function testRetIntegerActNumber() {
       var r_val: Integer = retvalIntegerActNumber();
       if (gen)javafx.lang.FX.print("\nvar resRetIntegerActNumber_val: Integer={r_val};");       
       assertEquals("{res.resRetIntegerActNumber_val as Long}", "{r_val as Long}");

       var r_var: Integer = retvarIntegerActNumber();
       if (gen)javafx.lang.FX.print("\nvar resRetIntegerActNumber_var: Integer={r_var};\n//");       
       assertEquals("{res.resRetIntegerActNumber_var as Long}", "{r_var as Long}");
    }

    function paramLong(x: Long): String {"{x as Long}"}
    function retvarLongActByte(): Long {var x: Byte = 120 as Byte ; x}
    function retvalLongActByte(): Long { 120 as Byte }
    function testParamLongActByte() {
       var r_val: String = paramLong(120 as Byte);
       if (gen)javafx.lang.FX.print("\nvar resParamLongActByte_val: Long={r_val};");       
       assertEquals("{res.resParamLongActByte_val as Long}", "{r_val}");

       var par: Byte = 120 as Byte;
       var r_var: String = paramLong(par);
       if (gen)javafx.lang.FX.print("\nvar resParamLongActByte_var: Long={r_var};\n//");       
       assertEquals("{res.resParamLongActByte_var as Long}", "{r_var}");
    }

    function testRetLongActByte() {
       var r_val: Long = retvalLongActByte();
       if (gen)javafx.lang.FX.print("\nvar resRetLongActByte_val: Long={r_val};");       
       assertEquals("{res.resRetLongActByte_val as Long}", "{r_val as Long}");

       var r_var: Long = retvarLongActByte();
       if (gen)javafx.lang.FX.print("\nvar resRetLongActByte_var: Long={r_var};\n//");       
       assertEquals("{res.resRetLongActByte_var as Long}", "{r_var as Long}");
    }

    function retvarLongActShort(): Long {var x: Short = 30000 as Short ; x}
    function retvalLongActShort(): Long { 30000 as Short }
    function testParamLongActShort() {
       var r_val: String = paramLong(30000 as Short);
       if (gen)javafx.lang.FX.print("\nvar resParamLongActShort_val: Long={r_val};");       
       assertEquals("{res.resParamLongActShort_val as Long}", "{r_val}");

       var par: Short = 30000 as Short;
       var r_var: String = paramLong(par);
       if (gen)javafx.lang.FX.print("\nvar resParamLongActShort_var: Long={r_var};\n//");       
       assertEquals("{res.resParamLongActShort_var as Long}", "{r_var}");
    }

    function testRetLongActShort() {
       var r_val: Long = retvalLongActShort();
       if (gen)javafx.lang.FX.print("\nvar resRetLongActShort_val: Long={r_val};");       
       assertEquals("{res.resRetLongActShort_val as Long}", "{r_val as Long}");

       var r_var: Long = retvarLongActShort();
       if (gen)javafx.lang.FX.print("\nvar resRetLongActShort_var: Long={r_var};\n//");       
       assertEquals("{res.resRetLongActShort_var as Long}", "{r_var as Long}");
    }

    function retvarLongActCharacter(): Long {var x: Character = 64 as Character ; x}
    function retvalLongActCharacter(): Long { 64 as Character }
    function testParamLongActCharacter() {
       var r_val: String = paramLong(64 as Character);
       if (gen)javafx.lang.FX.print("\nvar resParamLongActCharacter_val: Long={r_val};");       
       assertEquals("{res.resParamLongActCharacter_val as Long}", "{r_val}");

       var par: Character = 64 as Character;
       var r_var: String = paramLong(par);
       if (gen)javafx.lang.FX.print("\nvar resParamLongActCharacter_var: Long={r_var};\n//");       
       assertEquals("{res.resParamLongActCharacter_var as Long}", "{r_var}");
    }

    function testRetLongActCharacter() {
       var r_val: Long = retvalLongActCharacter();
       if (gen)javafx.lang.FX.print("\nvar resRetLongActCharacter_val: Long={r_val};");       
       assertEquals("{res.resRetLongActCharacter_val as Long}", "{r_val as Long}");

       var r_var: Long = retvarLongActCharacter();
       if (gen)javafx.lang.FX.print("\nvar resRetLongActCharacter_var: Long={r_var};\n//");       
       assertEquals("{res.resRetLongActCharacter_var as Long}", "{r_var as Long}");
    }

    function retvarLongActInteger(): Long {var x: Integer = 1000000 as Integer ; x}
    function retvalLongActInteger(): Long { 1000000 as Integer }
    function testParamLongActInteger() {
       var r_val: String = paramLong(1000000 as Integer);
       if (gen)javafx.lang.FX.print("\nvar resParamLongActInteger_val: Long={r_val};");       
       assertEquals("{res.resParamLongActInteger_val as Long}", "{r_val}");

       var par: Integer = 1000000 as Integer;
       var r_var: String = paramLong(par);
       if (gen)javafx.lang.FX.print("\nvar resParamLongActInteger_var: Long={r_var};\n//");       
       assertEquals("{res.resParamLongActInteger_var as Long}", "{r_var}");
    }

    function testRetLongActInteger() {
       var r_val: Long = retvalLongActInteger();
       if (gen)javafx.lang.FX.print("\nvar resRetLongActInteger_val: Long={r_val};");       
       assertEquals("{res.resRetLongActInteger_val as Long}", "{r_val as Long}");

       var r_var: Long = retvarLongActInteger();
       if (gen)javafx.lang.FX.print("\nvar resRetLongActInteger_var: Long={r_var};\n//");       
       assertEquals("{res.resRetLongActInteger_var as Long}", "{r_var as Long}");
    }

    function retvarLongActLong(): Long {var x: Long = 1000000000 as Long ; x}
    function retvalLongActLong(): Long { 1000000000 as Long }
    function testParamLongActLong() {
       var r_val: String = paramLong(1000000000 as Long);
       if (gen)javafx.lang.FX.print("\nvar resParamLongActLong_val: Long={r_val};");       
       assertEquals("{res.resParamLongActLong_val as Long}", "{r_val}");

       var par: Long = 1000000000 as Long;
       var r_var: String = paramLong(par);
       if (gen)javafx.lang.FX.print("\nvar resParamLongActLong_var: Long={r_var};\n//");       
       assertEquals("{res.resParamLongActLong_var as Long}", "{r_var}");
    }

    function testRetLongActLong() {
       var r_val: Long = retvalLongActLong();
       if (gen)javafx.lang.FX.print("\nvar resRetLongActLong_val: Long={r_val};");       
       assertEquals("{res.resRetLongActLong_val as Long}", "{r_val as Long}");

       var r_var: Long = retvarLongActLong();
       if (gen)javafx.lang.FX.print("\nvar resRetLongActLong_var: Long={r_var};\n//");       
       assertEquals("{res.resRetLongActLong_var as Long}", "{r_var as Long}");
    }

    function retvarLongActFloat(): Long {var x: Float = 10.5 as Float ; x}
    function retvalLongActFloat(): Long { 10.5 as Float }
    function testParamLongActFloat() {
       var r_val: String = paramLong(10.5 as Float);
       if (gen)javafx.lang.FX.print("\nvar resParamLongActFloat_val: Long={r_val};");       
       assertEquals("{res.resParamLongActFloat_val as Long}", "{r_val}");

       var par: Float = 10.5 as Float;
       var r_var: String = paramLong(par);
       if (gen)javafx.lang.FX.print("\nvar resParamLongActFloat_var: Long={r_var};\n//");       
       assertEquals("{res.resParamLongActFloat_var as Long}", "{r_var}");
    }

    function testRetLongActFloat() {
       var r_val: Long = retvalLongActFloat();
       if (gen)javafx.lang.FX.print("\nvar resRetLongActFloat_val: Long={r_val};");       
       assertEquals("{res.resRetLongActFloat_val as Long}", "{r_val as Long}");

       var r_var: Long = retvarLongActFloat();
       if (gen)javafx.lang.FX.print("\nvar resRetLongActFloat_var: Long={r_var};\n//");       
       assertEquals("{res.resRetLongActFloat_var as Long}", "{r_var as Long}");
    }

    function retvarLongActDouble(): Long {var x: Double = 1.25e4 as Double ; x}
    function retvalLongActDouble(): Long { 1.25e4 as Double }
    function testParamLongActDouble() {
       var r_val: String = paramLong(1.25e4 as Double);
       if (gen)javafx.lang.FX.print("\nvar resParamLongActDouble_val: Long={r_val};");       
       assertEquals("{res.resParamLongActDouble_val as Long}", "{r_val}");

       var par: Double = 1.25e4 as Double;
       var r_var: String = paramLong(par);
       if (gen)javafx.lang.FX.print("\nvar resParamLongActDouble_var: Long={r_var};\n//");       
       assertEquals("{res.resParamLongActDouble_var as Long}", "{r_var}");
    }

    function testRetLongActDouble() {
       var r_val: Long = retvalLongActDouble();
       if (gen)javafx.lang.FX.print("\nvar resRetLongActDouble_val: Long={r_val};");       
       assertEquals("{res.resRetLongActDouble_val as Long}", "{r_val as Long}");

       var r_var: Long = retvarLongActDouble();
       if (gen)javafx.lang.FX.print("\nvar resRetLongActDouble_var: Long={r_var};\n//");       
       assertEquals("{res.resRetLongActDouble_var as Long}", "{r_var as Long}");
    }

    function retvarLongActNumber(): Long {var x: Number = 100 as Number ; x}
    function retvalLongActNumber(): Long { 100 as Number }
    function testParamLongActNumber() {
       var r_val: String = paramLong(100 as Number);
       if (gen)javafx.lang.FX.print("\nvar resParamLongActNumber_val: Long={r_val};");       
       assertEquals("{res.resParamLongActNumber_val as Long}", "{r_val}");

       var par: Number = 100 as Number;
       var r_var: String = paramLong(par);
       if (gen)javafx.lang.FX.print("\nvar resParamLongActNumber_var: Long={r_var};\n//");       
       assertEquals("{res.resParamLongActNumber_var as Long}", "{r_var}");
    }

    function testRetLongActNumber() {
       var r_val: Long = retvalLongActNumber();
       if (gen)javafx.lang.FX.print("\nvar resRetLongActNumber_val: Long={r_val};");       
       assertEquals("{res.resRetLongActNumber_val as Long}", "{r_val as Long}");

       var r_var: Long = retvarLongActNumber();
       if (gen)javafx.lang.FX.print("\nvar resRetLongActNumber_var: Long={r_var};\n//");       
       assertEquals("{res.resRetLongActNumber_var as Long}", "{r_var as Long}");
    }

    function paramFloat(x: Float): String {"{x as Number}"}
    function retvarFloatActByte(): Float {var x: Byte = 120 as Byte ; x}
    function retvalFloatActByte(): Float { 120 as Byte }
    function testParamFloatActByte() {
       var r_val: String = paramFloat(120 as Byte);
       if (gen)javafx.lang.FX.print("\nvar resParamFloatActByte_val: Float={r_val};");       
       assertEquals("{res.resParamFloatActByte_val as Number}", "{r_val}");

       var par: Byte = 120 as Byte;
       var r_var: String = paramFloat(par);
       if (gen)javafx.lang.FX.print("\nvar resParamFloatActByte_var: Float={r_var};\n//");       
       assertEquals("{res.resParamFloatActByte_var as Number}", "{r_var}");
    }

    function testRetFloatActByte() {
       var r_val: Float = retvalFloatActByte();
       if (gen)javafx.lang.FX.print("\nvar resRetFloatActByte_val: Float={r_val};");       
       assertEquals("{res.resRetFloatActByte_val as Number}", "{r_val as Number}");

       var r_var: Float = retvarFloatActByte();
       if (gen)javafx.lang.FX.print("\nvar resRetFloatActByte_var: Float={r_var};\n//");       
       assertEquals("{res.resRetFloatActByte_var as Number}", "{r_var as Number}");
    }

    function retvarFloatActShort(): Float {var x: Short = 30000 as Short ; x}
    function retvalFloatActShort(): Float { 30000 as Short }
    function testParamFloatActShort() {
       var r_val: String = paramFloat(30000 as Short);
       if (gen)javafx.lang.FX.print("\nvar resParamFloatActShort_val: Float={r_val};");       
       assertEquals("{res.resParamFloatActShort_val as Number}", "{r_val}");

       var par: Short = 30000 as Short;
       var r_var: String = paramFloat(par);
       if (gen)javafx.lang.FX.print("\nvar resParamFloatActShort_var: Float={r_var};\n//");       
       assertEquals("{res.resParamFloatActShort_var as Number}", "{r_var}");
    }

    function testRetFloatActShort() {
       var r_val: Float = retvalFloatActShort();
       if (gen)javafx.lang.FX.print("\nvar resRetFloatActShort_val: Float={r_val};");       
       assertEquals("{res.resRetFloatActShort_val as Number}", "{r_val as Number}");

       var r_var: Float = retvarFloatActShort();
       if (gen)javafx.lang.FX.print("\nvar resRetFloatActShort_var: Float={r_var};\n//");       
       assertEquals("{res.resRetFloatActShort_var as Number}", "{r_var as Number}");
    }

    function retvarFloatActCharacter(): Float {var x: Character = 64 as Character ; x}
    function retvalFloatActCharacter(): Float { 64 as Character }
    function testParamFloatActCharacter() {
       var r_val: String = paramFloat(64 as Character);
       if (gen)javafx.lang.FX.print("\nvar resParamFloatActCharacter_val: Float={r_val};");       
       assertEquals("{res.resParamFloatActCharacter_val as Number}", "{r_val}");

       var par: Character = 64 as Character;
       var r_var: String = paramFloat(par);
       if (gen)javafx.lang.FX.print("\nvar resParamFloatActCharacter_var: Float={r_var};\n//");       
       assertEquals("{res.resParamFloatActCharacter_var as Number}", "{r_var}");
    }

    function testRetFloatActCharacter() {
       var r_val: Float = retvalFloatActCharacter();
       if (gen)javafx.lang.FX.print("\nvar resRetFloatActCharacter_val: Float={r_val};");       
       assertEquals("{res.resRetFloatActCharacter_val as Number}", "{r_val as Number}");

       var r_var: Float = retvarFloatActCharacter();
       if (gen)javafx.lang.FX.print("\nvar resRetFloatActCharacter_var: Float={r_var};\n//");       
       assertEquals("{res.resRetFloatActCharacter_var as Number}", "{r_var as Number}");
    }

    function retvarFloatActInteger(): Float {var x: Integer = 1000000 as Integer ; x}
    function retvalFloatActInteger(): Float { 1000000 as Integer }
    function testParamFloatActInteger() {
       var r_val: String = paramFloat(1000000 as Integer);
       if (gen)javafx.lang.FX.print("\nvar resParamFloatActInteger_val: Float={r_val};");       
       assertEquals("{res.resParamFloatActInteger_val as Number}", "{r_val}");

       var par: Integer = 1000000 as Integer;
       var r_var: String = paramFloat(par);
       if (gen)javafx.lang.FX.print("\nvar resParamFloatActInteger_var: Float={r_var};\n//");       
       assertEquals("{res.resParamFloatActInteger_var as Number}", "{r_var}");
    }

    function testRetFloatActInteger() {
       var r_val: Float = retvalFloatActInteger();
       if (gen)javafx.lang.FX.print("\nvar resRetFloatActInteger_val: Float={r_val};");       
       assertEquals("{res.resRetFloatActInteger_val as Number}", "{r_val as Number}");

       var r_var: Float = retvarFloatActInteger();
       if (gen)javafx.lang.FX.print("\nvar resRetFloatActInteger_var: Float={r_var};\n//");       
       assertEquals("{res.resRetFloatActInteger_var as Number}", "{r_var as Number}");
    }

    function retvarFloatActLong(): Float {var x: Long = 1000000000 as Long ; x}
    function retvalFloatActLong(): Float { 1000000000 as Long }
    function testParamFloatActLong() {
       var r_val: String = paramFloat(1000000000 as Long);
       if (gen)javafx.lang.FX.print("\nvar resParamFloatActLong_val: Float={r_val};");       
       assertEquals("{res.resParamFloatActLong_val as Number}", "{r_val}");

       var par: Long = 1000000000 as Long;
       var r_var: String = paramFloat(par);
       if (gen)javafx.lang.FX.print("\nvar resParamFloatActLong_var: Float={r_var};\n//");       
       assertEquals("{res.resParamFloatActLong_var as Number}", "{r_var}");
    }

    function testRetFloatActLong() {
       var r_val: Float = retvalFloatActLong();
       if (gen)javafx.lang.FX.print("\nvar resRetFloatActLong_val: Float={r_val};");       
       assertEquals("{res.resRetFloatActLong_val as Number}", "{r_val as Number}");

       var r_var: Float = retvarFloatActLong();
       if (gen)javafx.lang.FX.print("\nvar resRetFloatActLong_var: Float={r_var};\n//");       
       assertEquals("{res.resRetFloatActLong_var as Number}", "{r_var as Number}");
    }

    function retvarFloatActFloat(): Float {var x: Float = 10.5 as Float ; x}
    function retvalFloatActFloat(): Float { 10.5 as Float }
    function testParamFloatActFloat() {
       var r_val: String = paramFloat(10.5 as Float);
       if (gen)javafx.lang.FX.print("\nvar resParamFloatActFloat_val: Float={r_val};");       
       assertEquals("{res.resParamFloatActFloat_val as Number}", "{r_val}");

       var par: Float = 10.5 as Float;
       var r_var: String = paramFloat(par);
       if (gen)javafx.lang.FX.print("\nvar resParamFloatActFloat_var: Float={r_var};\n//");       
       assertEquals("{res.resParamFloatActFloat_var as Number}", "{r_var}");
    }

    function testRetFloatActFloat() {
       var r_val: Float = retvalFloatActFloat();
       if (gen)javafx.lang.FX.print("\nvar resRetFloatActFloat_val: Float={r_val};");       
       assertEquals("{res.resRetFloatActFloat_val as Number}", "{r_val as Number}");

       var r_var: Float = retvarFloatActFloat();
       if (gen)javafx.lang.FX.print("\nvar resRetFloatActFloat_var: Float={r_var};\n//");       
       assertEquals("{res.resRetFloatActFloat_var as Number}", "{r_var as Number}");
    }

    function retvarFloatActDouble(): Float {var x: Double = 1.25e4 as Double ; x}
    function retvalFloatActDouble(): Float { 1.25e4 as Double }
    function testParamFloatActDouble() {
       var r_val: String = paramFloat(1.25e4 as Double);
       if (gen)javafx.lang.FX.print("\nvar resParamFloatActDouble_val: Float={r_val};");       
       assertEquals("{res.resParamFloatActDouble_val as Number}", "{r_val}");

       var par: Double = 1.25e4 as Double;
       var r_var: String = paramFloat(par);
       if (gen)javafx.lang.FX.print("\nvar resParamFloatActDouble_var: Float={r_var};\n//");       
       assertEquals("{res.resParamFloatActDouble_var as Number}", "{r_var}");
    }

    function testRetFloatActDouble() {
       var r_val: Float = retvalFloatActDouble();
       if (gen)javafx.lang.FX.print("\nvar resRetFloatActDouble_val: Float={r_val};");       
       assertEquals("{res.resRetFloatActDouble_val as Number}", "{r_val as Number}");

       var r_var: Float = retvarFloatActDouble();
       if (gen)javafx.lang.FX.print("\nvar resRetFloatActDouble_var: Float={r_var};\n//");       
       assertEquals("{res.resRetFloatActDouble_var as Number}", "{r_var as Number}");
    }

    function retvarFloatActNumber(): Float {var x: Number = 100 as Number ; x}
    function retvalFloatActNumber(): Float { 100 as Number }
    function testParamFloatActNumber() {
       var r_val: String = paramFloat(100 as Number);
       if (gen)javafx.lang.FX.print("\nvar resParamFloatActNumber_val: Float={r_val};");       
       assertEquals("{res.resParamFloatActNumber_val as Number}", "{r_val}");

       var par: Number = 100 as Number;
       var r_var: String = paramFloat(par);
       if (gen)javafx.lang.FX.print("\nvar resParamFloatActNumber_var: Float={r_var};\n//");       
       assertEquals("{res.resParamFloatActNumber_var as Number}", "{r_var}");
    }

    function testRetFloatActNumber() {
       var r_val: Float = retvalFloatActNumber();
       if (gen)javafx.lang.FX.print("\nvar resRetFloatActNumber_val: Float={r_val};");       
       assertEquals("{res.resRetFloatActNumber_val as Number}", "{r_val as Number}");

       var r_var: Float = retvarFloatActNumber();
       if (gen)javafx.lang.FX.print("\nvar resRetFloatActNumber_var: Float={r_var};\n//");       
       assertEquals("{res.resRetFloatActNumber_var as Number}", "{r_var as Number}");
    }

    function paramDouble(x: Double): String {"{x as Number}"}
    function retvarDoubleActByte(): Double {var x: Byte = 120 as Byte ; x}
    function retvalDoubleActByte(): Double { 120 as Byte }
    function testParamDoubleActByte() {
       var r_val: String = paramDouble(120 as Byte);
       if (gen)javafx.lang.FX.print("\nvar resParamDoubleActByte_val: Double={r_val};");       
       assertEquals("{res.resParamDoubleActByte_val as Number}", "{r_val}");

       var par: Byte = 120 as Byte;
       var r_var: String = paramDouble(par);
       if (gen)javafx.lang.FX.print("\nvar resParamDoubleActByte_var: Double={r_var};\n//");       
       assertEquals("{res.resParamDoubleActByte_var as Number}", "{r_var}");
    }

    function testRetDoubleActByte() {
       var r_val: Double = retvalDoubleActByte();
       if (gen)javafx.lang.FX.print("\nvar resRetDoubleActByte_val: Double={r_val};");       
       assertEquals("{res.resRetDoubleActByte_val as Number}", "{r_val as Number}");

       var r_var: Double = retvarDoubleActByte();
       if (gen)javafx.lang.FX.print("\nvar resRetDoubleActByte_var: Double={r_var};\n//");       
       assertEquals("{res.resRetDoubleActByte_var as Number}", "{r_var as Number}");
    }

    function retvarDoubleActShort(): Double {var x: Short = 30000 as Short ; x}
    function retvalDoubleActShort(): Double { 30000 as Short }
    function testParamDoubleActShort() {
       var r_val: String = paramDouble(30000 as Short);
       if (gen)javafx.lang.FX.print("\nvar resParamDoubleActShort_val: Double={r_val};");       
       assertEquals("{res.resParamDoubleActShort_val as Number}", "{r_val}");

       var par: Short = 30000 as Short;
       var r_var: String = paramDouble(par);
       if (gen)javafx.lang.FX.print("\nvar resParamDoubleActShort_var: Double={r_var};\n//");       
       assertEquals("{res.resParamDoubleActShort_var as Number}", "{r_var}");
    }

    function testRetDoubleActShort() {
       var r_val: Double = retvalDoubleActShort();
       if (gen)javafx.lang.FX.print("\nvar resRetDoubleActShort_val: Double={r_val};");       
       assertEquals("{res.resRetDoubleActShort_val as Number}", "{r_val as Number}");

       var r_var: Double = retvarDoubleActShort();
       if (gen)javafx.lang.FX.print("\nvar resRetDoubleActShort_var: Double={r_var};\n//");       
       assertEquals("{res.resRetDoubleActShort_var as Number}", "{r_var as Number}");
    }

    function retvarDoubleActCharacter(): Double {var x: Character = 64 as Character ; x}
    function retvalDoubleActCharacter(): Double { 64 as Character }
    function testParamDoubleActCharacter() {
       var r_val: String = paramDouble(64 as Character);
       if (gen)javafx.lang.FX.print("\nvar resParamDoubleActCharacter_val: Double={r_val};");       
       assertEquals("{res.resParamDoubleActCharacter_val as Number}", "{r_val}");

       var par: Character = 64 as Character;
       var r_var: String = paramDouble(par);
       if (gen)javafx.lang.FX.print("\nvar resParamDoubleActCharacter_var: Double={r_var};\n//");       
       assertEquals("{res.resParamDoubleActCharacter_var as Number}", "{r_var}");
    }

    function testRetDoubleActCharacter() {
       var r_val: Double = retvalDoubleActCharacter();
       if (gen)javafx.lang.FX.print("\nvar resRetDoubleActCharacter_val: Double={r_val};");       
       assertEquals("{res.resRetDoubleActCharacter_val as Number}", "{r_val as Number}");

       var r_var: Double = retvarDoubleActCharacter();
       if (gen)javafx.lang.FX.print("\nvar resRetDoubleActCharacter_var: Double={r_var};\n//");       
       assertEquals("{res.resRetDoubleActCharacter_var as Number}", "{r_var as Number}");
    }

    function retvarDoubleActInteger(): Double {var x: Integer = 1000000 as Integer ; x}
    function retvalDoubleActInteger(): Double { 1000000 as Integer }
    function testParamDoubleActInteger() {
       var r_val: String = paramDouble(1000000 as Integer);
       if (gen)javafx.lang.FX.print("\nvar resParamDoubleActInteger_val: Double={r_val};");       
       assertEquals("{res.resParamDoubleActInteger_val as Number}", "{r_val}");

       var par: Integer = 1000000 as Integer;
       var r_var: String = paramDouble(par);
       if (gen)javafx.lang.FX.print("\nvar resParamDoubleActInteger_var: Double={r_var};\n//");       
       assertEquals("{res.resParamDoubleActInteger_var as Number}", "{r_var}");
    }

    function testRetDoubleActInteger() {
       var r_val: Double = retvalDoubleActInteger();
       if (gen)javafx.lang.FX.print("\nvar resRetDoubleActInteger_val: Double={r_val};");       
       assertEquals("{res.resRetDoubleActInteger_val as Number}", "{r_val as Number}");

       var r_var: Double = retvarDoubleActInteger();
       if (gen)javafx.lang.FX.print("\nvar resRetDoubleActInteger_var: Double={r_var};\n//");       
       assertEquals("{res.resRetDoubleActInteger_var as Number}", "{r_var as Number}");
    }

    function retvarDoubleActLong(): Double {var x: Long = 1000000000 as Long ; x}
    function retvalDoubleActLong(): Double { 1000000000 as Long }
    function testParamDoubleActLong() {
       var r_val: String = paramDouble(1000000000 as Long);
       if (gen)javafx.lang.FX.print("\nvar resParamDoubleActLong_val: Double={r_val};");       
       assertEquals("{res.resParamDoubleActLong_val as Number}", "{r_val}");

       var par: Long = 1000000000 as Long;
       var r_var: String = paramDouble(par);
       if (gen)javafx.lang.FX.print("\nvar resParamDoubleActLong_var: Double={r_var};\n//");       
       assertEquals("{res.resParamDoubleActLong_var as Number}", "{r_var}");
    }

    function testRetDoubleActLong() {
       var r_val: Double = retvalDoubleActLong();
       if (gen)javafx.lang.FX.print("\nvar resRetDoubleActLong_val: Double={r_val};");       
       assertEquals("{res.resRetDoubleActLong_val as Number}", "{r_val as Number}");

       var r_var: Double = retvarDoubleActLong();
       if (gen)javafx.lang.FX.print("\nvar resRetDoubleActLong_var: Double={r_var};\n//");       
       assertEquals("{res.resRetDoubleActLong_var as Number}", "{r_var as Number}");
    }

    function retvarDoubleActFloat(): Double {var x: Float = 10.5 as Float ; x}
    function retvalDoubleActFloat(): Double { 10.5 as Float }
    function testParamDoubleActFloat() {
       var r_val: String = paramDouble(10.5 as Float);
       if (gen)javafx.lang.FX.print("\nvar resParamDoubleActFloat_val: Double={r_val};");       
       assertEquals("{res.resParamDoubleActFloat_val as Number}", "{r_val}");

       var par: Float = 10.5 as Float;
       var r_var: String = paramDouble(par);
       if (gen)javafx.lang.FX.print("\nvar resParamDoubleActFloat_var: Double={r_var};\n//");       
       assertEquals("{res.resParamDoubleActFloat_var as Number}", "{r_var}");
    }

    function testRetDoubleActFloat() {
       var r_val: Double = retvalDoubleActFloat();
       if (gen)javafx.lang.FX.print("\nvar resRetDoubleActFloat_val: Double={r_val};");       
       assertEquals("{res.resRetDoubleActFloat_val as Number}", "{r_val as Number}");

       var r_var: Double = retvarDoubleActFloat();
       if (gen)javafx.lang.FX.print("\nvar resRetDoubleActFloat_var: Double={r_var};\n//");       
       assertEquals("{res.resRetDoubleActFloat_var as Number}", "{r_var as Number}");
    }

    function retvarDoubleActDouble(): Double {var x: Double = 1.25e4 as Double ; x}
    function retvalDoubleActDouble(): Double { 1.25e4 as Double }
    function testParamDoubleActDouble() {
       var r_val: String = paramDouble(1.25e4 as Double);
       if (gen)javafx.lang.FX.print("\nvar resParamDoubleActDouble_val: Double={r_val};");       
       assertEquals("{res.resParamDoubleActDouble_val as Number}", "{r_val}");

       var par: Double = 1.25e4 as Double;
       var r_var: String = paramDouble(par);
       if (gen)javafx.lang.FX.print("\nvar resParamDoubleActDouble_var: Double={r_var};\n//");       
       assertEquals("{res.resParamDoubleActDouble_var as Number}", "{r_var}");
    }

    function testRetDoubleActDouble() {
       var r_val: Double = retvalDoubleActDouble();
       if (gen)javafx.lang.FX.print("\nvar resRetDoubleActDouble_val: Double={r_val};");       
       assertEquals("{res.resRetDoubleActDouble_val as Number}", "{r_val as Number}");

       var r_var: Double = retvarDoubleActDouble();
       if (gen)javafx.lang.FX.print("\nvar resRetDoubleActDouble_var: Double={r_var};\n//");       
       assertEquals("{res.resRetDoubleActDouble_var as Number}", "{r_var as Number}");
    }

    function retvarDoubleActNumber(): Double {var x: Number = 100 as Number ; x}
    function retvalDoubleActNumber(): Double { 100 as Number }
    function testParamDoubleActNumber() {
       var r_val: String = paramDouble(100 as Number);
       if (gen)javafx.lang.FX.print("\nvar resParamDoubleActNumber_val: Double={r_val};");       
       assertEquals("{res.resParamDoubleActNumber_val as Number}", "{r_val}");

       var par: Number = 100 as Number;
       var r_var: String = paramDouble(par);
       if (gen)javafx.lang.FX.print("\nvar resParamDoubleActNumber_var: Double={r_var};\n//");       
       assertEquals("{res.resParamDoubleActNumber_var as Number}", "{r_var}");
    }

    function testRetDoubleActNumber() {
       var r_val: Double = retvalDoubleActNumber();
       if (gen)javafx.lang.FX.print("\nvar resRetDoubleActNumber_val: Double={r_val};");       
       assertEquals("{res.resRetDoubleActNumber_val as Number}", "{r_val as Number}");

       var r_var: Double = retvarDoubleActNumber();
       if (gen)javafx.lang.FX.print("\nvar resRetDoubleActNumber_var: Double={r_var};\n//");       
       assertEquals("{res.resRetDoubleActNumber_var as Number}", "{r_var as Number}");
    }

    function paramNumber(x: Number): String {"{x as Number}"}
    function retvarNumberActByte(): Number {var x: Byte = 120 as Byte ; x}
    function retvalNumberActByte(): Number { 120 as Byte }
    function testParamNumberActByte() {
       var r_val: String = paramNumber(120 as Byte);
       if (gen)javafx.lang.FX.print("\nvar resParamNumberActByte_val: Number={r_val};");       
       assertEquals("{res.resParamNumberActByte_val as Number}", "{r_val}");

       var par: Byte = 120 as Byte;
       var r_var: String = paramNumber(par);
       if (gen)javafx.lang.FX.print("\nvar resParamNumberActByte_var: Number={r_var};\n//");       
       assertEquals("{res.resParamNumberActByte_var as Number}", "{r_var}");
    }

    function testRetNumberActByte() {
       var r_val: Number = retvalNumberActByte();
       if (gen)javafx.lang.FX.print("\nvar resRetNumberActByte_val: Number={r_val};");       
       assertEquals("{res.resRetNumberActByte_val as Number}", "{r_val as Number}");

       var r_var: Number = retvarNumberActByte();
       if (gen)javafx.lang.FX.print("\nvar resRetNumberActByte_var: Number={r_var};\n//");       
       assertEquals("{res.resRetNumberActByte_var as Number}", "{r_var as Number}");
    }

    function retvarNumberActShort(): Number {var x: Short = 30000 as Short ; x}
    function retvalNumberActShort(): Number { 30000 as Short }
    function testParamNumberActShort() {
       var r_val: String = paramNumber(30000 as Short);
       if (gen)javafx.lang.FX.print("\nvar resParamNumberActShort_val: Number={r_val};");       
       assertEquals("{res.resParamNumberActShort_val as Number}", "{r_val}");

       var par: Short = 30000 as Short;
       var r_var: String = paramNumber(par);
       if (gen)javafx.lang.FX.print("\nvar resParamNumberActShort_var: Number={r_var};\n//");       
       assertEquals("{res.resParamNumberActShort_var as Number}", "{r_var}");
    }

    function testRetNumberActShort() {
       var r_val: Number = retvalNumberActShort();
       if (gen)javafx.lang.FX.print("\nvar resRetNumberActShort_val: Number={r_val};");       
       assertEquals("{res.resRetNumberActShort_val as Number}", "{r_val as Number}");

       var r_var: Number = retvarNumberActShort();
       if (gen)javafx.lang.FX.print("\nvar resRetNumberActShort_var: Number={r_var};\n//");       
       assertEquals("{res.resRetNumberActShort_var as Number}", "{r_var as Number}");
    }

    function retvarNumberActCharacter(): Number {var x: Character = 64 as Character ; x}
    function retvalNumberActCharacter(): Number { 64 as Character }
    function testParamNumberActCharacter() {
       var r_val: String = paramNumber(64 as Character);
       if (gen)javafx.lang.FX.print("\nvar resParamNumberActCharacter_val: Number={r_val};");       
       assertEquals("{res.resParamNumberActCharacter_val as Number}", "{r_val}");

       var par: Character = 64 as Character;
       var r_var: String = paramNumber(par);
       if (gen)javafx.lang.FX.print("\nvar resParamNumberActCharacter_var: Number={r_var};\n//");       
       assertEquals("{res.resParamNumberActCharacter_var as Number}", "{r_var}");
    }

    function testRetNumberActCharacter() {
       var r_val: Number = retvalNumberActCharacter();
       if (gen)javafx.lang.FX.print("\nvar resRetNumberActCharacter_val: Number={r_val};");       
       assertEquals("{res.resRetNumberActCharacter_val as Number}", "{r_val as Number}");

       var r_var: Number = retvarNumberActCharacter();
       if (gen)javafx.lang.FX.print("\nvar resRetNumberActCharacter_var: Number={r_var};\n//");       
       assertEquals("{res.resRetNumberActCharacter_var as Number}", "{r_var as Number}");
    }

    function retvarNumberActInteger(): Number {var x: Integer = 1000000 as Integer ; x}
    function retvalNumberActInteger(): Number { 1000000 as Integer }
    function testParamNumberActInteger() {
       var r_val: String = paramNumber(1000000 as Integer);
       if (gen)javafx.lang.FX.print("\nvar resParamNumberActInteger_val: Number={r_val};");       
       assertEquals("{res.resParamNumberActInteger_val as Number}", "{r_val}");

       var par: Integer = 1000000 as Integer;
       var r_var: String = paramNumber(par);
       if (gen)javafx.lang.FX.print("\nvar resParamNumberActInteger_var: Number={r_var};\n//");       
       assertEquals("{res.resParamNumberActInteger_var as Number}", "{r_var}");
    }

    function testRetNumberActInteger() {
       var r_val: Number = retvalNumberActInteger();
       if (gen)javafx.lang.FX.print("\nvar resRetNumberActInteger_val: Number={r_val};");       
       assertEquals("{res.resRetNumberActInteger_val as Number}", "{r_val as Number}");

       var r_var: Number = retvarNumberActInteger();
       if (gen)javafx.lang.FX.print("\nvar resRetNumberActInteger_var: Number={r_var};\n//");       
       assertEquals("{res.resRetNumberActInteger_var as Number}", "{r_var as Number}");
    }

    function retvarNumberActLong(): Number {var x: Long = 1000000000 as Long ; x}
    function retvalNumberActLong(): Number { 1000000000 as Long }
    function testParamNumberActLong() {
       var r_val: String = paramNumber(1000000000 as Long);
       if (gen)javafx.lang.FX.print("\nvar resParamNumberActLong_val: Number={r_val};");       
       assertEquals("{res.resParamNumberActLong_val as Number}", "{r_val}");

       var par: Long = 1000000000 as Long;
       var r_var: String = paramNumber(par);
       if (gen)javafx.lang.FX.print("\nvar resParamNumberActLong_var: Number={r_var};\n//");       
       assertEquals("{res.resParamNumberActLong_var as Number}", "{r_var}");
    }

    function testRetNumberActLong() {
       var r_val: Number = retvalNumberActLong();
       if (gen)javafx.lang.FX.print("\nvar resRetNumberActLong_val: Number={r_val};");       
       assertEquals("{res.resRetNumberActLong_val as Number}", "{r_val as Number}");

       var r_var: Number = retvarNumberActLong();
       if (gen)javafx.lang.FX.print("\nvar resRetNumberActLong_var: Number={r_var};\n//");       
       assertEquals("{res.resRetNumberActLong_var as Number}", "{r_var as Number}");
    }

    function retvarNumberActFloat(): Number {var x: Float = 10.5 as Float ; x}
    function retvalNumberActFloat(): Number { 10.5 as Float }
    function testParamNumberActFloat() {
       var r_val: String = paramNumber(10.5 as Float);
       if (gen)javafx.lang.FX.print("\nvar resParamNumberActFloat_val: Number={r_val};");       
       assertEquals("{res.resParamNumberActFloat_val as Number}", "{r_val}");

       var par: Float = 10.5 as Float;
       var r_var: String = paramNumber(par);
       if (gen)javafx.lang.FX.print("\nvar resParamNumberActFloat_var: Number={r_var};\n//");       
       assertEquals("{res.resParamNumberActFloat_var as Number}", "{r_var}");
    }

    function testRetNumberActFloat() {
       var r_val: Number = retvalNumberActFloat();
       if (gen)javafx.lang.FX.print("\nvar resRetNumberActFloat_val: Number={r_val};");       
       assertEquals("{res.resRetNumberActFloat_val as Number}", "{r_val as Number}");

       var r_var: Number = retvarNumberActFloat();
       if (gen)javafx.lang.FX.print("\nvar resRetNumberActFloat_var: Number={r_var};\n//");       
       assertEquals("{res.resRetNumberActFloat_var as Number}", "{r_var as Number}");
    }

    function retvarNumberActDouble(): Number {var x: Double = 1.25e4 as Double ; x}
    function retvalNumberActDouble(): Number { 1.25e4 as Double }
    function testParamNumberActDouble() {
       var r_val: String = paramNumber(1.25e4 as Double);
       if (gen)javafx.lang.FX.print("\nvar resParamNumberActDouble_val: Number={r_val};");       
       assertEquals("{res.resParamNumberActDouble_val as Number}", "{r_val}");

       var par: Double = 1.25e4 as Double;
       var r_var: String = paramNumber(par);
       if (gen)javafx.lang.FX.print("\nvar resParamNumberActDouble_var: Number={r_var};\n//");       
       assertEquals("{res.resParamNumberActDouble_var as Number}", "{r_var}");
    }

    function testRetNumberActDouble() {
       var r_val: Number = retvalNumberActDouble();
       if (gen)javafx.lang.FX.print("\nvar resRetNumberActDouble_val: Number={r_val};");       
       assertEquals("{res.resRetNumberActDouble_val as Number}", "{r_val as Number}");

       var r_var: Number = retvarNumberActDouble();
       if (gen)javafx.lang.FX.print("\nvar resRetNumberActDouble_var: Number={r_var};\n//");       
       assertEquals("{res.resRetNumberActDouble_var as Number}", "{r_var as Number}");
    }

    function retvarNumberActNumber(): Number {var x: Number = 100 as Number ; x}
    function retvalNumberActNumber(): Number { 100 as Number }
    function testParamNumberActNumber() {
       var r_val: String = paramNumber(100 as Number);
       if (gen)javafx.lang.FX.print("\nvar resParamNumberActNumber_val: Number={r_val};");       
       assertEquals("{res.resParamNumberActNumber_val as Number}", "{r_val}");

       var par: Number = 100 as Number;
       var r_var: String = paramNumber(par);
       if (gen)javafx.lang.FX.print("\nvar resParamNumberActNumber_var: Number={r_var};\n//");       
       assertEquals("{res.resParamNumberActNumber_var as Number}", "{r_var}");
    }

    function testRetNumberActNumber() {
       var r_val: Number = retvalNumberActNumber();
       if (gen)javafx.lang.FX.print("\nvar resRetNumberActNumber_val: Number={r_val};");       
       assertEquals("{res.resRetNumberActNumber_val as Number}", "{r_val as Number}");

       var r_var: Number = retvarNumberActNumber();
       if (gen)javafx.lang.FX.print("\nvar resRetNumberActNumber_var: Number={r_var};\n//");       
       assertEquals("{res.resRetNumberActNumber_var as Number}", "{r_var as Number}");
    }

var resParamByteActByte_val: Byte=120;
var resParamByteActByte_var: Byte=120;
//.
var resRetByteActByte_val: Byte=120;
var resRetByteActByte_var: Byte=120;
//.
var resParamByteActShort_val: Byte=48;
var resParamByteActShort_var: Byte=48;
//.
var resRetByteActShort_val: Byte=48;
var resRetByteActShort_var: Byte=48;
//..
var resRetByteActCharacter_val: Byte=64;
var resRetByteActCharacter_var: Byte=64;
//.
var resParamByteActInteger_val: Byte=64;
var resParamByteActInteger_var: Byte=64;
//.
var resRetByteActInteger_val: Byte=64;
var resRetByteActInteger_var: Byte=64;
//.
var resParamByteActLong_val: Byte=0;
var resParamByteActLong_var: Byte=0;
//.
var resRetByteActLong_val: Byte=0;
var resRetByteActLong_var: Byte=0;
//.
var resParamByteActFloat_val: Byte=10;
var resParamByteActFloat_var: Byte=10;
//.
var resRetByteActFloat_val: Byte=10;
var resRetByteActFloat_var: Byte=10;
//.
var resParamByteActDouble_val: Byte=-44;
var resParamByteActDouble_var: Byte=-44;
//.
var resRetByteActDouble_val: Byte=-44;
var resRetByteActDouble_var: Byte=-44;
//.
var resParamByteActNumber_val: Byte=100;
var resParamByteActNumber_var: Byte=100;
//.
var resRetByteActNumber_val: Byte=100;
var resRetByteActNumber_var: Byte=100;
//.
var resParamShortActByte_val: Short=120;
var resParamShortActByte_var: Short=120;
//.
var resRetShortActByte_val: Short=120;
var resRetShortActByte_var: Short=120;
//.
var resParamShortActShort_val: Short=30000;
var resParamShortActShort_var: Short=30000;
//.
var resRetShortActShort_val: Short=30000;
var resRetShortActShort_var: Short=30000;
//..
var resRetShortActCharacter_val: Short=64;
var resRetShortActCharacter_var: Short=64;
//.
var resParamShortActInteger_val: Short=16960;
var resParamShortActInteger_var: Short=16960;
//.
var resRetShortActInteger_val: Short=16960;
var resRetShortActInteger_var: Short=16960;
//.
var resParamShortActLong_val: Short=-13824;
var resParamShortActLong_var: Short=-13824;
//.
var resRetShortActLong_val: Short=-13824;
var resRetShortActLong_var: Short=-13824;
//.
var resParamShortActFloat_val: Short=10;
var resParamShortActFloat_var: Short=10;
//.
var resRetShortActFloat_val: Short=10;
var resRetShortActFloat_var: Short=10;
//.
var resParamShortActDouble_val: Short=12500;
var resParamShortActDouble_var: Short=12500;
//.
var resRetShortActDouble_val: Short=12500;
var resRetShortActDouble_var: Short=12500;
//.
var resParamShortActNumber_val: Short=100;
var resParamShortActNumber_var: Short=100;
//.
var resRetShortActNumber_val: Short=100;
var resRetShortActNumber_var: Short=100;
//..
var resRetCharacterActByte_val: Character=120;
var resRetCharacterActByte_var: Character=120;
//..
var resRetCharacterActShort_val: Character=30000;
var resRetCharacterActShort_var: Character=30000;
//.
var resParamCharacterActCharacter_val: Character=64;
var resParamCharacterActCharacter_var: Character=64;
//.
var resRetCharacterActCharacter_val: Character=64;
var resRetCharacterActCharacter_var: Character=64;
//..
var resRetCharacterActInteger_val: Character=16960;
var resRetCharacterActInteger_var: Character=16960;
//..
var resRetCharacterActLong_val: Character=51712;
var resRetCharacterActLong_var: Character=51712;
//..
var resRetCharacterActFloat_val: Character=10;
var resRetCharacterActFloat_var: Character=10;
//..
var resRetCharacterActDouble_val: Character=12500;
var resRetCharacterActDouble_var: Character=12500;
//..
var resRetCharacterActNumber_val: Character=100;
var resRetCharacterActNumber_var: Character=100;
//.
var resParamIntegerActByte_val: Integer=120;
var resParamIntegerActByte_var: Integer=120;
//.
var resRetIntegerActByte_val: Integer=120;
var resRetIntegerActByte_var: Integer=120;
//.
var resParamIntegerActShort_val: Integer=30000;
var resParamIntegerActShort_var: Integer=30000;
//.
var resRetIntegerActShort_val: Integer=30000;
var resRetIntegerActShort_var: Integer=30000;
//.
var resParamIntegerActCharacter_val: Integer=64;
var resParamIntegerActCharacter_var: Integer=64;
//.
var resRetIntegerActCharacter_val: Integer=64;
var resRetIntegerActCharacter_var: Integer=64;
//.
var resParamIntegerActInteger_val: Integer=1000000;
var resParamIntegerActInteger_var: Integer=1000000;
//.
var resRetIntegerActInteger_val: Integer=1000000;
var resRetIntegerActInteger_var: Integer=1000000;
//.
var resParamIntegerActLong_val: Integer=1000000000;
var resParamIntegerActLong_var: Integer=1000000000;
//.
var resRetIntegerActLong_val: Integer=1000000000;
var resRetIntegerActLong_var: Integer=1000000000;
//.
var resParamIntegerActFloat_val: Integer=10;
var resParamIntegerActFloat_var: Integer=10;
//.
var resRetIntegerActFloat_val: Integer=10;
var resRetIntegerActFloat_var: Integer=10;
//.
var resParamIntegerActDouble_val: Integer=12500;
var resParamIntegerActDouble_var: Integer=12500;
//.
var resRetIntegerActDouble_val: Integer=12500;
var resRetIntegerActDouble_var: Integer=12500;
//.
var resParamIntegerActNumber_val: Integer=100;
var resParamIntegerActNumber_var: Integer=100;
//.
var resRetIntegerActNumber_val: Integer=100;
var resRetIntegerActNumber_var: Integer=100;
//.
var resParamLongActByte_val: Long=120;
var resParamLongActByte_var: Long=120;
//.
var resRetLongActByte_val: Long=120;
var resRetLongActByte_var: Long=120;
//.
var resParamLongActShort_val: Long=30000;
var resParamLongActShort_var: Long=30000;
//.
var resRetLongActShort_val: Long=30000;
var resRetLongActShort_var: Long=30000;
//.
var resParamLongActCharacter_val: Long=64;
var resParamLongActCharacter_var: Long=64;
//.
var resRetLongActCharacter_val: Long=64;
var resRetLongActCharacter_var: Long=64;
//.
var resParamLongActInteger_val: Long=1000000;
var resParamLongActInteger_var: Long=1000000;
//.
var resRetLongActInteger_val: Long=1000000;
var resRetLongActInteger_var: Long=1000000;
//.
var resParamLongActLong_val: Long=1000000000;
var resParamLongActLong_var: Long=1000000000;
//.
var resRetLongActLong_val: Long=1000000000;
var resRetLongActLong_var: Long=1000000000;
//.
var resParamLongActFloat_val: Long=10;
var resParamLongActFloat_var: Long=10;
//.
var resRetLongActFloat_val: Long=10;
var resRetLongActFloat_var: Long=10;
//.
var resParamLongActDouble_val: Long=12500;
var resParamLongActDouble_var: Long=12500;
//.
var resRetLongActDouble_val: Long=12500;
var resRetLongActDouble_var: Long=12500;
//.
var resParamLongActNumber_val: Long=100;
var resParamLongActNumber_var: Long=100;
//.
var resRetLongActNumber_val: Long=100;
var resRetLongActNumber_var: Long=100;
//.
var resParamFloatActByte_val: Float=120.0;
var resParamFloatActByte_var: Float=120.0;
//.
var resRetFloatActByte_val: Float=120.0;
var resRetFloatActByte_var: Float=120.0;
//.
var resParamFloatActShort_val: Float=30000.0;
var resParamFloatActShort_var: Float=30000.0;
//.
var resRetFloatActShort_val: Float=30000.0;
var resRetFloatActShort_var: Float=30000.0;
//.
var resParamFloatActCharacter_val: Float=64.0;
var resParamFloatActCharacter_var: Float=64.0;
//.
var resRetFloatActCharacter_val: Float=64.0;
var resRetFloatActCharacter_var: Float=64.0;
//.
var resParamFloatActInteger_val: Float=1000000.0;
var resParamFloatActInteger_var: Float=1000000.0;
//.
var resRetFloatActInteger_val: Float=1000000.0;
var resRetFloatActInteger_var: Float=1000000.0;
//.
var resParamFloatActLong_val: Float=1.0E9;
var resParamFloatActLong_var: Float=1.0E9;
//.
var resRetFloatActLong_val: Float=1.0E9;
var resRetFloatActLong_var: Float=1.0E9;
//.
var resParamFloatActFloat_val: Float=10.5;
var resParamFloatActFloat_var: Float=10.5;
//.
var resRetFloatActFloat_val: Float=10.5;
var resRetFloatActFloat_var: Float=10.5;
//.
var resParamFloatActDouble_val: Float=12500.0;
var resParamFloatActDouble_var: Float=12500.0;
//.
var resRetFloatActDouble_val: Float=12500.0;
var resRetFloatActDouble_var: Float=12500.0;
//.
var resParamFloatActNumber_val: Float=100.0;
var resParamFloatActNumber_var: Float=100.0;
//.
var resRetFloatActNumber_val: Float=100.0;
var resRetFloatActNumber_var: Float=100.0;
//.
var resParamDoubleActByte_val: Double=120.0;
var resParamDoubleActByte_var: Double=120.0;
//.
var resRetDoubleActByte_val: Double=120.0;
var resRetDoubleActByte_var: Double=120.0;
//.
var resParamDoubleActShort_val: Double=30000.0;
var resParamDoubleActShort_var: Double=30000.0;
//.
var resRetDoubleActShort_val: Double=30000.0;
var resRetDoubleActShort_var: Double=30000.0;
//.
var resParamDoubleActCharacter_val: Double=64.0;
var resParamDoubleActCharacter_var: Double=64.0;
//.
var resRetDoubleActCharacter_val: Double=64.0;
var resRetDoubleActCharacter_var: Double=64.0;
//.
var resParamDoubleActInteger_val: Double=1000000.0;
var resParamDoubleActInteger_var: Double=1000000.0;
//.
var resRetDoubleActInteger_val: Double=1000000.0;
var resRetDoubleActInteger_var: Double=1000000.0;
//.
var resParamDoubleActLong_val: Double=1.0E9;
var resParamDoubleActLong_var: Double=1.0E9;
//.
var resRetDoubleActLong_val: Double=1.0E9;
var resRetDoubleActLong_var: Double=1.0E9;
//.
var resParamDoubleActFloat_val: Double=10.5;
var resParamDoubleActFloat_var: Double=10.5;
//.
var resRetDoubleActFloat_val: Double=10.5;
var resRetDoubleActFloat_var: Double=10.5;
//.
var resParamDoubleActDouble_val: Double=12500.0;
var resParamDoubleActDouble_var: Double=12500.0;
//.
var resRetDoubleActDouble_val: Double=12500.0;
var resRetDoubleActDouble_var: Double=12500.0;
//.
var resParamDoubleActNumber_val: Double=100.0;
var resParamDoubleActNumber_var: Double=100.0;
//.
var resRetDoubleActNumber_val: Double=100.0;
var resRetDoubleActNumber_var: Double=100.0;
//.
var resParamNumberActByte_val: Number=120.0;
var resParamNumberActByte_var: Number=120.0;
//.
var resRetNumberActByte_val: Number=120.0;
var resRetNumberActByte_var: Number=120.0;
//.
var resParamNumberActShort_val: Number=30000.0;
var resParamNumberActShort_var: Number=30000.0;
//.
var resRetNumberActShort_val: Number=30000.0;
var resRetNumberActShort_var: Number=30000.0;
//.
var resParamNumberActCharacter_val: Number=64.0;
var resParamNumberActCharacter_var: Number=64.0;
//.
var resRetNumberActCharacter_val: Number=64.0;
var resRetNumberActCharacter_var: Number=64.0;
//.
var resParamNumberActInteger_val: Number=1000000.0;
var resParamNumberActInteger_var: Number=1000000.0;
//.
var resRetNumberActInteger_val: Number=1000000.0;
var resRetNumberActInteger_var: Number=1000000.0;
//.
var resParamNumberActLong_val: Number=1.0E9;
var resParamNumberActLong_var: Number=1.0E9;
//.
var resRetNumberActLong_val: Number=1.0E9;
var resRetNumberActLong_var: Number=1.0E9;
//.
var resParamNumberActFloat_val: Number=10.5;
var resParamNumberActFloat_var: Number=10.5;
//.
var resRetNumberActFloat_val: Number=10.5;
var resRetNumberActFloat_var: Number=10.5;
//.
var resParamNumberActDouble_val: Number=12500.0;
var resParamNumberActDouble_var: Number=12500.0;
//.
var resRetNumberActDouble_val: Number=12500.0;
var resRetNumberActDouble_var: Number=12500.0;
//.
var resParamNumberActNumber_val: Number=100.0;
var resParamNumberActNumber_var: Number=100.0;
//.
var resRetNumberActNumber_val: Number=100.0;
var resRetNumberActNumber_var: Number=100.0;
//

}
