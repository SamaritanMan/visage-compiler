/* JFXC-222:  support for extending a single Java class from a JFX class
 * @test
 */
import javax.swing.JButton; 

public class But extends JButton { 
    public function getText1(): String { 
        getText(); 
    } 
}  
