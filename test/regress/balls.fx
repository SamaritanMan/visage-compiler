/*
 * Test generation of outer$ and getOuter$() in case of JFXInstanciate AST.
 * @test
 * @run
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.Timer;
class balls {
    var _is_running : Boolean;
    var timer = new Timer(5, ActionListener { 
        public function actionPerformed(evt : java.awt.event.ActionEvent): Void {
            if (_is_running) {
            }
        }
    });

}
