/*
 * Test for precedence and member conflict resolution.
 *
 * @compilefirst MxMemConfResol02Java.java
 * @test
 * @run
 */

mixin class Mixin1 {
    public-init var bar : Integer = 140420091;
    public function foo() : String { "Mixin1" }
}

mixin class Mixin2 {
    package var bar : Integer = 140420092;
    public function foo() : String { "Mixin2" }
}

/*
 * Both parent mixins and a java superclass contain
 * function foo() and a variable bar.
 */
/* Uncomment this when JFXC-3072 is fixed */
// class Mixee extends MxMemConfResol02Java, Mixin1, Mixin2 {}
class Mixee extends MxMemConfResol02Java {}

function run() {
    def m = Mixee {}
    println(m.bar);
    println(m.foo());
}
