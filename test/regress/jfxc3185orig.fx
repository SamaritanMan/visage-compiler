/**
 * JFXC-3185 : "Cannot find symbol getRoot$$bound$()" error
 *
 * @test
 */

class Grp {
  var content : String[]
}

class jfxc3185orig {
  var placeholder = "place";
  bound function getRoot() { "zork" }
  function create() {
    Grp { content: bind
      if (getRoot() != null) getRoot() else placeholder;
    }
  }
}
