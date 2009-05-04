/**
 * JFXC-3180 : incorrect inInstanceContext for function value in on-replace block
 *
 * core issue: simplest test case
 *
 * @test
 */

class jfxc3180simp {
  function updateTime() {}

  var media on replace {
    function() {
      updateTime()
    }
  }
}
