/**
 * JFXC-4125 : override var bound to a forward-referenced bound var (x) does not update flags or value on initialization of x
 *
 * On-replace on A
 *
 * @test
 * @run
 */

class A {
   var a : Integer on replace { println("A {a}") };
}

class B extends A {
   override var a = bind x;
   var x = bind 300;
}

println(B{}.a);
