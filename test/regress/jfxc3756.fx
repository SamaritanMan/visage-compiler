/**
 * JFXC-3756 : Compiled bind: bind to func call on a local var fails: AssertionError: Cannot find owner
 *
 * @test
 */

class cls1 {
    var location: String;
}

class cls {
  function func1() {
      var jjstr : String;
      var request = cls1 {
        location: bind jjstr.toString();
      }
  }
}
