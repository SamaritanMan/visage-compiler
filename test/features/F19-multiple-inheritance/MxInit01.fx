/*
 * mixin init block execution
 *
 * @test/fxunit
 * @run
 */
import javafx.fxunit.*;

var res: String = "";

// used in testA01()
mixin class A {
   var v: Integer = 5;
   init { v+=1; res ="{res}A"; }
   function getA() { return v; }
}

class B extends A {
   override var v = 15;
   init { v+=5; res="{res}B";}; 
   function getB() { return v; }
}

// used in testA02()
mixin class E extends A {
   override var v = 35;
   init { v+=7; res="{res}E";}; 
   function getE() { return v; }
}

class C extends A, E {
   override var v = 45;
   init {v+=10; res="{res}C";}; 
   function getC() { return v; }
}

// used in testA03()
class D extends A, C, E {
   override var v = 55;
   init {v+=13; res="{res}D";}; 
   function getD() { return v; }
}

// used in testA04()
class F extends A {
   function foo() { v = 65; }
   init {v+=17; res="{res}F";}; 
   function getF() { return v; }
}

// used in testA05()
class G extends A {
   override var v = 75;
   function foo() { v = 85; }
   function getG() { return v; }
}

// used in testA06()
mixin class I {
   init {res ="{res}I"; }
}

class M {
   init {res ="{res}M"; }
}

class L extends A, M, I {
   //override var v = 105;
   init { v+=19; res ="{res}L"; }
   function getL() { return v; }
}

public class MxInit01 extends FXTestCase {
/*
 *  initializing in mixin and mixee
 */
    function testA01() {
        res = "";
        var x = B{};
        //x.foo();
        assertEquals("AB", res);
        assertEquals(21, x.v);
        var y:A = x;
        assertEquals(21, y.v);
        assertEquals(21, y.getA());
    }
/*
 * initializing in mixee, mixin and super mixin
 */
    function INVALID_testA02() {
        res = "";
        var x = C{};
        x.v = 95;
        assertEquals("AEC", res);
        assertEquals(95, x.v);
        var y:A = x;
        assertEquals(95, y.v);
        assertEquals(95, y.getA());
        var z:E = x;
        assertEquals(95, z.v);
        assertEquals(95, z.getE());
    }

/*
 * initializing in mixee, supeclass and in two mixin
 */
    function INVALID_testA03() {
        res = "";
        var x = D{};
        x.v = 105;
        assertEquals("AECD", res);
        assertEquals(105, x.v);
        var y:A = x;
        assertEquals(105, y.v);
        assertEquals(105, y.getA());
        var z:E = x;
        assertEquals(105, z.v);
        assertEquals(105, z.getE());
        var w:C = x;
        assertEquals(105, w.v);
        assertEquals(105, w.getC());
    }

/*
 *  initializing in mixin and mixee, not overridden in mixee
 */
    function testA04() {
        res="";
        var x = F{};
        //x.foo();
        assertEquals("AF", res);
        assertEquals(23, x.v);
        var y:A = x;
        assertEquals(23, y.v);
        assertEquals(23, y.getA());
    }

/*
 *  initializing in mixin, overridden var in mixee
 */
    function testA05() {
        res="";
        var x = G{};
        assertEquals("A", res);
        assertEquals(76, x.v);
        var y:A = x;
        assertEquals(76, y.v);
        assertEquals(76, y.getA());
    }

/*
 *  initializing in mixee, mixin #1, supeclass, and mixin #2
 */
    function testA06() {
        res="";
        var x = L{};
        assertEquals("MAIL", res);
        assertEquals(25, x.v);
        var y:A = x;
        assertEquals(25, y.v);
        assertEquals(25, y.getA());
    }
}
