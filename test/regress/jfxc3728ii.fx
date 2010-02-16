/*
 * JFXC-3728 : Compiled bind: bind-with-inverse of sequence
 *
 * Direct bind of Integer.
 *
 * @test
 * @run
 * 
 */

class B {
  var bse : Integer[];
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
    insert 1000 into bse;
    show();
    insert [1..5] into bse;
    show();
    bse[5] = 99;
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
    insert 1000 into bsr;
    show();
    insert [1..5] into bsr;
    show();
    bsr[5] = 99;
    show();
    delete bsr[4];
    show();
    delete bsr;
    show();
  }
}
B{}.testa();
B{}.testb()
