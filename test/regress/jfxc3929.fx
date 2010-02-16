/**
 * JFXC-3929 : compiler generating wrong checkcast.
 *
 * @compilefirst jfxc3929/SGFont.java
 * @test
 * @run
 */

class Font {
   public function impl_getFont() : SGFont { null }
}

class jfxc3929 {
    public var ascent = bind font.impl_getFont().getAscent();
    public var font : Font = Font {};
}

var obj = jfxc3929 {};
println(obj.ascent);
invalidate obj.font;
// The following line used to result in ClassCastException being thrown
println(obj.ascent);
