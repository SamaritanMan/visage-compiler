/**
 * JFXC-3181 : Cannot find constructor error compiling MediaPlayer.fx
 *
 * @test
 */

class jfxc3181orig {
  var onEndOfMedia;
  var media on replace {
    var msl = _MediaStateListener{};
  }
}

class _MediaStateListener {
  function endOfMediaReached():Void {
            if (onEndOfMedia != null) {
            }
  }
}