/**
 * regression test: handling non-JavaFX primitive types
 * @test
 * @run
 */
import javax.swing.JPasswordField; 
import java.lang.System;

var s = "Hello";
// Sorry - we no longer handle implicit 'char' at top-level,
// because these are now fields and they all get "morphed"
// to Location fields.  We plan to fix this later.
var ch : java.lang.Character = s.charAt(0);

var textField = new JPasswordField();    
textField.setEchoChar(ch);

var i : Integer;
i = ch;
System.out.println("{i} -- {ch}");

