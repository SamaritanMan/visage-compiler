/*
 * Test for unresolvable conflicts.
 *
 * Mixin declares a non-private function foo.
 * Super declares a private override-incompatible function foo
 *
 * @compilefirst MxUnresConf07lib.fx
 * @test/fail
 */

import MxUnresConf07lib.*;

class Mixee extends Super04, Mixin04 {}
 