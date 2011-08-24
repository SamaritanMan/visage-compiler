/*
 * Copyright 2008-2009 Sun Microsystems, Inc.  All Rights Reserved.
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

package visage.reflect;

/** 
 * A reference to a specific attribute in a specific object. 
 *
 * @author Per Bothner
 * @profile desktop
 */
public class VisageVarMemberLocation extends VisageLocation {
    VisageObjectValue object;
    VisageVarMember attr;
    VisageVarMemberLocation(VisageObjectValue object, VisageVarMember attr)
    { this.object = object; this.attr = attr; }
    public VisageValue getValue() { return attr.getValue(object); }
    public void setValue(VisageValue newValue) { attr.setValue(object, newValue); }
}
