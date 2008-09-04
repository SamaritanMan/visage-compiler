/**
 * Regression test JFXC-1932 : Pre-user-default value of String and Duration instance is null (which is invalid)
 *
 * @test
 * @run
 */

import javafx.lang.Duration;

class Foo {
  var string1 : String on replace old { println("string1 old='{old}', new='{string1}'") }
  var duration1 : Duration on replace old { println("duration1 old={old}, new={duration1}") }
  var string2 = "Yo" on replace old { println("string2 old='{old}', new='{string2}'") }
  var duration2 = 12ms on replace old { println("duration2 old={old}, new={duration2}") }
}

Foo{}
