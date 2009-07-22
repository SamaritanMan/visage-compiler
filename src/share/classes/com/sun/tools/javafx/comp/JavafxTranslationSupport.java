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

import java.io.OutputStreamWriter;

import com.sun.javafx.api.JavafxBindStatus;
import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.CapturedType;
import com.sun.tools.javac.code.Type.ForAll;
import com.sun.tools.javac.code.Type.MethodType;
import com.sun.tools.javac.code.Type.WildcardType;
import com.sun.tools.javac.code.TypeTags;
import static com.sun.tools.javac.code.Flags.STATIC;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.Pretty;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Position;
import com.sun.tools.javafx.code.JavafxFlags;
import com.sun.tools.javafx.code.JavafxSymtab;
import com.sun.tools.javafx.code.JavafxTypes;
import static com.sun.tools.javafx.code.JavafxVarSymbol.*;
import static com.sun.tools.javafx.comp.JavafxDefs.*;
import com.sun.tools.javafx.comp.JavafxTypeMorpher.TypeMorphInfo;
import com.sun.tools.javafx.comp.JavafxTypeMorpher.VarMorphInfo;
import com.sun.tools.javafx.tree.*;
import java.util.Set;

/**
 * Common support routines needed for translation
 *
 * @author Robert Field
 */
public abstract class JavafxTranslationSupport {
    protected final JavafxDefs defs;
    protected final Log log;
    protected final JavafxTreeMaker fxmake;
    protected final TreeMaker make; // translation should yield a Java AST, use fxmake when building FX trees
    protected final Name.Table names;
    protected final JavafxResolve rs;
    protected final JavafxSymtab syms;
    protected final JavafxTypes types;
    protected final JavafxTypeMorpher typeMorpher;

    /*
     * other instance information
     */
    private int syntheticNameCounter = 0;

    protected JavafxTranslationSupport(Context context) {
        make = TreeMaker.instance(context);
        fxmake = JavafxTreeMaker.instance(context);
        log = Log.instance(context);
        names = Name.Table.instance(context);
        types = JavafxTypes.instance(context);
        syms = (JavafxSymtab)JavafxSymtab.instance(context);
        typeMorpher = JavafxTypeMorpher.instance(context);
        rs = JavafxResolve.instance(context);
        defs = JavafxDefs.instance(context);

        syntheticNameCounter = 0;
    }

    protected Symbol expressionSymbol(JFXExpression tree) {
        switch (tree.getFXTag()) {
            case IDENT:
                return ((JFXIdent) tree).sym;
            case SELECT:
                return ((JFXSelect) tree).sym;
            default:
                return null;
        }
    }

    protected boolean isValueFromJava(final JFXExpression expr) {
        // The value could come from Java if it is a variable, or a function result.
        Symbol sym = expressionSymbol(expr);
        if (sym != null && !types.isJFXClass(sym.owner)) {
            return true;
        }

        // test for function
        if (expr.getFXTag() == JavafxTag.APPLY) {
            JFXExpression func = ((JFXFunctionInvocation)expr).getMethodSelect();
            if (isValueFromJava(func)) {
                return true;
            }
        }
        return false;
    }

    boolean isImmutable(Symbol sym, Name name) {
        Symbol owner = sym.owner;
        boolean isLocal = owner.kind != Kinds.TYP;
        boolean isKnown = isLocal || (owner instanceof ClassSymbol && types.getFxClass((ClassSymbol) owner) != null); //TODO: consider Java supers
        long flags = sym.flags();
        boolean isWritable = (flags & (Flags.PUBLIC | Flags.PROTECTED | JavafxFlags.PACKAGE_ACCESS)) != 0L;
        boolean isAssignedTo = (flags & (JavafxFlags.VARUSE_INIT_ASSIGNED_TO | JavafxFlags.VARUSE_ASSIGNED_TO)) != 0;
        boolean isDef = (flags & JavafxFlags.IS_DEF) != 0;
        boolean isDefinedBound = (flags & JavafxFlags.VARUSE_BOUND_INIT) != 0;
        return
                    name == names._this ||
                    name == names._super ||
                    isKnown && isDef && !isDefinedBound || // unbound def
                    isKnown && !isWritable && !isAssignedTo && !isDefinedBound; // never reassigned
     }

    boolean isImmutable(JFXIdent tree) {
        return isImmutable(tree.sym, tree.getName());
    }

    boolean isImmutable(final JFXSelect tree) {
        return isImmutableSelector(tree) && isImmutable(tree.sym, tree.getIdentifier());
    }

    boolean isImmutableSelector(final JFXSelect tree) {
        if (tree.sym.isStatic()) {
            // If the symbol is static, no matter what the selector is, the selectot is immutable
            return true;
        }
        final JFXExpression selector = tree.getExpression();
        if (selector instanceof JFXIdent) {
            return isImmutable((JFXIdent) selector);
        } else if (selector instanceof JFXSelect) {
            return isImmutable((JFXSelect) selector);
        } else {
            return false;
        }
    }

    boolean hasDependencies(JFXExpression expr, final Set<Symbol> exclusions) {
        class DependencyScanner extends JavafxTreeScanner {

            boolean hasDependencies = false;

            private void markHasDependencies() {
                hasDependencies = true;
            }

            @Override
            public void visitFunctionInvocation(JFXFunctionInvocation tree) {
                Symbol sym = JavafxTreeInfo.symbol(tree.getMethodSelect());
                if (sym == null || (sym.flags() & JavafxFlags.BOUND) != 0L) {
                    markHasDependencies();
                } else {
                    super.visitFunctionInvocation(tree);
                }
            }

            @Override
            public void visitObjectLiteralPart(JFXObjectLiteralPart tree) {
                if (!tree.isBound()) {
                    super.visitObjectLiteralPart(tree);
                }
            }

            @Override
            public void visitIdent(JFXIdent tree) {
                if (!exclusions.contains(tree.sym) && !isImmutable(tree)) {
                    markHasDependencies();
                }
            }

            @Override
            public void visitSelect(JFXSelect tree) {
                if (!isImmutable(tree)) {
                    markHasDependencies();
                }
            }

            @Override
            public void visitInterpolateValue(JFXInterpolateValue that) {
                markHasDependencies(); // errr, umm, better safe than sorry
            }

            @Override
            public void visitKeyFrameLiteral(JFXKeyFrameLiteral that) {
                markHasDependencies(); // errr, umm, better safe than sorry
            }
        }
        DependencyScanner scanner = new DependencyScanner();
        scanner.scan(expr);
        return scanner.hasDependencies;
    }

    boolean hasSideEffects(JFXExpression expr) {
        class SideEffectScanner extends JavafxTreeScanner {

            boolean hse = false;

            private void markSideEffects() {
                hse = true;
            }

            @Override
            public void visitBlockExpression(JFXBlock tree) {
                markSideEffects(); // maybe doesn't but covers all statements
            }

            @Override
            public void visitUnary(JFXUnary tree) {
                markSideEffects();
            }

            @Override
            public void visitAssign(JFXAssign tree) {
                markSideEffects();
            }

            @Override
            public void visitAssignop(JFXAssignOp tree) {
                markSideEffects();
            }

            @Override
            public void visitInstanciate(JFXInstanciate tree) {
                markSideEffects();
            }

            @Override
            public void visitFunctionInvocation(JFXFunctionInvocation tree) {
                markSideEffects();
            }

            @Override
            public void visitSelect(JFXSelect tree) {
                // Doesn't really have side-effects but the dupllicate null checking is aweful
                //TODO: do this in a cleaner way
                markSideEffects();
            }
        }
        SideEffectScanner scanner = new SideEffectScanner();
        scanner.scan(expr);
        return scanner.hse;
    }

    /**
     * Return the generated interface name corresponding to the class
     * */
    protected Name interfaceName(JFXClassDeclaration cDecl) {
        Name name = cDecl.getName();
        if (!cDecl.isMixinClass())
            return name;
        return names.fromString(name.toString() + mixinSuffix);
    }

    protected boolean isMixinClass(ClassSymbol sym) {
        return (sym.flags_field & JavafxFlags.MIXIN) != 0;
    }
    
    protected JCExpression makeIdentifier(DiagnosticPosition diagPos, Name aName) {
        String str = aName.toString();
        if (str.indexOf('.') < 0 && str.indexOf('<') < 0) {
            return make.at(diagPos).Ident(aName);
        }
        return makeIdentifier(diagPos, str);
    }

    protected JCExpression makeIdentifier(DiagnosticPosition diagPos, String str) {
        assert str.indexOf('<') < 0 : "attempt to parse a type with 'Identifier'.  Use TypeTree";
        JCExpression tree = null;
        int inx;
        int lastInx = 0;
        do {
            inx = str.indexOf('.', lastInx);
            int endInx;
            if (inx < 0) {
                endInx = str.length();
            } else {
                endInx = inx;
            }
            String part = str.substring(lastInx, endInx);
            Name partName = names.fromString(part);
            tree = tree == null?
                make.at(diagPos).Ident(partName) :
                make.at(diagPos).Select(tree, partName);
            lastInx = endInx + 1;
        } while (inx >= 0);
        return tree;
    }

    protected JCVariableDecl makeParam(DiagnosticPosition diagPos, Name name, Type type) {
        return makeParam(diagPos, name, makeTypeTree(diagPos, type));

    }

    protected JCVariableDecl makeParam(DiagnosticPosition diagPos, Name name, JCExpression typeExpression) {
        return make.at(diagPos).VarDef(
                make.Modifiers(Flags.PARAMETER | Flags.FINAL),
                name,
                typeExpression,
                null);

    }

    protected JCMethodDecl makeMethod(DiagnosticPosition diagPos, Name methName, List<JCStatement> stmts, List<JCVariableDecl> params, Type returnType, long flags) {
        return makeMethod(diagPos, methName, stmts, params, makeTypeTree(diagPos, returnType, true), flags);
    }

    protected JCMethodDecl makeMethod(DiagnosticPosition diagPos, Name methName, List<JCStatement> stmts, List<JCVariableDecl> params, JCExpression typeExpression, long flags) {
        return make.at(diagPos).MethodDef(
                make.at(diagPos).Modifiers(flags),
                methName,
                typeExpression,
                List.<JCTypeParameter>nil(),
                params == null ? List.<JCVariableDecl>nil() : params,
                List.<JCExpression>nil(),
                make.at(diagPos).Block(0L, stmts),
                null);
    }

    /**
     * @param diagPos
     * @return expression representing null
     */
    protected JCExpression makeNull(DiagnosticPosition diagPos) {
        return make.at(diagPos).Literal(TypeTags.BOT, null);
    }

    protected JCExpression makeQualifiedTree(DiagnosticPosition diagPos, String str) {
        JCExpression tree = null;
        int inx;
        int lastInx = 0;
        do {
            inx = str.indexOf('.', lastInx);
            int endInx;
            if (inx < 0) {
                endInx = str.length();
            } else {
                endInx = inx;
            }
            String part = str.substring(lastInx, endInx);
            Name partName = names.fromString(part);
            tree = tree == null?
                makeIdentOfPresetKind(diagPos, partName, Kinds.PCK) :
                make.at(diagPos).Select(tree, partName);
            lastInx = endInx + 1;
        } while (inx >= 0);
        return tree;
    }

    
    protected JCExpression makeAccessExpression(DiagnosticPosition diagPos, Symbol sym, boolean makeIntf) {
        Symbol owner = sym.owner;
        Name name = sym.name;
        switch (sym.kind) {
            case Kinds.PCK:
                if (name == null || name == name.table.empty) {
                    return null;
                }
                break;
            case Kinds.TYP:
                if (owner.type != null && owner.type.tag == TypeTags.TYPEVAR) {
                    throw new RuntimeException("TYPEVAR: " + owner.type);
                }
                if (makeIntf) {
                    name = names.fromString(name.toString() + mixinSuffix);
                }
                break;
            default:
                return null;
        }
        if (owner != null) {
            JCExpression oExpr = makeAccessExpression(diagPos, owner, false);
            if (oExpr != null) {
                return make.at(diagPos).Select(oExpr, name);
            }
        }
        if (sym.kind == Kinds.PCK) {
            return makeIdentOfPresetKind(diagPos, name, Kinds.PCK);
        } else {
            return make.at(diagPos).Ident(name);
        }
    }

    /**
     * Build a Java AST representing the specified type.
     * Convert JavaFX class references to interface references.
     * */
    public JCExpression makeTypeTree(DiagnosticPosition diagPos, Type t) {
        return makeTypeTree(diagPos, t, true);
    }

    /**
     * Build a Java AST representing the specified type.
     * If "makeIntf" is set, convert JavaFX class references to interface references.
     * */
    public JCExpression makeTypeTree(DiagnosticPosition diagPos, Type t, boolean makeIntf) {
        while (t instanceof CapturedType) {
            WildcardType wtype = ((CapturedType) t).wildcard;
            // A kludge for Class.newInstance (and maybe other cases):
            // Applying newinstance of an object of type Class<? extends T>
            // should yield an instance of T rather than a ? extends T,
            // which would confuse the back-end.
            t = wtype.kind == BoundKind.EXTENDS ? wtype.type : wtype;
        }
        return makeTypeTreeInner(diagPos, t, makeIntf);
    }

    private JCExpression makeTypeTreeInner(DiagnosticPosition diagPos, Type t, boolean makeIntf) {
        while (t instanceof CapturedType)
            t = ((CapturedType) t).wildcard;
        switch (t.tag) {
            case TypeTags.CLASS: {
                JCExpression texp = null;
                boolean isMixin = types.isMixin(t.tsym);

                if (makeIntf && isMixin) {
                    texp = makeAccessExpression(diagPos, t.tsym, true);
                } else {
                    if (t.isCompound()) {
                        t = types.supertype(t);
                    }
                    texp = makeAccessExpression(diagPos, t.tsym, false);
                }

                // Type outer = t.getEnclosingType();
                if (!t.getTypeArguments().isEmpty()) {
                    List<JCExpression> targs = List.nil();
                    for (Type ta : t.getTypeArguments()) {
                        targs = targs.append(makeTypeTreeInner(diagPos, ta, makeIntf));
                    }
                    texp = make.at(diagPos).TypeApply(texp, targs);
                }
                return texp;
            }
            case TypeTags.BOT: { // it is the null type, punt and make it the Object type
                return makeQualifiedTree(diagPos, syms.objectType.tsym.getQualifiedName().toString());
            }
            case TypeTags.WILDCARD: {
                WildcardType wtype = (WildcardType) t;
                return make.at(diagPos).Wildcard(make.TypeBoundKind(wtype.kind),
                        wtype.kind == BoundKind.UNBOUND ? null
                        : makeTypeTreeInner(diagPos,wtype.type, makeIntf));
            }
            case TypeTags.ARRAY: {
                return make.at(diagPos).TypeArray(makeTypeTreeInner(diagPos,types.elemtype(t), makeIntf));
            }
            default: {
                return make.at(diagPos).Type(t);
            }
        }
    }

    /**
     * Build a Java AST representing the return type of a function.
     * Generate the return type as a Location if "isBound" is set.
     * */
    public JCExpression makeReturnTypeTree(DiagnosticPosition diagPos, MethodSymbol mth, boolean isBound) {
        Type returnType = mth.getReturnType();
        if (isBound) {
            VarMorphInfo vmi = typeMorpher.varMorphInfo(mth);
            returnType = vmi.getLocationType();
        }
        return makeTypeTree(diagPos, returnType);
    }

    JCExpression typeCast(final DiagnosticPosition diagPos, final Type targetType, final Type inType, final JCExpression expr) {
        TypeMorphInfo tmi = typeMorpher.typeMorphInfo(inType);
        if (tmi.getTypeKind() == TYPE_KIND_OBJECT) {
            // We can't just cast the Object to Float (for example)
            // because if the Object is not Float, we will get a ClassCastException at runtime.
            // And we can't just call java.lang.Number.floatValue() because java.lang.Number
            // doesn't exist on mobile, at least not as of Jan 2009.
            String method = null;
            switch (targetType.tag) {
                case TypeTags.BOOLEAN:
                    method="objectToBoolean";
                    break;
                case TypeTags.CHAR:
                    method="objectToCharacter";
                    break;
                case TypeTags.BYTE:
                    method="objectToByte";
                    break;
                case TypeTags.SHORT:
                    method="objectToShort";
                    break;
                case TypeTags.INT:
                    method="objectToInt";
                    break;
                case TypeTags.LONG:
                    method="objectToLong";
                    break;
                case TypeTags.FLOAT:
                    method="objectToFloat";
                    break;
                case TypeTags.DOUBLE:
                    method="objectToDouble";
                    break;
            }
            if (method != null) {
                //TODO: Use RuntimeMethod
                return callExpression(diagPos,
                                        makeQualifiedTree(diagPos, "com.sun.javafx.runtime.Util"),
                                        method,
                                        expr);
            }
        }

        // The makeTypeCast below is usually redundant, since translateAsValue
        // takes care of most conversions - except in the case of a plain object cast.
        // It would be cleaner to move the makeTypeCast to translateAsValue,
        // but it's painful to get it right.  FIXME.
        return makeTypeCast(diagPos, targetType, inType, expr);
    }

    JCExpression makeTypeCast(DiagnosticPosition diagPos, Type clazztype, Type exprtype, JCExpression translatedExpr) {
        if (types.isSameType(clazztype, exprtype)) {
            return translatedExpr;
        } else {
            Type castType = clazztype;
            if (!exprtype.isPrimitive()) {
                castType = types.boxedTypeOrType(castType);
            }
            if (castType.isPrimitive() && exprtype.isPrimitive()) {
                JCTree clazz = makeTypeTree(diagPos, exprtype, true);
                translatedExpr = make.at(diagPos).TypeCast(clazz, translatedExpr);
            }
            JCTree clazz = makeTypeTree(diagPos, castType, true);
            return make.at(diagPos).TypeCast(clazz, translatedExpr);
        }
    }

   /**
    * Make a receiver parameter.
    * Its type is that of the corresponding interface and it is a final parameter.
    * */
    JCVariableDecl makeReceiverParam(JFXClassDeclaration cDecl) {
        return make.VarDef(
                make.Modifiers(Flags.FINAL | Flags.PARAMETER),
                defs.receiverName,
                make.Ident(interfaceName(cDecl)),
                null);
    }

   /**
    * Make a receiver local.
    * Its type is that of the corresponding interface and it is a final parameter.
    * */
    JCVariableDecl makeReceiverLocal(JFXClassDeclaration cDecl) {
        return make.VarDef(
                make.Modifiers(Flags.FINAL),
                defs.receiverName,
                make.Ident(interfaceName(cDecl)),
                make.Ident(names._this));
    }

    JCExpression makeDefaultValue(DiagnosticPosition diagPos, TypeMorphInfo tmi) {
        return tmi.getTypeKind() == TYPE_KIND_SEQUENCE ?
                accessEmptySequence(diagPos, tmi.getElementType()) :
            tmi.getRealType() == syms.javafx_StringType ?
                make.Literal("") :
            tmi.getRealType() == syms.javafx_DurationType ?
                makeTimeDefaultValue(diagPos) :
                makeLit(diagPos, tmi.getRealType(), tmi.getDefaultValue());
    }

    JCExpression makeDefaultValue(DiagnosticPosition diagPos, Type type) {
        return makeDefaultValue(diagPos, typeMorpher.typeMorphInfo(type));
    }

    JCExpression makeTimeDefaultValue(DiagnosticPosition diagPos) {
        return makeDurationLiteral(diagPos, makeLit(diagPos, syms.doubleType, 0));
    }

    /** Make an attributed tree representing a literal. This will be
     *  a Literal node.
     *  @param type       The literal's type.
     *  @param value      The literal's value.
     */
    JCExpression makeLit(DiagnosticPosition diagPos, Type type, Object value) {
        int tag = value==null? TypeTags.BOT : type.tag;
        return make.at(diagPos).Literal(tag, value).setType(
            tag == TypeTags.BOT? syms.botType : type.constType(value)); 
    }

    JCExpression makeLocationLocalVariable(TypeMorphInfo tmi,
                                  DiagnosticPosition diagPos,
                                  List<JCExpression> makeArgs) {
        return makeLocationVariable(tmi, diagPos, makeArgs, defs.makeMethodName);
    }

    JCExpression makeLocationWithDefault(TypeMorphInfo tmi,
                                  DiagnosticPosition diagPos) {
        return makeLocationWithDefault(tmi, diagPos, null);
    }

    JCExpression makeLocationWithDefault(TypeMorphInfo tmi,
                                  DiagnosticPosition diagPos,
                                  JCExpression expr) {
        List<JCExpression> makeArgs;
        Name makeMethod;
        if (tmi.getTypeKind() == TYPE_KIND_OBJECT && 
                (tmi.getRealType() == syms.javafx_StringType || tmi.getRealType() == syms.javafx_DurationType)) {
            // This is an object type for which the default is something other than null
            // construct it specifying the default
            makeArgs = List.<JCExpression>of(makeDefaultValue(diagPos, tmi));
            makeMethod = defs.makeWithDefaultMethodName;
        } else {
            makeArgs = List.<JCExpression>nil();
            makeMethod = defs.makeMethodName;
        }
        if (expr != null) {
            makeArgs = makeArgs.append(expr);
        }
        return makeLocationVariable(tmi, diagPos, makeArgs, makeMethod);
    }
    
    JCExpression makeLocation(TypeMorphInfo tmi,
                                  DiagnosticPosition diagPos,
                                  List<JCExpression> makeArgs,
                                  Name makeMethod,
                                  JCExpression locationTypeExp, boolean primitiveSequence) {
        JCFieldAccess makeSelect = make.at(diagPos).Select(locationTypeExp, makeMethod);
        List<JCExpression> typeArgs = null;
        switch (tmi.getTypeKind()) {
            case TYPE_KIND_OBJECT:
                typeArgs = List.of(makeTypeTree(diagPos, tmi.getRealType(), true));
                break;
            case TYPE_KIND_SEQUENCE:
                Type elemType = types.boxedTypeOrType(tmi.getElementType());
                typeArgs = List.of(makeTypeTree(diagPos, elemType, true));
                makeArgs = makeArgs.prepend(makeTypeInfo(diagPos, elemType));
                break;
        }
        return make.at(diagPos).Apply(typeArgs, makeSelect, makeArgs);
    }

    JCExpression makeLocationVariable(TypeMorphInfo tmi,
                                  DiagnosticPosition diagPos,
                                  List<JCExpression> makeArgs,
                                  Name makeMethod) {
        int kind = tmi.getTypeKind();
        Name locName = typeMorpher.variableNCT[kind].name;
        boolean primitiveSequence = kind == TYPE_KIND_SEQUENCE && tmi.getElementType().isPrimitive();
        if (false && primitiveSequence)
            locName = ((ClassSymbol) tmi.getVariableType().tsym).fullname;
        JCExpression locationTypeExp = makeIdentifier(diagPos, locName);
        return makeLocation(tmi, diagPos, makeArgs, makeMethod, locationTypeExp, primitiveSequence);
    }

    JCExpression makeConstantLocation(DiagnosticPosition diagPos, Type type, JCExpression expr) {
        TypeMorphInfo tmi = typeMorpher.typeMorphInfo(type);
        List<JCExpression> makeArgs = List.of(expr);
        JCExpression locationTypeExp = makeTypeTree( diagPos,tmi.getConstantLocationType(), true);
        return makeLocation(tmi, diagPos, makeArgs, defs.makeMethodName, locationTypeExp, false);
    }

    JCExpression makeUnboundLocation(DiagnosticPosition diagPos, TypeMorphInfo tmi, JCExpression expr) {
        List<JCExpression> makeArgs = List.of(expr);
        return makeLocationLocalVariable(tmi, diagPos, makeArgs);
    }

    protected JCExpression makeUnboundLocation(DiagnosticPosition diagPos, Type type, JCExpression expr) {
        return makeUnboundLocation(diagPos, typeMorpher.typeMorphInfo(type), expr);
    }

    JCExpression runtime(DiagnosticPosition diagPos, RuntimeMethod meth, List<JCExpression> args) {
        return runtime(diagPos, meth, null, args);
    }

    JCExpression runtime(DiagnosticPosition diagPos, RuntimeMethod meth, List<JCExpression> typeArgs, List<JCExpression> args) {
        JCExpression select = make.at(diagPos).Select(makeQualifiedTree(diagPos, meth.classString), meth.methodName);
        return make.at(diagPos).Apply(typeArgs, select, args);
    }

    JCMethodInvocation callExpression(DiagnosticPosition diagPos, JCExpression receiver, Name methodName) {
        return callExpression(diagPos, receiver, methodName, null);
    }

    JCMethodInvocation callExpression(DiagnosticPosition diagPos, JCExpression receiver, Name methodName, Object args) {
        JCExpression expr = null;
        if (receiver == null) {
            expr = make.at(diagPos).Ident(methodName);
        } else {
            expr = make.at(diagPos).Select(receiver, methodName);
        }
        return make.at(diagPos).Apply(List.<JCExpression>nil(), expr, (args == null) ? List.<JCExpression>nil() : (args instanceof List) ? (List<JCExpression>) args : (args instanceof ListBuffer) ? ((ListBuffer<JCExpression>) args).toList() : (args instanceof JCExpression) ? List.<JCExpression>of((JCExpression) args) : null);
    }

    JCMethodInvocation callExpression(DiagnosticPosition diagPos, JCExpression receiver, String method) {
        return callExpression(diagPos, receiver, method, null);
    }

    JCMethodInvocation callExpression(DiagnosticPosition diagPos, JCExpression receiver, String method, Object args) {
        return callExpression(diagPos, receiver, names.fromString(method), args);
    }

    JCStatement callStatement(DiagnosticPosition diagPos, JCExpression receiver, Name methodName) {
        return callStatement(diagPos, receiver, methodName, null);
    }

    JCStatement callStatement(DiagnosticPosition diagPos, JCExpression receiver, Name methodName, Object args) {
        return make.at(diagPos).Exec(callExpression(diagPos, receiver, methodName, args));
    }

    JCStatement callStatement(DiagnosticPosition diagPos, Name methodName) {
        return callStatement(diagPos, null, methodName, null);
    }

    JCStatement callStatement(DiagnosticPosition diagPos, Name methodName, Object args) {
        return make.at(diagPos).Exec(callExpression(diagPos, null, methodName, args));
    }

    JCStatement callStatement(DiagnosticPosition diagPos, JCExpression receiver, String method) {
        return callStatement(diagPos, receiver, method, null);
    }

    JCStatement callStatement(DiagnosticPosition diagPos, JCExpression receiver, String method, Object args) {
        return make.at(diagPos).Exec(callExpression(diagPos, receiver, method, args));
    }

    Name functionInterfaceName(MethodSymbol sym, boolean isBound) {
        return functionName(sym, isBound);
    }

    Name functionName(MethodSymbol sym) {
        return functionName(sym, false);
    }

    Name functionName(MethodSymbol sym, String full, boolean markAsImpl, boolean isBound) {
        if (markAsImpl) {
            full = full + JavafxDefs.implFunctionSuffix;
        }
        if (isBound) {
            full = full + JavafxDefs.boundFunctionDollarSuffix + getParameterTypeSuffix(sym);
        }
        return names.fromString(full);
    }

    Name functionName(MethodSymbol sym, boolean isBound) {
        return functionName(sym, false, isBound);
    }

    Name functionName(MethodSymbol sym, boolean markAsImpl, boolean isBound) {
        if (!markAsImpl && !isBound) {
            return sym.name;
        }
        return functionName(sym, sym.name.toString(), markAsImpl, isBound);
    }

    Name varMapName(ClassSymbol sym) {
        String className = sym.fullname.toString();
        return names.fromString(varMapString + className.replace('.', '$'));
    }

    Name varGetMapName(ClassSymbol sym) {
        String className = sym.fullname.toString();
        return names.fromString(varGetMapString + className.replace('.', '$'));
    }

    Name attributeFieldName(Symbol sym) {
        return prefixedAttributeName(sym, "$");
    }

    Name attributeOffsetName(Symbol sym) {
        return prefixedAttributeName(sym, varOffsetString);
    }
    
    Name attributeBitsName(int word) {
        return names.fromString(varBitsString + word);
    }

    Name attributeValueName(Symbol sym) {
        return prefixedAttributeName(sym, varValueString);
    }

    Name attributeLocationName(Symbol sym) {
        return prefixedAttributeName(sym, varLocationString);
    }

    Name attributeGetterName(Symbol sym) {
        return prefixedAttributeName(sym, attributeGetMethodNamePrefix);
    }

    Name attributeSetterName(Symbol sym) {
        return prefixedAttributeName(sym, attributeSetMethodNamePrefix);
    }
    
    Name attributeGetLocationName(Symbol sym) {
        return prefixedAttributeName(sym, attributeGetLocationMethodNamePrefix);
    }
 
    Name attributeApplyDefaultsName(Symbol sym) {
        return prefixedAttributeName(sym, attributeApplyDefaultsMethodNamePrefix);
    }

    private Name prefixedAttributeName(Symbol sym, String prefix) {
        Symbol owner = sym.owner;
        if (!types.isJFXClass(owner)) {
            return sym.name;
        }
        String sname = sym.name.toString();
        // JFXC-2837 - Mixins: script-private vars no longer hidden -- var with same name as
        // var in subclass, but with different type fails
        long flags = sym.flags();
        boolean isStatic = (flags & STATIC) != 0;
        boolean privateAccess = (flags & JavafxFlags.JavafxInstanceVarFlags &
                                         ~(JavafxFlags.SCRIPT_PRIVATE | Flags.PRIVATE)) == 0L;
        if (!isStatic && privateAccess && types.isJFXClass(owner)) {
            // mangle name to hide it
            sname = owner.toString().replace('.', '$') + '$' + sname;
        }
        return names.fromString( prefix + sname );
    }

    private String getParameterTypeSuffix(MethodSymbol sym) {
        StringBuilder sb = new StringBuilder();
        if (sym != null && sym.type != null) {
            Type mtype = sym.type;
            if (sym.type.tag == TypeTags.FORALL) {
                mtype = ((ForAll) mtype).asMethodType();
            }
            if (mtype.tag == TypeTags.METHOD) {
                List<Type> argtypes = ((MethodType) mtype).getParameterTypes();
                int argtypesCount = argtypes.length();
                int counter = 0;
                for (Type argtype : argtypes) {
                    sb.append(escapeTypeName(types.erasure(argtype)));
                    if (counter < argtypesCount - 1) {
                        // Don't append type separator after the last type in the signature.
                        sb.append(defs.escapeTypeChar);
                        // Double separator between type names.
                        sb.append(defs.escapeTypeChar);
                    }
                    counter++;
                }
            }
        }
        return sb.toString();
    }

    JCExpression accessEmptySequence(DiagnosticPosition diagPos, Type elemType) {
        return make.at(diagPos).Select(makeTypeInfo(diagPos, elemType), defs.emptySequenceFieldString);
    }

    private String escapeTypeName(Type type) {
        return type.toString().replace(defs.typeCharToEscape, defs.escapeTypeChar);
    }

    private JCExpression primitiveTypeInfo(DiagnosticPosition diagPos, Name typeName) {
        return make.at(diagPos).Select(makeQualifiedTree(diagPos, typeInfosString), typeName);
    }
    
    /**
     * Given type, return an expression whose value is the corresponding TypeInfo.
     * @param diagPos
     * @param type
     * @return expression representing the TypeInfo of the class
     */
    JCExpression makeTypeInfo(DiagnosticPosition diagPos, Type type) {
        Type ubType = types.unboxedType(type);
        if (ubType.tag != TypeTags.NONE)
            type = ubType;
        if (types.isSameType(type, syms.javafx_BooleanType)) {
            return primitiveTypeInfo(diagPos, syms.booleanTypeName);
        } else if (types.isSameType(type, syms.javafx_CharacterType)) {
            return primitiveTypeInfo(diagPos, syms.charTypeName);
        } else if (types.isSameType(type, syms.javafx_ByteType)) {
            return primitiveTypeInfo(diagPos, syms.byteTypeName);
        } else if (types.isSameType(type, syms.javafx_ShortType)) {
            return primitiveTypeInfo(diagPos, syms.shortTypeName);
        } else if (types.isSameType(type, syms.javafx_IntegerType)) {
            return primitiveTypeInfo(diagPos, syms.integerTypeName);
        } else if (types.isSameType(type, syms.javafx_LongType)) {
            return primitiveTypeInfo(diagPos, syms.longTypeName);
        } else if (types.isSameType(type, syms.javafx_FloatType)) {
            return primitiveTypeInfo(diagPos, syms.floatTypeName);
        } else if (types.isSameType(type, syms.javafx_DoubleType)) {
            return primitiveTypeInfo(diagPos, syms.doubleTypeName);
        } else if (types.isSameType(type, syms.javafx_StringType)) {
            return primitiveTypeInfo(diagPos, syms.stringTypeName);
        } else if (types.isSameType(type, syms.javafx_DurationType)) {
            JCExpression fieldRef = make.at(diagPos).Select(makeTypeTree(diagPos, type), defs.defaultingTypeInfoFieldName);
            // If TYPE_INFO becomes a Location again, ad back this line
            //    fieldRef = getLocationValue(diagPos, fieldRef, TYPE_KIND_OBJECT);
            return fieldRef;
        } else {
            assert !type.isPrimitive();
            List<JCExpression> typeArgs = List.of(makeTypeTree(diagPos, type, true));
            return runtime(diagPos, defs.TypeInfo_getTypeInfo, typeArgs, List.<JCExpression>nil());
        }
    }

    protected Type operationalType(Type srcType) {
        switch (srcType.tag) {
            case TypeTags.BYTE:
            case TypeTags.SHORT:
                return syms.intType;
            default:
                return srcType;
        }
    }

    protected abstract String getSyntheticPrefix();

    Name getSyntheticName(String kind) {
        return names.fromString(getSyntheticPrefix() + syntheticNameCounter++ + kind);
    }

    public Name indexVarName(JFXForExpressionInClause clause) {
        return indexVarName(clause.getVar().getName());
    }
    public Name indexVarName(JFXIdent var) {
        return indexVarName(var.getName());
    }
    private Name indexVarName(Name name) {
        return names.fromString("$indexof$" + name.toString());
    }

    // expr.get()
    JCExpression getLocationValue(DiagnosticPosition diagPos, JCExpression expr, int typeKind) {
        return callExpression(diagPos, expr, defs.locationGetMethodName[typeKind]);
    }

    /**
     * For an attribute "attr" make an access to it via the provided receiver and getter
     *      "receiver.get$attr()"
     * Or direct (prefixed) access is static.
     * If receiver is null, use direct access.
     * */
   JCExpression makeAttributeAccess(DiagnosticPosition diagPos, Symbol attribSym, Name instanceName) {
       JCExpression instanceIdent = instanceName == null? null : make.at(diagPos).Ident(instanceName);
       return callExpression(diagPos, instanceIdent, attributeGetLocationName(attribSym));
   }

    JCIdent makeIdentOfPresetKind(DiagnosticPosition diagPos, Name name, int pkind) {
        AugmentedJCIdent id = new AugmentedJCIdent(name, pkind);
        id.pos = (diagPos == null ? Position.NOPOS : diagPos.getStartPosition());
        return id;
    }

    BlockExprJCBlockExpression makeBlockExpression(DiagnosticPosition diagPos, List<JCStatement> stmts, JCExpression value) {
        BlockExprJCBlockExpression bexpr = new BlockExprJCBlockExpression(0L, stmts, value);
        bexpr.pos = (diagPos == null ? Position.NOPOS : diagPos.getStartPosition());
        return bexpr;
    }

    BlockExprJCBlockExpression makeBlockExpression(DiagnosticPosition diagPos, ListBuffer<JCStatement> stmts, JCExpression value) {
        return makeBlockExpression(diagPos, stmts.toList(), value);
    }

    JCExpression makeLaziness(DiagnosticPosition diagPos, JavafxBindStatus bindStatus) {
        return make.at(diagPos).Literal(TypeTags.BOOLEAN, bindStatus.isLazy()? 1 : 0);
    }

    JCVariableDecl makeTmpLoopVar(DiagnosticPosition diagPos, int initValue) {
        return make.at(diagPos).VarDef(make.at(diagPos).Modifiers(0),
                                       getSyntheticName("loop"),
                                       makeTypeTree(diagPos, syms.intType),
                                       make.at(diagPos).Literal(TypeTags.INT, initValue));
    }

    JCVariableDecl makeTmpVar(DiagnosticPosition diagPos, String rootName, Type type, JCExpression value) {
        return makeTmpVar(diagPos, getSyntheticName(rootName), type, value);
    }

    JCVariableDecl makeTmpVar(DiagnosticPosition diagPos, Name tmpName, Type type, JCExpression value) {
        return make.at(diagPos).VarDef(make.at(diagPos).Modifiers(Flags.FINAL), tmpName, makeTypeTree(diagPos, type), value);
    }

    JCVariableDecl makeTmpVar(DiagnosticPosition diagPos, Type type, JCExpression value) {
        return makeTmpVar(diagPos, "tmp", type, value);
    }

    protected JCModifiers addAccessAnnotationModifiers(DiagnosticPosition diagPos, long flags, JCModifiers mods, List<JCAnnotation> annotations) {
        make.at(diagPos);
        JCModifiers ret = mods;
        if ((flags & Flags.PUBLIC) != 0) {
            annotations = annotations.prepend(make.Annotation(makeIdentifier(diagPos, JavafxSymtab.publicAnnotationClassNameString), List.<JCExpression>nil()));
        }
        else if ((flags & Flags.PRIVATE) != 0) {
            annotations = annotations.prepend(make.Annotation(makeIdentifier(diagPos, JavafxSymtab.privateAnnotationClassNameString), List.<JCExpression>nil()));
        }
        else if ((flags & Flags.PROTECTED) != 0) {
            annotations = annotations.prepend(make.Annotation(makeIdentifier(diagPos, JavafxSymtab.protectedAnnotationClassNameString), List.<JCExpression>nil()));
        }
        else if ((flags & JavafxFlags.SCRIPT_PRIVATE) != 0) {
            annotations = annotations.prepend(make.Annotation(makeIdentifier(diagPos, JavafxSymtab.scriptPrivateAnnotationClassNameString), List.<JCExpression>nil()));
        }
        else {        // otherwise it is package access
            annotations = annotations.prepend(make.Annotation(makeIdentifier(diagPos, JavafxSymtab.packageAnnotationClassNameString), List.<JCExpression>nil()));
        }

        if ((flags & JavafxFlags.PUBLIC_INIT) != 0) {
            annotations = annotations.prepend(make.Annotation(makeIdentifier(diagPos, JavafxSymtab.publicInitAnnotationClassNameString), List.<JCExpression>nil()));
        }
        if ((flags & JavafxFlags.PUBLIC_READ) != 0) {
            annotations = annotations.prepend(make.Annotation(makeIdentifier(diagPos, JavafxSymtab.publicReadAnnotationClassNameString), List.<JCExpression>nil()));
        }

        if ((flags & JavafxFlags.IS_DEF) != 0) {
            annotations = annotations.prepend(make.Annotation(makeIdentifier(diagPos, JavafxSymtab.defAnnotationClassNameString), List.<JCExpression>nil()));
        }

        if ((flags & Flags.STATIC) != 0) {
            annotations = annotations.prepend(make.Annotation(makeIdentifier(diagPos, JavafxSymtab.staticAnnotationClassNameString), List.<JCExpression>nil()));
        }

        if (annotations.nonEmpty()) {
            ret = make.Modifiers(mods.flags, annotations);
        }
        return ret;
    }

    protected JCModifiers addAccessAnnotationModifiers(DiagnosticPosition diagPos, long flags, JCModifiers mods) {
        return addAccessAnnotationModifiers(diagPos, flags, mods, List.<JCAnnotation>nil());
    }

    protected JCModifiers addInheritedAnnotationModifiers(DiagnosticPosition diagPos, long flags, JCModifiers mods) {
        return make.Modifiers(mods.flags, List.of(make.Annotation(makeIdentifier(diagPos, JavafxSymtab.inheritedAnnotationClassNameString), List.<JCExpression>nil())));
    }

    protected void pretty(JCTree tree) {
        OutputStreamWriter osw = new OutputStreamWriter(System.out);
        Pretty pretty = new Pretty(osw, false);
        try {
            pretty.println();
            pretty.print("+++++++++++++++++++++++++++++++++");
            pretty.println();
            pretty.printExpr(tree);
            pretty.println();
            pretty.print("---------------------------------");
            pretty.println();
            osw.flush();
        }catch(Exception ex) {
            System.err.println("Pretty print got: " + ex);
        }
    }

    protected void fxPretty(JFXTree tree) {
        OutputStreamWriter osw = new OutputStreamWriter(System.out);
        JavafxPretty pretty = new JavafxPretty(osw, false);
        try {
            pretty.println();
            pretty.print("+++++++++++++++++++++++++++++++++");
            pretty.println();
            pretty.printExpr(tree);
            pretty.println();
            pretty.print("---------------------------------");
            pretty.println();
            osw.flush();
        }catch(Exception ex) {
            System.err.println("Pretty print got: " + ex);
        }
    }

    // convert time literal to a javafx.lang.Duration object literal
    //TODO: should be eliminated in favor of makeDurationLiteral -- see visitTimeLiteral
    protected JFXFunctionInvocation timeLiteralToDuration(JFXTimeLiteral tree) {
        JFXSelect clsname = (JFXSelect) fxmake.at(tree.pos()).Type(syms.javafx_DurationType);
        clsname.sym = syms.javafx_DurationType.tsym;
        Name valueOf = names.fromString("valueOf");
        JFXSelect meth = fxmake.at(tree.pos).Select(clsname, valueOf);
        meth.sym = syms.javafx_DurationType.tsym.members().lookup(valueOf).sym;
        meth.type = meth.sym.type;
        List<JFXExpression> args = List.<JFXExpression>of(tree.value);
        JFXFunctionInvocation apply = fxmake.at(tree.pos).Apply(List.<JFXExpression>nil(), meth, args);
        apply.type = clsname.type;
        return apply;
    }

    protected JCExpression makeDurationLiteral(DiagnosticPosition diagPos, JCExpression value) {
        JCExpression clsname = makeTypeTree(diagPos, syms.javafx_DurationType);
        Name valueOf = names.fromString("valueOf");
        JCExpression meth = make.at(diagPos).Select(clsname, valueOf);
        List<JCExpression> args = List.of(value);
        JCExpression apply = make.at(diagPos).Apply(List.<JCExpression>nil(), meth, args);
        return apply;
    }
}
