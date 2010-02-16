/*
 * JFXC-3728 : Compiled bind: bind-with-inverse of sequence
 *
 * Direct bind of object.  No on-replace.
 *
 * @test
 * @run
 * 
 */

class B {
  var bse : Object[];
  var bsr = bind bse with inverse;
  
  function show() {
    print("bse: ");
    println(bse);
    print("bsr: ");
    println(bsr);
  }
  
  function testa() {
    show();
    insert 45 into bse;
    show();
    insert "gulp" into bse;
    show();
    insert [1..5] into bse;
    show();
    bse[5] = "Leah";
    show();
    delete bse[4];
    show();
    delete bse;
    show();
  }

  function testb() {
    show();
    insert 45 into bsr;
    show();
    insert "gulp" into bsr;
    show();
    insert [1..5] into bsr;
    show();
    bsr[5] = "Leah";
    show();
    delete bsr[4];
    show();
    delete bsr;
    show();
  }
}
B{}.testa();
B{}.testb()
