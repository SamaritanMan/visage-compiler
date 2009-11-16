/**
 * regression test: JFXC-3645 : compiled-bind : crash #2 Scene.fx - lower inserts unneeded TypeCast not supported in bind-with-inverse
 *
 * @test
 */

class Base {
 var bseq : String[];
}

class Foo extends Base {
 var sseq = ["hi", "lo"];
 override var bseq = bind sseq with inverse;
}
