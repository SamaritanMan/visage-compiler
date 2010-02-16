/**
 * Regression test JFXC-3269 : Crash caused by parameter order in calls within a mixin class
 *
 * @test
 */

mixin class test {

  public function failsToCompile(foo:test):Void {
     failsToCompile(0, foo); // uncomment this line to crash the compiler
  }

  public function failsToCompile(bar:Integer, foo:test):Void {}

}
