/**
 * Should-fail regression test: JFXC-1912 : Check that 'abstract' goes with empty function body
 *
 * @test/compile-error
 */

abstract class Foo {
   abstract function bar() : String { "Here I am" }
}
