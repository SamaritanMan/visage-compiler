/**
 * regression test: JFXC-1443 : Remove concept of 'statement' and 'block' -- breaks parsing
 *
 * @test
 * @run
 */

import java.lang.System;

var seq = [1, 2, 3];
for (z in [4, 5])
  insert z into seq;
for (v in [100..110])
  if (v mod 2 == 0) 
     insert v into seq
  else
     insert -v into seq;
System.out.println(seq);

/****
class It {
  var title : String;
  var a : It;
  var b : It;

  function show() : Void {
    System.out.println(title);
    if (a!=null) a.show();
    if (b!=null) b.show();
  }
}

def well = It {
  title: 'Out'
  a: def nest = It {title: 'In'}
  b: nest
};

well.show();
*****/
