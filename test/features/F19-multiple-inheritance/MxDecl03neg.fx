/*
 * Negative test for mixin declarations.
 * A mixin contains the abstract function declaration. 
 * The extending mixee does not override the function.
 * 
 * @test/fail
 */

/* Uncomment this when JFXC-3094 is resolved */
//mixin class AbstractMixin {
abstract class AbstractMixin {
    abstract function foo(s : String) : Integer;
}

class Mixee extends AbstractMixin {
    function foo(s : Integer) : Integer { 7 }
}

