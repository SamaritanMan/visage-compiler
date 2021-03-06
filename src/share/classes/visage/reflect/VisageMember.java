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

/** A run-time represention of a Visage member of a class.
 * Corresponds to {@code java.lang.reflect.Member},
 * and {@code com.sun.jdi.TypeComponent}, respectively.
 *
 * @author Per Bothner
 * @profile desktop
 */

public interface VisageMember {
    public abstract String getName();
    public abstract VisageClassType getDeclaringClass();
    public abstract boolean isStatic();

    /** The member was specified to have public access. */
    public abstract boolean isPublic();

    /** The member was specified to have package access.
     * For a member of a Java class, returns true if the member has default (blank) access.
     */
    public abstract boolean isPackage();

    /** The member was specified to have protected access. */
    public abstract boolean isProtected();
}
