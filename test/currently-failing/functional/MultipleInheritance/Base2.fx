/*
 * @subtest
 * @compilefirst ./Base1.fx
 * @compilefirst ./TestBase.fx
 */
import Base1.*;

public mixin class bc2 extends bc1,TestBase {
  public var bc2_ival  : Integer = 10;
  public var bc2_nval  : Number  = 10.0;
  public var bc2_name : String  = "a baseclass";
  public var bc2_isPublic : Boolean = true;
  public var bc2_iseq : Integer[] = [1,2,3];
  public var bc2_nseq : Number[] = [0.1, 2.3, 4.5];
  public var bc2_sseq : String[] = [ "public", "class"];
  public var bc2_bseq : Boolean[] = [ true,false,true ];
  public function bc2_m1():Integer {100}
  public function bc2_m2( s1:String, s2:String ):String[] { var seq = [ s1,s2]; return seq; }
  public var bc2_fpm1 = bc2_m1;
  public var bc2_fpm2 = bc2_m2;

  public function bc2Test() {
   TestAssert( check(bc1_ival,bc2_ival),"" );
   TestAssert( check(bc1_nval,bc2_nval),""  );
   TestAssert( check(bc1_name,bc2_name),""  );
   TestAssert( check(bc1_isPublic,bc2_isPublic),""  );
   TestAssert( checkis(bc1_iseq,bc2_iseq),""  );
   TestAssert( checkns(bc1_nseq,bc2_nseq),""  );
   TestAssert( checkss(bc1_sseq,bc2_sseq),""  );
   TestAssert( checkbs(bc1_bseq,bc2_bseq),""  );
   TestAssert( check( bc1_m1() , bc2_m1()),""  );
   TestAssert( checkss( bc1_m2("Hello","World"), bc2_m2("Hello","World")),"" );
   TestAssert( check( bc1_fpm1() , bc2_fpm1() ),""  );
   TestAssert( checkss(bc1_fpm2("Hello","World"),bc2_fpm2("Hello","World")),"Test inherited function pointers returning String[]" );
  }
}

function run() {
 var b2 = new bc2();
 b2.bc2Test();
}
