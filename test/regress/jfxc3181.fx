/**
 * JFXC-3181 : Cannot find constructor error compiling MediaPlayer.fx
 *
 * @test
 * @run
 */

class jfxc3181 {
  var pop = "pop";
  var boom = Nested{};
}

class Nested {
  function blip() {
    pop
  }
}

def it = jfxc3181{}
println(it.boom.blip());

