/*
 * Regression test
 * JFXC-3994 : Missing error message when variable override non-existent symbol whose clashes with an outer var
 *
 * @compilefirst jfxc3994sub.fx
 * @test/compile-error
 */

class jfxc3994b extends jfxc3994sub {
    override var x = "";
}

class Inner {
    override var x = "";
}