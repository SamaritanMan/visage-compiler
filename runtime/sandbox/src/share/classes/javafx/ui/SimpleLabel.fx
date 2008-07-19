/* 
 * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved. 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. 
 * 
 * This code is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License version 2 only, as 
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * version 2 for more details (a copy is included in the LICENSE file that 
 * accompanied this code). 
 * 
 * You should have received a copy of the GNU General Public License version 
 * 2 along with this work; if not, write to the Free Software Foundation, 
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA. 
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara, 
 * CA 95054 USA or visit www.sun.com if you need additional information or 
 * have any questions. 
 */  
 
package javafx.ui; 

import javafx.ui.Widget;
import javafx.ui.HorizontalAlignment;
import javafx.ui.KeyStroke;

public class SimpleLabel extends Widget {

    override attribute focusable = false;

    // TODO MARK AS FINAL
    private attribute jlabel:javax.swing.JLabel;
    
    public attribute text:String on replace {
        if(jlabel != null) {
            jlabel.setText(text);
        }
    };
    public attribute icon:Icon on replace {
        if(jlabel != null) {
            jlabel.setIcon(icon.getIcon());
        }
    };
    public attribute horizontalAlignment:HorizontalAlignment on replace {
        if(jlabel != null) {
           jlabel.setHorizontalAlignment(horizontalAlignment.id.intValue());
        }
    };
    public attribute mnemonic: KeyStroke on replace {
        if(jlabel != null) {
            jlabel.setDisplayedMnemonic(mnemonic.id);
        }
    };
    public attribute labelFor: Widget on replace {
        if(jlabel != null) {
            jlabel.setLabelFor(labelFor.getComponent());
        }
    };
    
    public function createComponent():javax.swing.JComponent{
        if (horizontalAlignment == null) {
            horizontalAlignment = HorizontalAlignment.LEADING;
        } 
        jlabel = UIElement.context.createSimpleLabel();
        jlabel.setOpaque(false);
        jlabel.setText(text);
        jlabel.setIcon(icon.getIcon());
        jlabel.setHorizontalAlignment(horizontalAlignment.id.intValue());
        if (labelFor != null) {
            jlabel.setLabelFor(labelFor.getComponent());
        }
        if (mnemonic != null) {
            jlabel.setDisplayedMnemonic(mnemonic.id.intValue());
        }
        return jlabel;
    }
}

