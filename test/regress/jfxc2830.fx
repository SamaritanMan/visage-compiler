/**
 * regression test:  JFXC-2830 Mixins: Same var name declared in .fx and .java causes problems.
 * @test/fail
 */

import javax.swing.JApplet; 
import java.awt.event.ComponentListener; 
import java.awt.event.ComponentEvent; 

public mixin class StageDelegate { 
     public-read protected var x; 
} 

public class Applet extends JApplet, StageDelegate, ComponentListener { 
  var x on replace { 
        println("Stage.appletDelegate.x"); 
  } 

    override public function componentResized(e: ComponentEvent) {} 
    override public function componentMoved(e: ComponentEvent) {} 
    override public function componentShown(e: ComponentEvent){} 
    override public function componentHidden(e: ComponentEvent){} 

}
