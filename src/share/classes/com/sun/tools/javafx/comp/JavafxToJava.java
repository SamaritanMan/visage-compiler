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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sun.javafx.api.JavafxBindStatus;
import com.sun.javafx.api.tree.SequenceSliceTree;
import com.sun.javafx.api.tree.Tree.JavaFXKind;
import com.sun.tools.mjavac.code.*;
import static com.sun.tools.mjavac.code.Flags.*;
import com.sun.tools.mjavac.code.Type.MethodType;
import com.sun.tools.mjavac.jvm.Target;
import com.sun.tools.mjavac.tree.JCTree;
import com.sun.tools.mjavac.tree.JCTree.JCAnnotation;
import com.sun.tools.mjavac.tree.JCTree.JCBlock;
import com.sun.tools.mjavac.tree.JCTree.JCCase;
import com.sun.tools.mjavac.tree.JCTree.JCCatch;
import com.sun.tools.mjavac.tree.JCTree.JCClassDecl;
import com.sun.tools.mjavac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.mjavac.tree.JCTree.JCExpression;
import com.sun.tools.mjavac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.mjavac.tree.JCTree.JCFieldAccess;
import com.sun.tools.mjavac.tree.JCTree.JCIdent;
import com.sun.tools.mjavac.tree.JCTree.JCMethodDecl;
import com.sun.tools.mjavac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.mjavac.tree.JCTree.JCModifiers;
import com.sun.tools.mjavac.tree.JCTree.JCReturn;
import com.sun.tools.mjavac.tree.JCTree.JCStatement;
import com.sun.tools.mjavac.tree.JCTree.JCTypeParameter;
import com.sun.tools.mjavac.tree.JCTree.JCVariableDecl;
import com.sun.tools.mjavac.tree.TreeInfo;
import com.sun.tools.mjavac.tree.TreeMaker;
import com.sun.tools.mjavac.tree.TreeTranslator;
import com.sun.tools.mjavac.util.Context;
import com.sun.tools.mjavac.util.List;
import com.sun.tools.mjavac.util.ListBuffer;
import com.sun.tools.mjavac.util.Name;
import com.sun.tools.mjavac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.mjavac.util.Position;
import com.sun.tools.javafx.code.FunctionType;
import com.sun.tools.javafx.code.JavafxFlags;
import com.sun.tools.javafx.comp.JavafxAbstractTranslation.Translator;
import com.sun.tools.javafx.comp.JavafxAbstractTranslation.STranslator;
import com.sun.tools.javafx.code.JavafxVarSymbol;
import static com.sun.tools.javafx.code.JavafxVarSymbol.*;
import com.sun.tools.javafx.comp.JavafxAnalyzeClass.*;
import static com.sun.tools.javafx.comp.JavafxDefs.*;
import com.sun.tools.javafx.comp.JavafxInitializationBuilder.*;
import com.sun.tools.javafx.comp.JavafxTypeMorpher.TypeMorphInfo;
import com.sun.tools.javafx.comp.JavafxTypeMorpher.VarMorphInfo;
import com.sun.tools.javafx.comp.JavafxTypeMorpher.VarRepresentation;
import static com.sun.tools.javafx.comp.JavafxTypeMorpher.VarRepresentation.*;
import com.sun.tools.javafx.tree.*;
import static com.sun.tools.javafx.comp.JavafxToJava.Yield.*;
import static com.sun.tools.javafx.comp.JavafxAbstractTranslation.Locationness.*;

/**
 * Translate JavaFX ASTs into Java ASTs
 *
 * @author Robert Field
 * @author Per Bothner
 * @author Lubo Litchev
 */
public class JavafxToJava extends JavafxAbstractTranslation implements JavafxVisitor {
    protected static final Context.Key<JavafxToJava> jfxToJavaKey =
        new Context.Key<JavafxToJava>();

    /*
     * the result of translating a tree by a visit method
     */
    JCTree result;

    /*
     * modules imported by context
     */
    private final JavafxToBound toBound;
    private final JavafxInitializationBuilder initBuilder;
    private final JavafxOptimizationStatistics optStat;

    /*
     * Buffers holding definitions waiting to be prepended to the current list of definitions.
     * At class or top-level these are the same.
     * Within a method (or block) prependToStatements is split off.
     * They need to be different because anonymous classes need to be declared in the scope of the method,
     * but interfaces can't be declared here.
     */
    private ListBuffer<JCStatement> prependToDefinitions = null;
    private ListBuffer<JCStatement> prependToStatements = null;

    private ListBuffer<JCExpression> additionalImports = null;

    private Map<Symbol, Name> substitutionMap = new HashMap<Symbol, Name>();

    private ListBuffer<OnReplaceClosureTranslator> triggers;

    enum ReceiverContext {
        // In a script function or script var init, implemented as a static method
        ScriptAsStatic,
        // In an instance function or instance var init, implemented as static
        InstanceAsStatic,
        // In an instance function or instance var init, implemented as an instance method
        InstanceAsInstance,
        // Should not see code in this state
        Oops
    }
    ReceiverContext inInstanceContext = ReceiverContext.Oops;

    enum Yield {
        ToExpression,
        ToStatement
    }

    private static class TranslationState {
        final Yield yield;
        final Locationness wrapper;
        final Type targetType;
        TranslationState(Yield yield, Locationness wrapper, Type targetType) {
            this.yield = yield;
            this.wrapper = wrapper;
            this.targetType = targetType;
        }
    }

    // Stack used to track literal symbols for the current class.
    LiteralInitClassMap literalInitClassMap = null;

    private TranslationState translationState = null; // should be set before use

    protected JavafxEnv<JavafxAttrContext> attrEnv;
    private Target target;
    boolean inOverrideInstanceVariableDefinition = false;

    /*
     * static information
     */
    private static final String sequenceBuilderString = "com.sun.javafx.runtime.sequence.ObjectArraySequence";
    private static final String boundSequenceBuilderString = "com.sun.javafx.runtime.sequence.BoundSequenceBuilder";
    private static final String noMainExceptionString = "com.sun.javafx.runtime.NoMainException";
    private static final String toSequenceString = "toSequence";
    private static final String methodThrowsString = "java.lang.Throwable";
    JFXClassDeclaration currentClass;  //TODO: this is redundant with attrEnv.enclClass

    /** Class symbols for classes that need a reference to the outer class. */
    Set<ClassSymbol> hasOuters = new HashSet<ClassSymbol>();

    abstract class NullCheckTranslator extends Translator {

        protected final JFXExpression toCheck;
        protected final Type resultType;
        private final boolean needNullCheck;
        private boolean hasSideEffects;
        private ListBuffer<JCStatement> tmpVarList;
        protected final Locationness wrapper;

        NullCheckTranslator(DiagnosticPosition diagPos, JFXExpression toCheck, Type resultType, boolean knownNonNull, Locationness wrapper) {
            super(diagPos);
            this.toCheck = toCheck;
            this.resultType = resultType;
            this.needNullCheck = !knownNonNull && !toCheck.type.isPrimitive() && possiblyNull(toCheck);
            this.hasSideEffects = needNullCheck && hasSideEffects(toCheck);
            this.tmpVarList = ListBuffer.<JCStatement>lb();
            this.wrapper = wrapper;
        }

        abstract JCExpression fullExpression(JCExpression mungedToCheckTranslated);

        abstract JCExpression translateToCheck(JFXExpression expr);

        protected JCExpression preserveSideEffects(Type type, JFXExpression expr, JCExpression trans) {
            if (needNullCheck && expr!=null && hasSideEffects(expr)) {
                // if there is going to be a null check (which thus could keep expr
                // from being evaluated), and expr has side-effects, then eval
                // it first and put it in a temp var.
                return addTempVar(type, trans);
            } else {
                // no side-effects, just pass-through
                return trans;
            }
        }

        protected JCExpression addTempVar(Type varType, JCExpression trans) {
            JCVariableDecl tmpVar = makeTmpVar(diagPos, "pse", varType, trans);
            tmpVarList.append(tmpVar);
            return m().Ident(tmpVar.name);
        }

        private boolean possiblyNull(JFXExpression expr) {
            if (expr == null) {
                return true;
            }
            switch (expr.getFXTag()) {
               case ASSIGN:
                   return possiblyNull(((JFXAssign)expr).getExpression());
               case APPLY:
                   return true;
               case BLOCK_EXPRESSION:
                   return possiblyNull(((JFXBlock)expr).getValue());
               case IDENT: {
                   if (((JFXIdent)expr).sym instanceof VarSymbol) {
                       Symbol sym = ((JFXIdent)expr).sym;
                       return sym.name != names._this && sym.name != names._super;
                   } else {
                       return false;
                   }
               }
               case CONDEXPR:
                   return possiblyNull(((JFXIfExpression)expr).getTrueExpression()) || possiblyNull(((JFXIfExpression)expr).getFalseExpression());
               case LITERAL:
                   return expr.getJavaFXKind() == JavaFXKind.NULL_LITERAL;
               case PARENS:
                   return possiblyNull(((JFXParens)expr).getExpression());
               case SELECT:
                   return ((JFXSelect)expr).sym instanceof VarSymbol;
               case SEQUENCE_INDEXED:
                   return true;
               case TYPECAST:
                   return possiblyNull(((JFXTypeCast)expr).getExpression());
               case VAR_DEF:
                   return possiblyNull(((JFXVar)expr).getInitializer());
                default:
                    return false;
            }
        }

        protected JCTree doit() {
            JCExpression mungedToCheckTranslated = translateToCheck(toCheck);
            JCVariableDecl tmpVar = null;
            if (hasSideEffects) {
                // if the toCheck sub-expression has side-effects, compute it and stash in a
                // temp var so we don't invoke it twice.
                tmpVar = makeTmpVar(diagPos, "toCheck", toCheck.type, mungedToCheckTranslated);
                tmpVarList.append(tmpVar);
                mungedToCheckTranslated = m().Ident(tmpVar.name);
            }
            JCExpression full = fullExpression(mungedToCheckTranslated);
            if (!needNullCheck && tmpVarList.isEmpty()) {
                return full;
            }
            // Do a null check
            JCExpression toTest = hasSideEffects ? m().Ident(tmpVar.name) : translateToCheck(toCheck);
            // we have a testable guard for null, test before the invoke (boxed conversions don't need a test)
            JCExpression cond = m().Binary(JCTree.NE, toTest, make.Literal(TypeTags.BOT, null));
            if (resultType == syms.voidType) {
                // if this is a void expression, check it with an If-statement
                JCStatement stmt = m().Exec(full);
                if (needNullCheck) {
                    stmt = m().If(cond, stmt, null);
                }
                // a statement is the desired result of the translation, return the If-statement
                if (tmpVarList.nonEmpty()) {
                    // if the selector has side-effects, we created a temp var to hold it
                    // so we need to make a block to include the temp var
                    return m().Block(0L, tmpVarList.toList().append(stmt));
                } else {
                    return stmt;
                }
            } else {
                if (needNullCheck) {
                    // it has a non-void return type, convert it to a conditional expression
                    // if it would dereference null, then the full expression instead yields the default value
                    TypeMorphInfo returnTypeInfo = typeMorpher.typeMorphInfo(resultType);
                    JCExpression defaultExpr = makeDefaultValue(diagPos, returnTypeInfo);
                    if (wrapper == AsLocation) {
                        defaultExpr = makeUnboundLocation(diagPos, returnTypeInfo, defaultExpr);
                    }
                    full = m().Conditional(cond, full, defaultExpr);
                }
                if (tmpVarList.nonEmpty()) {
                    // if the selector has side-effects, we created a temp var to hold it
                    // so we need to make a block-expression to include the temp var
                    full = makeBlockExpression(diagPos, tmpVarList.toList(), full);
                }
                return full;
            }
        }
    }

    public static JavafxToJava instance(Context context) {
        JavafxToJava instance = context.get(jfxToJavaKey);
        if (instance == null)
            instance = new JavafxToJava(context);
        return instance;
    }

    protected JavafxToJava(Context context) {
        super(context, null);

        context.put(jfxToJavaKey, this);

        toBound = JavafxToBound.instance(context);
        initBuilder = JavafxInitializationBuilder.instance(context);
        optStat = JavafxOptimizationStatistics.instance(context);
        target = Target.instance(context);
    }

    JCExpression convertTranslated(JCExpression translated, DiagnosticPosition diagPos,
            Type sourceType, Type targetType) {
        assert sourceType != null;
        assert targetType != null;
        if (targetType.tag == TypeTags.UNKNOWN) {
            //TODO: this is bad attribution
            return translated;
        }
        if (types.isSameType(targetType, sourceType)) {
            return translated;
        }
        boolean sourceIsSequence = types.isSequence(sourceType);
        boolean targetIsSequence = types.isSequence(targetType);
        boolean sourceIsArray = types.isArray(sourceType);
        boolean targetIsArray = types.isArray(targetType);
        if (targetIsArray) {
            Type elemType = types.elemtype(targetType);
            if (sourceIsSequence) {
                if (elemType.isPrimitive()) {
                    String mname;
                    if (elemType == syms.byteType)
                        mname = "toByteArray";
                    else if (elemType == syms.shortType)
                        mname = "toShortArray";
                    else if (elemType == syms.intType)
                        mname = "toIntArray";
                    else if (elemType == syms.longType)
                        mname = "toLongArray";
                    else if (elemType == syms.floatType)
                        mname = "toFloatArray";
                    else if (elemType == syms.doubleType)
                        mname = "toDoubleArray";
                    else if (elemType == syms.booleanType)
                        mname = "toBooleanArray";
                    else
                        mname = "toArray";
                    return callExpression(diagPos, makeTypeTree(diagPos, syms.javafx_SequencesType, false),
                            mname, translated);
                }
                ListBuffer<JCStatement> stats = ListBuffer.lb();
                JCVariableDecl tmpVar = makeTmpVar(diagPos, sourceType, translated);
                stats.append(tmpVar);
                JCVariableDecl sizeVar = makeTmpVar(diagPos, syms.intType, callExpression(diagPos, make.at(diagPos).Ident(tmpVar.name), "size"));
                stats.append(sizeVar);
                JCVariableDecl arrVar = makeTmpVar(diagPos, "arr", targetType, make.at(diagPos).NewArray(
                        makeTypeTree(diagPos, elemType, true),
                        List.<JCExpression>of(make.at(diagPos).Ident(sizeVar.name)),
                        null));
                stats.append(arrVar);
                stats.append(callStatement(diagPos, make.at(diagPos).Ident(tmpVar.name), "toArray", List.of(
                        make.Literal(TypeTags.INT, 0),
                        make.at(diagPos).Ident(sizeVar.name),
                        make.at(diagPos).Ident(arrVar.name),
                        make.at(diagPos).Literal(TypeTags.INT, 0))));
                JCIdent ident2 = make.at(diagPos).Ident(arrVar.name);
                return makeBlockExpression(diagPos, stats, ident2);
            } else {
                //TODO: conversion may be needed here, but this is better than what we had
                return translated;
            }
        }
        if (sourceIsArray && targetIsSequence) {
            Type sourceElemType = types.elemtype(sourceType);
            List<JCExpression> args;
            if (sourceElemType.isPrimitive()) {
                args = List.of(translated);
            } else {
                args = List.of(makeTypeInfo(diagPos, sourceElemType), translated);
            }
            JCExpression cSequences = makeTypeTree(diagPos, syms.javafx_SequencesType, false);
            return callExpression(diagPos, cSequences, "fromArray", args);
        }
        if (targetIsSequence && ! sourceIsSequence) {
            //if (sourceType.tag == TypeTags.BOT) {
            //    // it is a null, convert to empty sequence
            //    //TODO: should we leave this null?
            //    Type elemType = types.elemtype(type);
            //    return makeEmptySequenceCreator(diagPos, elemType);
            //}
            Type targetElemType = types.elementType(targetType);
            JCExpression cSequences = makeTypeTree(diagPos, syms.javafx_SequencesType, false);
            translated = convertTranslated(translated, diagPos, sourceType, targetElemType);
            // This would be redundant, if convertTranslated did a cast if needed.
            translated = makeTypeCast(diagPos, targetElemType, sourceType, translated);
            return callExpression(diagPos,
                    cSequences,
                    "singleton",
                    List.of(makeTypeInfo(diagPos, targetElemType), translated));
        }
        if (targetIsSequence && sourceIsSequence) {
            Type sourceElementType = types.elementType(sourceType);
            Type targetElementType = types.elementType(targetType);
            if (!types.isSameType(sourceElementType, targetElementType) &&
                    types.isNumeric(sourceElementType) && types.isNumeric(targetElementType)) {
                return convertNumericSequence(diagPos,
                        translated,
                        sourceElementType,
                        targetElementType);
            }
        }

        // Convert primitive/Object types
        Type unboxedTargetType = targetType.isPrimitive() ? targetType : types.unboxedType(targetType);
        Type unboxedSourceType = sourceType.isPrimitive() ? sourceType : types.unboxedType(sourceType);
        if (unboxedTargetType != Type.noType && unboxedSourceType != Type.noType) {
            // (boxed or unboxed) primitive to (boxed or unboxed) primitive
            if (!sourceType.isPrimitive()) {
                // unboxed source if sourceboxed
                translated = make.at(diagPos).TypeCast(unboxedSourceType, translated);
                sourceType = unboxedSourceType;
            }
            if (unboxedSourceType != unboxedTargetType) {
                // convert as primitive types
                translated = make.at(diagPos).TypeCast(unboxedTargetType, translated);
                sourceType = unboxedTargetType;
            }
            if (!targetType.isPrimitive()) {
                // box target if target boxed
                translated = make.at(diagPos).TypeCast(makeTypeTree(diagPos, targetType, false), translated);
                sourceType = targetType;
            }
        } else {
            if (sourceType.isCompound() || sourceType.isPrimitive()) {
                translated = make.at(diagPos).TypeCast(makeTypeTree(diagPos, types.erasure(targetType), true), translated);
            }
        }
        // We should add a cast "when needed".  Then visitTypeCast would just
        // call this function, and not need to call makeTypeCast on the result.
        // However, getting that to work is a pain - giving up for now.  FIXME
        return translated;
    }

    private JCExpression convertNumericSequence(final DiagnosticPosition diagPos,
            final JCExpression expr, final Type inElementType, final Type targetElementType) {
        JCExpression inTypeInfo = makeTypeInfo(diagPos, inElementType);
        JCExpression targetTypeInfo = makeTypeInfo(diagPos, targetElementType);
        return runtime(diagPos,
                defs.Sequences_convertNumberSequence,
                List.of(targetTypeInfo, inTypeInfo, expr));
    }

    /**
     * Special handling for Strings and Durations. If a value assigned to one of these is null,
     * the default value for the type must be substituted.
     * inExpr is the input expression.  outType is the desired result type.
     * expr is the result value to use in the normal case.
     * This doesn't handle the case   var ss: String = if (true) null else "Hi there, sailor"
     * But it does handle nulls coming in from Java method returns, and variables.
     */
    protected JCExpression convertNullability(final DiagnosticPosition diagPos, final JCExpression expr,
                                              final JFXExpression inExpr, final Type outType) {
        if (outType != syms.stringType && outType != syms.javafx_DurationType) {
            return expr;
        }

        final Type inType = inExpr.type;
        if (inType == syms.botType || inExpr.getJavaFXKind() == JavaFXKind.NULL_LITERAL) {
            return makeDefaultValue(diagPos, outType);
        }

        if (!types.isSameType(inType, outType) || isValueFromJava(inExpr)) {
            JCVariableDecl daVar = makeTmpVar(diagPos, outType, expr);
            JCExpression toTest = make.at(diagPos).Ident(daVar.name);
            JCExpression cond = make.at(diagPos).Binary(JCTree.NE, toTest, make.Literal(TypeTags.BOT, null));
            JCExpression ret = make.at(diagPos).Conditional(
                                                        cond,
                                                        make.at(diagPos).Ident(daVar.name),
                                                        makeDefaultValue(diagPos, outType));
            return makeBlockExpression(diagPos, List.<JCStatement>of(daVar), ret);
        }
        return expr;
    }

    /** Visitor method: Translate a single node.
     */
    @SuppressWarnings("unchecked")
    private <TFX extends JFXTree, TC extends JCTree> TC translateGeneric(TFX tree) {
        if (tree == null) {
            return null;
        } else {
            TC ret;
            JFXTree prevWhere = getAttrEnv().where;
            attrEnv.where = tree;
            tree.accept(this);
            attrEnv.where = prevWhere;
            ret = (TC) this.result;
            this.result = null;
            return ret;
        }
    }

    private JCMethodDecl translate(JFXFunctionDefinition tree) {
        return translateGeneric(tree);
    }

    private JCVariableDecl translate(JFXVar tree) {
        return translateGeneric(tree);
    }

    private JCCompilationUnit translate(JFXScript tree) {
        return translateGeneric(tree);
    }

    private JCTree translate(JFXTree tree) {
        return translateGeneric(tree);
    }

    private JCStatement translateClassDef(JFXClassDeclaration tree) {
        return translateGeneric(tree);
    }

    /** Visitor method: translate a list of nodes.
     */
    private <TFX extends JFXTree, TC extends JCTree> List<TC> translateGeneric(List<TFX> trees) {
        ListBuffer<TC> translated = ListBuffer.lb();
        if (trees == null) {
            return null;
        }
        for (List<TFX> l = trees; l.nonEmpty(); l = l.tail) {
            TC tree = this.<TFX, TC>translateGeneric(l.head);
            if (tree != null) {
                translated.append(tree);
            }
        }
        return translated.toList();
    }

    public List<JCExpression> translateExpressions(List<JFXExpression> trees) {
        return translateGeneric(trees);
    }

    private List<JCCatch> translateCatchers(List<JFXCatch> trees) {
        return translateGeneric(trees);
    }

    private JCBlock translateBlockExpressionToBlock(JFXBlock bexpr) {
        JCStatement stmt = translateToStatement(bexpr);
        return stmt==null? null : asBlock(stmt);
    }

    JCExpression translateToExpression(JFXExpression expr, Locationness wrapper, Type targetType) {
        if (expr == null) {
            return null;
        } else {
            JFXTree prevWhere = getAttrEnv().where;
            attrEnv.where = expr;
            TranslationState prevZ = translationState;
            translationState = new TranslationState(ToExpression, wrapper, targetType);
            expr.accept(this);
            translationState = prevZ;
            attrEnv.where = prevWhere;
            JCExpression ret = (JCExpression)this.result;
            this.result = null;
            return (targetType==null)? ret : convertTranslated(ret, expr.pos(), expr.type, targetType);
        }
    }

    JCExpression translateAsValue(JFXExpression expr, Type targetType) {
        return translateToExpression(expr, AsValue, targetType);
    }

    JCExpression translateAsUnconvertedValue(JFXExpression expr) {
        return translateToExpression(expr, AsValue, null);
    }

    JCExpression translateAsLocation(JFXExpression expr) {
        return translateToExpression(expr, AsLocation, null);
    }

    JCExpression translateAsSequenceVariable(JFXExpression expr) {
        return translateToExpression(expr, AsLocation, null);
    }

    private JCStatement translateToStatement(JFXExpression expr, Type targetType) {
        if (expr == null) {
            return null;
        } else {
            JFXTree prevWhere = getAttrEnv().where;
            attrEnv.where = expr;
            TranslationState prevZ = translationState;
            translationState = new TranslationState(ToStatement, AsValue, targetType);
            expr.accept(this);
            translationState = prevZ;
            attrEnv.where = prevWhere;
            JCTree ret = this.result;
            this.result = null;
            if (ret instanceof JCStatement) {
                return (JCStatement) ret; // already converted
            }
            JCExpression translated = (JCExpression) ret;
            DiagnosticPosition diagPos = expr.pos();
            if (targetType == syms.voidType) {
                return make.at(diagPos).Exec(translated);
            } else {
                JFXVar var = null;
                if (expr instanceof JFXVar) {
                    var = (JFXVar)expr;
                } else if (expr instanceof JFXVarScriptInit) {
                    var = ((JFXVarScriptInit)expr).getVar();
                }

                if ((var != null) && var.isBound()) {
                    translated = getLocationValue(diagPos, translated,
                        TYPE_KIND_OBJECT);
                }
                return make.at(diagPos).Return(convertTranslated(translated, diagPos, expr.type, targetType));
            }
        }
    }

    private JCStatement translateToStatement(JFXExpression expr) {
        return translateToStatement(expr, syms.voidType);
    }

    /**
     * For special cases where the expression may not be fully attributed.
     * Specifically: package and import names.
     * Do a dumb simple conversion.
     *
     * @param tree
     * @return
     */
    private JCExpression straightConvert(JFXExpression tree) {
        if (tree == null) {
            return null;
        }
        DiagnosticPosition diagPos = tree.pos();
        switch (tree.getFXTag()) {
            case IDENT: {
                JFXIdent id = (JFXIdent) tree;
                return make.at(diagPos).Ident(id.name);
            }
            case SELECT: {
                JFXSelect sel = (JFXSelect) tree;
                return make.at(diagPos).Select(
                        straightConvert(sel.getExpression()),
                        sel.getIdentifier());
            }
            default:
                throw new RuntimeException("bad conversion");
        }
    }

    JCBlock asBlock(JCStatement stmt) {
        if (stmt.getTag() == JCTree.BLOCK) {
            return (JCBlock)stmt;
        } else {
            return make.at(stmt).Block(0L, List.of(stmt));
        }
    }

    private boolean substitute(Symbol sym, Locationness wrapper) {
        if (wrapper == AsLocation) {
            return false;
        }
        Name name = substitutionMap.get(sym);
        if (name == null) {
            return false;
        } else {
            result = make.Ident(name);
            return true;
        }
    }

    private void setSubstitution(JFXTree target, Symbol sym) {
        if (target instanceof JFXInstanciate) {
            // Set up to substitute a reference to the this var within its definition
            ((JFXInstanciate) target).varDefinedByThis = sym;
        }
    }

    public void toJava(JavafxEnv<JavafxAttrContext> attrEnv) {
        this.attrEnv = attrEnv;

        attrEnv.translatedToplevel = translate(attrEnv.toplevel);
        attrEnv.translatedToplevel.endPositions = attrEnv.toplevel.endPositions;
    }

    /**
     * Translator for on replace triggers.
     * When possible they become instanciations of the per-script class (with support the add the needed listener code).
     * When not possible, they are generated as in-line anonymous inner classes of ChangeListener.
     */
    private class OnReplaceClosureTranslator extends ScriptClosureTranslator {

        final JFXOnReplace onReplace;
        final boolean isSequence;
        final Type valueType;

        OnReplaceClosureTranslator(JFXOnReplace onReplace, boolean isSequence, Type valueType) {
            super(onReplace.pos(), triggers.size());
            this.onReplace = onReplace;
            this.isSequence = isSequence;
            this.valueType = valueType;
            this.generateInLine = mustGenerateInline(onReplace);
            if (!generateInLine)
                triggers.append(this);
        }

        /**
         * Build the closure body
         * @return if the closure will be generated in-line, the list of class memebers of the closure, otherwise return null
         */
        protected List<JCTree> makeBody() {
            resultStatement = makeResultBlock();
            if (generateInLine) {
                JCTree listenerMethod = makeChangeListenerMethod(diagPos, isSequence, valueType.tag, List.of(resultStatement), valueType);
                members.append(listenerMethod);
                return completeMembers();
            } else {
                // Add to per-script class
                return null;
            }
        }

        /**
         * The class to instanciate that includes the closure
         */
        @Override
        protected JCExpression makeBaseClass() {
            return m().TypeApply(
                    generateInLine? makeIdentifier(diagPos, JavafxDefs.cChangeListener) : super.makeBaseClass(),
                    List.of(makeTypeTree(diagPos, types.boxedTypeOrType(valueType))));
        }

        @Override
        protected List<JCExpression> makeConstructorArgs() {
            if (generateInLine) {
                return List.nil();
            } else {
                return super.makeConstructorArgs();
            }
        }

        /*** handled renames from method parameters and instance parameters ***/

        private void addRenamingVar(ListBuffer<JCStatement> renamings, Name vname, Type type, JCExpression init) {
           renamings.append( m().VarDef(
                    m().Modifiers(Flags.FINAL),
                    vname,
                    makeExpression(type),
                    init));
        }

        private void renamingVar(ListBuffer<JCStatement> renamings, Name name, Name vname, Type type, boolean isLocation) {
            TypeMorphInfo tmi = typeMorpher.typeMorphInfo(type);
            Type varType = isLocation ? tmi.getLocationType() : type;
            FieldInfo rcvrField = new FieldInfo(name.toString(), typeMorpher.typeMorphInfo(varType), false, ArgKind.FREE);
            addRenamingVar(renamings, vname, varType, buildArgField(m().Ident(name), rcvrField));
        }

        private void renameParam(ListBuffer<JCStatement> renamings, Type type, JFXVar var, Name nameDefault) {
            if (var != null) {
                renameParam(renamings, type, var, m().Ident(nameDefault));
            }
        }

        private void renameParam(ListBuffer<JCStatement> renamings, Type type, JFXVar var, JCExpression init) {
            addRenamingVar(renamings, var.getName(), type, m().TypeCast(this.makeExpression(type), init));
        }

        /**
         * Make the block that will live under a switch-case in the per-script class
         * or be the body of the method if it needs to be generated in-line.
         * Also, switch to a static receiver context for translating the body of the trigger
         * since the trigger body will be in what needs to be considered a static context.
         * @return The translated body of the trigger.
         */
        protected JCBlock makeResultBlock() {
            JFXExpression expr = onReplace.getBody();
            ListBuffer<JCStatement> renamings = ListBuffer.<JCStatement>lb();

            ReceiverContext prevContext = inInstanceContext;
            if (!generateInLine) {
                // Set-up receiver$, if needed
                switch (inInstanceContext) {
                    case ScriptAsStatic:
                        // No receiver
                        break;
                    case InstanceAsStatic:
                        renamingVar(renamings, defs.receiverName, defs.receiverName, getAttrEnv().enclClass.type, false);
                        break;
                    case InstanceAsInstance:
                        renamingVar(renamings, names._this, defs.receiverName, getAttrEnv().enclClass.type, false);
                        inInstanceContext = ReceiverContext.InstanceAsStatic;  // We are converting to an effectively static context
                        break;
                    default:
                        assert false : "Bad receiver context";
                        break;
                }

                // Fetch any references to local variables from the instance parameters
                if (expr != null) {
                    for (VarSymbol vsym : localVars(onReplace)) {
                        renamingVar(renamings, vsym.name, vsym.name, vsym.type, representation(vsym)==AlwaysLocation);
                    }
                }
            }

            OnReplaceInfo saveOnReplaceInfo = onReplaceInfo;
            // Rename method parameters to the user specified names
            if (isSequence) {
                OnReplaceInfo info = new OnReplaceInfo();
                info.onReplace = onReplace;
                info.outer = onReplaceInfo;
                onReplaceInfo = info;

                //Type seqValType = types.sequenceType(valueType, false);
                Type seqWithExtendsType = types.sequenceType(valueType, true);
                info.seqWithExtendsType = seqWithExtendsType;
                info.arraySequenceType = types.arraySequenceType(valueType);
                renameParam(renamings, info.arraySequenceType, null, defs.onReplaceArgNameBuffer);
                if (onReplace.getOldValue() != null) {
                    Symbol sym = onReplace.getOldValue().sym;
                    if ((sym.flags_field & JavafxFlags.VARUSE_OPT_TRIGGER) != 0) {// optimized away
                        info.oldValueSym = sym;
                    }
                    else {
                        JCExpression init = callExpression(diagPos, makeTypeTree(diagPos, syms.javafx_SequencesType, false),
                            "getOldValue",
                            List.of(make.Ident(defs.onReplaceArgNameBuffer), make.Ident(defs.onReplaceArgNameOld),
                            make.Ident(defs.onReplaceArgNameFirstIndex), make.Ident(defs.onReplaceArgNameLastIndex)));
                        renameParam(renamings, seqWithExtendsType, onReplace.getOldValue(), init);
                    }
                }
                renameParam(renamings, syms.intType, onReplace.getFirstIndex(), defs.onReplaceArgNameFirstIndex);
                if (onReplace.getLastIndex() != null) {
                    JCExpression last = m().Ident(defs.onReplaceArgNameLastIndex);
                    if (onReplace.getEndKind() == JFXSequenceSlice.END_INCLUSIVE)
                        last = m().Binary(JCTree.MINUS, last, m().Literal(Integer.valueOf(1)));
                    renameParam(renamings, syms.intType, onReplace.getLastIndex(), last);
                }
                if (onReplace.getNewElements() != null) {
                    Symbol sym = onReplace.getNewElements().sym;
                    if ((sym.flags_field & JavafxFlags.VARUSE_OPT_TRIGGER) != 0) {// optimized away
                        info.newElementsSym = sym;
                    }
                    else {
                        JCExpression init = callExpression(diagPos, makeTypeTree(diagPos, syms.javafx_SequencesType, false),
                            "getNewElements",
                            List.of(make.Ident(defs.onReplaceArgNameBuffer), make.Ident(defs.onReplaceArgNameFirstIndex), make.Ident(defs.onReplaceArgNameNewElements)));
                        renameParam(renamings, seqWithExtendsType, onReplace.getNewElements(), init);
                    }
                }
            } else {
                renameParam(renamings, valueType, onReplace.getOldValue(), defs.onReplaceArgNameOld);
                renameParam(renamings, valueType, onReplace.getNewElements(), defs.onReplaceArgNameNew);
            }

            JCStatement translatedTriggerBody = translateToStatement(expr);
            onReplaceInfo = saveOnReplaceInfo;
            inInstanceContext = prevContext;

            // The resulting embedded trigger block is the translated trigger body prefixed by any renamings
            return m().Block(0L, renamings.toList().append(translatedTriggerBody));
        }
    }

    static class OnReplaceInfo {
        public OnReplaceInfo outer;
        JFXOnReplace onReplace;
        Symbol newElementsSym;
        Symbol oldValueSym;
        Type arraySequenceType;
        Type seqWithExtendsType;
    }

    OnReplaceInfo onReplaceInfo;

    OnReplaceInfo findOnReplaceInfo(Symbol sym) {
        OnReplaceInfo info = onReplaceInfo;
        while (info != null && sym != info.newElementsSym && sym != info.oldValueSym)
            info = info.outer;
        return info;
    }

    /**
     * Make a version of the on-replace to be used in inline in a setter.
     */
    private JCStatement translateOnReplaceAsInline(VarSymbol vsym, JFXOnReplace onReplace) {
        if (onReplace == null) return null;
        VarMorphInfo vmi = typeMorpher.varMorphInfo(vsym);
        if (vmi.representation() == VarRepresentation.AlwaysLocation) return null;
        return translateToStatement(onReplace.getBody());
    }

    /**
     * Non-destructive creation of "on change" change listener set-up call.
     */
    private JCStatement makeInstanciateChangeListener(VarSymbol vsym, JFXOnReplace onReplace) {
        if (onReplace == null) return null;
        DiagnosticPosition diagPos = onReplace.pos();
        final boolean isSequence = types.isSequence(vsym.type);

        Name addListenerName;
        Type valueType;
        if (isSequence) {
            addListenerName = defs.addSequenceChangeListenerName;
            valueType = types.elementType(vsym.type);
        } else {
            addListenerName = defs.addChangeListenerName;
            valueType = vsym.type;
        }

        JCExpression onReplaceListener = new OnReplaceClosureTranslator(onReplace, isSequence, valueType).doit();

        JCExpression varRef;
        if (vsym.owner.kind == Kinds.TYP) {
            // on replace is on class variable
            varRef = makeAttributeAccess(diagPos, vsym,
                    inInstanceContext==ReceiverContext.InstanceAsStatic? defs.receiverName : null);
        } else {
            // on replace is on local variable
            varRef = make.at(diagPos).Ident(vsym.name);
        }
        return callStatement(diagPos, varRef, addListenerName, onReplaceListener);
    }

    void scriptBegin() {
        triggers = ListBuffer.lb();
        toBound.scriptBeginBinding();
    }


    boolean hasScriptTriggers() {
        return triggers.nonEmpty();
    }

    List<JCTree> scriptCompleteTriggers(DiagnosticPosition diagPos) {
        if (triggers.isEmpty()) {
            return List.nil();
        } else {
            // Collect all the switch-cases for all the listener types
            ListBuffer<JCCase> sequenceCases = ListBuffer.lb();
            ListBuffer<JCCase> otherCases[] = new ListBuffer[TypeTags.CLASS + 1];
            for (int tag = 0; tag <= TypeTags.CLASS; ++tag) {
                otherCases[tag] = ListBuffer.lb();
            }
            for (OnReplaceClosureTranslator b : triggers) {
                if (!b.generateInLine) {
                    if (b.isSequence) {
                        sequenceCases.append(b.makeCase());
                    } else {
                        otherCases[b.valueType.tag].append(b.makeCase());
                    }
                }
            }

            // Make all the onChange methods
            ListBuffer<JCTree> members = ListBuffer.lb();
            for (int tag = 0; tag <= TypeTags.CLASS; ++tag) {
                if (otherCases[tag].nonEmpty()) {
                    members.append(makeChangeListenerMethod(diagPos, false, tag, otherCases[tag]));
                }
            }
            if (sequenceCases.nonEmpty()) {
                members.append(makeChangeListenerMethod(diagPos, true, TypeTags.ERROR, sequenceCases));
            }

            // Make constructor
            Type objectArrayType = new Type.ArrayType(syms.objectType, syms.arrayClass);
            ListBuffer<JCVariableDecl> params = ListBuffer.lb();
            params.append(makeParam(diagPos, defs.idName, syms.intType));
            params.append(makeParam(diagPos, defs.arg0Name, syms.objectType));
            params.append(makeParam(diagPos, defs.arg1Name, syms.objectType));
            params.append(makeParam(diagPos, defs.moreArgsName, objectArrayType));
            JCStatement cbody = make.Exec(make.Apply(null, make.Ident(names._super), List.<JCExpression>of(
                    make.Ident(defs.idName),
                    make.Ident(defs.arg0Name),
                    make.Ident(defs.arg1Name),
                    make.Ident(defs.moreArgsName)
                    )));
            JCTree constr = makeMethod(diagPos, names.init, List.of(cbody), params.toList(), syms.voidType, Flags.PRIVATE);
            members.append(constr);

            return members.toList();
        }
    }

    /*private JCExpression makeSequenceTypeExpression(DiagnosticPosition diagPos, Type concreteType) {
        return make.at(diagPos).TypeApply(
                makeIdentifier(diagPos, JavafxDefs.cSequence),
                List.<JCExpression>of(
                       concreteType==null?
                           make.at(diagPos).Ident(defs.typeParamName) :
                           makeTypeTree(diagPos, concreteType)));
    }*/

    private JCExpression makeSequenceWithExtendsTypeExpression(DiagnosticPosition diagPos, Type concreteType) {
        return make.at(diagPos).TypeApply(
                makeIdentifier(diagPos, JavafxDefs.cSequence),
                List.<JCExpression>of(
                       concreteType==null?
                           make.at(diagPos).Wildcard(make.at(diagPos).TypeBoundKind(BoundKind.EXTENDS), make.at(diagPos).Ident(defs.typeParamName)) :
                           makeTypeTree(diagPos, concreteType)));
    }

    private JCExpression makeArraySequenceTypeExpression(DiagnosticPosition diagPos, Type concreteType) {
        return make.at(diagPos).TypeApply(
                makeIdentifier(diagPos, JavafxDefs.arraySequence),
                List.<JCExpression>of(
                       concreteType==null?
                           make.at(diagPos).Ident(defs.typeParamName) :
                           makeTypeTree(diagPos, concreteType)));
    }

    private JCExpression makeTypeExpression(DiagnosticPosition diagPos, int typeTag, Type concreteType) {
        switch (typeTag) {
            case TypeTags.BYTE:
            case TypeTags.CHAR:
            case TypeTags.SHORT:
            case TypeTags.INT:
            case TypeTags.LONG:
            case TypeTags.FLOAT:
            case TypeTags.DOUBLE:
            case TypeTags.BOOLEAN:
                return make.at(diagPos).TypeIdent(typeTag);
            case TypeTags.CLASS:
                return
                       concreteType==null?
                           make.at(diagPos).Ident(defs.typeParamName) :
                           makeTypeTree(diagPos, concreteType);
            default:
                throw new AssertionError("should not reach here");
        }
    }

    /**
     * construct a change listener method for insertion in a listener class from a list of cases clauses.
     */
    //TODO: this is evil there are two, almost identical, versions of this method
    private JCMethodDecl makeChangeListenerMethod(
            DiagnosticPosition diagPos,
            boolean isSequence,
            int typeTag,
            ListBuffer<JCCase> cases) {
        List<JCExpression> superArgs = isSequence?
                List.<JCExpression>of(
                    make.Ident(defs.onReplaceArgNameBuffer),
                    make.Ident(defs.onReplaceArgNameOld),
                    make.Ident(defs.onReplaceArgNameFirstIndex),
                    make.Ident(defs.onReplaceArgNameLastIndex),
                    make.Ident(defs.onReplaceArgNameNewElements))
                :
               List.<JCExpression>of(
                    make.Ident(defs.onReplaceArgNameOld),
                    make.Ident(defs.onReplaceArgNameNew));
        JCCase forward = make.at(diagPos).Case(null, List.<JCStatement>of(
                callStatement(diagPos, make.at(diagPos).Ident(names._super), defs.onChangeMethodName, superArgs),
                make.at(diagPos).Break(null)));
        JCStatement swit = make.at(diagPos).Switch(make.at(diagPos).Ident(defs.bindingIdName), cases.toList().append(forward));
        return makeChangeListenerMethod(
                diagPos,
                isSequence,
                typeTag,
                List.of(swit),
                null);
    }

    /**
     * construct a change listener method for insertion in a listener class from a list of statements which make up the body.
     */
    private JCMethodDecl makeChangeListenerMethod(
            DiagnosticPosition diagPos,
            boolean isSequence,
            int typeTag,
            List<JCStatement> stmts,
            Type concreteType) {
        List<JCVariableDecl> onChangeArgs = isSequence?
            List.of(
                    makeParam(diagPos, defs.onReplaceArgNameBuffer, makeArraySequenceTypeExpression(diagPos, concreteType)),
                    makeParam(diagPos, defs.onReplaceArgNameOld, makeSequenceWithExtendsTypeExpression(diagPos, concreteType)),
                    makeParam(diagPos, defs.onReplaceArgNameFirstIndex, makeTypeExpression(diagPos, TypeTags.INT, null)),
                    makeParam(diagPos, defs.onReplaceArgNameLastIndex, makeTypeExpression(diagPos, TypeTags.INT, null)),
                    makeParam(diagPos, defs.onReplaceArgNameNewElements, makeSequenceWithExtendsTypeExpression(diagPos, concreteType)))
        :
            List.of(
                    makeParam(diagPos, defs.onReplaceArgNameOld, makeTypeExpression(diagPos, typeTag, concreteType)),
                    makeParam(diagPos, defs.onReplaceArgNameNew, makeTypeExpression(diagPos, typeTag, concreteType)));

        return make.at(diagPos).MethodDef(
                make.at(diagPos).Modifiers(Flags.PUBLIC),
                defs.onChangeMethodName,
                make.at(diagPos).TypeIdent(TypeTags.VOID),
                List.<JCTypeParameter>nil(),
                onChangeArgs,
                List.<JCExpression>nil(),
                make.at(diagPos).Block(0L, stmts),
                null);
    }

    /**
     * Add per-script BindingExpression/ChangeListener class, is needed
     * @param diagPos
     * @return
     */
    List<JCTree> scriptComplete(DiagnosticPosition diagPos) {
        if (!toBound.hasScriptBinding() && !hasScriptTriggers()) {
            return List.nil();
        } else {
            JCExpression scriptClosureClass = make.at(diagPos).TypeApply(
                    makeIdentifier(diagPos, JavafxDefs.baseBindingListenerClassString),
                    List.<JCExpression>of(make.at(diagPos).Ident(defs.typeParamName)));
            JCTypeParameter typeParam = make.at(diagPos).TypeParameter(defs.typeParamName, List.<JCExpression>nil());

            List<JCTree> bindingMembers = toBound.scriptCompleteBinding(diagPos);
            List<JCTree> triggerMembers = scriptCompleteTriggers(diagPos);
            JCClassDecl bindingClass = make.at(diagPos).ClassDef(
                    make.at(diagPos).Modifiers(Flags.PRIVATE | Flags.STATIC),
                    defs.scriptBindingClassName,
                    List.of(typeParam),
                    scriptClosureClass,
                    List.<JCExpression>nil(),
                    bindingMembers.appendList(triggerMembers));
            return List.<JCTree>of(bindingClass);
        }
    }

    //@Override
    public void visitScript(JFXScript tree) {
        // add to the hasOuters set the class symbols for classes that need a reference to the outer class
        fillClassesWithOuters(tree);

        ListBuffer<JCTree> translatedDefinitions = ListBuffer.lb();
        ListBuffer<JCTree> imports = ListBuffer.lb();
        additionalImports = ListBuffer.lb();
        prependToStatements = prependToDefinitions = ListBuffer.lb();
        for (JFXTree def : tree.defs) {
            if (def.getFXTag() != JavafxTag.IMPORT) {
                // anything but an import
                translatedDefinitions.append(translate(def));
            }
        }

       // order is imports, any prepends, then the translated non-imports
        for (JCTree prepend : prependToDefinitions) {
            translatedDefinitions.prepend(prepend);
        }

        for (JCTree prepend : imports) {
            translatedDefinitions.prepend(prepend);
        }

        for (JCExpression prepend : additionalImports) {
            translatedDefinitions.append(make.Import(prepend, false));
        }

        prependToStatements = prependToDefinitions = null; // shouldn't be used again until the next top level

        JCExpression pid = straightConvert(tree.pid);
        JCCompilationUnit translated = make.at(tree.pos).TopLevel(List.<JCAnnotation>nil(), pid, translatedDefinitions.toList());
        translated.sourcefile = tree.sourcefile;
        //System.err.println("<translated src="+tree.sourcefile+">"); System.err.println(translated); System.err.println("</translated>");
        translated.docComments = null;
        translated.lineMap = tree.lineMap;
        translated.flags = tree.flags;
        result = translated;
    }

    //@Override
    public void visitClassDeclaration(JFXClassDeclaration tree) {
        JFXClassDeclaration prevClass = currentClass;
        JFXClassDeclaration prevEnclClass = getAttrEnv().enclClass;
        currentClass = tree;

        if (tree.isScriptClass) {
            scriptBegin();
        }

        try {
            DiagnosticPosition diagPos = tree.pos();
            if (tree.isScriptClass) literalInitClassMap = new LiteralInitClassMap();

            attrEnv.enclClass = tree;

            ListBuffer<JCStatement> translatedInitBlocks = ListBuffer.lb();
            ListBuffer<JCStatement> translatedPostInitBlocks = ListBuffer.lb();
            ListBuffer<JCTree> translatedDefs = ListBuffer.lb();
            ListBuffer<TranslatedVarInfo> attrInfo = ListBuffer.lb();
            ListBuffer<TranslatedOverrideClassVarInfo> overrideInfo = ListBuffer.lb();
            boolean isMixinClass = tree.isMixinClass();

           // translate all the definitions that make up the class.
           // collect any prepended definitions, and prepend then to the tranlations
            ListBuffer<JCStatement> prevPrependToDefinitions = prependToDefinitions;
            ListBuffer<JCStatement> prevPrependToStatements = prependToStatements;
            prependToStatements = prependToDefinitions = ListBuffer.lb();
            {
                for (JFXTree def : tree.getMembers()) {
                    switch(def.getFXTag()) {
                        case INIT_DEF: {
                            inInstanceContext = isMixinClass? ReceiverContext.InstanceAsStatic : ReceiverContext.InstanceAsInstance;
                            translateAndAppendStaticBlock(((JFXInitDefinition) def).getBody(), translatedInitBlocks);
                            inInstanceContext = ReceiverContext.Oops;
                            break;
                        }
                        case POSTINIT_DEF: {
                            inInstanceContext = isMixinClass? ReceiverContext.InstanceAsStatic : ReceiverContext.InstanceAsInstance;
                            translateAndAppendStaticBlock(((JFXPostInitDefinition) def).getBody(), translatedPostInitBlocks);
                            inInstanceContext = ReceiverContext.Oops;
                            break;
                        }
                        case VAR_DEF: {
                            JFXVar attrDef = (JFXVar) def;
                            boolean isStatic = (attrDef.getModifiers().flags & STATIC) != 0;
                            inInstanceContext = isStatic? ReceiverContext.ScriptAsStatic : isMixinClass? ReceiverContext.InstanceAsStatic : ReceiverContext.InstanceAsInstance;
                            JCStatement init = (!isStatic || getAttrEnv().toplevel.isLibrary)?
                                translateDefinitionalAssignmentToSet(attrDef.pos(),
                                attrDef.getInitializer(), attrDef.getBindStatus(), attrDef.sym,
                                isStatic? null : defs.receiverName)
                                : null;
                            attrInfo.append(new TranslatedVarInfo(
                                    attrDef,
                                    typeMorpher.varMorphInfo(attrDef.sym),
                                    init,
                                    getterInit(attrDef.sym, attrDef.getInitializer()),
                                    attrDef.getOnReplace(),
                                    translateOnReplaceAsInline(attrDef.sym, attrDef.getOnReplace()),
                                    makeInstanciateChangeListener(attrDef.sym, attrDef.getOnReplace())));
                            inInstanceContext = ReceiverContext.Oops;
                            break;
                        }
                        case OVERRIDE_ATTRIBUTE_DEF: {
                            JFXOverrideClassVar override = (JFXOverrideClassVar) def;
                            boolean isStatic = (override.sym.flags() & STATIC) != 0;
                            inInstanceContext = isStatic? ReceiverContext.ScriptAsStatic : isMixinClass? ReceiverContext.InstanceAsStatic : ReceiverContext.InstanceAsInstance;
                            JCStatement init;
                            inOverrideInstanceVariableDefinition = true;
                            init = translateDefinitionalAssignmentToSet(override.pos(),
                                    override.getInitializer(), override.getBindStatus(), override.sym,
                                    isStatic ? null : defs.receiverName);
                            inOverrideInstanceVariableDefinition = false;
                            overrideInfo.append(new TranslatedOverrideClassVarInfo(
                                    override,
                                    typeMorpher.varMorphInfo(override.sym),
                                    init,
                                    getterInit(override.sym, override.getInitializer()),
                                    override.getOnReplace(),
                                    makeInstanciateChangeListener(override.sym, override.getOnReplace())));
                            inInstanceContext = ReceiverContext.Oops;
                            break;
                        }
                       case FUNCTION_DEF : {
                           JFXFunctionDefinition funcDef = (JFXFunctionDefinition) def;
                           translatedDefs.append(translate(funcDef));
                           break;
                        }
                        default: {
                            JCTree tdef =  translate(def);
                            if ( tdef != null ) {
                                translatedDefs.append( tdef );
                            }
                            break;
                        }
                    }
                }
            }
            // the translated defs have prepends in front
            for (JCTree prepend : prependToDefinitions) {
                translatedDefs.prepend(prepend);
            }
            prependToDefinitions = prevPrependToDefinitions ;
            prependToStatements = prevPrependToStatements;
            // WARNING: translate can't be called directly or indirectly after this point in the method, or the prepends won't be included

            JavafxClassModel model = initBuilder.createJFXClassModel(tree, attrInfo.toList(), overrideInfo.toList(), literalInitClassMap);
            additionalImports.appendList(model.additionalImports);

            // include the interface only once
            if (!tree.hasBeenTranslated) {
                if (isMixinClass) {
                    JCModifiers mods = make.Modifiers(Flags.PUBLIC | Flags.INTERFACE);
                    mods = addAccessAnnotationModifiers(diagPos, tree.mods.flags, mods);

                    JCClassDecl cInterface = make.ClassDef(mods,
                            model.interfaceName, List.<JCTypeParameter>nil(), null,
                            model.interfaces, model.iDefinitions);

                    prependToDefinitions.append(cInterface); // prepend to the enclosing class or top-level
                }
                tree.hasBeenTranslated = true;
            }

            translatedDefs.appendList(model.additionalClassMembers);

            ClassSymbol superClassSym = model.superClassSym;
            List<ClassSymbol> immediateMixins = model.immediateMixins;
            boolean forceInit = !immediateMixins.isEmpty() || superClassSym == null || isMixinClass;

            if (forceInit || !translatedInitBlocks.isEmpty()) {
                // Add the userInit$ method
                List<JCVariableDecl> receiverVarDeclList = isMixinClass? List.of(makeReceiverParam(tree)) : List.<JCVariableDecl>nil();
                ListBuffer<JCStatement> initStats = ListBuffer.lb();

                // Mixin super calls will be handled when inserted into real classes.
                if (!isMixinClass) {
                    //TODO:
                    // Some implementation code is still generated assuming a receiver parameter.  Until this is fixed
                    //    var receiver = this;
                    initStats.prepend(
                            make.at(diagPos).VarDef(
                            make.at(diagPos).Modifiers(Flags.FINAL),
                            defs.receiverName,
                            make.Ident(initBuilder.interfaceName(currentClass)),
                            make.at(diagPos).Ident(names._this))
                            );

                    if (superClassSym != null) {
                        List<JCExpression> args1 = List.nil();
                        //args1 = args1.append(make.TypeCast(makeTypeTree(diagPos, superClassSym.type, true), make.Ident(names._this)));
                        initStats = initStats.append(callStatement(tree.pos(), makeIdentifier(diagPos, names._super), defs.userInitName, args1));
                    }

                    for (ClassSymbol mixinClassSym : immediateMixins) {
                        String mixinName = mixinClassSym.fullname.toString();
                        List<JCExpression> args1 = List.nil();
                        args1 = args1.append(make.TypeCast(makeTypeTree(diagPos, mixinClassSym.type, true), make.Ident(names._this)));
                        initStats = initStats.append(callStatement(tree.pos(), makeIdentifier(diagPos, mixinName), defs.userInitName, args1));
                    }
                }

                initStats.appendList(translatedInitBlocks);

                // Only create method if necessary (rely on FXBase.)
                if (initStats.nonEmpty() || isMixinClass || superClassSym == null) {
                    JCBlock userInitBlock = make.Block(0L, initStats.toList());
                    translatedDefs.append(make.MethodDef(
                            make.Modifiers(!isMixinClass ? Flags.PUBLIC : (Flags.PUBLIC | Flags.STATIC)),
                            defs.userInitName,
                            makeTypeTree( null,syms.voidType),
                            List.<JCTypeParameter>nil(),
                            receiverVarDeclList,
                            List.<JCExpression>nil(),
                            userInitBlock,
                            null));
                }
            }

            if (forceInit || !translatedPostInitBlocks.isEmpty()) {
                // Add the userPostInit$ method
                List<JCVariableDecl> receiverVarDeclList = isMixinClass? List.of(makeReceiverParam(tree)) : List.<JCVariableDecl>nil();
                ListBuffer<JCStatement> initStats = ListBuffer.lb();

                // Mixin super calls will be handled when inserted into real classes.
                if (!isMixinClass) {
                    //TODO:
                    // Some implementation code is still generated assuming a receiver parameter.  Until this is fixed
                    //    var receiver = this;
                    initStats.prepend(
                            make.at(diagPos).VarDef(
                            make.at(diagPos).Modifiers(Flags.FINAL),
                            defs.receiverName,
                            make.Ident(initBuilder.interfaceName(currentClass)),
                            make.at(diagPos).Ident(names._this))
                            );

                    if (superClassSym != null) {
                        List<JCExpression> args1 = List.nil();
                        //args1 = args1.append(make.TypeCast(makeTypeTree(diagPos, superClassSym.type, true), make.Ident(names._this)));
                        initStats = initStats.append(callStatement(tree.pos(), makeIdentifier(diagPos, names._super), defs.postInitName, args1));
                    }

                    for (ClassSymbol mixinClassSym : immediateMixins) {
                        String mixinName = mixinClassSym.fullname.toString();
                        List<JCExpression> args1 = List.nil();
                        args1 = args1.append(make.TypeCast(makeTypeTree(diagPos, mixinClassSym.type, true), make.Ident(names._this)));
                        initStats = initStats.append(callStatement(tree.pos(), makeIdentifier(diagPos, mixinName), defs.postInitName, args1));
                    }
                }

                initStats.appendList(translatedPostInitBlocks);

                // Only create method if necessary (rely on FXBase.)
                if (initStats.nonEmpty() || isMixinClass || superClassSym == null) {
                    JCBlock postInitBlock = make.Block(0L, initStats.toList());
                    translatedDefs.append(make.MethodDef(
                            make.Modifiers(!isMixinClass ? Flags.PUBLIC : (Flags.PUBLIC | Flags.STATIC)),
                            defs.postInitName,
                            makeTypeTree( null,syms.voidType),
                            List.<JCTypeParameter>nil(),
                            receiverVarDeclList,
                            List.<JCExpression>nil(),
                            postInitBlock,
                            null));
                }
            }

            if (tree.isScriptClass) {
                if (!isMixinClass) {
                   // JFXC-1888: Do *not* add main method!
                   // com.sun.javafx.runtime.Main has the
                   // "main" that will call Entry.start().
                   // translatedDefs.append(makeMainMethod(diagPos, tree.getName()));
                }

                // Add binding support
                translatedDefs.appendList(scriptComplete(tree.pos()));
            }

            // build the list of implemented interfaces
            List<JCExpression> implementing = model.interfaces;

            // Class must be visible for reflection.
            long flags = tree.mods.flags & (Flags.FINAL | Flags.ABSTRACT | Flags.SYNTHETIC);
            if ((flags & Flags.SYNTHETIC) == 0) {
                flags |= Flags.PUBLIC;
            }
            if (tree.sym.owner.kind == Kinds.TYP) {
                flags |= Flags.STATIC;
            }
            // JFXC-2831 - Mixins should be abstract.
            if (tree.sym.kind == Kinds.TYP && isMixinClass) {
                flags |= Flags.ABSTRACT;
            }

            JCModifiers classMods = make.at(diagPos).Modifiers(flags);
            classMods = addAccessAnnotationModifiers(diagPos, tree.mods.flags, classMods);

            // make the Java class corresponding to this FX class, and return it
            JCClassDecl res = make.at(diagPos).ClassDef(
                    classMods,
                    tree.getName(),
                    List.<JCTypeParameter>nil(), // type parameters
                    model.superType==null? null : makeTypeTree( null,model.superType, false),
                    implementing,
                    translatedDefs.toList());
            res.sym = tree.sym;
            res.type = tree.type;
            result = res;
        }
        finally {
            attrEnv.enclClass = prevEnclClass;

            currentClass = prevClass;
        }
    }
    //where
    private void translateAndAppendStaticBlock(JFXBlock block, ListBuffer<JCStatement> translatedBlocks) {
        JCStatement stmt = translateToStatement(block);
        if (stmt != null) {
            translatedBlocks.append(stmt);
        }
    }
    //where
    private JCExpression getterInit(VarSymbol vsym, JFXExpression expr) {
        if (expr == null) {
            return null;
        }
        VarMorphInfo vmi = typeMorpher.varMorphInfo(vsym);
        if ((vsym.flags() & JavafxFlags.VARUSE_BOUND_DEFINITION) != 0 && vmi.representation() != VarRepresentation.AlwaysLocation) {
            // This is a Slacker Binding, translate for the in-lined getter
            return translateAsValue(expr, vmi.getRealType());
        }
        return null;
    }

    //@Override
    public void visitInitDefinition(JFXInitDefinition tree) {
        assert false : "should be processed by class tree";
    }

    public void visitPostInitDefinition(JFXPostInitDefinition tree) {
        assert false : "should be processed by class tree";
    }

    abstract static class NewInstanceTranslator extends STranslator {

        // Statements for the initialization steps.
        protected ListBuffer<JCStatement> stats = ListBuffer.lb();

        // Statements to set symbols with initial values.
        protected ListBuffer<JCStatement> varInits = ListBuffer.lb();

        // Symbols corresponding to caseStats.
        protected ListBuffer<VarSymbol> varSyms = ListBuffer.lb();

        NewInstanceTranslator(DiagnosticPosition diagPos, JavafxToJava toJava) {
            super(diagPos, toJava);
        }

        /**
         * Initialize the instance variables of the instance
         * @param instName
         */
        protected abstract void initInstanceVariables(Name instName);

        /**
         * @return the constructor args -- translating any supplied args
         */
        protected abstract List<JCExpression> completeTranslatedConstructorArgs();

        protected JCExpression translateInstanceVariableInit(JFXExpression init, JavafxBindStatus bindStatus, VarSymbol vsym) {
            VarMorphInfo vmi = toJava.typeMorpher.varMorphInfo(vsym);
            return toJava.translateDefinitionalAssignmentToValueArg(init.pos(), init, bindStatus, vmi);
        }

        void setInstanceVariable(DiagnosticPosition diagPos, Name instName, JavafxBindStatus bindStatus, VarSymbol vsym, JCExpression transInit) {
            //TODO: should not be calling definitionalAssignmentToSetExpression, instead should be translateDefinitionalAssignmentToSetExpression
            varInits.append(m().Exec( toJava.definitionalAssignmentToSetExpression(diagPos, transInit, bindStatus, instName,
                                                     toJava.typeMorpher.varMorphInfo(vsym)) ) );
            varSyms.append(vsym);
        }

        void setInstanceVariable(DiagnosticPosition diagPos, Name instName, VarSymbol vsym, JCExpression transInit) {
            setInstanceVariable(diagPos, instName, JavafxBindStatus.UNBOUND, vsym, transInit);
        }

        void setInstanceVariable(Name instName, JavafxBindStatus bindStatus, VarSymbol vsym, JFXExpression init) {
            DiagnosticPosition initPos = init.pos();
            JCExpression transInit = translateInstanceVariableInit(init, bindStatus, vsym);
            setInstanceVariable(initPos, instName, bindStatus, vsym, transInit);
        }

        void setInstanceVariable(Name instName, VarSymbol vsym, JFXExpression init) {
            setInstanceVariable(instName, JavafxBindStatus.UNBOUND, vsym, init);
        }

        void makeInitSupportCall(Name methName, Name receiverName) {
            JCExpression receiver = m().Ident(receiverName);
            JCStatement callExec = toJava.callStatement(diagPos, receiver, methName, List.<JCExpression>nil());
            stats.append(callExec);
        }

        void makeInitApplyDefaults(Type classType, Name receiverName) {
            ClassSymbol classSym = (ClassSymbol)classType.tsym;
            int count = varSyms.size();

            JCVariableDecl loopVar = toJava.makeTmpLoopVar(diagPos, 0);
            Name loopName = loopVar.name;
            JCExpression loopLimit = m().Apply(null, m().Select(m().Ident(receiverName), names.fromString(attributeCountMethodString)),
                                               List.<JCExpression>nil());
            JCVariableDecl loopLimitVar = toJava.makeTmpVar(diagPos, "count", syms.intType, loopLimit);
            stats.append(loopLimitVar);
            JCExpression loopTest = m().Binary(JCTree.LT, m().Ident(loopName), m().Ident(loopLimitVar.name));
            List<JCExpressionStatement> loopStep = List.of(m().Exec(m().Assignop(JCTree.PLUS_ASG, m().Ident(loopName), m().Literal(TypeTags.INT, 1))));
            JCStatement loopBody;

            List<JCExpression> args = List.<JCExpression>of(m().Ident(loopName));
            JCStatement applyDefaultsExpr = toJava.callStatement(diagPos, m().Ident(receiverName), defs.applyDefaultsPrefixName, args);

            if (1 < count) {
                // final short[] jfx$0map = GETMAP$X();
                JCExpression getmapExpr = m().Apply(null, m().Ident(toJava.varGetMapName(classSym)), List.<JCExpression>nil());
                JCVariableDecl mapVar = toJava.makeTmpVar(diagPos, "map", syms.javafx_ShortArray, getmapExpr);
                stats.append(mapVar);

                LiteralInitVarMap varMap = toJava.literalInitClassMap.getVarMap(classSym);
                int[] tags = new int[count];

                int index = 0;
                for (VarSymbol varSym : varSyms.toList()) {
                    tags[index++] = varMap.addVar(varSym);
                }

                ListBuffer<JCCase> cases = ListBuffer.lb();
                index = 0;
                for (JCStatement varInit : varInits) {
                    cases.append(m().Case(m().Literal(TypeTags.INT, tags[index++]), List.<JCStatement>of(varInit, m().Break(null))));
                }

                cases.append(m().Case(null, List.<JCStatement>of(applyDefaultsExpr, m().Break(null))));

                JCExpression mapExpr = m().Indexed(m().Ident(mapVar.name), m().Ident(loopName));
                loopBody = m().Switch(mapExpr, cases.toList());
            } else {
                VarSymbol varSym = varSyms.first();
                JCExpression varOffsetExpr = m().Select(toJava.makeTypeTree(diagPos, classType, false), toJava.attributeOffsetName(varSym));
                JCVariableDecl offsetVar = toJava.makeTmpVar(diagPos, "off", syms.intType, varOffsetExpr);
                stats.append(offsetVar);
                JCExpression condition = m().Binary(JCTree.EQ, m().Ident(loopName), m().Ident(offsetVar.name));
                loopBody = m().If(condition, varInits.first(), applyDefaultsExpr);
            }

            stats.append(m().ForLoop(List.<JCStatement>of(loopVar), loopTest, loopStep, loopBody));
        }

        /**
         * Return the instance building expression
         * @param declaredType
         * @param cdef
         * @param isFX
         * @return
         */
        protected JCExpression buildInstance(Type declaredType, JFXClassDeclaration cdef, boolean isFX) {
            Type type;

            if (cdef == null) {
                type = declaredType;
            } else {
                stats.append(toJava.translateClassDef(cdef));
                type = cdef.type;
            }
            JCExpression classTypeExpr = toJava.makeTypeTree(diagPos, type, false);

            List<JCExpression> newClassArgs = completeTranslatedConstructorArgs();

            Name tmpVarName = toJava.getSyntheticName("objlit");
            initInstanceVariables(tmpVarName);  // Must preceed varSyms.nonEmpty() test

            JCExpression instExpression;
            if (varSyms.nonEmpty() || (isFX && newClassArgs.nonEmpty()) || cdef != null) {
                // it is a instanciation of a JavaFX class which has instance variable initializers
                // (or is anonymous, or has an outer class argument)
                //
                //   {
                //       final X jfx$0objlit = new X(true);
                //       jfx$0objlit.addTriggers$();
                //       final short[] jfx$0map = GETMAP$X();
                //
                //       for (int jfx$0initloop = 0; i < X.$VAR_COUNT; i++) {
                //           if (!isInitialized(jfx$0initloop) {
                //               switch (jfx$0map[jfx$0initloop]) {
                //                   1: jfx$0objlit.set$a(0); break;
                //                   2: jfx$0objlit.set$b(0); break;
                //                   ...
                //                   n: jfx$0objlit.set$z(0); break;
                //                   default: jfx$0objlit.applyDefaults$(jfx$0initloop);
                //               }
                //           }
                //       }
                //
                //       jfx$0objlit.complete$();
                //       jfx$0objlit
                //   }

                // Use the JavaFX constructor by adding a marker argument. The "true" in:
                //       ... new X(true);
                newClassArgs = newClassArgs.append(m().Literal(TypeTags.BOOLEAN, 1));

                // Create the new instance, placing it in a temporary variable "jfx$0objlit"
                //       final X jfx$0objlit = new X(true);
                stats.append(toJava.makeTmpVar(diagPos,
                        tmpVarName,
                        type,
                        m().NewClass(null, null, classTypeExpr, newClassArgs, null)));

                // addTriggers# to set-up triggers
                //       jfx$0objlit.addTriggers$();
                makeInitSupportCall(defs.addTriggersName, tmpVarName);

                // Apply defaults to the instance variables
                //
                //       final short[] jfx$0map = GETMAP$X();
                //       for (int jfx$0initloop = 0; i < X.$VAR_COUNT; i++) {
                //           ...
                //       }
                if (varSyms.nonEmpty()) {
                    makeInitApplyDefaults(type, tmpVarName);
                } else {
                    makeInitSupportCall(defs.applyDefaultsPrefixName, tmpVarName);
                }

                // Call complete$ to do user's init and postinit blocks
                //       jfx$0objlit.complete$();
                makeInitSupportCall(defs.completeName, tmpVarName);

                // Return the instance from the block expressions
                //       jfx$0objlit
                JCExpression instValue = m().Ident(tmpVarName);

                // Wrap it in a block expression
                instExpression = toJava.makeBlockExpression(diagPos, stats, instValue);

            } else {
                // this is a Java class or has no instance variable initializers, just instanciate it
                instExpression = m().NewClass(null, null, classTypeExpr, newClassArgs, null);
            }

            return instExpression;
        }
    }



    /**
     * Translate to a built-in type
     */
    abstract static class NewBuiltInInstanceTranslator extends NewInstanceTranslator {

        protected final Type builtIn;

        NewBuiltInInstanceTranslator(DiagnosticPosition diagPos, Type builtIn, JavafxToJava toJava) {
            super(diagPos, toJava);
            this.builtIn = builtIn;
        }

        /**
         * Arguments for the constructor.
         * There are no user arguments to built-in class constructors.
         * Just generate the default init 'true' flag for JavaFX generated constructors.
         */
        protected List<JCExpression> completeTranslatedConstructorArgs() {
            return List.<JCExpression>nil();
        }

        VarSymbol varSym(Name varName) {
            return (VarSymbol) builtIn.tsym.members().lookup(varName).sym;
        }

        void setInstanceVariable(Name instName, Name varName, JFXExpression init) {
            VarSymbol vsym = varSym(varName);
            setInstanceVariable(instName, vsym, init);
        }

        @Override
        protected JCExpression doit() {
            return buildInstance(builtIn, null, true);
        }
    }

    /**
     * Translator for object literals
     */
    abstract static class InstanciateTranslator extends NewInstanceTranslator {

        protected final JFXInstanciate tree;
        private final Symbol idSym;

        InstanciateTranslator(final JFXInstanciate tree, JavafxToJava toJava) {
            super(tree.pos(), toJava);
            this.tree = tree;
            this.idSym = JavafxTreeInfo.symbol(tree.getIdentifier());
        }

        abstract protected void processLocalVar(JFXVar var);

        protected void initInstanceVariables(Name instName) {
            if (tree.varDefinedByThis != null) {
                toJava.substitutionMap.put(tree.varDefinedByThis, instName);
            }
            for (JFXObjectLiteralPart olpart : tree.getParts()) {
                diagPos = olpart.pos(); // overwrite diagPos (must restore)
                JavafxBindStatus bindStatus = olpart.getBindStatus();
                JFXExpression init = olpart.getExpression();
                VarSymbol vsym = (VarSymbol) olpart.sym;
                setInstanceVariable(instName, bindStatus, vsym, init);
            }
            if (tree.varDefinedByThis != null) {
                toJava.substitutionMap.remove(tree.varDefinedByThis);
            }
            diagPos = tree.pos();
        }

        protected List<JCExpression> translatedConstructorArgs() {
            List<JFXExpression> args = tree.getArgs();
            Symbol sym = tree.constructor;
            if (sym != null && sym.type != null) {
                ListBuffer<JCExpression> translated = ListBuffer.lb();
                List<Type> formals = sym.type.asMethodType().getParameterTypes();
                boolean usesVarArgs = (sym.flags() & VARARGS) != 0L &&
                        (formals.size() != args.size() ||
                        types.isConvertible(args.last().type, types.elemtype(formals.last())));
                boolean handlingVarargs = false;
                Type formal = null;
                List<Type> t = formals;
                for (List<JFXExpression> l = args; l.nonEmpty(); l = l.tail) {
                    if (!handlingVarargs) {
                        formal = t.head;
                        t = t.tail;
                        if (usesVarArgs && t.isEmpty()) {
                            formal = types.elemtype(formal);
                            handlingVarargs = true;
                        }
                    }
                    JCExpression targ = toJava.translateAsValue(l.head, formal);
                    if (targ != null) {
                        translated.append(targ);
                    }
                }
                return translated.toList();
            } else {
                return toJava.translateExpressions(args);
            }
        }

        @Override
        protected List<JCExpression> completeTranslatedConstructorArgs() {
            List<JCExpression> translated = translatedConstructorArgs();
            if (tree.getClassBody() != null &&
                    tree.getClassBody().sym != null && toJava.hasOuters.contains(tree.getClassBody().sym) ||
                    idSym != null && toJava.hasOuters.contains(idSym)) {
                JCIdent thisIdent = m().Ident(defs.receiverName);
                translated = translated.prepend(thisIdent);
            }
            return translated;
        }

        protected JCExpression doit() {
            for (JFXVar var : tree.getLocalvars()) {
                // add the variable before the class definition or object litersl assignment
                processLocalVar(var);
            }
            return buildInstance(tree.type, tree.getClassBody(), types.isJFXClass(idSym));
        }
    }

    //@Override
    public void visitInstanciate(JFXInstanciate tree) {
        JFXExpression texp = tree.getIdentifier();
        Type type = texp.type;
        /* MAYBE FUTURE:
        if (tree.getJavaFXKind() == JavaFXKind.INSTANTIATE_NEW &&
                type.tag == TypeTags.ARRAY) {
            JCExpression elemtype = makeTypeTree(texp, ((Type.ArrayType) type).getComponentType());
            JCExpression len = translateAsValue(tree.getArgs().head, syms.intType);
            result = make.at(tree).NewArray(elemtype, List.of(len), null);
            return;
        }
        */

        ListBuffer<JCStatement> prevPrependToStatements = prependToStatements;
        prependToStatements = ListBuffer.lb();

        result = new InstanciateTranslator(tree, this) {
            protected void processLocalVar(JFXVar var) {
                stats.append(translateToStatement(var));
            }
        }.doit();

        if (result instanceof BlockExprJCBlockExpression) {
            BlockExprJCBlockExpression blockExpr = (BlockExprJCBlockExpression) result;
            blockExpr.stats = blockExpr.getStatements().prependList(prependToStatements.toList());
        }
        prependToStatements = prevPrependToStatements;
    }

    //@Override
    public void visitStringExpression(JFXStringExpression tree) {
        result = new StringExpressionTranslator(tree) {
            protected JCExpression translateArg(JFXExpression arg) {
                return translateAsUnconvertedValue(arg);
            }
        }.doit();
    }

    private JCExpression translateNonBoundInit(DiagnosticPosition diagPos,
            JFXExpression init, VarMorphInfo vmi) {
        // normal init -- unbound
        if (init == null) {
            // no initializing expression determine a default value from the type
            return makeDefaultValue(diagPos, vmi);
        } else {
            // do a vanilla translation of the expression
            Type resultType = vmi.getSymbol().type;
            JCExpression trans = translateAsValue(init, resultType);
            return convertNullability(diagPos, trans, init, resultType);
        }
    }

    //TODO: get rid of this -- its existance has too many assumptions
    private JCExpression translateDefinitionalAssignmentToValueArg(DiagnosticPosition diagPos,
            JFXExpression init, JavafxBindStatus bindStatus, VarMorphInfo vmi) {
        if (bindStatus.isUnidiBind()) {
            return toBound.translateAsLocationOrBE(init, bindStatus, vmi);
        } else if (bindStatus.isBidiBind()) {
            // Bi-directional bind translate so it stays in a Location
            return translateAsLocation(init);
        } else {
            // normal init -- unbound
            return translateNonBoundInit(diagPos, init, vmi);
        }
    }

    private JCExpression translateDefinitionalAssignmentToValue(DiagnosticPosition diagPos,
            JFXExpression init, JavafxBindStatus bindStatus, VarSymbol vsym) {
        VarMorphInfo vmi = typeMorpher.varMorphInfo(vsym);
        assert !vmi.isMemberVariable();
        assert vmi.representation() == SlackerLocation;
        if (bindStatus.isUnidiBind()) {
            return toBound.translateAsLocationOrBE(init, bindStatus, vmi);
        } else if (bindStatus.isBidiBind()) {
            assert vmi.representation() == AlwaysLocation;
            // Bi-directional bind translate so it stays in a Location
            return makeLocationVariable(vmi, diagPos, List.of(translateAsLocation(init)), defs.makeBijectiveMethodName);
        } else if (vmi.representation() == AlwaysLocation) {
            return makeLocationVariable(vmi, diagPos, List.of(translateAsLocation(init)), defs.makeMethodName);
        } else {
            return translateNonBoundInit(diagPos, init, vmi);
        }
    }

    //TODO: make this private again
    JCStatement translateDefinitionalAssignmentToSet(DiagnosticPosition diagPos,
            JFXExpression init, JavafxBindStatus bindStatus, VarSymbol vsym,
            Name instanceName) {
        if (init == null) {
            // Nothing to set, no init statement
            return null;
        }
        VarMorphInfo vmi = typeMorpher.varMorphInfo(vsym);
        return make.at(diagPos).Exec( translateDefinitionalAssignmentToSetExpression(diagPos,
            init, bindStatus, vmi, instanceName) );
    }

    private JCExpression translateDefinitionalAssignmentToSetExpression(DiagnosticPosition diagPos,
            JFXExpression init, JavafxBindStatus bindStatus, VarMorphInfo vmi,
            Name instanceName) {
        Symbol vsym = vmi.getSymbol();
        assert( (vsym.flags() & Flags.PARAMETER) == 0L): "Parameters are not initialized";
        setSubstitution(init, vsym);
        JCExpression valueArg = translateDefinitionalAssignmentToValueArg(diagPos, init, bindStatus, vmi);
        return definitionalAssignmentToSetExpression(diagPos, valueArg, bindStatus, instanceName, vmi);
    }

    //TODO: should go away and be folded with above
    private JCExpression definitionalAssignmentToSetExpression(DiagnosticPosition diagPos,
            JCExpression init, JavafxBindStatus bindStatus,
            Name instanceName, VarMorphInfo vmi) {
        Symbol vsym = vmi.getSymbol();
        final boolean isLocal = !vmi.isMemberVariable();
        assert !isLocal || instanceName == null;
        final JCExpression nonNullInit = (init == null) ? makeDefaultValue(diagPos, vmi) : init;  //TODO: is this needed?

        if (!bindStatus.isBound() && vmi.useAccessors()) {
            JCExpression tc = instanceName == null ? null : make.at(diagPos).Ident(instanceName);
            return callExpression(diagPos, tc, attributeSetterName(vsym), nonNullInit);
        }
        final JCExpression varRef = isLocal ?
                  make.at(diagPos).Ident(vsym) // It is a local variable
                : makeAttributeAccess(diagPos, vsym, instanceName);     // It is a member variable
        if (vmi.representation() == NeverLocation) {
            // It is a local variable which is not a Location, just assign to it
            return make.at(diagPos).Assign(varRef, nonNullInit);
        } else {
            return makeLocation(diagPos, varRef, nonNullInit, bindStatus, vmi);
        }
    }

    private JCExpression makeLocation(DiagnosticPosition diagPos, JCExpression varRef,
            JCExpression init, JavafxBindStatus bindStatus,
            VarMorphInfo vmi) {
        Name methName;
        ListBuffer<JCExpression> args = new ListBuffer<JCExpression>();
        if (bindStatus.isUnidiBind()) {
            methName = defs.locationBindMethodName;
            args.append(makeLaziness(diagPos, bindStatus));
        } else if (bindStatus.isBidiBind()) {
            methName = defs.locationBijectiveBindMethodName;
        } else {
            methName = defs.locationSetMethodName[vmi.getTypeKind()];
        }
        args.append(init);

        return callExpression(diagPos, varRef, methName, args);
    }

    //@Override
    public void visitVarScriptInit(JFXVarScriptInit tree) {
        DiagnosticPosition diagPos = tree.pos();
        JFXModifiers mods = tree.getModifiers();
        long modFlags = mods.flags | Flags.FINAL;
        VarSymbol vsym = tree.getSymbol();
        VarMorphInfo vmi = typeMorpher.varMorphInfo(vsym);
        JFXVar var = tree.getVar();
        assert vsym.owner.kind == Kinds.TYP : "this should just be for script-level variables";
        assert (modFlags & Flags.STATIC) != 0;
        assert (modFlags & JavafxFlags.SCRIPT_LEVEL_SYNTH_STATIC) != 0;
        assert !attrEnv.toplevel.isLibrary;

        result = translateDefinitionalAssignmentToSetExpression(diagPos, var.init, var.getBindStatus(), vmi, null);
    }

    //@Override
    public void visitVar(JFXVar tree) {
        DiagnosticPosition diagPos = tree.pos();
        JFXModifiers mods = tree.getModifiers();
        final VarSymbol vsym = tree.getSymbol();
        final VarMorphInfo vmi = typeMorpher.varMorphInfo(vsym);
        assert vsym.owner.kind != Kinds.TYP : "attributes are processed in the class and should never come here";
        final long flags = vsym.flags();
        final boolean requiresLocation = representation(vsym).possiblyLocation();
        final boolean isParameter = (flags & Flags.PARAMETER) != 0;
        final boolean hasInnerAccess = (flags & JavafxFlags.VARUSE_INNER_ACCESS) != 0;
        final long modFlags = (mods.flags & ~Flags.FINAL) | ((hasInnerAccess | requiresLocation | isParameter)? Flags.FINAL : 0L);
        final JCModifiers tmods = make.at(diagPos).Modifiers(modFlags);
        final Type type =
                requiresLocation?
                    (isParameter?
                        vmi.getLocationType() :
                        vmi.getVariableType()) :
                    tree.type;
        final JCExpression typeExpression = makeTypeTree(diagPos, type, true);

        // for class vars, initialization happens during class init, so set to
        // default Location.  For local vars translate as definitional
        JCExpression init;
        if (isParameter) {
            // parameters aren't initialized
            init = null;
            result = make.at(diagPos).VarDef(tmods, tree.name, typeExpression, init);
        } else {
            // create a blank variable declaration and move the declaration to the beginning of the block
            if (requiresLocation) {
                // location types: XXXVariable.make()
                optStat.recordLocalVar(vsym, tree.getBindStatus().isBound(), true);
                init = makeLocationWithDefault(vmi, diagPos);
            } else {
                optStat.recordLocalVar(vsym, tree.getBindStatus().isBound(), false);
                if ((modFlags & Flags.FINAL) != 0) {
                    //TODO: this case probably won't be used any more, but it will
                    // be again if we optimize the case for initializer which don't reference locals
                    init = translateDefinitionalAssignmentToValue(tree.pos(), tree.init,
                            tree.getBindStatus(), vsym);
                    JCStatement var = make.at(diagPos).VarDef(tmods, tree.name, typeExpression, init);
                    prependToStatements.append(var);
                    result = make.at(diagPos).Skip();
                    return;
                }
                // non location types:
                init = makeDefaultValue(diagPos, vmi);
            }
            prependToStatements.prepend(make.at(Position.NOPOS).VarDef(tmods, tree.name, typeExpression, init));

            // create change Listener and append it to the beginning of the block after the blank variable declaration
            JFXOnReplace onReplace = tree.getOnReplace();
            if ( onReplace != null ) {
                JCStatement changeListener = makeInstanciateChangeListener(vsym, onReplace);
                prependToStatements.append(changeListener);
            }

            result = translateDefinitionalAssignmentToSetExpression(diagPos, tree.init, tree.getBindStatus(), vmi, null);
        }
    }

    //@Override
    public void visitOverrideClassVar(JFXOverrideClassVar tree) {
        assert false : "should be processed by parent tree";
    }

    //@Override
    public void visitOnReplace(JFXOnReplace tree) {
        assert false : "should be processed by parent tree";
    }


    //@Override
    public void visitFunctionValue(JFXFunctionValue tree) {
        JFXFunctionDefinition def = tree.definition;
        result = makeFunctionValue(make.Ident(defs.lambdaName), def, tree.pos(), (MethodType) def.type);
    }

   JCExpression makeFunctionValue (JCExpression meth, JFXFunctionDefinition def, DiagnosticPosition diagPos, MethodType mtype) {
        ListBuffer<JCTree> members = new ListBuffer<JCTree>();
        if (def != null) {
            // Translate the definition, maintaining the current inInstanceContext
            members.append( new FunctionTranslator(def, true).doit() );
        }
        JCExpression encl = null;
        int nargs = mtype.argtypes.size();
        Type ftype = syms.javafx_FunctionTypes[nargs];
        JCExpression t = makeQualifiedTree(null, ftype.tsym.getQualifiedName().toString());
        ListBuffer<JCExpression> typeargs = new ListBuffer<JCExpression>();
        Type rtype = types.boxedTypeOrType(mtype.restype);
        typeargs.append(makeTypeTree(diagPos, rtype));
        ListBuffer<JCVariableDecl> params = new ListBuffer<JCVariableDecl>();
        ListBuffer<JCExpression> margs = new ListBuffer<JCExpression>();
        int i = 0;
        for (List<Type> l = mtype.argtypes;  l.nonEmpty();  l = l.tail) {
            Name pname = make.paramName(i++);
            Type ptype = types.boxedTypeOrType(l.head);
            JCVariableDecl param = make.VarDef(make.Modifiers(0), pname,
                    makeTypeTree(diagPos, ptype), null);
            params.append(param);
            JCExpression marg = make.Ident(pname);
            margs.append(marg);
            typeargs.append(makeTypeTree(diagPos, ptype));
        }

        // The backend's Attr skips SYNTHETIC methods when looking for a matching method.
        long flags = PUBLIC | BRIDGE; // | SYNTHETIC;

        JCExpression call = make.Apply(null, meth, margs.toList());

        List<JCStatement> stats;
        if (mtype.restype == syms.voidType)
            stats = List.of(make.Exec(call), make.Return(make.Literal(TypeTags.BOT, null)));
        else {
            if (mtype.restype.isPrimitive())
                call = makeBox(diagPos, call, mtype.restype);
            stats = List.<JCStatement>of(make.Return(call));
        }
       JCMethodDecl bridgeDef = make.at(diagPos).MethodDef(
                make.Modifiers(flags),
                defs.invokeName,
                makeTypeTree(diagPos, rtype),
                List.<JCTypeParameter>nil(),
                params.toList(),
                make.at(diagPos).Types(mtype.getThrownTypes()),
                make.Block(0, stats),
                null);

        members.append(bridgeDef);
        JCClassDecl cl = make.AnonymousClassDef(make.Modifiers(0), members.toList());
        List<JCExpression> nilArgs = List.nil();
        return make.NewClass(encl, nilArgs, make.TypeApply(t, typeargs.toList()), nilArgs, cl);
    }

   boolean isInnerFunction (MethodSymbol sym) {
       return sym.owner != null && sym.owner.kind != Kinds.TYP
                && (sym.flags() & Flags.SYNTHETIC) == 0;
   }

    private class FunctionTranslator extends Translator {

        final JFXFunctionDefinition tree;
        final boolean maintainContext;
        final MethodType mtype;
        final MethodSymbol sym;
        final Symbol owner;
        final Name name;
        final boolean isBound;
        final boolean isRunMethod;
        final boolean isAbstract;
        final boolean isStatic;
        final boolean isSynthetic;
        final boolean isInstanceFunction;
        final boolean isInstanceFunctionAsStaticMethod;
        final boolean isMixinClass;

        FunctionTranslator(JFXFunctionDefinition tree, boolean maintainContext) {
            super(tree.pos());
            this.tree = tree;
            this.maintainContext = maintainContext;
            this.mtype = (MethodType) tree.type;
            this.sym = (MethodSymbol) tree.sym;
            this.owner = sym.owner;
            this.name = tree.name;
            this.isBound = (sym.flags() & JavafxFlags.BOUND) != 0;
            this.isRunMethod = syms.isRunMethod(tree.sym);
            this.isMixinClass = currentClass.isMixinClass();
            long originalFlags = tree.mods.flags;
            this.isAbstract = (originalFlags & Flags.ABSTRACT) != 0L;
            this.isSynthetic = (originalFlags & Flags.SYNTHETIC) != 0L;
            this.isStatic = (originalFlags & Flags.STATIC) != 0L;
            this.isInstanceFunction = !isAbstract && !isStatic && !isSynthetic;
            this.isInstanceFunctionAsStaticMethod = isInstanceFunction && isMixinClass;
        }

        private JCBlock makeRunMethodBody(JFXBlock bexpr) {
            final JFXExpression value = bexpr.value;
            JCBlock block;
            if (value == null || value.type == syms.voidType) {
                // the block has no value: translate as simple statement and add a null return
                block = asBlock(translateToStatement(bexpr));
                block.stats = block.stats.append(make.Return(make.at(diagPos).Literal(TypeTags.BOT, null)));
            } else {
                // block has a value, return it
                block = asBlock(translateToStatement(bexpr, value.type));
                final Type valueType = value.type;
                if (valueType != null && valueType.isPrimitive()) {
                    // box up any primitives returns so they return Object -- the return type of the run method
                    new TreeTranslator() {

                        @Override
                        public void visitReturn(JCReturn tree) {
                            tree.expr = makeBox(tree.expr.pos(), tree.expr, valueType);
                            result = tree;
                        }
                        // do not descend into inner classes

                        @Override
                        public void visitClassDef(JCClassDecl tree) {
                            result = tree;
                        }
                    }.translate(block);
                }
            }
            return block;
        }

        private long methodFlags() {
            long methodFlags = tree.mods.flags;
            methodFlags &= ~Flags.PROTECTED;
            methodFlags &= ~Flags.SYNTHETIC;
            methodFlags |= Flags.PUBLIC;
            if (isInstanceFunctionAsStaticMethod) {
                methodFlags |= Flags.STATIC;
            }
            return methodFlags;
        }

        private List<JCVariableDecl> methodParameters() {
            ListBuffer<JCVariableDecl> params = ListBuffer.lb();
            if (isInstanceFunctionAsStaticMethod) {
                // if we are converting a standard instance function (to a static method), the first parameter becomes a reference to the receiver
                params.prepend(makeReceiverParam(currentClass));
            }
            for (JFXVar fxVar : tree.getParams()) {
                JCVariableDecl var = (JCVariableDecl) translate(fxVar);
                params.append(var);
            }
            return params.toList();
        }

        private JCBlock methodBody() {
            // construct the body of the translated function
            JFXBlock bexpr = tree.getBodyExpression();
            JCBlock body;
            if (bexpr == null) {
                body = null; // null if no block expression
            } else if (isBound) {
                // Build the body of a bound function by treating it as a bound expression
                //TODO: Remove entry in findbugs-exclude.xml if permeateBind is implemented -- it is, so it should be
                JCExpression expr = toBound.translateAsLocation(bexpr, JavafxBindStatus.UNIDIBIND, typeMorpher.varMorphInfo(tree.sym));
                body = asBlock(m().Return(expr));
            } else if (isRunMethod) {
                // it is a module level run method, do special translation
                body = makeRunMethodBody(bexpr);
            } else {
                // the "normal" case
                ListBuffer<JCStatement> stmts = ListBuffer.lb();
                for (JFXVar fxVar : tree.getParams()) {
                    if (types.isSequence(fxVar.sym.type)) {
                         stmts.append(callStatement(fxVar,  make.at(fxVar).Ident(fxVar.getName()), defs.incrementSharingMethodName));
                    }
                }
                stmts.append(translateToStatement(bexpr, mtype.getReturnType()));
                body = make.at(bexpr).Block(0L, stmts.toList());
            }

            if (isInstanceFunction && !isMixinClass) {
                //TODO: unfortunately, some generated code still expects a receiver$ to always be present.
                // In the instance as instance case, there is no receiver param, so allow generated code
                // to function by adding:   var receiver = this;
                //TODO: this should go away
                body.stats = body.stats.prepend( m().VarDef(
                        m().Modifiers(Flags.FINAL),
                        defs.receiverName,
                        m().Ident(interfaceName(currentClass)),
                        m().Ident(names._this)));
            }
            return body;
        }

        private JCMethodDecl makeMethod(long flags, JCBlock body, List<JCVariableDecl> params) {
            JCMethodDecl meth = m().MethodDef(
                    addAccessAnnotationModifiers(diagPos, tree.mods.flags, m().Modifiers(flags)),
                    functionName(sym, isInstanceFunctionAsStaticMethod, isBound),
                    makeReturnTypeTree(diagPos, sym, isBound),
                    m().TypeParams(mtype.getTypeArguments()),
                    params,
                    m().Types(mtype.getThrownTypes()), // makeThrows(diagPos), //
                    body,
                    null);
            meth.sym = sym;
            meth.type = tree.type;
            return meth;
        }

        protected JCTree doit() {
            TranslationState prevZ = translationState;
            translationState = null; //should be explicitly set
            ReceiverContext prevContext = inInstanceContext;
            if (!maintainContext) {
                inInstanceContext = isStatic?
                    ReceiverContext.ScriptAsStatic :
                    isInstanceFunctionAsStaticMethod ?
                        ReceiverContext.InstanceAsStatic :
                        ReceiverContext.InstanceAsInstance;
            }

            try {
                return makeMethod(methodFlags(), methodBody(), methodParameters());
            } finally {
                translationState = prevZ;
                inInstanceContext = prevContext;
            }
        }
    }

   /**
    * Translate JavaFX a class definition into a Java static implementation method.
    */
    //@Override
    public void visitFunctionDefinition(JFXFunctionDefinition tree) {
        result = new FunctionTranslator(tree, false).doit();
    }

    public void visitBlockExpression(JFXBlock tree) {
        Type targetType = translationState.targetType;
        Yield yield = translationState.yield;

        DiagnosticPosition diagPos = tree.pos();
        ListBuffer<JCStatement> prevPrependToStatements = prependToStatements;
        prependToStatements = ListBuffer.lb();

        JFXExpression value = tree.value;
        ListBuffer<JCStatement> translatedStmts = ListBuffer.lb();
        for(JFXExpression expr : tree.getStmts()) {
                JCStatement stmt = translateToStatement(expr);
                if (stmt != null) {
                    translatedStmts.append(stmt);
                }
        }
        List<JCStatement> localDefs = translatedStmts.toList();

        if (yield == ToExpression) {
            // make into block expression
            assert (tree.type != syms.voidType) : "void block expressions should be handled below";
            //TODO: this may be unneeded, or even wrong
            if (value.getFXTag() == JavafxTag.RETURN) {
                value = ((JFXReturn) value).getExpression();
            }
            JCExpression tvalue = translateToExpression(value, translationState.wrapper, null); // must be before prepend
            localDefs = prependToStatements.appendList(localDefs).toList();
            result = makeBlockExpression(tree.pos(), localDefs, tvalue);  //TODO: tree.flags lost
        } else {
            // make into block
            prependToStatements.appendList(localDefs);
            if (value != null) {
                JCStatement stmt = translateToStatement(value, targetType);
                if (stmt != null) {
                    prependToStatements.append(stmt);
                }
            }
            localDefs = prependToStatements.toList();
            result = localDefs.size() == 1? localDefs.head : make.at(diagPos).Block(0L, localDefs);
        }
        prependToStatements = prevPrependToStatements;
    }

    abstract class AssignTranslator extends Translator {

        protected final JFXExpression lhs;
        protected final JFXExpression rhs;
        protected final Symbol sym;
        protected final VarMorphInfo vmi;
        protected final JCExpression rhsTranslated;

        AssignTranslator(final DiagnosticPosition diagPos, final JFXExpression lhs, final JFXExpression rhs) {
            super(diagPos);
            this.lhs = lhs;
            this.rhs = rhs;
            this.sym = expressionSymbol(lhs);
            this.vmi = sym==null? null : typeMorpher.varMorphInfo(sym);
            this.rhsTranslated = convertNullability(diagPos, translateAsValue(rhs, rhsType()), rhs, rhsType());
        }

        abstract JCExpression defaultFullExpression(JCExpression lhsTranslated, JCExpression rhsTranslated);

        abstract JCExpression buildRHS(JCExpression rhsTranslated);

        /**
         * Override to change the translation type of the right-hand side
         */
        protected Type rhsType() {
            return sym==null? lhs.type : sym.type; // Handle type inferencing not reseting the ident type
        }

        /**
         * Override to change result in the non-default case.
          */
        protected JCExpression postProcess(JCExpression built) {
            return built;
        }

        private JCExpression buildSetter(JCExpression tc, JCExpression rhsComplete) {
            final Name setter = attributeSetterName(sym);
            JCExpression toApply = (tc==null)? m().Ident(setter) : m().Select(tc, setter);
            return m().Apply(null, toApply, List.of(rhsComplete));
        }

        protected JCTree doit() {
            if (lhs.getFXTag() == JavafxTag.SEQUENCE_INDEXED) {
                // set of a sequence element --  s[i]=8, call the sequence set method
                JFXSequenceIndexed si = (JFXSequenceIndexed) lhs;
                JFXExpression seq = si.getSequence();
                JCExpression index = translateAsValue(si.getIndex(), syms.intType);
                if (seq.type.tag == TypeTags.ARRAY) {
                    JCExpression tseq = translateAsUnconvertedValue(seq);
                    return postProcess(m().Assign(m().Indexed(tseq, index), buildRHS(rhsTranslated)));
                }
                else {
                    JCExpression tseq = translateAsSequenceVariable(seq);
                    List<JCExpression> args;
                    JCFieldAccess select;
                    if (types.elementType(seq.type).isPrimitive()) { // KLUDGE
                        select = m().Select(makeQualifiedTree(diagPos, JavafxDefs.locationPackageNameString+".SequenceVariable"), defs.setMethodName);
                        args = List.of(tseq, index, buildRHS(rhsTranslated));
                    }
                    else {
                        select = m().Select(tseq, defs.setMethodName);
                        args = List.of(index, buildRHS(rhsTranslated));
                    }
                    return postProcess(m().Apply(null, select, args));
                }
            } else if (!vmi.useAccessors() && vmi.representation() == AlwaysLocation) {
                // we are setting a var Location, call the set method
                JCExpression lhsTranslated = translateAsLocation(lhs);
                JCFieldAccess setSelect = m().Select(lhsTranslated, defs.locationSetMethodName[typeMorpher.typeMorphInfo(lhs.type).getTypeKind()]);
                List<JCExpression> setArgs = List.of(buildRHS(rhsTranslated));
                return postProcess(m().Apply(null, setSelect, setArgs));
            } else {
                final boolean useSetters = vmi.useAccessors();
                // If sequence we need to call incrementShared.  Thus:
                assert ! types.isSequence(lhs.type);
                if (lhs.getFXTag() == JavafxTag.SELECT) {
                    final JFXSelect select = (JFXSelect) lhs;
                    return new NullCheckTranslator(diagPos, select.getExpression(), lhs.type, false, AsValue) { //assume assignment doesn't yield Location

                        private final JCExpression rhsTranslatedPreserved = preserveSideEffects(lhs.type, rhs, rhsTranslated);

                        @Override
                        JCExpression translateToCheck( JFXExpression expr) {
                            return translateAsUnconvertedValue(expr);
                        }

                        @Override
                        JCExpression fullExpression( JCExpression mungedToCheckTranslated) {
                            Symbol selectorSym = expressionSymbol(select.getExpression());
                            // If LHS is OuterClass.memberName or MixinClass.memberName, then
                            // we want to create expression to get the proper receiver.
                            if (!sym.isStatic() && selectorSym != null && selectorSym.kind == Kinds.TYP) {
                                mungedToCheckTranslated = makeReceiver(diagPos, sym);
                            }
                            if (useSetters) {
                                return postProcess(buildSetter(mungedToCheckTranslated, buildRHS(rhsTranslatedPreserved)));
                            } else {
                                //TODO: possibly should use, or be unified with convertVariableReference
                                JCFieldAccess fa = m().Select(mungedToCheckTranslated, attributeFieldName(select.sym));
                                return defaultFullExpression(fa, rhsTranslatedPreserved);
                            }
                        }
                    }.doit();
                } else {
                    // not SELECT
                    if (useSetters) {
                        JCExpression recv = sym.isStatic()?
                            makeTypeTree(diagPos, sym.owner.type, false) :
                            makeReceiver(diagPos, sym, true);
                        return postProcess(buildSetter(recv, buildRHS(rhsTranslated)));
                    } else {
                        return defaultFullExpression(translateToExpression(lhs, AsValue, null), rhsTranslated);
                    }
                }
            }
        }
    }

    //@Override
    public void visitAssign(final JFXAssign tree) {
        DiagnosticPosition diagPos = tree.pos();

        if (tree.lhs.getFXTag() == JavafxTag.SEQUENCE_SLICE) {
            // assignment of a sequence slice --  s[i..j]=8, call the sequence set method
            JFXSequenceSlice si = (JFXSequenceSlice)tree.lhs;
            JCExpression rhs = translateAsValue(tree.rhs, si.getSequence().type);
            JCExpression seq = translateAsSequenceVariable(si.getSequence());
            JCExpression startPos = translateAsValue(si.getFirstIndex(), syms.intType);
            JCExpression endPos = makeSliceEndPos(si);
            JCFieldAccess select = make.Select(seq, defs.replaceSliceMethodName);
            List<JCExpression> args = List.of(startPos, endPos, rhs);
            result = make.at(diagPos).Apply(null, select, args);
        } else {
            result = new AssignTranslator(diagPos, tree.lhs, tree.rhs) {

                JCExpression buildRHS(JCExpression rhsTranslated) {
                    return rhsTranslated;
                }

                JCExpression defaultFullExpression(JCExpression lhsTranslated, JCExpression rhsTranslated) {
                    return m().Assign(lhsTranslated, rhsTranslated);
                }
            }.doit();
        }
    }

    //@Override
    public void visitAssignop(final JFXAssignOp tree) {
        result = new AssignTranslator(tree.pos(), tree.lhs, tree.rhs) {

            private boolean useDurationOperations() {
                return types.isSameType(lhs.type, syms.javafx_DurationType);
            }

            @Override
            JCExpression buildRHS(JCExpression rhsTranslated) {
                final JCExpression lhsTranslated = translateAsUnconvertedValue(lhs);
                if (useDurationOperations()) {
                    JCExpression method = m().Select(lhsTranslated, tree.operator);
                    return m().Apply(null, method, List.<JCExpression>of(rhsTranslated));
                } else {
                    JCExpression ret = m().Binary(getBinaryOp(), lhsTranslated, rhsTranslated);
                    if (!types.isSameType(rhsType(), lhs.type)) {
                        // Because the RHS is a different type than the LHS, a cast may be needed
                        ret = m().TypeCast(lhs.type, ret);
                    }
                    return ret;
                }
            }

            @Override
            protected Type rhsType() {
                switch (tree.getFXTag()) {
                    case MUL_ASG:
                    case DIV_ASG:
                        // Allow for cases like 'k *= 0.5' where k is an Integer or Duration
                        return operationalType(useDurationOperations()? syms.javafx_NumberType : rhs.type);
                    default:
                        return operationalType(lhs.type);
                }
            }

            @Override
            JCExpression defaultFullExpression( JCExpression lhsTranslated, JCExpression rhsTranslated) {
                return useDurationOperations()?
                    m().Assign(lhsTranslated, buildRHS(rhsTranslated)) :
                    m().Assignop(tree.getOperatorTag(), lhsTranslated, rhsTranslated);
            }

            private int getBinaryOp() {
                switch (tree.getFXTag()) {
                    case PLUS_ASG:
                        return JCTree.PLUS;
                    case MINUS_ASG:
                        return JCTree.MINUS;
                    case MUL_ASG:
                        return JCTree.MUL;
                    case DIV_ASG:
                        return JCTree.DIV;
                    default:
                        assert false : "unexpected assign op kind";
                        return JCTree.PLUS;
                }
            }
        }.doit();
    }

    class SelectTranslator extends NullCheckTranslator {

        protected final Symbol sym;
        protected final boolean isFunctionReference;
        protected final boolean staticReference;
        protected final Name name;

        protected SelectTranslator(JavafxToJava toJava, JFXSelect tree, Locationness wrapper) {
            super(tree.pos(), tree.getExpression(), tree.type, tree.sym.isStatic(), wrapper);
            sym = tree.sym;
            isFunctionReference = tree.type instanceof FunctionType && sym.type instanceof MethodType;
            staticReference = sym.isStatic();
            name = tree.getIdentifier();
        }

        @Override
        protected JCExpression translateToCheck(JFXExpression expr) {
            // this may or may not be in a LHS but in either
            // event the selector is a value expression
            JCExpression translatedSelected = translateAsUnconvertedValue(expr);

            if (staticReference) {
                translatedSelected = makeTypeTree(diagPos, types.erasure(sym.owner.type), false);
            } else if (expr instanceof JFXIdent) {
                JFXIdent ident = (JFXIdent)expr;
                Symbol identSym = ident.sym;

                if (identSym != null && types.isJFXClass(identSym)) {
                    if ((identSym.flags_field & JavafxFlags.MIXIN) != 0) {
                        translatedSelected = m().Ident(defs.receiverName);
                    } else if (identSym == getAttrEnv().enclClass.sym) {
                        translatedSelected = m().Ident(names._this);
                    } else {
                        translatedSelected = m().Ident(names._super);
                    }
                }
            }

            return translatedSelected;
        }

        @Override
        protected JCExpression fullExpression(JCExpression mungedToCheckTranslated) {
            Symbol selectorSym = (toCheck != null)? expressionSymbol(toCheck) : null;
            // If this is OuterClass.memberName or MixinClass.memberName, then
            // we want to create expression to get the proper receiver.
            if (!staticReference && selectorSym != null && selectorSym.kind == Kinds.TYP) {
                mungedToCheckTranslated = makeReceiver(diagPos, sym);
            }
            if (isFunctionReference) {
                MethodType mtype = (MethodType) sym.type;
                JCExpression tc = staticReference?
                    mungedToCheckTranslated :
                    addTempVar(toCheck.type, mungedToCheckTranslated);
                JCExpression translated = m().Select(tc, name);
                return makeFunctionValue(translated, null, diagPos, mtype);
            } else {
                JCExpression tc = mungedToCheckTranslated;
                if (toCheck.type != null && toCheck.type.isPrimitive()) {  // expr.type is null for package symbols.
                    tc = makeBox(diagPos, tc, toCheck.type);
                }
                JCFieldAccess translated = make.at(diagPos).Select(tc, name);

                return convertVariableReference(
                        diagPos,
                        translated,
                        sym,
                        wrapper);
            }
        }
    }

    public void visitInvalidate(JFXInvalidate tree) {
       throw new AssertionError("Unsupported AST node: " + tree.getJavaFXKind());
    }

    public void visitSelect(JFXSelect tree) {
        Locationness wrapper = translationState.wrapper;

        if (substitute(tree.sym, wrapper)) {
            return;
        }
        result = new SelectTranslator(this, tree, wrapper).doit();
    }

    //@Override
    public void visitIdent(JFXIdent tree) {
        Locationness wrapper = translationState.wrapper;

        DiagnosticPosition diagPos = tree.pos();
        if (substitute(tree.sym, wrapper)) {
            return;
        }

        if (tree.name == names._this) {
            // in the static implementation method, "this" becomes "receiver$"
            JCExpression rcvr = makeReceiver(diagPos, tree.sym);
            if (wrapper == AsLocation) {
                rcvr = makeConstantLocation(diagPos, tree.type, rcvr);
            }
            result = rcvr;
            return;
        } else if (tree.name == names._super) {
            if (types.isMixin(tree.type.tsym)) {
                // "super" becomes just the class where the static implementation method is defined
                //  the rest of the implementation is in visitFunctionInvocation
                result = make.at(diagPos).Ident(tree.type.tsym.name);
            } else {
                // Just use super.
                result = make.at(diagPos).Ident(tree.name);
            }
            return;
        }

        int kind = tree.sym.kind;
        if (kind == Kinds.TYP) {
            // This is a class name, replace it with the full name (no generics)
            result = makeTypeTree(diagPos, types.erasure(tree.sym.type), false);
            return;
        }

       // if this is an instance reference to an attribute or function, it needs to go the the "receiver$" arg,
       // and possible outer access methods
        JCExpression convert;
        boolean isStatic = tree.sym.isStatic();
        if (isStatic) {
            // make class-based direct static reference:   Foo.x
            convert = make.at(diagPos).Select(makeTypeTree(diagPos, tree.sym.owner.type, false), tree.name);
        } else {
            if ((kind == Kinds.VAR || kind == Kinds.MTH) &&
                    tree.sym.owner.kind == Kinds.TYP) {
                // it is a non-static attribute or function class member
                // reference it through the receiver
                JCExpression mRec = makeReceiver(diagPos, tree.sym, true);
                convert = (mRec==null)? make.at(diagPos).Ident(tree.name) : make.at(diagPos).Select(mRec, tree.name);
            } else {
                convert = make.at(diagPos).Ident(tree.name);
            }
        }

        if (tree.type instanceof FunctionType && tree.sym.type instanceof MethodType) {
            MethodType mtype = (MethodType) tree.sym.type;
            JFXFunctionDefinition def = null; // FIXME
            result = makeFunctionValue(convert, def, tree.pos(), mtype);
            return;
        }

        result = convertVariableReference(diagPos,
                convert,
                tree.sym,
                wrapper);
    }

    private class ExplicitSequenceTranslator extends Translator {

        final List<JFXExpression> items;
        final Type elemType;

        ExplicitSequenceTranslator(DiagnosticPosition diagPos, List<JFXExpression> items, Type elemType) {
            super(diagPos);
            this.items = items;
            this.elemType = elemType;
        }

        /***
         * In cases where the components of an explicitly constructed
         * sequence are all singletons, we can revert to this (more
         * optimal) implementation.

        DiagnosticPosition diagPos = tree.pos();
        JCExpression meth = ((JavafxTreeMaker)make).at(diagPos).Identifier(sequencesMakeString);
        Type elemType = tree.type.getTypeArguments().get(0);
        ListBuffer<JCExpression> args = ListBuffer.<JCExpression>lb();
        List<JCExpression> typeArgs = List.<JCExpression>of(makeTypeTree(elemType, diagPos));
        // type name .class
        args.append(makeTypeInfo(diagPos, elemType));
        args.appendList( translate( tree.getItems() ) );
        result = make.at(diagPos).Apply(typeArgs, meth, args.toList());
        */
        protected JCExpression doit() {
            ListBuffer<JCStatement> stmts = ListBuffer.lb();
            UseSequenceBuilder builder = toJava.useSequenceBuilder(diagPos, elemType, items.length());
            stmts.append(builder.makeBuilderVar());
            for (JFXExpression item : items) {
                if (item.getJavaFXKind() != JavaFXKind.NULL_LITERAL) {
                    // Insert all non-null elements
                    stmts.append(builder.addElement(item));
                }
            }
            return toJava.makeBlockExpression(diagPos, stmts, builder.makeToSequence());
        }
    }

    //@Override
    public void visitSequenceExplicit(JFXSequenceExplicit tree) {
        result = new ExplicitSequenceTranslator(
                tree.pos(),
                tree.getItems(),
                types.elementType(tree.type)
        ).doit();
    }

    //@Override
    public void visitSequenceRange(JFXSequenceRange tree) {
        DiagnosticPosition diagPos = tree.pos();
        RuntimeMethod rm  = tree.isExclusive()?
                    defs.Sequences_rangeExclusive :
                    defs.Sequences_range;
        Type elemType = syms.javafx_IntegerType;
        int ltag = tree.getLower().type.tag;
        int utag = tree.getUpper().type.tag;
        int stag = tree.getStepOrNull() == null? TypeTags.INT : tree.getStepOrNull().type.tag;
        if (ltag == TypeTags.FLOAT || ltag == TypeTags.DOUBLE ||
                utag == TypeTags.FLOAT || utag == TypeTags.DOUBLE ||
                stag == TypeTags.FLOAT || stag == TypeTags.DOUBLE) {
            elemType = syms.javafx_NumberType;
        }
        ListBuffer<JCExpression> args = ListBuffer.lb();
        args.append( translateAsValue( tree.getLower(), elemType ));
        args.append( translateAsValue( tree.getUpper(), elemType ));
        if (tree.getStepOrNull() != null) {
            args.append( translateAsValue( tree.getStepOrNull(), elemType ));
        }
        result = convertTranslated(
                runtime(diagPos, rm, args.toList()),
                diagPos,
                types.sequenceType(elemType),
                tree.type);
    }

    //@Override
    public void visitSequenceEmpty(JFXSequenceEmpty tree) {
        if (types.isSequence(tree.type)) {
            Type elemType = types.boxedElementType(tree.type);
            JCExpression expr = accessEmptySequence(tree.pos(), elemType);
            result =  castFromObject(expr, syms.javafx_SequenceTypeErasure);
        }
        else
            result = make.at(tree.pos).Literal(TypeTags.BOT, null);
    }

    public JCExpression translateSequenceExpression (JFXExpression seq) {
        return translateToExpression(seq, AsValue, null);
    }

    public JCExpression translateSequenceIndexed(DiagnosticPosition diagPos, JFXExpression seq, JCExpression tseq, JCExpression index, Type elementType) {
        Name getMethodName;
        boolean primitive = elementType.isPrimitive();

        if (primitive)
            getMethodName = defs.locationGetMethodName[typeMorpher.kindFromPrimitiveType(elementType.tsym)];
        else
            getMethodName = defs.getMethodName;
        if (seq instanceof JFXIdent) {
            JFXIdent var = (JFXIdent) seq;
            OnReplaceInfo info = findOnReplaceInfo(var.sym);
            if (info != null) {
                String mname = getMethodName.toString();
                ListBuffer<JCExpression> args = new ListBuffer<JCExpression>();
                args.append(make.TypeCast(makeTypeTree(diagPos, info.arraySequenceType, true), make.Ident(defs.onReplaceArgNameBuffer)));
                if (var.sym == info.oldValueSym) {
                    mname = mname + "FromOldValue";
                    args.append(make.TypeCast(makeTypeTree(diagPos, info.seqWithExtendsType, true), make.Ident(defs.onReplaceArgNameOld)));
                    args.append(make.Ident(defs.onReplaceArgNameFirstIndex));
                    args.append(make.Ident(defs.onReplaceArgNameLastIndex));
                }
                else { // var.sym == info.newElementsSym
                    mname = mname + "FromNewElements";
                    args.append(make.Ident(defs.onReplaceArgNameFirstIndex));
                    args.append(make.TypeCast(makeTypeTree(diagPos, info.seqWithExtendsType, true), make.Ident(defs.onReplaceArgNameNewElements)));
                }
                args.append(index);
                return callExpression(diagPos, makeQualifiedTree(diagPos, "com.sun.javafx.runtime.sequence.Sequences"),
                                      mname, args.toList());
            }
        }
        return callExpression(diagPos, tseq, getMethodName, index);
    }

    //@Override
    public void visitSequenceIndexed(final JFXSequenceIndexed tree) {
        DiagnosticPosition diagPos = tree.pos();
        JFXExpression seq = tree.getSequence();
        JCExpression index = translateAsValue(tree.getIndex(), syms.intType);
        JCExpression tseq = translateToExpression(seq, AsValue, null);
        if (seq.type.tag == TypeTags.ARRAY) {
            result = make.at(diagPos).Indexed(tseq, index);
        }
        else {
            result = translateSequenceIndexed(diagPos, seq, tseq, index, tree.type);
        }
    }

    //@Override
    public void visitSequenceSlice(JFXSequenceSlice tree) {
        DiagnosticPosition diagPos = tree.pos();
        JCExpression seq = translateAsSequenceVariable(tree.getSequence());
        JCExpression firstIndex = translateAsValue(tree.getFirstIndex(), syms.intType);
        JCExpression lastIndex = makeSliceEndPos(tree);
        JCFieldAccess select = make.at(diagPos).Select(seq, defs.getSliceMethodName);
        List<JCExpression> args = List.of(firstIndex, lastIndex);
        result = make.at(diagPos).Apply(null, select, args);
    }

    //@Override
    public void visitSequenceInsert(JFXSequenceInsert tree) {
        DiagnosticPosition diagPos = tree.pos();
        JCExpression seqLoc = translateAsSequenceVariable(tree.getSequence());
        JCExpression elem = translateAsUnconvertedValue( tree.getElement() );
        Type elemType = tree.getElement().type;
        if (types.isArray(elemType) || types.isSequence(elemType))
            elem = convertTranslated(elem, diagPos, elemType, tree.getSequence().type);
        else
            elem = convertTranslated(elem, diagPos, elemType, types.elementType(tree.getSequence().type));
        ListBuffer<JCExpression> args = new ListBuffer<JCExpression>();
        JCExpression receiver;
        if (elemType.isPrimitive()) {
            // In this case we call a static method.  Kind of ugly ...
            receiver = makeQualifiedTree(diagPos, JavafxDefs.locationPackageNameString+".SequenceVariable");
            args.append(seqLoc);
        }
        else
            receiver = seqLoc;
        args.append(elem);
        String method;
        if (tree.getPosition() == null) {
            method = "insert";
        } else {
            JCExpression position = translateAsValue(tree.getPosition(), syms.intType);
            if (tree.shouldInsertAfter())
                position = make.Binary(JCTree.PLUS, position, make.Literal(Integer.valueOf(1)));
            method = "insertBefore";
            args.append(position);
        }
        result = callStatement(diagPos, receiver, method, args.toList());
    }

    //@Override
    public void visitSequenceDelete(JFXSequenceDelete tree) {
        JFXExpression seq = tree.getSequence();

        if (tree.getElement() != null) {
            JCExpression seqLoc = translateAsSequenceVariable(seq);
            result = callStatement(tree.pos(), seqLoc, "deleteValue", translateAsUnconvertedValue(tree.getElement())); //TODO: convert to seqence type
        } else {
            if (seq.getFXTag() == JavafxTag.SEQUENCE_INDEXED) {
                // deletion of a sequence element -- delete s[i]
                JFXSequenceIndexed si = (JFXSequenceIndexed) seq;
                JFXExpression seqseq = si.getSequence();
                JCExpression seqLoc = translateAsSequenceVariable(seqseq);
                JCExpression index = translateAsValue(si.getIndex(), syms.intType);
                result = callStatement(tree.pos(), seqLoc, "delete", index);
            } else if (seq.getFXTag() == JavafxTag.SEQUENCE_SLICE) {
                // deletion of a sequence slice --  delete s[i..j]=8
                JFXSequenceSlice slice = (JFXSequenceSlice) seq;
                JFXExpression seqseq = slice.getSequence();
                JCExpression seqLoc = translateAsSequenceVariable(seqseq);
                JCExpression first = translateAsValue(slice.getFirstIndex(), syms.intType);
                JCExpression end = makeSliceEndPos(slice);
                result = callStatement(tree.pos(), seqLoc, "deleteSlice", List.of(first, end));
            } else if (types.isSequence(seq.type)) {
                JCExpression seqLoc = translateAsSequenceVariable(seq);
                result = callStatement(tree.pos(), seqLoc, "deleteAll");
            } else {
                result = make.at(tree.pos()).Exec(
                            make.Assign(
                                translateAsUnconvertedValue(seq), //TODO: does this work?
                                make.Literal(TypeTags.BOT, null)));
            }
        }
    }

    /**** utility methods ******/

     JCExpression makeSliceEndPos(JFXSequenceSlice tree) {
        JCExpression endPos;
        if (tree.getLastIndex() == null) {
            endPos = callExpression(tree,
                    translateAsUnconvertedValue(tree.getSequence()),
                    defs.sizeMethodName);
            if (tree.getEndKind() == SequenceSliceTree.END_EXCLUSIVE)
                endPos = make.at(tree).Binary(JCTree.MINUS,
                        endPos, make.Literal(TypeTags.INT, 1));
        }
        else {
            endPos = translateAsValue(tree.getLastIndex(), syms.intType);
            if (tree.getEndKind() == SequenceSliceTree.END_INCLUSIVE)
                endPos = make.at(tree).Binary(JCTree.PLUS,
                        endPos, make.Literal(TypeTags.INT, 1));
        }
        return endPos;
    }

   JCMethodDecl makeMainMethod(DiagnosticPosition diagPos, Name className) {
        List<JCStatement> mainStats;
        if (!attrEnv.toplevel.isRunnable) {
            List<JCExpression> newClassArgs = List.<JCExpression>of(make.at(diagPos).Literal(TypeTags.CLASS, className.toString()));
            mainStats = List.<JCStatement>of(make.at(diagPos).Throw(make.at(diagPos).NewClass(null, null, makeIdentifier(diagPos, noMainExceptionString), newClassArgs, null)));
        } else {
            List<JCExpression> emptyExpressionList = List.nil();
            JCExpression classIdent = make.at(diagPos).Ident(className);
            JCExpression classConstant = make.at(diagPos).Select(classIdent, names._class);
            JCExpression commandLineArgs = makeIdentifier(diagPos, "args");
            JCExpression startIdent = makeQualifiedTree(diagPos, JavafxDefs.startMethodString);
            ListBuffer<JCExpression>args = new ListBuffer<JCExpression>();
            args.append(classConstant);
            args.append(commandLineArgs);
            JCMethodInvocation runCall = make.at(diagPos).Apply(emptyExpressionList, startIdent, args.toList());
            mainStats = List.<JCStatement>of(make.at(diagPos).Exec(runCall));
        }
            List<JCVariableDecl> paramList = List.nil();
            paramList = paramList.append(make.at(diagPos).VarDef(make.Modifiers(0),
                    names.fromString("args"),
                    make.at(diagPos).TypeArray(make.Ident(names.fromString("String"))),
                    null));
            JCBlock body = make.Block(0, mainStats);
            return make.at(diagPos).MethodDef(make.Modifiers(Flags.PUBLIC | Flags.STATIC),
                    defs.mainName,
                    make.at(diagPos).TypeIdent(TypeTags.VOID),
                    List.<JCTypeParameter>nil(),
                    paramList,
                    makeThrows(diagPos),
                    body,
                    null);
    }

    /** Equivalent to make.at(pos.getStartPosition()) with side effect of caching
     *  pos as make_pos, for use in diagnostics.
     **/
    TreeMaker make_at(DiagnosticPosition pos) {
        return make.at(pos);
    }

    /** Look up a method in a given scope.
     */
    private MethodSymbol lookupMethod(DiagnosticPosition pos, Name name, Type qual, List<Type> args) {
	return rs.resolveInternalMethod(pos,getAttrEnv(), qual, name, args, null);
    }

    /** Look up a constructor.
     */
    private MethodSymbol lookupConstructor(DiagnosticPosition pos, Type qual, List<Type> args) {
	return rs.resolveInternalConstructor(pos,getAttrEnv(), qual, args, null);
    }

    /** Box up a single primitive expression. */
    JCExpression makeBox(DiagnosticPosition diagPos, JCExpression translatedExpr, Type primitiveType) {
        make_at(translatedExpr.pos());
        Type boxedType = types.boxedTypeOrType(primitiveType);
        JCExpression box;
        if (target.boxWithConstructors()) {
            Symbol ctor = lookupConstructor(translatedExpr.pos(),
                    boxedType,
                    List.<Type>nil().prepend(primitiveType));
            box = make.Create(ctor, List.of(translatedExpr));
        } else {
            Symbol valueOfSym = lookupMethod(translatedExpr.pos(),
                    names.valueOf,
                    boxedType,
                    List.<Type>nil().prepend(primitiveType));
//            JCExpression meth =makeIdentifier(valueOfSym.owner.type.toString() + "." + valueOfSym.name.toString());
            JCExpression meth = make.Select(makeTypeTree(diagPos, valueOfSym.owner.type), valueOfSym.name);
            TreeInfo.setSymbol(meth, valueOfSym);
            meth.type = valueOfSym.type;
            box = make.App(meth, List.of(translatedExpr));
        }
        return box;
    }

    public List<JCExpression> makeThrows(DiagnosticPosition diagPos) {
        return List.of(makeQualifiedTree(diagPos, methodThrowsString));
    }

    UseSequenceBuilder useSequenceBuilder(DiagnosticPosition diagPos, Type elemType, final int initLength) {
        return new UseSequenceBuilder(diagPos, elemType, null) {

            JCStatement addElement(JFXExpression exprToAdd) {
                JCExpression expr = translateAsValue(exprToAdd, targetType(exprToAdd));
                return makeAdd(expr);
            }

            List<JCExpression> makeConstructorArgs() {
                ListBuffer<JCExpression> lb = ListBuffer.lb();
                if (initLength != -1) {
                    lb.append(make.at(diagPos).Literal(Integer.valueOf(initLength)));
                }
                if (addTypeInfoArg)
                    lb.append(makeTypeInfo(diagPos, elemType));
                return lb.toList();
            }

            @Override
            JCExpression makeToSequence() {
                return makeBuilderVarAccess();
            }
        };
    }

    UseSequenceBuilder useSequenceBuilder(DiagnosticPosition diagPos, Type elemType) {
        return useSequenceBuilder(diagPos, elemType, -1);
    }


    UseSequenceBuilder useBoundSequenceBuilder(DiagnosticPosition diagPos, Type elemType, final JCExpression laziness, final int initLength) {
        return new UseSequenceBuilder(diagPos, elemType, boundSequenceBuilderString) {

            JCStatement addElement(JFXExpression exprToAdd) {
                JCExpression expr = toBound.translate(exprToAdd, targetType(exprToAdd));
                return makeAdd(expr);
            }

            List<JCExpression> makeConstructorArgs() {
                ListBuffer<JCExpression> lb = ListBuffer.lb();
                lb.append(laziness);
                if (initLength != -1) {
                    lb.append(make.at(diagPos).Literal(Integer.valueOf(initLength)));
                }
                if (addTypeInfoArg)
                    lb.append(makeTypeInfo(diagPos, elemType));
                return lb.toList();
            }
        };
    }

    abstract class UseSequenceBuilder {
        final DiagnosticPosition diagPos;
        final Type elemType;
        final String seqBuilder;

        Name sbName;
        boolean addTypeInfoArg = true;

        private UseSequenceBuilder(DiagnosticPosition diagPos, Type elemType, String seqBuilder) {
            this.diagPos = diagPos;
            this.elemType = elemType;
            this.seqBuilder = seqBuilder;
        }

        Type targetType(JFXExpression exprToAdd) {
            Type exprType = exprToAdd.type;
            if (types.isArray(exprType) || types.isSequence(exprType)) {
                return types.sequenceType(elemType);
            } else {
                Type unboxed = types.unboxedType(elemType);
                return (unboxed.tag != TypeTags.NONE) ? unboxed : elemType;
            }
        }

        JCStatement makeBuilderVar() {
            String localSeqBuilder = this.seqBuilder;
            boolean primitive = false;
            if (localSeqBuilder == null) {
                if (elemType.isPrimitive()) {
                    primitive = true;
                    addTypeInfoArg = false;
                    int kind = typeMorpher.kindFromPrimitiveType(elemType.tsym);
                    localSeqBuilder = "com.sun.javafx.runtime.sequence." + JavafxVarSymbol.getTypePrefix(kind) + "ArraySequence";
                }
                else
                    localSeqBuilder = sequenceBuilderString;
            }
            JCExpression builderTypeExpr = makeQualifiedTree(diagPos, localSeqBuilder);
            JCExpression builderClassExpr = makeQualifiedTree(diagPos, localSeqBuilder);
            if (! primitive) {
                builderTypeExpr = make.at(diagPos).TypeApply(builderTypeExpr,
                        List.of(makeTypeTree(diagPos, elemType)));
                // class name -- SequenceBuilder<elemType>
                builderClassExpr = make.at(diagPos).TypeApply(builderClassExpr,
                        List.<JCExpression>of(makeTypeTree(diagPos, elemType)));
            }

            // Sequence builder temp var name "sb"
            sbName = getSyntheticName("sb");

            // Build "sb" initializing expression -- new SequenceBuilder<T>(clazz)
            JCExpression newExpr = make.at(diagPos).NewClass(
                null,                               // enclosing
                List.<JCExpression>nil(),           // type args
                builderClassExpr, // class name -- SequenceBuilder<elemType>
                makeConstructorArgs(),              // args
                null                                // empty body
                );

            // Build the sequence builder variable
            return make.at(diagPos).VarDef(
                make.at(diagPos).Modifiers(0L),
                sbName, builderTypeExpr, newExpr);
        }

        JCIdent makeBuilderVarAccess() {
            return make.Ident(sbName);
        }

        abstract JCStatement addElement(JFXExpression expr);

        abstract List<JCExpression> makeConstructorArgs();

        JCStatement makeAdd(JCExpression expr) {
            JCMethodInvocation addCall = make.Apply(
                    List.<JCExpression>nil(),
                    make.at(diagPos).Select(
                        makeBuilderVarAccess(),
                        names.fromString("add")),
                    List.<JCExpression>of(expr));
            return make.at(diagPos).Exec(addCall);
        }

        JCExpression makeToSequence() {
            return make.Apply(
                List.<JCExpression>nil(), // type arguments
                make.at(diagPos).Select(
                    makeBuilderVarAccess(),
                    names.fromString(toSequenceString)),
                List.<JCExpression>nil() // arguments
                );
        }
    }

   JCExpression castFromObject (JCExpression arg, Type castType) {
       return make.TypeCast(makeTypeTree(arg.pos(), types.boxedTypeOrType(castType)), arg);
    }

    //@Override
    public void visitBinary(final JFXBinary tree) {
        result = (new BinaryOperationTranslator(tree.pos(), tree) {

            protected JCExpression translateArg(JFXExpression arg, Type type) {
                return translateAsValue(arg, type);
            }
        }).doit();
    }

    public void visitBreak(JFXBreak tree) {
        result = make.at(tree.pos).Break(tree.label);
    }

    public void visitCatch(JFXCatch tree) {
        JCVariableDecl param = translate(tree.param);
        JCBlock body = translateBlockExpressionToBlock(tree.body);
        result = make.at(tree.pos).Catch(param, body);
    }

    /**
     * assume seq is a sequence of element type U
     * convert   for (x in seq where cond) { body }
     * into the following block expression
     *
     *   {
     *     SequenceBuilder<T> sb = new SequenceBuilder<T>(clazz);
     *     for (U x : seq) {
     *       if (!cond)
     *         continue;
     *       sb.add( { body } );
     *     }
     *     sb.toSequence()
     *   }
     *
     * **/
    //@Override
    public void visitForExpression(JFXForExpression tree) {
        Type targetType = translationState.targetType;
        Yield yield = translationState.yield;

        // sub-translation in done inline -- no super.visitForExpression(tree);
        if (yield == ToStatement && targetType == syms.voidType) {
             result = wrapWithInClause(tree, translateToStatement(tree.getBodyExpression()));
        } else {
            // body has value (non-void)
            assert tree.type != syms.voidType : "should be handled above";
            DiagnosticPosition diagPos = tree.pos();
            ListBuffer<JCStatement> stmts = ListBuffer.lb();
            JCStatement stmt;
            JCExpression value;

            // Compute the element type from the sequence type
            assert tree.type.getTypeArguments().size() == 1;
            Type elemType = types.elementType(tree.type);

            UseSequenceBuilder builder = useSequenceBuilder(diagPos, elemType);
            stmts.append(builder.makeBuilderVar());

            // Build innermost loop body
            stmt = builder.addElement( tree.getBodyExpression() );

            // Build the result value
            value = builder.makeToSequence();
            stmt = wrapWithInClause(tree, stmt);
            stmts.append(stmt);

            if (yield == ToStatement) {
                stmts.append(make.at(tree).Return(value));
                result = make.at(diagPos).Block(0L, stmts.toList());
            } else {
                // Build the block expression -- which is what we translate to
                result = makeBlockExpression(diagPos, stmts, value);
            }
        }
    }

    //where
    private JCStatement wrapWithInClause(JFXForExpression tree, JCStatement coreStmt) {
        JCStatement stmt = coreStmt;
        for (int inx = tree.getInClauses().size() - 1; inx >= 0; --inx) {
            JFXForExpressionInClause clause = (JFXForExpressionInClause)tree.getInClauses().get(inx);
            stmt = new InClauseTranslator(clause, stmt).doit();
        }
        return stmt;
    }


    /**
     * Translator class for for-expression in/where clauses
     */
    private class InClauseTranslator extends Translator {
        final JFXForExpressionInClause clause;      // in clause being translated
        final JFXVar var;                           // user named JavaFX induction variable
        final Type type;                            // type of the induction variable
        final JCVariableDecl inductionVar;          // generated induction variable
        JCStatement body;                           // statement being generated by wrapping
        boolean indexedLoop;

        InClauseTranslator(JFXForExpressionInClause clause, JCStatement coreStmt) {
            super(clause);
            this.clause = clause;
            this.var = clause.getVar();
            this.type = var.type;
            this.body = coreStmt;
            JFXExpression seq = clause.seqExpr;
            this.indexedLoop =
                    (seq.getFXTag() == JavafxTag.SEQUENCE_SLICE ||
                     (seq.getFXTag() != JavafxTag.SEQUENCE_RANGE &&
                      types.isSequence(seq.type)));
            this.inductionVar = makeVar("ind", indexedLoop ? syms.intType : type, null);
        }

        private JCVariableDecl makeVar(String id, Type varType, JCExpression initialValue) {
            return makeVar(0L, id, varType, initialValue);
        }

        private JCVariableDecl makeFinalVar(String id, JCExpression initialValue) {
            return makeFinalVar(id, type, initialValue);
        }

        private JCVariableDecl makeFinalVar(String id, Type varType, JCExpression initialValue) {
            return makeVar(Flags.FINAL, id, varType, initialValue);
        }

        private JCVariableDecl makeFinalVar(Name varName, JCExpression initialValue) {
            return makeFinalVar(varName, type, initialValue);
        }

        private JCVariableDecl makeFinalVar(Name varName, Type varType, JCExpression initialValue) {
            return makeVar(Flags.FINAL, varName, varType, initialValue);
        }

        private JCVariableDecl makeVar(long flags, String id, Type varType, JCExpression initialValue) {
            Name varName = names.fromString(var.name.toString() + "$" + id);
            return makeVar(flags, varName, varType, initialValue);
        }

        private JCVariableDecl makeVar(long flags, Name varName, Type varType, JCExpression initialValue) {
            return m().VarDef(
                    m().Modifiers(flags),
                    varName,
                    makeTypeTree(var, varType, true),
                    initialValue);
        }

        /**
         * Generate a range sequence conditional test.
         * Result depends on if the range is ascending/descending and if the range is exclusive
         */
        private JCExpression condTest(JFXSequenceRange range, boolean stepNegative, JCVariableDecl upperVar) {
            int op;
            if (stepNegative) {
                if (range.isExclusive()) {
                    op = JCTree.GT;
                } else {
                    op = JCTree.GE;
                }
            } else {
                if (range.isExclusive()) {
                    op = JCTree.LT;
                } else {
                    op = JCTree.LE;
                }
            }
            return m().Binary(op, ident(inductionVar), ident(upperVar));
        }

        /**
         * Determine if a literal is negative
         */
        private boolean isNegative(JFXExpression expr) {
            JFXLiteral lit = (JFXLiteral) expr;
            Object val = lit.getValue();

            switch (lit.typetag) {
                case TypeTags.INT:
                    return ((int) (Integer) val) < 0;
                case TypeTags.SHORT:
                    return ((short) (Short) val) < 0;
                case TypeTags.BYTE:
                    return ((byte) (Byte) val) < 0;
                case TypeTags.CHAR:
                    return ((char) (Character) val) < 0;
                case TypeTags.LONG:
                    return ((long) (Long) val) < 0L;
                case TypeTags.FLOAT:
                    return ((float) (Float) val) < 0.0f;
                case TypeTags.DOUBLE:
                    return ((double) (Double) val) < 0.0;
                default:
                    throw new AssertionError("unexpected literal kind " + this);
            }
        }

        /**
         * Generate the loop for a slice sequence.  Loop wraps the current body.
         * For the loop:
         *    for (x in seq[lo..<hi]) body
         * Generate:
         *     for (int $i = max(lo,0);  i++; $i < min(hi,seq.size())) { def x = seq[$i]; body }
         * (We may need to put seq and/or hi in a temporary variable first.)
         */
        void translateSliceInClause(JFXExpression seq, JFXExpression first, JFXExpression last, int endKind, JCVariableDecl seqVar) {
            // Collect all the loop initializing statements (variable declarations)
            ListBuffer<JCStatement> tinits = ListBuffer.lb();
            if (! (seq instanceof JFXIdent) || findOnReplaceInfo(((JFXIdent) seq).sym) == null)
                tinits.append(seqVar);
            JCExpression init = translateAsValue(first, syms.intType);
            boolean maxForStartNeeded = true;
            if (first == null)
                init = make.Literal(TypeTags.INT, 0);
            else {
                if (first.getFXTag() == JavafxTag.LITERAL && ! isNegative(first))
                    maxForStartNeeded = false;
                // FIXME set maxForStartNeeded false if first is replace-trigger startPos and seq is oldValue
                if (maxForStartNeeded)
                    init = callExpression(first,
                           makeQualifiedTree(first, "java.lang.Math"), "max",
                           List.of(init, make.Literal(TypeTags.INT, 0)));
            }
            inductionVar.init = init;
            tinits.append(inductionVar);
            JCExpression sizeExpr = translateSizeof(diagPos, seq, ident(seqVar));
            //callExpression(diagPos, ident(seqVar), "size");
            JCExpression limitExpr;
            // Compare the logic in makeSliceEndPos.
            if (last == null) {
                limitExpr = sizeExpr;
                if (endKind == SequenceSliceTree.END_EXCLUSIVE)
                    limitExpr = make.at(diagPos).Binary(JCTree.MINUS,
                        limitExpr, make.Literal(TypeTags.INT, 1));
            }
            else {
                limitExpr = translateAsValue(last, syms.intType);
                if (endKind == SequenceSliceTree.END_INCLUSIVE)
                    limitExpr = make.at(last).Binary(JCTree.PLUS,
                        limitExpr, make.Literal(TypeTags.INT, 1));
                // FIXME can optimize if last is replace-trigger endPos and seq is oldValue
                if (true)
                    limitExpr = callExpression(last,
                           makeQualifiedTree(last, "java.lang.Math"), "min",
                           List.of(limitExpr, sizeExpr));
            }
            JCVariableDecl limitVar = makeFinalVar("limit", syms.intType, limitExpr);
            tinits.append(limitVar);
            // The condition that will be tested each time through the loop
            JCExpression tcond = make.Binary(JCTree.LT, ident(inductionVar), ident(limitVar));
            // Generate the step statement as: x += 1
            List<JCExpressionStatement> tstep = List.of(m().Exec(m().Assignop(JCTree.PLUS_ASG, ident(inductionVar), m().Literal(TypeTags.INT, 1))));
            tinits.append(m().ForLoop(List.<JCStatement>nil(), tcond, tstep, body));
            body = make.Block(0L, tinits.toList());
        }

        /**
         * Generate the loop for a range sequence.  Loop wraps the current body.
         * For the loop:
         *    for (x in [lo..hi step st]) body
         * Generate (assuming x is float):
         *    for (float x = lo, final float x$upper = up, final float x$step = st;, final boolean x$negative = x$step < 0.0;
         *        x$negative? x >= x$upper : x <= x$upper;
         *        x += x$step)
         *            body
         * Without a step specified (or a literal step) the form reduces to:
         *    for (float x = lo, final float x$upper = up;
         *        x <= x$upper;
         *        x += 1)
         *            body
         */
        void translateRangeInClause() {
            JFXSequenceRange range = (JFXSequenceRange) clause.seqExpr;
            // Collect all the loop initializing statements (variable declarations)
            ListBuffer<JCStatement> tinits = ListBuffer.lb();
            // Set the initial value of the induction variable to be the low end of the range, and add it the the initializing statements
            inductionVar.init = translateAsValue(range.getLower(), type);
            tinits.append(inductionVar);
            // Record the upper end of the range in a final variable, and add it the the initializing statements
            JCVariableDecl upperVar = makeFinalVar("upper", translateAsValue(range.getUpper(), type));
            tinits.append(upperVar);
            // The expression which will be used in increment the induction variable
            JCExpression tstepIncrExpr;
            // The condition that will be tested each time through the loop
            JCExpression tcond;
            // The user's step expression, or null if none specified
            JFXExpression step = range.getStepOrNull();
            if (step != null) {
                // There is a user specified step expression
                JCExpression stepVal = translateAsValue(step, type);
                if (step.getFXTag() == JavafxTag.LITERAL) {
                    // The step expression is a literal, no need for a variable to hold it, and we can test if the range is scending at compile time
                    tstepIncrExpr = stepVal;
                    tcond = condTest(range, isNegative(step), upperVar);
                } else {
                    // Arbitrary step expression, do all the madness shown in the method comment
                    JCVariableDecl stepVar = makeFinalVar("step", stepVal);
                    tinits.append(stepVar);
                    tstepIncrExpr = ident(stepVar);
                    JCVariableDecl negativeVar = makeFinalVar("negative", syms.booleanType, m().Binary(JCTree.LT, ident(stepVar), m().Literal(type.tag, 0)));
                    tinits.append(negativeVar);
                    tcond = m().Conditional(ident(negativeVar), condTest(range, true, upperVar), condTest(range, false, upperVar));
                }
            } else {
                // No step expression, use one as the increment
                tstepIncrExpr = m().Literal(type.tag, 1);
                tcond = condTest(range, false, upperVar);
            }
            // Generate the step statement as: x += x$step
            List<JCExpressionStatement> tstep = List.of(m().Exec(m().Assignop(JCTree.PLUS_ASG, ident(inductionVar), tstepIncrExpr)));
            // Finally, build the for loop
            body = m().ForLoop(tinits.toList(), tcond, tstep, body);
        }

        /**
         * Make an identifier which references the specified variable declaration
         */
        private JCIdent ident(JCVariableDecl aVar) {
            return m().Ident(aVar.name);
        }

        /**
         * Core of the in-clause translation.  Given an in-clause and the current body, build the for-loop
         */
        public JCStatement doit() {
            // If there is a where expression, make the execution of the body conditional on the where condition
            if (clause.getWhereExpression() != null) {
                body = m().If(translateAsUnconvertedValue(clause.getWhereExpression()), body, null);
            }

            // Because the induction variable may be used in inner contexts, make a final
            // variable inside the loop that holds the current iterations value.
            // Same with the index if used.   That is:
            //   for (x in seq) body
            // Becomes (assume Number sequence):
            //   x$incrindex = 0;
            //   loop over x$ind {
            //     final int x$index = x$incrindex++;
            //     final float x = x$ind;
            //     body;
            //   }
            JCVariableDecl incrementingIndexVar = null;
            JFXExpression seq = clause.seqExpr;
            JCVariableDecl seqVar = null;
            {
                ListBuffer<JCStatement> stmts = ListBuffer.lb();
                diagPos = var;
                if (clause.getIndexUsed()) {
                    incrementingIndexVar = makeVar("incrindex", syms.javafx_IntegerType, m().Literal(Integer.valueOf(0)));
                    JCVariableDecl finalIndexVar = makeFinalVar(
                            indexVarName(clause),
                            syms.javafx_IntegerType,
                            m().Unary(JCTree.POSTINC, ident(incrementingIndexVar)));
                    stmts.append(finalIndexVar);
                }
                JCExpression varInit;     // Initializer for var.

                if (indexedLoop) {
                    JFXExpression sseq;
                    if (clause.seqExpr instanceof JFXSequenceSlice)
                        sseq = ((JFXSequenceSlice) clause.seqExpr).getSequence();
                    else
                       sseq = clause.seqExpr;
                    seqVar = makeFinalVar("seq", seq.type, translateAsValue(sseq, seq.type));
                    varInit = translateSequenceIndexed(diagPos, sseq, ident(seqVar), ident(inductionVar), type);
                }
                else
                    varInit = ident(inductionVar);

                stmts.append(makeFinalVar(var.getName(), varInit));
                stmts.append(body);
                body = m().Block(0L, stmts.toList());
            }

            // Translate the sequence into the loop
            diagPos = seq;
            if (seq.getFXTag() == JavafxTag.SEQUENCE_RANGE) {
                // Iterating over a range sequence
                translateRangeInClause();
            } else if (seq.getFXTag() == JavafxTag.SEQUENCE_SLICE) {
                JFXSequenceSlice slice = (JFXSequenceSlice) clause.seqExpr;
                translateSliceInClause(slice.getSequence(), slice.getFirstIndex(), slice.getLastIndex(),
                        slice.getEndKind(), seqVar);
            } else {
                // We will be using the sequence as a whole, so translate it
                JCExpression tseq = translateAsUnconvertedValue(seq);
                if (types.isSequence(seq.type)) {
                    // Iterating over a non-range sequence, use a foreach loop, but first convert null to an empty sequence
                    tseq = runtime(diagPos,
                            defs.Sequences_forceNonNull,
                            List.of(makeTypeInfo(diagPos, type), tseq));
                    translateSliceInClause(seq, null, null, SequenceSliceTree.END_INCLUSIVE, seqVar);
                    //body = m().ForeachLoop(inductionVar, tseq, body);
                } else if (seq.type.tag == TypeTags.ARRAY ||
                             types.asSuper(seq.type, syms.iterableType.tsym) != null) {
                    // Iterating over an array or iterable type, use a foreach loop
                    body = m().ForeachLoop(inductionVar, tseq, body);
                } else {
                    // The "sequence" isn't aactually a sequence, treat it as a singleton.
                    // Compile: { var tmp = seq; if (tmp!=null) body; }
                    if (!type.isPrimitive()) {
                        body = m().If(m().Binary(JCTree.NE, ident(inductionVar),
                                m().Literal(TypeTags.BOT, null)),
                                body, null);
                    }
                    // the "induction" variable will have only one value, set it to that
                    inductionVar.init = tseq;
                    // wrap the induction variable and the body in a block to protect scope
                    body = m().Block(0L, List.of(inductionVar, body));
                }
            }

            if (clause.getIndexUsed()) {
                // indexof is used, define the index counter variable at the top of everything
                body = m().Block(0L, List.of(incrementingIndexVar, body));
            }

            return body;
        }
    }

    public void visitIndexof(JFXIndexof tree) {
        final DiagnosticPosition diagPos = tree.pos();
        assert tree.clause.getIndexUsed() : "assert that index used is set correctly";
        JCExpression transIndex = make.at(diagPos).Ident(indexVarName(tree.fname));
        VarSymbol vsym = (VarSymbol)tree.clause.getVar().sym;
        if (representation(vsym).possiblyLocation()) {
            // from inside the bind, its a Location, convert to value
            result = getLocationValue(diagPos, transIndex, TYPE_KIND_INT);
        } else {
            // it came from outside of the bind, not a Location
            result = transIndex;
        }
    }

    //@Override
    public void visitIfExpression(JFXIfExpression tree) {
        Yield yield = translationState.yield;

        final DiagnosticPosition diagPos = tree.pos();
        JCExpression cond = translateAsUnconvertedValue(tree.getCondition());
        JFXExpression trueSide = tree.getTrueExpression();
        JFXExpression falseSide = tree.getFalseExpression();
        if (yield == ToExpression) {
            Type targetType = tree.type;
            result = make.at(diagPos).Conditional(
                    cond,
                    translateAsValue(trueSide, targetType),
                    translateAsValue(falseSide, targetType));
        } else {
            Type targetType = translationState.targetType;
            result = make.at(diagPos).If(
                    cond,
                    translateToStatement(trueSide, targetType),
                    falseSide == null ? null : translateToStatement(falseSide, targetType));
        }
    }

    //@Override
    public void visitContinue(JFXContinue tree) {
        result = make.at(tree.pos).Continue(tree.label);
    }

    //@Override
    public void visitErroneous(JFXErroneous tree) {
        List<? extends JCTree> errs = translateGeneric(tree.getErrorTrees());
        result = make.at(tree.pos).Erroneous(errs);
    }

    //@Override
    public void visitReturn(JFXReturn tree) {
        JFXExpression exp = tree.getExpression();
        if (exp == null) {
            result = make.at(tree).Return(null);
        } else {
            result = translateToStatement(exp, tree.returnType);
        }
    }

    //@Override
    public void visitParens(JFXParens tree) {
        Type targetType = translationState.targetType;
        Yield yield = translationState.yield;

        if (yield == ToExpression) {
            JCExpression expr = translateAsUnconvertedValue(tree.expr);
            result = make.at(tree.pos).Parens(expr);
        } else {
            result = translateToStatement(tree.expr, targetType);
        }
    }

    //@Override
    public void visitImport(JFXImport tree) {
        //JCTree qualid = straightConvert(tree.qualid);
        //result = make.at(tree.pos).Import(qualid, tree.staticImport);
        assert false : "should be processed by parent tree";
    }

    //@Override
    public void visitInstanceOf(JFXInstanceOf tree) {
        Type type = types.boxedTypeOrType(tree.clazz.type);
        JCTree clazz = this.makeTypeTree( tree, type);
        JCExpression expr = translateAsUnconvertedValue(tree.expr);
        if (tree.expr.type.isPrimitive()) {
            expr = this.makeBox(tree.expr.pos(), expr, tree.expr.type);
        }
        if (types.isSequence(tree.expr.type) && ! types.isSequence(type))
            expr = callExpression(tree.expr,
                    makeQualifiedTree(tree.expr, "com.sun.javafx.runtime.sequence.Sequences"),
                    "getSingleValue", expr);
        result = make.at(tree.pos).TypeTest(expr, clazz);
    }

    //@Override
    public void visitTypeCast(final JFXTypeCast tree) {
        final DiagnosticPosition diagPos = tree.pos();

        JCExpression val = translateAsValue(tree.expr, tree.clazz.type);
        // The makeTypeCast below is usually redundant, since translateAsValue
        // takes care of most conversions - except in the case of a plain object cast.
        // It would be cleaner to move the makeTypeCast to translateAsValue,
        // but it's painful to get it right.  FIXME.
        JCExpression ret = typeCast(diagPos, tree.clazz.type, tree.expr.type, val);
        result = convertNullability(diagPos, ret, tree.expr, tree.clazz.type);
    }

    //@Override
    public void visitLiteral(JFXLiteral tree) {
        if (tree.typetag == TypeTags.BOT && types.isSequence(tree.type)) {
            Type elemType = types.boxedElementType(tree.type);
            JCExpression expr = accessEmptySequence(tree.pos(), elemType);
            result =  castFromObject(expr, syms.javafx_SequenceTypeErasure);
        } else {
            result = make.at(tree.pos).Literal(tree.typetag, tree.value);
        }
    }

    //@Override
    public void visitFunctionInvocation(final JFXFunctionInvocation tree) {
        final Locationness wrapper = translationState.wrapper;

        result = (new FunctionCallTranslator(tree) {
            private Name funcName = null;

            protected JCTree doit() {
                JFXExpression toCheckOrNull;
                boolean knownNonNull;

                if (useInvoke) {
                    // this is a function var call, check the whole expression for null
                    toCheckOrNull = meth;
                    funcName = defs.invokeName;
                    knownNonNull = false;
                } else if (selector == null) {
                    // This is not an function var call and not a selector, so we assume it is a simple foo()
                    if (meth.getFXTag() == JavafxTag.IDENT) {
                        JFXIdent fr = fxm().Ident(functionName(msym, superToStatic, callBound));
                        fr.type = meth.type;
                        fr.sym = msym;
                        toCheckOrNull = fr;
                        funcName = null;
                        knownNonNull = true;
                    } else {
                        // Should never get here
                        assert false : meth;
                        toCheckOrNull = meth;
                        funcName = null;
                        knownNonNull = true;
                    }
                } else {
                    // Regular selector call  foo.bar() -- so, check the selector not the whole meth
                    toCheckOrNull = selector;
                    funcName = functionName(msym, superToStatic, callBound);
                    knownNonNull =  selector.type.isPrimitive() || !selectorMutable;
                }

                return new NullCheckTranslator(diagPos, toCheckOrNull, returnType, knownNonNull, wrapper) {

                    List<JCExpression> args = determineArgs();

                    JCExpression translateToCheck(JFXExpression expr) {
                        JCExpression trans;
                        if (renameToSuper || superCall) {
                           trans = m().Ident(names._super);
                        } else if (renameToThis || thisCall) {
                           trans = m().Ident(names._this);
                        } else if (superToStatic) {
                            trans = makeTypeTree(diagPos, types.erasure(msym.owner.type), false);
                        } else if (selector != null && !useInvoke && msym != null && msym.isStatic()) {
                            //TODO: clean this up -- handles referencing a static function via an instance
                            trans = makeTypeTree(diagPos, types.erasure(msym.owner.type), false);
                        } else {
                            if (selector != null && msym != null && !msym.isStatic()) {
                                Symbol selectorSym = expressionSymbol(selector);
                                // If this is OuterClass.memberName() then we want to
                                // to create expression to get the proper receiver.
                                if (selectorSym != null && selectorSym.kind == Kinds.TYP) {
                                    trans = makeReceiver(diagPos, msym);
                                    return trans;
                                }
                            }

                            trans = translateAsUnconvertedValue(expr);
                            if (expr.type.isPrimitive()) {
                                // Java doesn't allow calls directly on a primitive, wrap it
                                trans = makeBox(diagPos, trans, expr.type);
                            }
                        }
                        return trans;
                    }

                    @Override
                    JCExpression fullExpression( JCExpression mungedToCheckTranslated) {
                        JCExpression tc = mungedToCheckTranslated;
                        if (funcName != null) {
                            // add the selector name back
                            tc = m().Select(tc, funcName);
                        }
                        JCMethodInvocation app =  m().Apply(translateExpressions(tree.typeargs), tc, args);

                        JCExpression full = callBound ? makeBoundCall(app) : app;
                        if (useInvoke) {
                            if (tree.type.tag != TypeTags.VOID) {
                                full = castFromObject(full, tree.type);
                            }
                        }
                        return full;
                    }

                    /**
                     * Compute the translated arguments.
                     */
                    List<JCExpression> determineArgs() {
                        ListBuffer<JCExpression> targs = ListBuffer.lb();
                        // if this is a super.foo(x) call, "super" will be translated to referenced class,
                        // so we add a receiver arg to make a direct call to the implementing method  MyClass.foo(receiver$, x)
                        if (superToStatic) {
                            targs.append(make.Ident(defs.receiverName));
                        }

                        if (callBound) {
                            //TODO: this code looks completely messed-up
                            /**
                             * If this is a bound call, use left-hand side references for arguments consisting
                             * solely of a  var or attribute reference, or function call, otherwise, wrap it
                             * in an expression location
                             */
                            List<Type> formal = formals;
                            for (JFXExpression arg : tree.args) {
                                switch (arg.getFXTag()) {
                                    case IDENT:
                                    case SELECT:
                                    case APPLY:
                                        // This arg expression is one that will translate into a Location;
                                        // since that is needed for a this into Location, do so.
                                        // However, if the types need to by changed (subclass), this won't
                                        // directly work.
                                        // Also, if this is a mismatched sequence type, we will need
                                        // to do some different
                                        //TODO: never actually gets here
                                        if (arg.type.equals(formal.head) ||
                                                types.isSequence(formal.head) ||
                                                formal.head == syms.objectType // don't add conversion for parameter type of java.lang.Object: doing so breaks the Pointer trick to obtain the original location (JFC-826)
                                                ) {
                                            targs.append(translateAsLocation(arg));
                                            break;
                                        }
                                    //TODO: handle sequence subclasses
                                    //TODO: use more efficient mechanism (use currently apears rare)
                                    //System.err.println("Not match: " + arg.type + " vs " + formal.head);
                                    // Otherwise, fall-through, presumably a conversion will work.
                                    default: {
                                        targs.append(makeUnboundLocation(
                                                arg.pos(),
                                                typeMorpher.typeMorphInfo(formal.head),
                                                translateAsValue(arg, arg.type)));
                                    }
                                }
                                formal = formal.tail;
                            }
                        } else {
                            boolean handlingVarargs = false;
                            Type formal = null;
                            List<Type> t = formals;
                            for (List<JFXExpression> l = tree.args; l.nonEmpty(); l = l.tail) {
                                if (!handlingVarargs) {
                                    formal = t.head;
                                    t = t.tail;
                                    if (usesVarArgs && t.isEmpty()) {
                                        formal = types.elemtype(formal);
                                        handlingVarargs = true;
                                    }
                                }
                                JCExpression targ;
                                if (magicIsInitializedFunction) {
                                    //TODO: in theory, this could have side-effects (but only in theory)
                                    targ = translateAsLocation(l.head);
                                } else {
                                    targ = preserveSideEffects(formal, l.head, translateAsValue(l.head, formal));
                                }
                                targs.append(targ);
                            }
                        }
                        return targs.toList();
                    }
                }.doit();
            }


            // This is for calls from non-bound contexts (code for true bound calls is in JavafxToBound)
            JCExpression makeBoundCall(JCExpression applyExpression) {
                JavafxTypeMorpher.TypeMorphInfo tmi = typeMorpher.typeMorphInfo(msym.getReturnType());
                if (wrapper == AsLocation) {
                    return applyExpression;
                } else {
                    return callExpression(diagPos,
                        m().Parens(applyExpression),
                        defs.locationGetMethodName[tmi.getTypeKind()]);
                }
            }

        }).doit();
    }

    //@Override
    public void visitModifiers(JFXModifiers tree) {
        result = make.at(tree.pos).Modifiers(tree.flags, List.<JCAnnotation>nil());
    }

    //@Override
    public void visitSkip(JFXSkip tree) {
        result = make.at(tree.pos).Skip();
    }

    //@Override
    public void visitThrow(JFXThrow tree) {
        JCTree expr = translateAsUnconvertedValue(tree.expr);
        result = make.at(tree.pos).Throw(expr);
    }

    //@Override
    public void visitTry(JFXTry tree) {
        JCBlock body = translateBlockExpressionToBlock(tree.body);
        List<JCCatch> catchers = translateCatchers(tree.catchers);
        JCBlock finalizer = translateBlockExpressionToBlock(tree.finalizer);
        result = make.at(tree.pos).Try(body, catchers, finalizer);
    }

    //@Override
    public void visitUnary(final JFXUnary tree) {
        final Locationness wrapper = translationState.wrapper;

        result = (new Translator( tree.pos() ) {
            private final JFXExpression expr = tree.getExpression();

            private JCExpression translateForSizeof(JFXExpression expr) {
                return translateSequenceExpression(expr);
            }
            private final JCExpression transExpr =
                    tree.getFXTag() == JavafxTag.SIZEOF && wrapper == AsValue &&
                (expr instanceof JFXIdent || expr instanceof JFXSelect) ? translateForSizeof(expr)
                : translateAsUnconvertedValue(expr);

            private JCExpression doIncDec(final int binaryOp, final boolean postfix) {
                return (JCExpression) new AssignTranslator(diagPos, expr, fxm().Literal(1)) {

                    private JCExpression castIfNeeded(JCExpression transExpr) {
                        int ttag = expr.type.tag;
                        if (ttag == TypeTags.BYTE || ttag == TypeTags.SHORT) {
                            return m().TypeCast(expr.type, transExpr);
                        }
                        return transExpr;
                    }

                    @Override
                    JCExpression buildRHS(JCExpression rhsTranslated) {
                        return castIfNeeded(m().Binary(binaryOp, transExpr, rhsTranslated));
                    }

                    @Override
                    JCExpression defaultFullExpression( JCExpression lhsTranslated, JCExpression rhsTranslated) {
                        return m().Unary(tree.getOperatorTag(), lhsTranslated);
                    }

                    @Override
                    protected JCExpression postProcess(JCExpression built) {
                        if (postfix) {
                            // this is a postfix operation, undo the value (not the variable) change
                            return castIfNeeded(m().Binary(binaryOp, (JCExpression) built, m().Literal(-1)));
                        } else {
                            // prefix operation
                            return built;
                        }
                    }
                }.doit();
            }

            public JCTree doit() {
                switch (tree.getFXTag()) {
                    case SIZEOF:
                        if (expr.type.tag == TypeTags.ARRAY) {
                            return m().Select(transExpr, defs.lengthName);
                        }
                        return translateSizeof(diagPos, expr, transExpr);
                    case REVERSE:
                        if (types.isSequence(expr.type)) {
                            // call runtime reverse of a sequence
                            return callExpression(diagPos,
                                    makeQualifiedTree(diagPos, "com.sun.javafx.runtime.sequence.Sequences"),
                                    "reverse", transExpr);
                        } else {
                            // this isn't a sequence, just make it a sequence
                            return convertTranslated(transExpr, diagPos, expr.type, tree.type);
                        }
                    case PREINC:
                        return doIncDec(JCTree.PLUS, false);
                    case PREDEC:
                        return doIncDec(JCTree.MINUS, false);
                    case POSTINC:
                        return doIncDec(JCTree.PLUS, true);
                    case POSTDEC:
                        return doIncDec(JCTree.MINUS, true);
                    case NEG:
                        if (types.isSameType(tree.type, syms.javafx_DurationType)) {
                            return m().Apply(null, m().Select(translateAsUnconvertedValue(tree.arg), names.fromString("negate")), List.<JCExpression>nil());
                        }
                    default:
                        return m().Unary(tree.getOperatorTag(), transExpr);
                }
            }
        }).doit();
    }

    JCExpression translateSizeof(DiagnosticPosition diagPos, JFXExpression expr, JCExpression transExpr) {
        if (expr instanceof JFXIdent) {
            JFXIdent var = (JFXIdent) expr;
            OnReplaceInfo info = findOnReplaceInfo(var.sym);
            if (info != null) {
                String mname;
                ListBuffer<JCExpression> args = new ListBuffer<JCExpression>();
                args.append(make.Ident(defs.onReplaceArgNameBuffer));
                if (var.sym == info.oldValueSym) {
                    mname = "sizeOfOldValue";
                    args.append(make.Ident(defs.onReplaceArgNameOld));
                    args.append(make.Ident(defs.onReplaceArgNameLastIndex));
                }
                else { // var.sym == info.newElementsSym
                    mname = "sizeOfNewElements";
                    args.append(make.Ident(defs.onReplaceArgNameFirstIndex));
                    args.append(make.Ident(defs.onReplaceArgNameNewElements));
                }
                return callExpression(diagPos, makeQualifiedTree(diagPos, "com.sun.javafx.runtime.sequence.Sequences"),
                        mname, args.toList());
            }
        }
        return runtime(diagPos, defs.Sequences_size, List.of(transExpr));
    }

    //@Override
    public void visitWhileLoop(JFXWhileLoop tree) {
        JCStatement body = translateToStatement(tree.body);
        JCExpression cond = translateAsUnconvertedValue(tree.cond);
        result = make.at(tree.pos).WhileLoop(cond, body);
    }

    /******** goofy visitors, most of which should go away ******/

    public void visitObjectLiteralPart(JFXObjectLiteralPart that) {
        assert false : "should be processed by parent tree";
    }

    public void visitTypeAny(JFXTypeAny that) {
        assert false : "should be processed by parent tree";
    }

    public void visitTypeClass(JFXTypeClass that) {
        assert false : "should be processed by parent tree";
    }

    public void visitTypeFunctional(JFXTypeFunctional that) {
        assert false : "should be processed by parent tree";
    }

    //@Override
    public void visitTypeArray(JFXTypeArray tree) {
        assert false : "should be processed by parent tree";
    }

    public void visitTypeUnknown(JFXTypeUnknown that) {
        assert false : "should be processed by parent tree";
    }

    public void visitForExpressionInClause(JFXForExpressionInClause that) {
        assert false : "should be processed by parent tree";
    }

    /***********************************************************************
     *
     * Utilities
     *
     */

    protected String getSyntheticPrefix() {
        return "jfx$";
    }

   VarRepresentation representation(Symbol sym) {
        return sym == null? NeverLocation : typeMorpher.varMorphInfo(sym).representation();
    }

    JCBlock translatedOnReplaceBody(JFXOnReplace onr) {
        return (onr == null)?  null : translateBlockExpressionToBlock(onr.getBody());
    }

    JCExpression convertVariableReference(DiagnosticPosition diagPos,
                                                 JCExpression varRef, Symbol sym,
                                                 Locationness wrapper) {
        JCExpression expr = varRef;

        if (sym instanceof VarSymbol) {
            final VarSymbol vsym = (VarSymbol) sym;
            VarMorphInfo vmi = typeMorpher.varMorphInfo(vsym);
            int typeKind = vmi.getTypeKind();
            boolean isSequence = vmi.isSequence();
            boolean isClassVar = vmi.isFXMemberVariable();

            if (isClassVar) {
                // this is a reference to a JavaFX class variable, use getter
                Name accessName = ((wrapper == AsLocation) || isSequence)? attributeGetLocationName(vsym) : attributeGetterName(vsym);
                JCExpression accessFunc = switchName(diagPos, varRef, accessName);
                List<JCExpression> emptyArgs = List.nil();
                expr = make.at(diagPos).Apply(null, accessFunc, emptyArgs);
            }

            if (!vmi.useAccessors()) {
                if (vmi.representation() == AlwaysLocation && wrapper != AsLocation) {
                    // Anything still in the form of a Location (and that isn't what we want), get the value
                    if (isSequence || !isClassVar) {
                        expr = getLocationValue(diagPos, expr, typeKind);
                    }
                } else if (vmi.representation() == NeverLocation && wrapper == AsLocation) {
                    // We are directly accessing a non-Location local, a Location is wanted, wrap it
                    assert !isClassVar;
                    expr = makeUnboundLocation(diagPos, vmi, expr);
                }
            }
        }

        return expr;
    }
    //where
    private JCExpression switchName(DiagnosticPosition diagPos, JCExpression identOrSelect, Name name) {
        switch (identOrSelect.getTag()) {
            case JCTree.IDENT:
                return make.at(diagPos).Ident(name);
            case JCTree.SELECT:
                return make.at(diagPos).Select(((JCFieldAccess)identOrSelect).getExpression(), name);
            default:
                throw new AssertionError();
        }
    }

    JCExpression makeReceiver(DiagnosticPosition diagPos, Symbol sym) {
        return makeReceiver(diagPos, sym, false);
    }

    /**
     * Build the AST for accessing the outer member.
     * The accessors might be chained if the member accessed is more than one level up in the outer chain.
     * */
    JCExpression makeReceiver(DiagnosticPosition diagPos, Symbol sym, boolean nullForThis) {
        // !sym.isStatic()
        Symbol siteOwner = getAttrEnv().enclClass.sym;
        // This following cannot be used until anonymous classes like BoundComprehensions are handled
        // JCExpression ret = make.Ident(inInstanceContext == ReceiverContext.InstanceAsStatic ? defs.receiverName : names._this);
        JCExpression thisExpr = make.at(diagPos).Select(makeTypeTree(diagPos, siteOwner.type), names._this);
        JCExpression ret = inInstanceContext == ReceiverContext.InstanceAsStatic ?
            make.at(diagPos).Ident(defs.receiverName) :
            thisExpr;
        ret.type = siteOwner.type;

        // check if it is in the chain
        if (sym != null && siteOwner != null && siteOwner != sym.owner) {
            Symbol siteCursor = siteOwner;
            boolean foundOwner = false;
            int numOfOuters = 0;
            ownerSearch:
            while (siteCursor.kind != Kinds.PCK) {
                ListBuffer<Type> supertypes = ListBuffer.lb();
                Set<Type> superSet = new HashSet<Type>();
                if (siteCursor.type != null) {
                    supertypes.append(siteCursor.type);
                    superSet.add(siteCursor.type);
                }

                if (siteCursor.kind == Kinds.TYP) {
                    types.getSupertypes(siteCursor, supertypes, superSet);
                }

                for (Type supType : supertypes) {
                    if (types.isSameType(supType, sym.owner.type)) {
                        foundOwner = true;
                        break ownerSearch;
                    }
                }

                if (siteCursor.kind == Kinds.TYP) {
                    numOfOuters++;
                }

                siteCursor = siteCursor.owner;
            }

            if (foundOwner) {
                // site was found up the outer class chain, add the chaining accessors
                siteCursor = siteOwner;
                while (numOfOuters > 0) {
                    if (siteCursor.kind == Kinds.TYP) {
                        ret = callExpression(diagPos, ret, defs.outerAccessorName);
                        ret.type = siteCursor.type;
                    }

                    if (siteCursor.kind == Kinds.TYP) {
                        numOfOuters--;
                    }
                    siteCursor = siteCursor.owner;
                }
            }
        }
        return (nullForThis && ret == thisExpr)? null : ret;
    }

    private void fillClassesWithOuters(JFXScript tree) {
        class FillClassesWithOuters extends JavafxTreeScanner {
            JFXClassDeclaration currentClass;

            @Override
            public void visitClassDeclaration(JFXClassDeclaration tree) {
                JFXClassDeclaration prevClass = currentClass;
                try {
                    currentClass = tree;
                    super.visitClassDeclaration(tree);
                }
                finally {
                    currentClass = prevClass;
                }
            }

            @Override
            public void visitIdent(JFXIdent tree) {
                super.visitIdent(tree);
                if (currentClass != null && tree.sym.kind != Kinds.TYP) {
                    addOutersForOuterAccess(tree.sym, currentClass.sym);
                }
            }

            @Override
            public void visitSelect(JFXSelect tree) {
                super.visitSelect(tree);
                Symbol sym = expressionSymbol(tree.selected);
                if (currentClass != null && sym != null && sym.kind == Kinds.TYP) {
                    addOutersForOuterAccess(tree.sym, currentClass.sym);
                }
            }

            @Override // Need this because JavafxTreeScanner is not visiting the args of the JFXInstanciate tree. Starting to visit them generate tons of errors.
            public void visitInstanciate(JFXInstanciate tree) {
                super.visitInstanciate(tree);
                super.scan(tree.getArgs());
            }

            private void addOutersForOuterAccess(Symbol sym, Symbol currentClass) {
                if (sym != null && sym.owner != null && sym.owner.type != null
                        && !sym.isStatic() && currentClass != null) {
                    Symbol outerSym = currentClass;
                    ListBuffer<ClassSymbol> potentialOuters = new ListBuffer<ClassSymbol>();
                    boolean foundOuterOwner = false;
                    while (outerSym != null) {
                        if (outerSym.kind == Kinds.TYP) {
                            ClassSymbol outerCSym = (ClassSymbol) outerSym;
                            if (types.isSuperType(sym.owner.type, outerCSym)) {
                                foundOuterOwner = true;
                                break;
                             }
                            potentialOuters.append(outerCSym);
                        }
                        else if (sym.owner == outerSym)
                            break;
                        outerSym = outerSym.owner;
                    }

                    if (foundOuterOwner) {
                        for (ClassSymbol cs : potentialOuters) {
                            hasOuters.add(cs);
                        }
                    }
                }
            }
        }

        new FillClassesWithOuters().scan(tree);
    }

    //@Override
    public void visitTimeLiteral(JFXTimeLiteral tree) {
        //TODO: code should be something like the below, but this requires a similar change to visitInterpolateValue
        /***
           result = makeDurationLiteral(tree.pos(), translate(tree.value));
         */

        // convert this time literal to a javafx.lang.Duration.valueOf() invocation
        JFXFunctionInvocation duration = timeLiteralToDuration(tree); // sets result

        // now convert that FX invocation to Java
        visitFunctionInvocation(duration); // sets result
   }

    abstract static class InterpolateValueTranslator extends NewBuiltInInstanceTranslator {

        final JFXInterpolateValue tree;

        InterpolateValueTranslator(JFXInterpolateValue tree, JavafxToJava toJava) {
            super(tree.pos(), toJava.syms.javafx_KeyValueType, toJava);
            this.tree = tree;
            this.tree.value = tree.funcValue;
        }

        protected abstract JCExpression translateTarget();

        protected void initInstanceVariables(Name instName) {
            // value
            setInstanceVariable(instName, defs.valueName, tree.value);

            // interpolator kind
            if (tree.interpolation != null) {
                VarSymbol vsym = varSym(defs.interpolateName);
                setInstanceVariable(instName, JavafxBindStatus.UNBOUND, vsym, tree.interpolation);
            }

            // target -- convert to Pointer
            setInstanceVariable(tree.attribute.pos(), instName, JavafxBindStatus.UNBOUND, varSym(defs.targetName), translateTarget());
        }
    }

    public void visitInterpolateValue(final JFXInterpolateValue tree) {
        result = new InterpolateValueTranslator(tree, JavafxToJava.this) {

            protected JCExpression translateTarget() {
                JCExpression target = toJava.translateAsLocation(tree.attribute);
                return toJava.callExpression(diagPos, makeExpression(syms.javafx_PointerType), "make", target);
            }
        }.doit();
    }

    public void visitKeyFrameLiteral(final JFXKeyFrameLiteral tree) {
        result = new NewBuiltInInstanceTranslator(tree.pos(), syms.javafx_KeyFrameType, JavafxToJava.this) {

            protected void initInstanceVariables(Name instName) {
                // start time
                setInstanceVariable(instName, defs.timeName, tree.start);

                // key values -- as sequence
                JCExpression values = new ExplicitSequenceTranslator(
                        tree.pos(),
                        tree.getInterpolationValues(),
                        syms.javafx_KeyValueType
                ).doit();
                setInstanceVariable(tree.pos(), instName, varSym(defs.valuesName), values);
            }
        }.doit();
    }
}
