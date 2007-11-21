/* 
 * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved. 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. 
 * 
 * This code is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License version 2 only, as 
 * published by the Free Software Foundation.  Sun designates this 
 * particular file as subject to the "Classpath" exception as provided 
 * by Sun in the LICENSE file that accompanied this code. 
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


public class CardPanel extends Widget {
    protected attribute selectionGeneration: Number;
    private attribute jpanel:javax.swing.JPanel;
    private attribute layout:java.awt.CardLayout;
    public attribute selection: Number = -1 on replace {
        if (selection >= 0) {
            selectionGeneration = selectionGeneration + 1;
            //TODO JXFC-187
            //var gen = selectionGeneration;
            //var sel = selection;
            javax.swing.SwingUtilities.invokeLater(java.lang.Runnable {
                 public function run():Void {
                    //TODO JXFC-187
                    //if (gen == selectionGeneration) {
                        //var w = cards[sel];
                        var w = cards[selection.intValue()];
                        if (sizeof w > 0) {
                            var comp = w.getComponent();
                            var id = "{java.lang.System.identityHashCode(comp)}";

                            if (comp.getParent() <> jpanel) {
                                jpanel.add(comp, id);
                                jpanel.validate();
                            }
                            layout.show(jpanel, id);
                        }
                    //}
                }
            });
         }
    };
    public attribute cards: Widget[]
       on insert [indx] (newValue) {
            if (component <> null and indx == selection) {
                var comp = newValue.getComponent();
                var id = "{java.lang.System.identityHashCode(comp)}";
                jpanel.add(comp, id);
                layout.show(jpanel, id);
                jpanel.validate();
                jpanel.repaint();
            }
        }
        on delete [indx] (oldValue) {
            if (oldValue.component <> null) {
                jpanel.remove(oldValue.component);
            }
        };

    public function createComponent():javax.swing.JComponent {
        jpanel = UIElement.context.createPanel();
        layout = java.awt.CardLayout{};
        jpanel.setLayout(layout);
        jpanel.setOpaque(false);
        if (selection <> -1) {
            var w = cards[selection.intValue()];
            if (w <> null) {
                var comp = w.getComponent();
                var id = "{java.lang.System.identityHashCode(comp)}";
                jpanel.add(comp, id);
                layout.show(jpanel, id);
            }
        }
        return jpanel;
    }

    init {
        focusable = false;
    }
}


