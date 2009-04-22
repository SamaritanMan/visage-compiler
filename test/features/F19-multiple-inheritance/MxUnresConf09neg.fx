/*
 * Test for unresolvable conflicts.
 *
 * Mixin declares a private function foo.
 * Super declares a non-private override-incompatible function foo
 *
 * @compilefirst MxUnresConf07lib.fx
 * @test/fail
 */

import MxUnresConf07lib.*;

class Mixee extends Super03, Mixin03 {}
 