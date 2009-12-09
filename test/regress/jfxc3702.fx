/**
 * JFXC-3702 : assert: Cannot find owner, in bind, for, object lit, sequence, indexed
 *
 * @test
 */

class Rectangle {
   var x: Integer;
}

class Group {
  var content: Rectangle[];
}

var ticks = [5..7];
var verticalRowFill:Group = Group {
        content: bind for (ii in [0..2]) {
            Rectangle {
                x: bind ticks[ii]
            }
}}
