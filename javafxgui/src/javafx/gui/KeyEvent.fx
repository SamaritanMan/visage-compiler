/*
 * Copyright 2008 Sun Microsystems, Inc.  All Rights Reserved.
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
package javafx.gui;

// PENDING_DOC_REVIEW
/**
 * An event which indicates that a keystroke occurred in a {@link Node}.
 * <p>
 * This low-level event is generated by a {@link Node} object 
 * when a key is pressed, released, or typed.  
 * Depends on the type of the event it is passed 
 * to {@link Node#onKeyPressed}, {@link Node#onKeyTyped} 
 * or {@link Node#onKeyReleased} function.  
 * 
 * @profile common conditional keyboard
 */          
public class KeyEvent {

    attribute awtKeyEvent:java.awt.event.KeyEvent;
    
    // PENDING_DOC_REVIEW
    /**
     * The {@link Node} that originated the event.
     *
     * @profile common conditional keyboard
     */              
    public attribute node:Node;

    private function isKeyTyped():Boolean {
        awtKeyEvent.getID() == java.awt.event.KeyEvent.KEY_TYPED
    }

    // PENDING_DOC_REVIEW
    /**
     * Returns the character associated with the key in this event.
     * For example, the key typed event for shift + "a" 
     * returns the value for "A".
     *
     * @profile common conditional keyboard
     */              
    public function getKeyChar():String { 
        if (isKeyTyped()) "{awtKeyEvent.getKeyChar()}" else null;
    }

    // PENDING_DOC_REVIEW
    /**
     * Returns a String describing the keyCode, such as "HOME", "F1" or "A"
     * for key pressed and key released events.
     * For key typed events returns {@code null}
     *
     * @profile common conditional keyboard
     */              
    public function getKeyText():String { 
        if (isKeyTyped()) null else awtKeyEvent.getKeyText(awtKeyEvent.getKeyCode());
    }

    // PENDING_DOC_REVIEW
    /**
     * Returns the integer keyCode associated with the key in this event.
     *
     * @profile common conditional keyboard
     */              
    public function getKeyCode():Integer { 
        if (isKeyTyped()) awtKeyEvent.VK_UNDEFINED else awtKeyEvent.getKeyCode();
    }

    // PENDING_DOC_REVIEW
    /**
     * Returns whether or not the Shift modifier is down on this event.
     *
     * @profile common conditional keyboard
     */              
    public function isShiftDown():Boolean { awtKeyEvent.isShiftDown() }
    
    // PENDING_DOC_REVIEW
    /**
     * Returns whether or not the Control modifier is down on this event.
     *
     * @profile common conditional keyboard
     */              
    public function isControlDown():Boolean { awtKeyEvent.isControlDown() }
    
    // PENDING_DOC_REVIEW
    /**
     * Returns whether or not the Alt modifier is down on this event.
     *
     * @profile common conditional keyboard
     */                  
    public function isAltDown():Boolean { awtKeyEvent.isAltDown() }
    
    // PENDING_DOC_REVIEW
    /**
     * Returns whether or not the Meta modifier is down on this event.
     *
     * @profile common conditional keyboard
     */              
    public function isMetaDown():Boolean { awtKeyEvent.isMetaDown() }

}
