/**
 * Should-fail test JFXC-3654 : Compiled bind: disallow assign in bind
 *
 * @test/compile-error
 */

import java.lang.System;

class Outer {
  var content : Inner[]
}

class Inner {
  var name : String;
  var thing : Inner
}

var it = bind
  Outer {
    var firstInner : Inner;
    content:
      [firstInner = 
        Inner {name: "yo"},
       Inner {name: "two" thing: firstInner}
      ]
  };
System.out.println(it.content[1].name);
System.out.println(it.content[1].thing.name);