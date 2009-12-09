/*
 * test emulating the bound boolean operations in Node.
 * No sequences.
 */
public class Node {

    public-read package var parent: Node = null;

    public-read package var scene: Object = null;

    public var visible:Boolean = true on replace {
        setVisible(visible);
    }

    public-read var disabled:Boolean = bind disable or (parent != null and parent.disabled);

    public var disable:Boolean = false;

    package def treeVisible:Boolean = bind lazy
        visible and
           ( if (parent != null)
                 parent.treeVisible
             else
                 true
           );

    package def canReceiveFocus:Boolean = bind
        scene != null
          and not disabled
          and treeVisible
    on replace oldValue = newValue {
        requestFocus();
    }

    public function requestFocus():Void {
    }

    public function setVisible(vis : Boolean):Void {
    }
}

public class visibleNode extends cbm {

    var top  : Node;
    var curr : Node;

    function addNode() : Void {
        curr = Node {
                 parent: curr
                 scene: "scene"
        }
    }

    function cycleVis() : Void {
        top.visible = false;
        var tv = curr.treeVisible;
        top.visible = true;
    }

    override public function test():Number {
        top = Node{}
        curr = top;

        for (n in [1..300]) {
            addNode();
        }
        for (n in [1..1000]) {
            cycleVis();
        }
        1 // I'm OK
    }
}

public function run(args:String[]) {
    var bs = new visibleNode();
    cbm.runtest(args,bs)
}
