/*
 * Testing the 'super' keyword in mixin function bodies.
 *
 * @compile MxSuperKw01p.fx
 * @test
 * @run
 */


/* Mixin extends mixin */
mixin class Mixin2 extends MxSuperKw01p {
    override public function foo() : String {
        /* Uncomment this when JFXC-2660 is fixed */
//        super.foo();
        "MxSuperKw01p.foo"
    }
    public function bar() : String {
        /* Uncomment this when JFXC-2660 is fixed */
//        super.foo();
        "MxSuperKw01p.foo"
    }
}

/* Non-mixin extends mixin */
class Mixee1 extends MxSuperKw01p {
    override public function foo() : String {
        /* Uncomment this when JFXC-2660 is fixed */
//        super.foo();
        "MxSuperKw01p.foo"
    }
    public function bar() : String {
        /* Uncomment this when JFXC-2660 is fixed */
//        super.foo();
        "MxSuperKw01p.foo"
    }
}

class Mixee2 extends Mixin2 {}


/*
 * Use 'super' keyword to access the parent implementation 
 * of the function that is non-abstract in a mixin.
 */
public function run() {
    var m1 = Mixee1 {};
    var m2 = new Mixee2();
    println(m1.foo());
    println(m1.bar());
    println(m2.foo());
    println(m2.bar());
}

