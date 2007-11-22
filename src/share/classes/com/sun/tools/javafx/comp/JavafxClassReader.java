/*
 * Copyright 1999-2007 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.tools.javafx.comp;

import java.io.*;
import java.net.URI;
import java.nio.CharBuffer;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileManager;
import javax.tools.StandardJavaFileManager;

import com.sun.tools.javac.jvm.ClassReader;

import com.sun.tools.javac.comp.Annotate;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Type.*;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.List;

import static com.sun.tools.javac.code.Flags.*;
import static com.sun.tools.javac.code.Kinds.*;
import static com.sun.tools.javac.code.TypeTags.*;
import com.sun.tools.javac.jvm.ClassFile.NameAndType;
import com.sun.tools.javafx.code.JavafxClassSymbol;
import javax.tools.JavaFileManager.Location;
import static javax.tools.StandardLocation.*;

import static com.sun.tools.javafx.code.JavafxVarSymbol.*;

/** This class provides operations to read a classfile into an internal
 *  representation. The internal representation is anchored in a
 *  ClassSymbol which contains in its scope symbol representations
 *  for all other definitions in the classfile. Top-level Classes themselves
 *  appear as members of the scopes of PackageSymbols.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
public class JavafxClassReader extends ClassReader {

    private final JavafxTypeMorpher typeMorpher;
    private Name currentMethodName;
    private JavafxInitializationBuilder initBuilder;
    private String intfNameStr;
    private int intfNameLen;

    public static void preRegister(final Context context) {
        context.put(classReaderKey, new Context.Factory<ClassReader>() {
	       public ClassReader make() {
		   return new JavafxClassReader(context, true);
	       }
        });
    }

    /** Construct a new class reader, optionally treated as the
     *  definitive classreader for this invocation.
     */
    protected JavafxClassReader(Context context, boolean definitive) {
        super(context, definitive);
        typeMorpher = JavafxTypeMorpher.instance(context);
        initBuilder = JavafxInitializationBuilder.instance(context);
        intfNameStr = initBuilder.interfaceNameSuffix.toString();
        intfNameLen = intfNameStr.length();    
    }

    /** Convert class signature to type, where signature is implicit.
     */
    protected Type classSigToType() {
        if (signature[sigp] != 'L')
            throw badClassFile("bad.class.signature",
                               Convert.utf2string(signature, sigp, 10));
        sigp++;
        Type outer = Type.noType;
        int startSbp = sbp;

        while (true) {
            final byte c = signature[sigp++];
            switch (c) {

            case ';': {         // end
                Name className = names.fromUtf(signatureBuffer,
                                                         startSbp,
                                                         sbp - startSbp);
                className = removeIntfPart(className);
                ClassSymbol t = enterClass(className);
                if (!keepClassFileSignatures()) {
                    if (t == typeMorpher.declLocation[TYPE_KIND_BOOLEAN].sym) {
                        sbp = startSbp;
                        return syms.booleanType;
                    } else if (t == typeMorpher.declLocation[TYPE_KIND_DOUBLE].sym) {
                        sbp = startSbp;
                        return syms.doubleType;
                    } else if (t == typeMorpher.declLocation[TYPE_KIND_INT].sym) {
                        sbp = startSbp;
                        return syms.intType;
                    }
                }
                if (outer == Type.noType)
                    outer = t.erasure(types);
                else
                    outer = new ClassType(outer, List.<Type>nil(), t);
                sbp = startSbp;
                return outer;
            }

            case '<':           // generic arguments
                Name className = names.fromUtf(signatureBuffer,
                                                         startSbp,
                                                         sbp - startSbp);
                className = removeIntfPart(className);
                ClassSymbol t = enterClass(className);
                List<Type> genericArgs = sigToTypes('>');
                if (!keepClassFileSignatures() &&
                               types.erasure(t.type).tsym == typeMorpher.declLocation[TYPE_KIND_OBJECT].sym) {
                    outer = genericArgs.head;
                } else {
                    outer = new ClassType(outer, genericArgs, t) {
                        boolean completed = false;
                        public Type getEnclosingType() {
                            if (!completed) {
                                completed = true;
                                tsym.complete();
                                Type enclosingType = tsym.type.getEnclosingType();
                                if (enclosingType != Type.noType) {
                                    List<Type> typeArgs =
                                        super.getEnclosingType().allparams();
                                    List<Type> typeParams =
                                        enclosingType.allparams();
                                    if (typeParams.length() != typeArgs.length()) {
                                        // no "rare" types
                                        super.setEnclosingType(types.erasure(enclosingType));
                                    } else {
                                        super.setEnclosingType(types.subst(enclosingType,
                                                                           typeParams,
                                                                           typeArgs));
                                    }
                                } else {
                                    super.setEnclosingType(Type.noType);
                                }
                            }
                            return super.getEnclosingType();
                        }
                        public void setEnclosingType(Type outer) {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
                switch (signature[sigp++]) {
                case ';':
                    if (sigp < signature.length && signature[sigp] == '.') {
                        // support old-style GJC signatures
                        // The signature produced was
                        // Lfoo/Outer<Lfoo/X;>;.Lfoo/Outer$Inner<Lfoo/Y;>;
                        // rather than say
                        // Lfoo/Outer<Lfoo/X;>.Inner<Lfoo/Y;>;
                        // so we skip past ".Lfoo/Outer$"
                        sigp += (sbp - startSbp) + // "foo/Outer"
                            3;  // ".L" and "$"
                        signatureBuffer[sbp++] = (byte)'$';
                        break;
                    } else {
                        sbp = startSbp;
                        return outer;
                    }
                case '.':
                    signatureBuffer[sbp++] = (byte)'$';
                    break;
                default:
                    throw new AssertionError(signature[sigp-1]);
                }
                continue;

            case '.':
                signatureBuffer[sbp++] = (byte)'$';
                continue;
            case '/':
                signatureBuffer[sbp++] = (byte)'.';
                continue;
            default:
                signatureBuffer[sbp++] = c;
                continue;
            }
        }
    }

    /** Read a method.
     */
    protected MethodSymbol readMethod() {
        Name prevMethodName = currentMethodName;
        try {
            long flags = adjustMethodFlags(nextChar());
            Name name = readName(nextChar());
            currentMethodName = name;
            Type type = readType(nextChar());
            if (name == names.init && currentOwner.hasOuterInstance()) {
                // Sometimes anonymous classes don't have an outer
                // instance, however, there is no reliable way to tell so
                // we never strip this$n
                if (currentOwner.name.len != 0)
                    type = new MethodType(type.getParameterTypes().tail,
                                          type.getReturnType(),
                                          type.getThrownTypes(),
                                          syms.methodClass);
            }
            MethodSymbol m = new MethodSymbol(flags, name, type, currentOwner);
            Symbol prevOwner = currentOwner;
            currentOwner = m;
            try {
                readMemberAttrs(m);
            } finally {
                currentOwner = prevOwner;
            }
            return m;
        }
        finally {
            currentMethodName = prevMethodName;
        }
    }

    private boolean keepClassFileSignatures() {
        if (currentMethodName != null) {
            String currMethodName = currentMethodName.toString();
            if (currMethodName.startsWith(initBuilder.attributeGetMethodNamePrefix) ||
                    currMethodName.startsWith(initBuilder.attributeInitMethodNamePrefix)) {
                return true;
            }
        }
        
        return false;
    }

    private Name removeIntfPart(Name className) {
        if (className.endsWith(initBuilder.interfaceNameSuffix)) {
            String classNameStr = className.toString();
            return names.fromString(classNameStr.substring(0, classNameStr.length() - intfNameLen));
        }

        return className;
    }

    /** Define a new class given its name and owner.
     */
    public ClassSymbol defineClass(Name name, Symbol owner) {
        ClassSymbol c = new JavafxClassSymbol(0, name, owner, this);
        if (owner.kind == PCK)
            assert classes.get(c.flatname) == null : c;
        c.completer = this;
        return c;
    }

    public JavafxInitializationBuilder getInitBuilder() {
        return initBuilder;
    }
}
