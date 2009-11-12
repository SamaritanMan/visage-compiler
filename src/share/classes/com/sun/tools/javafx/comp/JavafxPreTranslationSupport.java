/*
* Copyright 2009 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.tools.javafx.comp;

import com.sun.tools.javafx.code.JavafxClassSymbol;
import com.sun.tools.javafx.code.JavafxFlags;
import com.sun.tools.javafx.code.JavafxSymtab;
import com.sun.tools.javafx.code.JavafxTypes;
import com.sun.tools.javafx.tree.*;
import com.sun.tools.mjavac.code.Flags;
import com.sun.tools.mjavac.code.Kinds;
import com.sun.tools.mjavac.code.Scope;
import com.sun.tools.mjavac.code.Symbol;
import com.sun.tools.mjavac.code.Symbol.*;
import com.sun.tools.mjavac.code.Type.ClassType;
import com.sun.tools.mjavac.util.Context;
import com.sun.tools.mjavac.util.Name;
import javax.tools.JavaFileObject;

/**
 * Shared support for the pre-translation passes.  Not a pass itself.
 *
 * @author Maurizio Cimadamore
 * @author Robert Field
 */
public class JavafxPreTranslationSupport {

    private final JavafxTreeMaker fxmake;
    private final JavafxDefs defs;
    private final Name.Table names;
    private final JavafxCheck chk;
    private final JavafxTypes types;
    private final JavafxSymtab syms;

    private int tmpCount = 0;

    protected static final Context.Key<JavafxPreTranslationSupport> preTranslation =
            new Context.Key<JavafxPreTranslationSupport>();

    public static JavafxPreTranslationSupport instance(Context context) {
        JavafxPreTranslationSupport instance = context.get(preTranslation);
        if (instance == null) {
            instance = new JavafxPreTranslationSupport(context);
        }
        return instance;
    }

    private JavafxPreTranslationSupport(Context context) {
        context.put(preTranslation, this);

        fxmake = JavafxTreeMaker.instance(context);
        defs = JavafxDefs.instance(context);
        names = Name.Table.instance(context);
        chk = JavafxCheck.instance(context);
        types = JavafxTypes.instance(context);
        syms = (JavafxSymtab)JavafxSymtab.instance(context);
    }

    public Name syntheticName(String prefix) {
        return names.fromString(prefix + "$" + tmpCount++);
    }

    public Scope getEnclosingScope(Symbol s) {
        if (s.owner.kind == Kinds.TYP) {
            return ((ClassSymbol)s.owner).members();
        }
        else if (s.owner.kind == Kinds.PCK) {
            return ((PackageSymbol)s.owner).members();
        }
        else
            return null;
    }

    public JavaFileObject sourceFile(Symbol owner) {
        for (Symbol currOwner = owner; currOwner != null; currOwner = currOwner.owner) {
            if (currOwner instanceof ClassSymbol) {
                JavaFileObject src = ((ClassSymbol)currOwner).sourcefile;
                if (src != null) {
                    return src;
                }
            }
        }
        return null;
    }

    public JavafxClassSymbol makeClassSymbol(Name name, Symbol owner) {
        JavafxClassSymbol classSym = new JavafxClassSymbol(0L, name, owner);
        classSym.flatname = chk.localClassName(classSym);
        chk.compiled.put(classSym.flatname, classSym);

        // we may be able to get away without any scope stuff
        //  s.enter(sym);

        // Fill out class fields.
        classSym.completer = null;
        /*
         * These class symbol flags of the local classes are used in translation.
         * For FX_SYNTHETIC_LOCAL_CLASS classes, initialize$ is not called from
         * Java entry constructor so that function code executes in source order.
         * See comment in JavafxInitializationBuilder.makeJavaEntryConstructor().
         * FX_BOUND_FUNCTION_CLASS classes are treated specially for many aspects
         * like handling Pointer/FXObject+varNum registration stuff for bound
         * function implementation.
         */
        classSym.flags_field = JavafxFlags.FX_SYNTHETIC_LOCAL_CLASS;
        if (classSym.owner instanceof MethodSymbol &&
            (classSym.owner.flags() & JavafxFlags.BOUND) != 0L) {
            classSym.flags_field |= JavafxFlags.FX_BOUND_FUNCTION_CLASS;
        }
        classSym.sourcefile = sourceFile(owner);
        classSym.members_field = new Scope(classSym);

        ClassType ct = (ClassType) classSym.type;
        // We are seeing a local or inner class.
        // Set outer_field of this class to closest enclosing class
        // which contains this class in a non-static context
        // (its "enclosing instance class"), provided such a class exists.
        Symbol owner1 = owner.enclClass();
        if (owner1.kind == Kinds.TYP) {
            ct.setEnclosingType(owner1.type);
        }

        ct.supertype_field = syms.javafx_FXBaseType;
        classSym.addSuperType(syms.javafx_FXBaseType);

        return classSym;
    }

    public MethodSymbol makeDummyMethodSymbol(Symbol owner) {
        return new MethodSymbol(Flags.BLOCK, names.empty, null, owner);
    }
}

