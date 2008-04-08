/**
 * Regression test JFXC-1014 : Non-int,double,boolean Java primitive Locations should be represented as ObjectLocation<Wrapper>
 *
 * @test
 * @run
 */

import java.lang.System;
import java.lang.Float;

class Foo {
  attribute ob = new Float(1);
  attribute ff = ob.floatValue();
  attribute bb = ob.byteValue();
  attribute ss = ob.shortValue();
}

var ooh = Foo{}
System.out.println(ooh.ff);
System.out.println(ooh.bb);
System.out.println(ooh.ss);
