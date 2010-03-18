/**
 * JFXC-4215 : An erroneous IllegalArgumentException "duplicate children detected" occurs when running Indaba with b27 respin Soma
 *
 * Emulation 
 *
 * @compilefirst XNode.fx
 * @compilefirst XParent.fx
 * @compilefirst XRectangle.fx
 * @compilefirst XGroup.fx
 * @test
 * @run
 */

class XImeline extends XNode {
   var playhead = XRectangle{x:7.0};
   var rseq : XRectangle[]
}

var xim = XImeline{};

public class jfxc4215 extends XNode {
    
    var z = 10.0;
    var bgView = XRectangle {x: 2.25};
    var box = bind xim.playhead;
    var brseq = bind xim.rseq;

    var grp =
        XGroup {
            content: bind [
                XRectangle {x: z},
                xim,
                brseq,
                bgView,
                xim.playhead,
                xim.rseq,
                box
            ]
            childrenUpdateNotification: cun
        }
}

var mirror : XNode[];

function cun(a : Integer, b : Integer, kidNodes : XNode[], newNodes : XNode[], oldNodes : XNode[]) : Void {
    mirror[a..b] = newNodes;
    if (mirror != kidNodes) {
        println("ERROR: mirror doesn't match children: {mirror.toString()} VS  {mirror.toString()}");
    }
}

function run() {
    var t = jfxc4215{};
    xim.rseq = [XRectangle{x:4.0}, XRectangle{x:1.0}];
    t.bgView = null;
    xim.playhead = null;
    insert XRectangle{x:45} into xim.rseq;
    t.z = 33.3;
    t.bgView = XRectangle{x:99};
    xim.playhead = XRectangle{x:777.0};
}
