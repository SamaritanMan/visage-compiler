/*
 * JFXC-3720 : Compiled bind: cannot find symbol constructor Label$anon217(<anonymous com.sun.javafx.functions.Function0<java.lang.Void>>,boolean)
 *
 * @test
 *
 */

class Label {var textFill = ""}
class jfxc3720 {
   var selectedTextFill:String;
}

class TreeCell {
   var onUpdate = function() {
      Label { textFill: bind selectedTextFill }
   }
}
