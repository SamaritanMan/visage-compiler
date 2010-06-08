/*
 * Regression: JFXC-3329 - cannot find symbol.
 *
 * @test
 *
 */

class A {
    var c:String;
    var f: function(Object);
}

class B {
    var b : String
}

var bSeq:B[];

var aSeq:Object[] = bind for (b in bSeq) A {
                            override var f = function(e) {
                                b.b = null
                            }
                        };
