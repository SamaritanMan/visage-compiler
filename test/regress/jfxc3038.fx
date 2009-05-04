/**
 * JFXC-3038 -  Fatal compiler error in sequence selection.
 *
 * @test
 * @run
 */

class Node {
    var translateX : Integer;
    var translateY : Integer;
}

class Rect extends Node {
    var selected = false;
}

class Group extends Node {
    var content : Node[];
}

var g = Group {
    var rects = [
        Rect { translateX: 70 translateY: 70, selected: true },
        Rect { translateX: 40 translateY: 40 },
        Rect { translateX: 100 translateY: 100 }
    ]

    // the bind conditional below used to crash in back-end
    content: bind rects[r | not r.selected]
}

for (i in g.content) {
    println((i as Rect).translateX);
    println((i as Rect).translateY);
}
