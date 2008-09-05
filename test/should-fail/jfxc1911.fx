/**
 * Regression test JFXC-1911 : Check that there are no access modifiers on local variables
 *
 * @test/compile-error
 */

function foo() {
  public var x = 0;
  package var y = 0;
  protected var z = 0;
  public-init var a = 0;
  public-read var b = 0;
}

function fuu() {
  public def x = 0;
  package def y = 0;
  protected def z = 0;
  public-read def b = 0;
}

  
