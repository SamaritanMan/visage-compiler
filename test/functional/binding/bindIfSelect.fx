/*
 * @test/fxunit
 * @run
 */

import javafx.fxunit.*;

public class bindIfSelect extends FXTestCase {
    function testBoundSelect() {
        var f = Foo {
            vb : 1, vs : 2, vi : 3, vl : 4, vf : 5.0, vd : 6.0, vc : 7, vz : false, vt : "", vo : null, vq : [ ]
        }
        //var bfb = bind f.vb;
        //var bfs = bind f.vs;
        var bfi = bind f.vi;
        //var bfl = bind f.vl;
        var bff = bind f.vf;
        var bfd = bind f.vd;
        //var bfc = bind f.vc;
        var bfz = bind f.vz;
        var bft = bind f.vt;
        var bfo = bind f.vo;
        var bfq = bind f.vq;
//        assertEquals(1, f.vb as Object);
//        assertEquals(1, bfb as Object);
//        assertEquals(2, f.vs as Object);
//        assertEquals(2, bfs as Object);
        assertEquals(3, f.vi as Object);
        assertEquals(3, bfi as Object);
//        assertEquals(4, f.vl as Object);
//        assertEquals(4, bfl as Object);
        assertEquals(5.0, f.vf as Object);
        assertEquals(5.0, bff as Object);
        assertEquals(6.0 as Double, f.vd as Object);
        assertEquals(6.0 as Double, bfd as Object);
//        assertEquals(7, f.vc as Object);
//        assertEquals(7, bfc as Object);
        assertEquals(false, f.vz as Object);
        assertEquals(false, bfz as Object);
        assertEquals(null, f.vo as Object);
        assertEquals(null, bfo as Object);
        assertEquals("", f.vt as Object);
        assertEquals("", bft as Object);
        assertEquals([], f.vq as Object);
        assertEquals([], bfq as Object);

        f.vb = 10;
        f.vs = 20;
        f.vi = 30;
        f.vl = 40;
        f.vf = 50.0;
        f.vd = 60.0;
        f.vc = 70;
        f.vz = true;
        f.vt = "80";
        f.vo = f;
        f.vq = [ 90 ];

//        assertEquals(10, f.vb as Object);
//        assertEquals(10, bfb as Object);
//        assertEquals(20, f.vs as Object);
//        assertEquals(20, bfs as Object);
        assertEquals(30, f.vi as Object);
        assertEquals(30, bfi as Object);
//        assertEquals(40, f.vl as Object);
//        assertEquals(40, bfl as Object);
        assertEquals(50.0, f.vf as Object);
        assertEquals(50.0, bff as Object);
        assertEquals(60.0 as Double, f.vd as Object);
        assertEquals(60.0 as Double, bfd as Object);
//        assertEquals(70, f.vc as Object);
//        assertEquals(70, bfc as Object);
        assertEquals(true, f.vz as Object);
        assertEquals(true, bfz as Object);
        assertEquals(f, f.vo as Object);
        assertEquals(f, bfo as Object);
        assertEquals("80", f.vt as Object);
        assertEquals("80", bft as Object);
        assertEquals([ 90 ], f.vq as Object);
        assertEquals([ 90 ], bfq as Object);
    }

    function testBoundSelectInverse() {
        var f = Foo {
            vb : 1, vs : 2, vi : 3, vl : 4, vf : 5.0, vd : 6.0, vc : 7, vz : false, vt : "", vo : null, vq : [ ]
        }
        //var bfb = bind f.vb with inverse;
        //var bfs = bind f.vs with inverse;
        var bfi = bind f.vi with inverse;
        //var bfl = bind f.vl with inverse;
        var bff = bind f.vf with inverse;
        var bfd = bind f.vd with inverse;
        //var bfc = bind f.vc with inverse;
        var bfz = bind f.vz with inverse;
        var bft = bind f.vt with inverse;
        var bfo = bind f.vo with inverse;
        var bfq = bind f.vq with inverse;
//        assertEquals(1, f.vb as Object);
//        assertEquals(1, bfb as Object);
//        assertEquals(2, f.vs as Object);
//        assertEquals(2, bfs as Object);
        assertEquals(3, f.vi as Object);
        assertEquals(3, bfi as Object);
//        assertEquals(4, f.vl as Object);
//        assertEquals(4, bfl as Object);
        assertEquals(5.0, f.vf as Object);
        assertEquals(5.0, bff as Object);
        assertEquals(6.0 as Double, f.vd as Object);
        assertEquals(6.0 as Double, bfd as Object);
//        assertEquals(7, f.vc as Object);
//        assertEquals(7, bfc as Object);
        assertEquals(false, f.vz as Object);
        assertEquals(false, bfz as Object);
        assertEquals(null, f.vo as Object);
        assertEquals(null, bfo as Object);
        assertEquals("", f.vt as Object);
        assertEquals("", bft as Object);
        assertEquals([], f.vq as Object);
        assertEquals([], bfq as Object);

        f.vb = 10;
        f.vs = 20;
        f.vi = 30;
        f.vl = 40;
        f.vf = 50.0;
        f.vd = 60.0;
        f.vc = 70;
        f.vz = true;
        f.vt = "80";
        f.vo = f;
        f.vq = [ 90 ];

//        assertEquals(10, f.vb as Object);
//        assertEquals(10, bfb as Object);
//        assertEquals(20, f.vs as Object);
//        assertEquals(20, bfs as Object);
        assertEquals(30, f.vi as Object);
        assertEquals(30, bfi as Object);
//        assertEquals(40, f.vl as Object);
//        assertEquals(40, bfl as Object);
        assertEquals(50.0, f.vf as Object);
        assertEquals(50.0, bff as Object);
        assertEquals(60.0 as Double, f.vd as Object);
        assertEquals(60.0 as Double, bfd as Object);
//        assertEquals(70, f.vc as Object);
//        assertEquals(70, bfc as Object);
        assertEquals(true, f.vz as Object);
        assertEquals(true, bfz as Object);
        assertEquals(f, f.vo as Object);
        assertEquals(f, bfo as Object);
        assertEquals("80", f.vt as Object);
        assertEquals("80", bft as Object);
        assertEquals([ 90 ], f.vq as Object);
        assertEquals([ 90 ], bfq as Object);

//        bfb = 100;
//        bfs = 200;
        bfi = 300;
//        bfl = 400;
        bff = 500.0;
        bfd = 600.0;
//        bfc = 700;
        bfz = false;
        bft = "800";
        bfo = null;
        bfq = [ 900 ];

//        assertEquals(100, f.vb as Object);
//        assertEquals(100, bfb as Object);
//        assertEquals(200, f.vs as Object);
//        assertEquals(200, bfs as Object);
        assertEquals(300, f.vi as Object);
        assertEquals(300, bfi as Object);
//        assertEquals(400, f.vl as Object);
//        assertEquals(400, bfl as Object);
        assertEquals(500.0, f.vf as Object);
        assertEquals(500.0, bff as Object);
        assertEquals(600.0 as Double, f.vd as Object);
        assertEquals(600.0 as Double, bfd as Object);
//        assertEquals(700, f.vc as Object);
//        assertEquals(700, bfc as Object);
        assertEquals(false, f.vz as Object);
        assertEquals(false, bfz as Object);
        assertEquals(null, f.vo as Object);
        assertEquals(null, bfo as Object);
        assertEquals("800", f.vt as Object);
        assertEquals("800", bft as Object);
        assertEquals([ 900 ], f.vq as Object);
        assertEquals([ 900 ], bfq as Object);
    }

    function testBoundIf() {
        var o1 : Object = new Object();
        var o2 : Object = new Object();
        var z : Boolean = true;
//        var bb : Byte = bind if (z) then 1 as Byte else 2 as Byte ;
//        var bs : Short = bind if (z) then 1 as Short else 2 as Short;
        var bi : Integer = bind if (z) then 1 else 2;
//        var bl : Long = bind if (z) then 1 as Long else 2 as Long ;
        var bf : Float = bind if (z) then 1 as Float else 2 as Float;
        var bd : Double = bind if (z) then 1 as Double else 2 as Double ;
        var bz : Boolean = bind if (z) then true else false;
//        var bc : Character = bind if (z) then 1 as Character else 2 as Character;
        var bt : String = bind if (z) then "true" else "false";
        var bo : Object = bind if (z) then o1 else o2;
        var bq : Integer[] = bind if (z) then [ 1 ] else [ 2 ];

//        assertEquals(1, bb as Object);
//        assertEquals(1, bs as Object);
        assertEquals(1, bi as Object);
//        assertEquals(1, bl as Object);
        assertEquals(1.0, bf as Object);
        assertEquals(1.0 as Double, bd as Object);
        assertEquals(true, bz as Object);
//        assertEquals(1, bc as Object);
        assertEquals(o1, bo as Object);
        assertEquals("true", bt as Object);
        assertEquals([ 1 ], bq);

        z = false;
//        assertEquals(2, bb as Object);
//        assertEquals(2, bs as Object);
        assertEquals(2, bi as Object);
//        assertEquals(2, bl as Object);
        assertEquals(2.0, bf as Object);
        assertEquals(2.0 as Double, bd as Object);
        assertEquals(false, bz as Object);
//        assertEquals(2, bc as Object);
        assertEquals(o2, bo as Object);
        assertEquals("false", bt as Object);
        assertEquals([ 2 ], bq);

    }
}

class Foo {
    var vb : Byte;
    var vs : Short;
    var vi : Integer;
    var vl : Long;
    var vf : Float;
    var vd : Double;
    var vc : Character;
    var vz : Boolean;
    var vo : Object;
    var vt : String;
    var vq : Integer[];
}