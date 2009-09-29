/*
 * @test
 * @run
 */
import java.lang.System;
import com.sun.javafx.runtime.*;
class Foo {
   var alpha : Integer;
   var _alpha = {var bp = bind Pointer.make(this, "alpha"); bp.unwrap(); };
 };
var foo = Foo { alpha: 100 };
var alphap : Pointer = foo._alpha;
System.out.println("alpha: {foo.alpha} _alpha: {alphap.get()}");
foo.alpha = 150;
System.out.println("alpha: {foo.alpha} _alpha: {alphap.get()}");
