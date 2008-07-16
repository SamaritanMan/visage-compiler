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

package javafx.application;

// PENDING_DOC_REVIEW
/**
 * This class defines the possible styles for a {@code Window} which are: {@code  WindowStyle.DECORATED},
 * {@code WindowStyle.UNDECORATED}, or {@code  WindowStyle.TRANSPARENT}.
 */
public class WindowStyle {

    private attribute name:String;

    // PENDING_DOC_REVIEW
    /**
     * Defines a normal window style with a solid white background and platform decorations.
     */
    public static attribute  DECORATED = WindowStyle { name: "DECORATED" }

    // PENDING_DOC_REVIEW
    /**
     * Defines a window style with a solid white background and no decorations.
     */
    public static attribute UNDECORATED = WindowStyle { name: "UNDECORATED" }

    // PENDING_DOC_REVIEW
    /**
     * Defines a window style with a transparent background and no decorations.
     */
    public static attribute TRANSPARENT = WindowStyle { name: "TRANSPARENT" }

    public function toString(): String { name }

}
