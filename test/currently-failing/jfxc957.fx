/*
 * @test/fail
 */

var v : Foo[] = if (true) Foo { a: 1 } else null;
var w = if (true) [ Foo { a: 1 } ] else null;

class Foo {
  attribute a : Integer;
}
