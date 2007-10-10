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

package com.sun.tools.javafx.code;

import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.util.Context;

import static com.sun.tools.javac.jvm.ByteCodes.*;

/**
 *
 * @author Robert Field
 */
public class JavafxSymtab extends Symtab {
    
    public final Type javafx_SequenceType;
    public final Type javafx_SequenceTypeErasure;
    static public final int MAX_FIXED_PARAM_LENGTH = 8;
    public final Type[] javafx_FunctionTypes = new Type[MAX_FIXED_PARAM_LENGTH+1];
    public final Type javafx_FXObjectType;
    
    public static void preRegister(final Context context) {
        context.put(symtabKey, new Context.Factory<Symtab>() {
            public Symtab make() {
                return new JavafxSymtab(context);
            }
        });
    }
    
    /** Creates a new instance of JavafxSymtab */
    JavafxSymtab(Context context) {
        super(context);

        // used only with constructor
        Types types = Types.instance(context);
        
        javafx_IntegerType = intType;
        javafx_NumberType = doubleType;
        javafx_AnyType = unknownType;
        javafx_StringType = stringType;
        javafx_BooleanType = booleanType;
        javafx_VoidType = voidType;
        javafx_SequenceType = enterClass("com.sun.javafx.runtime.sequence.Sequence");
        javafx_SequenceTypeErasure = types.erasure(javafx_SequenceType);
        for (int i = MAX_FIXED_PARAM_LENGTH; --i >= 0;  ) {
            javafx_FunctionTypes[i] = enterClass("com.sun.javafx.functions.Function"+i);
        }
        
        javafx_FXObjectType = enterClass("com.sun.javafx.runtime.FXObject");
        enterOperators();
    }
    
    // Javafx types
    public final Type javafx_IntegerType;
    public final Type javafx_NumberType;
    public final Type javafx_AnyType;
    public final Type javafx_StringType;
    public final Type javafx_BooleanType;
    public final Type javafx_VoidType;
    
    public void enterOperators() {
        super.enterOperators();
        
        enterBinop("<>", objectType, objectType, booleanType, if_acmpne);
        enterBinop("<>", booleanType, booleanType, booleanType, if_icmpne);
        enterBinop("<>", doubleType, doubleType, booleanType, dcmpl, ifne);
        enterBinop("<>", floatType, floatType, booleanType, fcmpl, ifne);
        enterBinop("<>", longType, longType, booleanType, lcmp, ifne);
        enterBinop("<>", intType, intType, booleanType, if_icmpne);

        enterBinop("and", booleanType, booleanType, booleanType, bool_and);
        enterBinop("or", booleanType, booleanType, booleanType, bool_or);
        
        // Enter JavaFX operators.
        enterUnop("lazy", doubleType, doubleType, 0);
        enterUnop("lazy", intType, intType, 0);
        enterUnop("lazy", booleanType, booleanType, 0);
        enterUnop("lazy", objectType, objectType, 0);

        enterUnop("bind", doubleType, doubleType, 0);
        enterUnop("bind", intType, intType, 0);
        enterUnop("bind", booleanType, booleanType, 0);
        enterUnop("bind", objectType, objectType, 0);
    }
}
