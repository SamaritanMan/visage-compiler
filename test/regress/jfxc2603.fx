/**
 * JFXC-2603 : javadump generated for binding XX[] to XX
 *
 * @test
 * @run
 */

function run() {
  var str = "Uday Rocks!";
  var i = 5;
  def bstr : String[] = bind str;
  def bi : Integer[] = bind i;
  def bobj : Object[] = bind str;
  def biobj : Object[] = bind i;
  def bn : Number[] = bind i;

  println(bstr);
  println(bi);
  println(bobj);
  println(biobj);
  println(bn);
}