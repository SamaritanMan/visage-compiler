/**
 * JFXC-3952 : Samples: Mosaic fails with an NPE in initVars$
 *
 * Original, self-referential version
 *
 * @compilefirst jfxc3952mix.fx
 * @compilefirst jfxc3952sub.fx
 * @test
 * @run
 */

var sb : jfxc3952sub = jfxc3952sub {
  thing: bind (sb.furb)
}

println(sb.thing)