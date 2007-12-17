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

import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ComponentAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.lang.*;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javafx.ui.Keyboard;
import javafx.ui.KeyStroke;
import javafx.ui.Widget;
import javafx.ui.canvas.*;
import com.sun.javafx.api.ui.FXMouseListener;
import com.sun.javafx.api.ui.FXNodeListener;
import com.sun.javafx.api.ui.UIContext;
import com.sun.scenario.scenegraph.JSGPanel;
import com.sun.scenario.scenegraph.SGAlignment;
import com.sun.scenario.scenegraph.SGGroup;
import com.sun.scenario.scenegraph.SGNode;
import com.sun.scenario.scenegraph.SGTransform.Affine;

public class Canvas extends Widget, CanvasElement, Container {
    // private:
    public attribute scaleToFitList: Node[];
    public attribute sizeToFitList: SizeableCanvasElement[];
    public attribute jsgpanel: JSGPanel = new JSGPanel();
    protected attribute root: SGGroup;   
    protected attribute focusRect: Rect;
    protected attribute focusBounds: Rectangle2D  = bind if (focusedNode == null) null else focusedNode.bounds;
    public attribute focusedNode: Node;
    
    protected function getFX(obj:SGNode): Node {
        while (obj <> null) {
           var n = obj.getAttribute("FX") as Node;
            if (n <> null) {
                return n;
            }
            obj = obj.getParent();
        }
        return null;
    };

    // public:
    public attribute content: Node[]
        on insert [indx] (newValue) {
            newValue.parentCanvasElement = this as CanvasElement;

            if (root <> null) {
                root.add(newValue.getNode());
            }
        }
        on delete [indx] (oldValue) { 
            if (root <> null) {
                root.remove(oldValue.getNode());
            }
        }
        on replace [indx] (oldValue) {
            var newValue = content[indx];
          newValue.parentCanvasElement = this;
            if (root <> null) {
                root.remove(oldValue.getNode());
                root.add(newValue.getNode());
            }
        };

    public attribute scale1ToFit: Boolean;
    public attribute viewport: CanvasViewport = CanvasViewport {};
    
    public function pick(x: Integer, y: Integer, w:Integer, h:Integer): Node[] {
        var result:Node[] = [];
        // TODO: SGNode.pick() needs to take (x,y,w,h)
        var path = jsgpanel.getScene().pick(new <<java.awt.Point>>(x, y));
        if (path.isEmpty()) {
            return result;
        }
        var lastNode:Node = null;
        var size = path.size();
        for (i in [0..path.size()-1]) {
            var n = getFX(path.get(i) as SGNode);
            if (n <> null and n <> lastNode) {
                insert n into result;
            }
            lastNode = n;
        }
        return result;
    }

    public function print() {
        // TODO: printing support?
        //zcanvas.getDrawingSurface().printSurface();
    }

    /**
     * Optional handler called when the user drops an object into this canvas.
     */
    public attribute onDrop: function(e:CanvasDropEvent);
    public attribute onDragEnter: function(e:CanvasDropEvent);
    public attribute onDragExit: function(e:CanvasDropEvent);
    /**
     * Optional filter for the types of objects that may be dropped into this
     * canvas.
     */
    public attribute dropType: Class;
    /**
     * <code>attribute acceptDrop: function(e:CanvasDropEvent): Boolean</code><br></br>
     * Optional handler called when the user drops an object into this canvas.
     * If it returns false, the drop is rejected. 
     */
    public attribute canAcceptDrop: function(e:CanvasDropEvent):Boolean;

    private function acceptDrop(value):Boolean {
        var info = MouseInfo.getPointerInfo();
        var p = info.getLocation();
        var location = jsgpanel.getLocationOnScreen();
        p.x -= location.x;
        p.y -= location.y;
        var path = pick(p.x, p.y, 1, 1);
        var e = CanvasDropEvent {
            x: p.x,
            y: p.y,
            transferData: [value]
        };
        for (i in path) {
            if (i.handleAcceptDrop(e)) {          
               if (dropTargetNode <> i) {
                   dropTargetNode.handleDragExit(e);
               }
               dropTargetNode = i;
               return true;
            }
        }
        dropTargetNode.handleDragExit(e);
        dropTargetNode = null;
        if (this.canAcceptDrop <> null) {
            e = CanvasDropEvent {
                x: p.x, y: p.y, localX: p.x, localY: p.y
            };
            return (this.canAcceptDrop)(e);
        }
        return onDrop <> null;
    }

    private function handleDragEnter(value) {
        var info = MouseInfo.getPointerInfo();
        var p = info.getLocation();
        var location = jsgpanel.getLocationOnScreen();
        p.x -= location.x;
        p.y -= location.y;
        var path = pick(p.x, p.y, 1, 1);
        var e = CanvasDropEvent {
            x: p.x,
            y: p.y
            transferData: [value]
        };
        for (i in path) {
            if (i.handleDragEnter(e)) {
               if (dropTargetNode <> i) {
                   dropTargetNode.handleDragExit(e);
               }
               dropTargetNode = i;
               break;
            }
        }
        if (this.onDragEnter <> null) {
            e.localX = e.x;
            e.localY = e.y;
            (this.onDragEnter)(e);
        }
    }

    private function handleDragExit(value) {
        var info = MouseInfo.getPointerInfo();
        var p = info.getLocation();
        var location = jsgpanel.getLocationOnScreen();
        p.x -= location.x;
        p.y -= location.y;
        var path = pick(p.x, p.y, 1, 1);
        var e = CanvasDropEvent {
            x: p.x,
            y: p.y
            transferData: [value]
        };
        for (i in path) {
            if (i.handleDragExit(e)) {
               break;
            }
        }
        if (this.onDragExit <> null) {
            e.localX = e.x;
            e.localY = e.y;
            (this.onDragExit)(e);
        }
    }

    private function getDragNode(): Node {
        return dragNode;
    }

    private function getDragValue():Object {
        return dragValue;
    }

    private function setDropValue(value): Void {
        var info = MouseInfo.getPointerInfo();
        var p = info.getLocation();
        var location = jsgpanel.getLocationOnScreen();
        p.x -= location.x;
        p.y -= location.y;
        var path = pick(p.x, p.y, 1, 1);
        var e = CanvasDropEvent {
            x: p.x,
            y: p.y
            transferData: [value]
        };
        for (i in path) {
            if (i.handleDrop(e)) {
                return;
            }
        }
        dropTargetNode.handleDragExit(e);
        dropTargetNode = null;
        e.localX = e.x;
        e.localY = e.y;
        (this.onDrop)(e);
    }

    public function doExportAsDrag(e:CanvasDragEvent) {
         //dragNode = e.source;
         dragNode = e.dragView;
         dragValue = e.dragValue as Object;
    }

    attribute dragNode: Node;
    attribute dragValue: Object;
    attribute dropTargetNode: Node;

    private function sizeAllToFit() {
        var canvasBounds = jsgpanel.getBounds();
        var parent = jsgpanel.getParent();
        if (parent instanceof JViewport) {
            canvasBounds = parent.getBounds();
        }
        viewport.setSize(canvasBounds.width, canvasBounds.height);
        if (sizeof scaleToFitList > 0) {
            for (n in scaleToFitList) {
                var root = (n.getNode() as SGAlignment).getChild() as Affine;
                var nodeBounds = root.getBounds();
                if ((nodeBounds.getWidth() > 0) and (nodeBounds.getHeight() > 0)) {
                    var t = AffineTransform.getScaleInstance(
                        (canvasBounds.getWidth()  - nodeBounds.getX()) / nodeBounds.getWidth(),
                        (canvasBounds.getHeight() - nodeBounds.getY()) / nodeBounds.getHeight());
                    root.transformBy(t);
                }
            }
        }
        if (sizeof sizeToFitList > 0) then {
            for (n in sizeToFitList) {
                n.setSize(canvasBounds.getWidth(), canvasBounds.getHeight());
            }
        }
    }

    attribute border: Border = EmptyBorder {top: 5, left: 5, right: 5, bottom: 5};

    protected function getCanvas() {
        return this;
    }

    protected function raiseNode(n:Node):Void {
        //TODO: need indexof
        //var i = select indexof x from x in content where x == n;
        var i = 0;
        for(x in content) {
            if(x == n) {
                break;
            }
            i = i + 1;
        }
        if (i == sizeof content -1) {
            return;
        }
        var tmp = content[i];
        content[i] = content[i+1];
        content[i+1] = tmp;
    }

    protected function lowerNode(n:Node):Void {
        //TODO: need indexof
        //var i = select first indexof x from x in content where x == n;
        var i = 0;
        for(x in content) {
            if(x == n) {
                break;
            }
            i = i + 1;
        }
        if (i == 0) {
            return;
        }
        var tmp = content[i-1];
        content[i-1] = content[i];
        content[i] = tmp;
    }

    protected function moveNodeToFront(n:Node):Void {
        //TODO: need indexof
        //var i = select indexof x from x in content where x == n;
        var i = 0;
        for(x in content) {
            if(x == n) {
                break;
            }
            i = i + 1;
        }
        delete content[i];
        insert n  into content;
        
    }

    protected function moveNodeToBack(n:Node):Void {
        //TODO: need indexof
        //var i = select indexof x from x in content where x == n;
        var i = 0;
        for(x in content) {
            if(x == n) {
                break;
            }
            i = i + 1;
        }
        delete content[i];
        //insert n as first into content;
        content = [n, content];
    }

    public function createComponent():javax.swing.JComponent {
        root = new SGGroup();
        jsgpanel.setCursor(java.awt.Cursor.getDefaultCursor());
        jsgpanel.setOpaque(false);
        jsgpanel.setScene(root);
        for (i in content) {
            i.parentCanvasElement = this;
            root.add(i.getNode());
        }
        var hoverListener = new FXMouseListener();
        jsgpanel.addMouseListener(hoverListener);
        jsgpanel.addMouseMotionListener(hoverListener);
        jsgpanel.addComponentListener(ComponentAdapter {
                function componentShown(e) {
                    sizeAllToFit();
                }
                function componentResized(e) {
                    sizeAllToFit();
                }
            });
        javax.swing.ToolTipManager.sharedInstance().registerComponent(jsgpanel);
        var javaVersion = System.getProperty("java.version") as java.lang.String;
        var javaBroken = javaVersion.startsWith("1.5.0_0") as Boolean;
        UIElement.context.addTransferHandler(
            jsgpanel, dropType,
            com.sun.javafx.api.ui.ValueGetter {
                public function get():Object {
                    return getDragValue();
                }
            },
            com.sun.javafx.api.ui.ValueSetter {
                public function set(value:Object) {
                    setDropValue(value);
                }
            },
            com.sun.javafx.api.ui.ValueAcceptor {
                public function accept(value:Object):Boolean {
                    if (javaBroken) {
                        return true;
                    }
                    var result = acceptDrop(value);
                    return result;
                }
                public function dragEnter(value:Object):Void {
                    handleDragEnter(value);
                }
                public function dragExit(value:Object):Void {
                    handleDragExit(value);
                }
            },
            com.sun.javafx.api.ui.VisualRepresentation {
                public function getIcon(value:Object):javax.swing.Icon {
                    var c = CanvasIcon {
                        content: [ getDragNode() ]
                    };
                    var icon = c.getIcon();
                    return icon;
                }
                public function getComponent(value:Object):java.awt.Component {
                    return null;
                }
            }
        );
        jsgpanel.addKeyListener(KeyListener {
            public function keyTyped(e:KeyEvent): Void {
                if(focusedNode.onKeyTyped <> null)
                    focusedNode.onKeyTyped(makeKeyEvent(e));
            }
            public function keyPressed(e:KeyEvent): Void {
                if(focusedNode.onKeyDown <> null)
                    focusedNode.onKeyDown(makeKeyEvent(e));
            }
            public function keyReleased(e:KeyEvent): Void {
                if(focusedNode.onKeyUp <> null)
                    focusedNode.onKeyUp(makeKeyEvent(e));
            }
        });
        jsgpanel.repaint();
        return jsgpanel;
    }
}
