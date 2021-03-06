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

package org.visage.tools.visagedoc;

import com.sun.javadoc.*;

import com.sun.tools.mjavac.code.Symbol.ClassSymbol;
import com.sun.tools.mjavac.code.Symbol.MethodSymbol;
import com.sun.tools.mjavac.util.Position;
import org.visage.tools.tree.VisageFunctionDefinition;

/**
 * Represents a constructor of a java class.
 *
 * @since 1.2
 * @author Robert Field
 * @author Neal Gafter (rewrite)
 */

public class ConstructorDocImpl
        extends ExecutableMemberDocImpl implements ConstructorDoc {

    /**
     * constructor.
     */
    public ConstructorDocImpl(DocEnv env, MethodSymbol sym) {
        super(env, sym);
    }

    /**
     * constructor.
     */
    public ConstructorDocImpl(DocEnv env, MethodSymbol sym,
                              String docComment, VisageFunctionDefinition tree, Position.LineMap lineMap) {
        super(env, sym, docComment, tree, lineMap);
    }

    /**
     * Return true if it is a constructor, which it is.
     *
     * @return true
     */
    @Override
    public boolean isConstructor() {
        return true;
    }

    /**
     * Get the name.
     *
     * @return the name of the member qualified by class (but not package)
     */
    public String name() {
        ClassSymbol c = sym.enclClass();
        String n = c.name.toString();
        for (c = c.owner.enclClass(); c != null; c = c.owner.enclClass()) {
            n = c.name.toString() + "." + n;
        }
        return n;
    }

    /**
     * Get the name.
     *
     * @return the qualified name of the member.
     */
    public String qualifiedName() {
        return sym.enclClass().getQualifiedName().toString();
    }

    /**
     * Returns a string representation of this constructor.  Includes the
     * qualified signature and any type parameters.
     * Type parameters precede the class name, as they do in the syntax
     * for invoking constructors with explicit type parameters using "new".
     * (This is unlike the syntax for invoking methods with explicit type
     * parameters.)
     */
    @Override
    public String toString() {
        return qualifiedName() + signature();
    }
}
