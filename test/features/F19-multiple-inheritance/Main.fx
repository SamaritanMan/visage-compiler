/*
 * Feature test #19 -- multiple inheritance
 *
 * @ test
 * @ compile Base1.fx
 * @ compile Base2.fx
 * @ compile Subclass.fx
 */

var v = Subclass { a: 1 }
System.out.println("a={v.a}, b={v.b}, c={v.c}, d={v.d}");
System.out.println("foo={v.foo()}, moo={v.moo()}, bark={v.bark()}, wahoo={v.wahoo()}");
