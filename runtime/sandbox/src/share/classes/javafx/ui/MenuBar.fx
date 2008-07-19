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
import javafx.ui.Menu;

public class MenuBar extends Widget {

    override attribute opaque = true;
    override attribute focusable = false;

    // TODO MARK AS FINAL
    attribute jmenubar:javax.swing.JMenuBar= javax.swing.JMenuBar{};
    public attribute menus: Menu[] on replace oldValue[lo..hi]=newVals {
        for(k in [lo..hi]) { 
            jmenubar.remove(lo);
        }
        var ndx = lo;
        for(menu in newVals) {
            jmenubar.add(menu.getComponent(), ndx);
            ndx++
        }
        jmenubar.revalidate();
        jmenubar.repaint();        
    };
    public function createComponent():javax.swing.JComponent {
        jmenubar.setOpaque(true);
        for (m in menus) {
            jmenubar.add(m.getComponent());
        }
        return jmenubar;
    }
}

