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

package com.sun.tools.javafx.tree;

import com.sun.tools.javac.code.Type;

/**
 *
 * @author Robert Field
 */
public abstract class JFXAbstractOnChange extends JFXStatement {
    private final JFXVar index;
    private final JFXVar oldValue;
    private final JCBlock body;
    
    public Type elementType = null;

    public JFXAbstractOnChange(JFXVar index, JFXVar oldValue, JCBlock body) {
        this.index = index;
        this.oldValue = oldValue;
        this.body = body;
    }

    public JFXVar getIndex() {
        return index;
    }
    
    public JFXVar getOldValue() {
        return oldValue;
    }
    
    public JCBlock getBody() {
        return body;
    }
}
