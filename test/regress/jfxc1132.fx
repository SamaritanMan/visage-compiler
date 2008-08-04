/**
 * Regression test for JFXC-1132 : 'incompatible types' compiler error when using bind to create sequences of subclasses
 *
 * @test
 */

class Foo {}
class Foob extends Foo {}

class Bar {
  var fs : Foo[];
}

Bar { fs: bind for (i in [1..5]) [Foob{}] }
