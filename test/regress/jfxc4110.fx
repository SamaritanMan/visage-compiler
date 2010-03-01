/*
 * @test
 * @run
 */
import javafx.reflect.*;
public class A1 extends B1 {
}
public mixin class B1 {
    public var flda:String;
    public-init var fldb:String;
    public-read var fldc:String;
    protected var fldd:String;
}

function testfld(cls: FXClassType, name:String): Void {
  def v = cls.getVariable(name);
  print(name);
  print(" is");
  if (v.isPublic())  print(" public");
  if (v.isPublicInit())  print(" public-init");
  if (v.isPublicRead())  print(" public-read");
  if (v.isProtected())  print(" protected");
  println(".");
}

function run() {
  def a1 = A1{flda:"TestA"};

  def obj = FXLocal.getContext().mirrorOf(a1);
  def cls = obj.getClassType();
  testfld(cls, "flda");
  testfld(cls, "fldb");
  testfld(cls, "fldc");
  testfld(cls, "fldd");
}

