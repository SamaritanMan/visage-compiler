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

import com.sun.tools.javafx.tree.*;
import com.sun.javafx.api.tree.ForExpressionInClauseTree;
import com.sun.tools.javafx.code.JavafxFlags;
import com.sun.tools.javafx.comp.JavafxAbstractTranslation.ExpressionResult;
import com.sun.tools.javafx.comp.JavafxDefs.RuntimeMethod;
import com.sun.tools.mjavac.code.Symbol;
import com.sun.tools.mjavac.code.Symbol.VarSymbol;
import com.sun.tools.mjavac.code.Type;
import com.sun.tools.mjavac.tree.JCTree;
import com.sun.tools.mjavac.tree.JCTree.*;
import com.sun.tools.mjavac.util.Context;
import com.sun.tools.mjavac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.mjavac.util.List;
import com.sun.tools.mjavac.util.ListBuffer;
import com.sun.tools.mjavac.util.Name;

/**
 * Translate bind expressions into code in bind defining methods
 * 
 * @author Robert Field
 */
public class JavafxTranslateBind extends JavafxAbstractTranslation implements JavafxVisitor {

    protected static final Context.Key<JavafxTranslateBind> jfxBoundTranslation =
        new Context.Key<JavafxTranslateBind>();

    // Symbol for the var whose bound expression we are translating.
    private VarSymbol targetSymbol;

    // Is this a bi-diractional bind?
    private boolean isBidiBind;

    // The outer bound expression
    private JFXExpression boundExpression;

    public static JavafxTranslateBind instance(Context context) {
        JavafxTranslateBind instance = context.get(jfxBoundTranslation);
        if (instance == null) {
            JavafxToJava toJava = JavafxToJava.instance(context);
            instance = new JavafxTranslateBind(context, toJava);
        }
        return instance;
    }

    public JavafxTranslateBind(Context context, JavafxToJava toJava) {
        super(context, toJava);

        context.put(jfxBoundTranslation, this);
    }

    static JCExpression TODO(String msg, JFXExpression tree) {
        return TODO(msg + " -- " + tree.getClass());
    }

    /**
     * Entry-point into JavafxTranslateBind.
     *
     * @param expr Bound expression to translate.  Directly held by a var (or bound function?). Not a sub-expression/
     * @param targettedType Type of the result.  Note: this may be different from the type of expr.
     * @param targetSymbol Symbol for the var whose bound expression we are translating.
     * @param isBidiBind Is this a bi-diractional bind?
     * @return
     */
    ExpressionResult translateBoundExpression(JFXExpression expr, Type targettedType, VarSymbol targetSymbol, boolean isBidiBind) {
        this.targetSymbol = targetSymbol;
        this.isBidiBind = isBidiBind;
        this.boundExpression = expr;
        if (types.isSequence(targetType) && !types.isSequence(expr.type)) {
            TODO("bound sequence implicit up-conversion", expr);
        }
        ExpressionResult res = translateToExpressionResult(expr, targettedType);
        this.targetSymbol = null;
        this.boundExpression = null;
        return res;
    }

/****************************************************************************
 *                     Bound Non-Sequence Translators
 ****************************************************************************/

    /**
     * Translate a bound function call
     */
    private class BoundFunctionCallTranslator extends FunctionCallTranslator {

        BoundFunctionCallTranslator(JFXFunctionInvocation tree) {
            super(tree);
        }
        JCExpression condition = null;

        @Override
        List<JCExpression> determineArgs() {
            ListBuffer<JCExpression> targs = ListBuffer.lb();
            // if this is a super.foo(x) call, "super" will be translated to referenced class,
            // so we add a receiver arg to make a direct call to the implementing method  MyClass.foo(receiver$, x)
            if (superToStatic) {
                targs.append(id(defs.receiverName));
            }

            if (callBound) {
                for (JFXExpression arg : args) {
                    if (arg.getFXTag() == JavafxTag.IDENT) {
                        JFXIdent ident = (JFXIdent)args.head;
                        JCExpression receiver = ident.sym.isStatic() ?
                            call(defs.scriptLevelAccessMethod(names, ident.sym.owner), List.<JCExpression>nil()) :
                            makeReceiver(ident.sym, false);
                        targs.append(receiver);
                        targs.append(Offset(ident.sym));
                    } else {
                        TODO("non-Ident in bound call");
                    }
                }
                return targs.toList();
            } else {
                return super.determineArgs();
            }
        }

        @Override
        JCExpression translateArg(JFXExpression arg, Type formal) {
            if (arg instanceof JFXIdent) {
                Symbol sym = ((JFXIdent) arg).sym;
                JCVariableDecl oldVar = makeTmpVar("old", formal, id(attributeValueName(sym)));
                JCVariableDecl newVar = makeTmpVar("new", formal, call(attributeGetterName(sym)));
                addPreface(oldVar);
                addPreface(newVar);
                addBindee((VarSymbol) sym);   //TODO: isn't this redundant?

                // oldArg != newArg
                JCExpression compare = NE(id(oldVar), id(newVar));
                // concatenate with OR --  oldArg1 != newArg1 || oldArg2 != newArg2
                condition = condition == null ? compare : OR(condition, compare);

                return id(newVar);
            } else {
                return super.translateArg(arg, formal);
            }
        }

        @Override
        JCExpression fullExpression(JCExpression mungedToCheckTranslated) {
            if (callBound) {
                // call to a bound function in bind context
                JCExpression tMeth = select(mungedToCheckTranslated, methodName());
                return m().Apply(translateExprs(typeargs), tMeth, determineArgs());
            } else {
                JCExpression full = super.fullExpression(mungedToCheckTranslated);
                if (condition != null) {
                    // if no args have changed, don't call function, just return previous value
                    //TODO: must call if selector changes
                    full = m().Conditional(condition, full, id(attributeValueName(targetSymbol)));
                }
                return full;
            }
        }
    }

    /**
     * Translate if-expression
     *
     * bind if (cond) foo else bar
     *
     * becomes preface statements:
     *
     *   T res;
     *   cond.preface;
     *   if (cond) {
     *     foo.preface;
     *     res = foo;
     *   } else {
     *     bar.preface;
     *     res = bar;
     *   }
     *
     * result value:
     *
     *   res
     *
     */
    private class IfExpressionTranslator extends ExpressionTranslator {

        private final JFXIfExpression tree;
        private final JCVariableDecl resVar;
        private final Type type;

        IfExpressionTranslator(JFXIfExpression tree) {
            super(tree.pos());
            this.tree = tree;
            this.type = (targetType != null)? targetType : tree.type;
            this.resVar = makeTmpVar("res", type, null);
        }

        JCStatement side(JFXExpression expr) {
            ExpressionResult res = translateToExpressionResult(expr, type);
            addBindees(res.bindees());
            addInterClassBindees(res.interClass());
            return m().Block(0L, res.statements().append(makeExec(m().Assign(id(resVar), res.expr()))));
        }

        protected ExpressionResult doit() {
            JCExpression cond = translateExpr(tree.getCondition(), syms.booleanType);
            addPreface(resVar);
            addPreface(m().If(
                    cond,
                    side(tree.getTrueExpression()),
                    side(tree.getFalseExpression())));
            return toResult( id(resVar), type );
        }
    }

    /**
     * Bound identifier reference (non-sequence)
     */
    private class BoundIdentTranslator extends IdentTranslator {

        BoundIdentTranslator(JFXIdent tree) {
            super(tree);
        }

        @Override
        protected ExpressionResult doit() {
            if (sym instanceof VarSymbol) {
                VarSymbol vsym = (VarSymbol) sym;
                if (currentClass().sym.isSubClass(sym.owner, types)) {
                    // The var is in our class (or a superclass)
                    if ((receiverContext() == ReceiverContext.ScriptAsStatic) || !sym.isStatic()) {
                        addBindee(vsym);
                    }
                } else {
                    // The reference is to a presumably outer class
                    //TODO:
                    }

            }
            return super.doit();
        }
    }

    /**
     * Bound member select (non-sequence)
     */
    private class BoundSelectTranslator extends SelectTranslator {

        BoundSelectTranslator(JFXSelect tree) {
            super(tree);
        }

        @Override
        protected ExpressionResult doit() {
            JFXExpression selectorExpr = tree.getExpression();
            if (canChange() && (selectorExpr instanceof JFXIdent)) {
                // cases that need a null check are the same as cases that have changing dependencies
                JFXIdent selector = (JFXIdent) selectorExpr;
                Symbol selectorSym = selector.sym;
                buildDependencies(selectorSym);
                addBindee((VarSymbol) selectorSym);
                addInterClassBindee((VarSymbol) selectorSym, refSym);
            }
            return (ExpressionResult) super.doit();
        }

        /**
         * Override to handle mutable selector changing dependencies
         *
         *  // def w = bind r.v
         * 	String get$w() {
         *	  if ( ! isValidValue$( VOFF$w ) ) {
         *	    Baz oldSelector = $r;
         *	    Baz newSelector = get$r();
         *	    switchDependence$(VOFF$v, oldSelector, newSelector);
         *	    be$w( (newSelector==null)? "" : newSelector.get$v() );
         *	  }
         *	  return $w;
         *	}
         */
        protected void buildDependencies(Symbol selectorSym) {
                if (types.isJFXClass(selectorSym.owner)) {
                    Type selectorType = selectorSym.type;
                    JCExpression rcvr;
                    JCVariableDecl oldSelector;
                    JCVariableDecl newSelector;
                    JCVariableDecl oldOffset;
                    JCVariableDecl newOffset;

                    //

                    if ((targetSymbol.owner.flags() & JavafxFlags.MIXIN) != 0) {
                        rcvr = id(defs.receiverName);
                        oldSelector = makeTmpVar(selectorType, call(id(defs.receiverName), attributeGetMixinName(selectorSym)));
                        newSelector = makeTmpVar(selectorType, call(id(defs.receiverName), attributeGetterName(selectorSym)));
                    } else {
                        rcvr = selectorSym.isStatic() ? call(scriptLevelAccessMethod(selectorSym.owner)) : id(names._this);
                        oldSelector = makeTmpVar(selectorType, id(attributeValueName(selectorSym)));
                        newSelector = makeTmpVar(selectorType, call(attributeGetterName(selectorSym)));
                    }

                    addPreface(oldSelector);
                    addPreface(newSelector);

                    if ((selectorSym.type.tsym.flags() & JavafxFlags.MIXIN) != 0) {
                        JCExpression oldNullCheck = makeNullCheck(id(oldSelector));
                        JCExpression oldInit = m().Conditional(oldNullCheck, makeInt(0), call(id(oldSelector), attributeGetVOFFName(tree.sym)));
                        oldOffset = makeTmpVar(syms.intType, oldInit);
                        addPreface(oldOffset);

                        JCExpression newNullCheck = makeNullCheck(id(newSelector));
                        JCExpression newInit = m().Conditional(newNullCheck, makeInt(0), call(id(newSelector), attributeGetVOFFName(tree.sym)));
                        newOffset = makeTmpVar(syms.intType, newInit);
                        addPreface(newOffset);
                    } else {
                        newOffset = oldOffset = makeTmpVar(syms.intType, Offset(tree.sym));
                        addPreface(oldOffset);
                    }

                    if (isBidiBind) {
                        JCVariableDecl selectorOffset;
                        if ((targetSymbol.owner.flags() & JavafxFlags.MIXIN) != 0) {
                            selectorOffset = makeTmpVar(syms.intType, call(id(defs.receiverName), attributeGetVOFFName(targetSymbol)));
                        } else {
                            selectorOffset = makeTmpVar(syms.intType, Offset(targetSymbol));
                        }

                        addPreface(selectorOffset);
                        addPreface(callStmt(defs.FXBase_switchBiDiDependence,
                                rcvr,
                                id(selectorOffset),
                                id(oldSelector), id(oldOffset),
                                id(newSelector), id(newOffset)));
                    } else {
                        addPreface(callStmt(defs.FXBase_switchDependence,
                                rcvr,
                                id(oldSelector), id(oldOffset),
                                id(newSelector), id(newOffset)));
                    }
                }
        }
    }

/****************************************************************************
 *                     Bound Sequence Translators
 ****************************************************************************/

    abstract class BoundSequenceTranslator extends ExpressionTranslator {

        abstract JCStatement makeSizeBody();
        abstract JCStatement makeGetElementBody();
        abstract void setupInvalidators();

        BoundSequenceTranslator(DiagnosticPosition diagPos) {
            super(diagPos);
        }

        BoundSequenceResult doit() {
            setupInvalidators();
            return new BoundSequenceResult(invalidators(), interClass(), makeGetElementBody(), makeSizeBody());
        }

        JCStatement InvalidateCall(JCExpression begin, JCExpression end, JCExpression newLen) {
            return callStmt(attributeInvalidateName(targetSymbol), begin, end, newLen, id(defs.invalidateArgNamePhase));
        }

        private Name flagBit = defs.varFlagDEFAULT_APPLIED;
        private VarSymbol flagSymbol = (VarSymbol)targetSymbol;

        JCExpression isSequenceValid() {
            return makeFlagExpression(flagSymbol, defs.varFlagActionTest, flagBit, flagBit);
        }

        JCStatement setSequenceValid() {
            return makeFlagStatement(flagSymbol, defs.varFlagActionChange, null, flagBit);
        }

        JCExpression posArg() {
            return id(defs.getArgNamePos);
        }

        JCExpression phaseArg() {
            return id(defs.invalidateArgNamePhase);
        }

        JCExpression IsTriggerPhase() {
            return EQ(phaseArg(), id(defs.varFlagNEEDS_TRIGGER));
        }

        JCExpression iZero() {
            return m().Literal(syms.intType.tag, 0);
        }
        JCExpression iOne() {
            return m().Literal(syms.intType.tag, 1);
        }
        JCStatement Assign(JCExpression vid, JCExpression value) {
            return makeExec(m().Assign(vid, value));
        }
        JCStatement Assign(JCVariableDecl var, JCExpression value) {
            return Assign(id(var), value);
        }
        JCStatement Block(JCStatement... stmts) {
            return m().Block(0L, List.from(stmts));
        }
        JCStatement If(JCExpression cond, JCStatement thenStmt, JCStatement elseStmt) {
            return m().If(cond, thenStmt, elseStmt);
        }
        JCStatement If(JCExpression cond, JCStatement thenStmt) {
            return m().If(cond, thenStmt, null);
        }
    }

    class BoundIdentSequenceTranslator extends BoundSequenceTranslator {
        private final JFXIdent tree;
        private final ExpressionResult exprResult;
        BoundIdentSequenceTranslator(JFXIdent tree, ExpressionResult exprResult) {
            super(tree.pos());
            this.tree = tree;
            this.exprResult = exprResult;
        }

        JCStatement makeSizeBody() {
            return makeReturn(call(attributeSizeName(tree.sym)));
        }

        JCStatement makeGetElementBody() {
            return makeReturn(call(attributeGetElementName(tree.sym), posArg()));
        }

        /**
         * Simple bindee info from normal translation will do it
         */
        void setupInvalidators() {
            mergeResults(exprResult);
        }
    }

    private class BoundSelectSequenceTranslator extends BoundSequenceTranslator {

        private final SelectTranslator strans;
        private final Symbol refSym;
        private final VarSymbol selfSym = (VarSymbol) targetSymbol;
        private final VarSymbol selectorSym;
        private final VarSymbol sizeSym;


        BoundSelectSequenceTranslator(JFXSelect tree) {
            super(tree.pos());
            this.strans = new SelectTranslator(tree);
            this.refSym = strans.refSym;
            this.sizeSym = tree.boundSize.sym;
            JFXExpression selectorExpr = tree.getExpression();
            assert canChange();
            assert (selectorExpr instanceof JFXIdent);
            JFXIdent selector = (JFXIdent) selectorExpr;
            this.selectorSym = (VarSymbol) selector.sym;
        }

        /*** forward to SelectTranslator ***/
        private JFXExpression getToCheck() { return strans.getToCheck(); }
        private JCExpression translateToCheck(JFXExpression expr) { return strans.translateToCheck(expr); }
        private boolean canChange() { return strans.canChange(); }
        private JCExpression wrapInNullCheckExpression(JCExpression full, JCExpression tToCheck, Type theResultType, Type theFullType) {
             return strans.wrapInNullCheckExpression(full, tToCheck, theResultType, theFullType);
        }
        private JCStatement wrapInNullCheckStatement(JCExpression full, JCExpression tToCheck, Type theResultType, Type theFullType) {
             return strans.wrapInNullCheckStatement(full, tToCheck, theResultType, theFullType);
        }

        /**
         * Size accessor
         * (
         *     if ( default-not-set ) {
         *         set-default-flag;
         *         clear selector's trigger flag
         *         invalidate the selector
         *     }
         *     // redirect to the size of the referenced sequence, updating the selector
         *     return get$selector()==null? 0 : get$selector().size$ref();
         * }
         *
         */
        JCStatement makeSizeBody() {
            JCExpression tToCheck = translateToCheck(getToCheck());

            return
                Block(
                    If (NOT(makeFlagExpression((VarSymbol)targetSymbol, defs.varFlagActionChange, null, defs.varFlagDEFAULT_APPLIED)),
                        Block(
                            makeFlagStatement(selectorSym, defs.varFlagActionChange, defs.varFlagNEEDS_TRIGGER, null),
                            callStmt(attributeInvalidateName(selectorSym), id(defs.varFlagNEEDS_TRIGGER))
                        )
                    ),
                    buildBody(tToCheck, call(tToCheck, attributeSizeName(refSym)), syms.intType)
                );
        }

        /**
         * Get sequence element
         * {
         *     size$self(); // Access size to make sure we are initialized
         *     return get$selector()==null? <default> : get$selector().get$ref(pos);
         * }
         */
        JCStatement makeGetElementBody() {
            JCExpression tToCheck = translateToCheck(getToCheck());

            return
                Block(
                    callStmt(attributeSizeName(selfSym)),
                    buildBody(tToCheck, call(tToCheck, attributeGetElementName(refSym), posArg()), types.elementType(refSym.type))
                );
        }

        protected JCStatement buildBody(JCExpression tToCheck, JCExpression full, Type theResultType) {
            return wrapInNullCheckStatement(full, tToCheck, theResultType, theResultType);
        }

        /**
         *  $selector === null? 0 : $selector.size$ref();
         */
        private JCExpression getSize(VarSymbol selectorSym) {
            return  m().Conditional(makeNullCheck(id(attributeValueName(selectorSym))),
                        makeInt(0),
                        call(id(attributeValueName(selectorSym)), attributeSizeName(refSym)));
        }

        /**
         * Invalidator for the selector
         *
         * invalidator$selector(int phase) {
         *     if (valid-for-this-phase) {
         *         clear-valid-for-this-phase;
         *         int oldSize = $selector === null? 0 : $selector.size$ref();
         *         if ( is-trigger-phase) {
         *             get$selector();  // update selector
         *             int newSize = $selector === null? 0 : $selector.size$ref();
         *             addDependent($selector, VOFF$ref);
         *             invalidate$self(0, oldSize, newSize, phase);
         *         } else {
         *             // remove dependent, do it now, so updates from referenced don't interject
         *             removeDependent($selector, VOFF$ref);
         *             // invalidate with undefined newSize, since we can't compute without enter trigger phase
         *             invalidate$self(0, oldSize, -1, phase);
         *         }
         *     }
         * }
         */
        private JCStatement makeInvalidateSelector(VarSymbol selectorSym) {
            JCVariableDecl oldSize = makeTmpVar(syms.intType, getSize(selectorSym));

            return
                If (NOT(makeFlagExpression(selectorSym, defs.varFlagActionChange, null, phaseArg())),
                    Block(
                        oldSize,
                        If (IsTriggerPhase(),
                            Block(
                                callStmt(attributeGetterName(selectorSym)),
                                If (makeNotNullCheck(id(attributeValueName(selectorSym))),
                                    callStmt(defs.FXBase_addDependent,
                                        id(attributeValueName(selectorSym)),
                                        Offset(id(attributeValueName(selectorSym)), refSym),
                                        getReceiverOrThis(selectorSym)
                                    )
                                ),
                                callStmt(attributeInvalidateName(selfSym),
                                    makeInt(0),
                                    id(oldSize),
                                    getSize(selectorSym),
                                    phaseArg()
                                )
                            ),
                            Block(
                                If (makeNotNullCheck(id(attributeValueName(selectorSym))),
                                    callStmt(defs.FXBase_removeDependent,
                                       id(attributeValueName(selectorSym)),
                                       Offset(id(attributeValueName(selectorSym)), refSym),
                                       getReceiverOrThis(selectorSym)
                                    )
                                ),
                                callStmt(attributeInvalidateName(selfSym),
                                    makeInt(0),
                                    id(oldSize),
                                    makeInt(-1),
                                    phaseArg())
                            )
                        )
                    )
                );
        }

        /**
         * Addition to the invalidate for this bound select sequence
         *
         * invalidate$self(int start, int end, int newLen, int phase) {
         *     if ( is-trigger-phase ) {
         *         $size = $size + newLen - (end - start);
         *     }
         *     ....
         * }
         */
        private JCStatement makeInvalidateSelf() {
            return
                If (IsTriggerPhase(),
                    Assign(id(attributeValueName(sizeSym)),
                        PLUS(
                            id(attributeValueName(sizeSym)),
                            MINUS(
                                id(defs.sliceArgNameNewLength),
                                MINUS(
                                    id(defs.sliceArgNameEndPos),
                                    id(defs.sliceArgNameStartPos)
                                )
                            )
                        )
                    )
                );
        }

        @Override
        void setupInvalidators() {
                addInvalidator(selectorSym, makeInvalidateSelector(selectorSym));
                addInvalidator(selfSym, makeInvalidateSelf());
                addInterClassBindee(selectorSym, refSym);
        }
    }

    private class BoundExplicitSequenceTranslator extends BoundSequenceTranslator {
        private final List<JFXVar> vars;
        private final Type elemType;
        private final VarSymbol sizeSymbol;
        private final int length;
        BoundExplicitSequenceTranslator(JFXSequenceExplicit tree) {
            super(tree.pos());
            this.vars = tree.boundItemsVars;
            this.length = vars.length();
            this.elemType = types.elementType(tree.type);
            this.sizeSymbol = tree.boundSizeVar.sym;
        }

        private boolean isNullable(int index) {
            Type type = type(index);
            //TODO: better test that looks at the actual expression (eg. object literal)
            return !type.isPrimitive() &&
                    type != syms.javafx_StringType &&
                    type != syms.javafx_DurationType;
        }

        private boolean isSequence(int index) {
            return types.isSequence(type(index));
        }

        private VarSymbol vsym(int index) {
            return vars.get(index).sym;
        }

        private Type type(int index) {
            return vars.get(index).type;
        }

        private JCExpression size() {
            return id(attributeValueName(sizeSymbol));
        }

        private JCExpression id(int index) {
            return id(attributeValueName(vsym(index)));
        }

        private JCExpression get(int index) {
            return call(attributeGetterName(vsym(index)));
        }

        private JCExpression computeSize(int index, JCExpression value) {
            if (isSequence(index)) {
                return call(attributeSizeName(vsym(index)));
            } else if (isNullable(index)) {
                return m().Conditional(makeNullCheck(value), iZero(), iOne());
            } else {
                return iOne();
            }
        }

        private JCExpression computeSize(int index) {
            return computeSize(index, id(index));
        }

        private JCExpression cummulativeSize(int index) {
            JCExpression sum = iZero();
            for (int i = 0; i < index; ++i) {
                sum = PLUS(sum, computeSize(i));
            }
            return sum;
        }

        private JCExpression element(int index, JCExpression pos) {
            if (isSequence(index)) {
                return call(attributeGetElementName(vsym(index)), pos);
            } else {
                return get(index);
            }
        }

        JCStatement makeSizeBody() {
            return makeReturn(call(attributeGetterName(sizeSymbol)));
        }

        /**
         * if (pos < 0) {
         *    return null; // default
         * }
         * int start = 0;
         * int next = 0;
         * next += size$s1();
         * if (pos < next) {
         *    return get$s1(pos – start);
         * }
         * start = next;
         * next += size$s2();
         * if (pos < next) {
         *    return get$s2(pos – start);
         * }
         * start = next;
         * next += size$s3();
         * if (pos < next) {
         *    return get$s3(pos – start);
         * )
         * return null; // default
         */
        JCStatement makeGetElementBody() {
            JCVariableDecl vStart = makeMutableTmpVar("start", syms.intType, iZero());
            JCVariableDecl vNext = makeMutableTmpVar("next", syms.intType, iZero());
            ListBuffer<JCStatement> stmts = ListBuffer.lb();

            stmts.append(If(LT(posArg(), iZero()), makeReturn(makeDefaultValue(elemType))));
            stmts.append(vStart);
            stmts.append(vNext);
            for (int index = 0; index < length; ++index) {
                stmts.append(Assign(vNext, PLUS(id(vNext), computeSize(index))));
                stmts.append(If(LT(posArg(), id(vNext)), makeReturn(element(index, MINUS(posArg(), id(vStart))))));
                stmts.append(Assign(vStart, id(vNext)));
            }
            stmts.append(makeReturn(makeDefaultValue(elemType)));
            return m().Block(0L, stmts.toList());
        }

        /**
         */
        private JCStatement makeItemInvalidate(int index) {
            JCVariableDecl vStart = makeTmpVar("start", syms.intType, cummulativeSize(index));
            
            if (isSequence(index)) {
                return 
                    If(isSequenceValid(),
                        Block(
                            vStart,
                            If(IsTriggerPhase(),
                                // Update the size by the difference
                                // size = size + newLen - (end - begin)
                                Assign(size(),
                                    PLUS(
                                        size(),
                                        MINUS(
                                            id(defs.sliceArgNameNewLength),
                                            MINUS(id(defs.sliceArgNameEndPos), id(defs.sliceArgNameStartPos))
                                        )
                                    )
                                )
                            ),
                            InvalidateCall(
                                PLUS(id(vStart), id(defs.sliceArgNameStartPos)),
                                PLUS(id(vStart), id(defs.sliceArgNameEndPos)),
                                id(defs.sliceArgNameNewLength)
                            )
                        )
                    );
            } else {
                //TODO: this is eager even for fixed length
                JCVariableDecl vValue = makeTmpVar("value", type(index), get(index));
                JCVariableDecl vNewLen = makeTmpVar("newLen", syms.intType, computeSize(index, id(vValue)));
                JCVariableDecl vOldLen = makeTmpVar("oldLen", syms.intType, computeSize(index));

                return 
                    If(isSequenceValid(),
                        Block(
                            vStart,
                            vValue,
                            vNewLen,
                            vOldLen,
                            If(IsTriggerPhase(),
                                Block(
                                    // Update the size by the difference
                                    Assign(size(), PLUS(size(), MINUS(id(vNewLen), id(vOldLen)))),
                                    Assign(id(index), id(vValue))
                                )
                            ),
                            InvalidateCall(
                                id(vStart),
                                PLUS(id(vStart), id(vOldLen)),
                                id(vNewLen)
                            )
                        )
                    );
            }
        }

        /**
         * Invalidate (and computation) for synthetic size var.
         */
        private JCStatement makeInvalidateSize() {
            // Initialize the singleton synthetic item vars (during IS_VALID phase)
            // Bound sequences don't have a value
            ListBuffer<JCStatement> stmts = ListBuffer.lb();
            for (int i = 0; i < length; ++i) {
                if (!isSequence(i)) {
                    stmts.append(Assign(id(i), get(i)));
                }
            }
            JCStatement varInits = m().Block(0L, stmts.toList());

            return
                Block(
                    If(IsTriggerPhase(),
                        setSequenceValid(),
                        varInits
                    ),
                    Assign(size(), cummulativeSize(length)),
                    InvalidateCall(iZero(), iZero(), size())
                );
        }

        /**
         * For each item, and for size, set-up the invalidate method
         */
        void setupInvalidators() {
            for (int index = 0; index < length; ++index) {
                addInvalidator(vsym(index), makeItemInvalidate(index));
            }
            addInvalidator(sizeSymbol, makeInvalidateSize());
        }
    }

    private class BoundRangeSequenceTranslator extends BoundSequenceTranslator {
        private final JFXVar varLower;
        private final JFXVar varUpper;
        private final JFXVar varStep;
        private final JFXVar varSize;
        private final Type elemType;
        private final Type szType;
        private final boolean exclusive;

        BoundRangeSequenceTranslator(JFXSequenceRange tree) {
            super(tree.pos());
            this.varLower = (JFXVar)tree.getLower();
            this.varUpper = (JFXVar)tree.getUpper();
            this.varStep = (JFXVar)tree.getStepOrNull();
            this.varSize = tree.boundSizeVar;
            if (varLower.type == syms.javafx_IntegerType) {
                this.elemType = syms.javafx_IntegerType;
                this.szType = syms.intType;
            } else {
                this.elemType = syms.javafx_NumberType;
                this.szType = syms.longType;
            }
            this.exclusive = tree.isExclusive();
        }

        private JCExpression zero() {
            return m().Literal(elemType.tag, 0);
        }

        private JCExpression field(JFXVar var) {
            return id(attributeValueName(var.getSymbol()));
        }
        private JCExpression lower() {
            return field(varLower);
        }
        private JCExpression upper() {
            return field(varUpper);
        }
        private JCExpression step() {
            return varStep == null?
                  m().Literal(elemType.tag, 1)
                : field(varStep);
        }
        private JCExpression size() {
            return field(varSize);
        }

        private JCExpression get(JFXVar var) {
            return call(attributeGetterName(var.getSymbol()));
        }
        private JCExpression getLower() {
            return get(varLower);
        }
        private JCExpression getUpper() {
            return get(varUpper);
        }
        private JCExpression getStep() {
            return varStep == null?
                  m().Literal(elemType.tag, 1)
                : get(varStep);
        }
        private JCExpression getSize() {
            return get(varSize);
        }

        private JCExpression DIVstep(JCExpression v1) {
            return varStep == null?
                  v1
                : DIV(v1, step());
        }
        private JCExpression MULstep(JCExpression v1) {
            return varStep == null?
                  v1
                : MUL(v1, step());
        }
        private JCExpression exclusive() {
            return makeBoolean(exclusive);
        }

        private JCExpression calculateSize(JCExpression vl, JCExpression vu, JCExpression vs) {
            RuntimeMethod rm =
                    (elemType == syms.javafx_NumberType)?
                          defs.Sequences_calculateFloatRangeSize
                        : defs.Sequences_calculateIntRangeSize;
            return call(rm, vl, vu, vs, exclusive());
        }

        /**
         * int size$range() {
         *     return getSize();
         * }
         */
        JCStatement makeSizeBody() {
            return makeReturn(getSize());
        }

        /**
         * float get$range(int pos) {
         *    return (pos >= 0 && pos < getSize())?
         *              pos * step + lower
         *            : 0.0f;
         * }
         */
        JCStatement makeGetElementBody() {
            JCExpression cond = AND(
                    GE(posArg(), iZero()),
                    LT(posArg(), getSize())
                    );
            JCExpression value = PLUS(MULstep(posArg()), lower());
            JCExpression res = m().Conditional(cond, value, zero());
            return makeReturn(res);
        }

        /**
         * float newLower = getLower();
         * if (step != 0 && lower != newLower) {
         *    int newSize = Sequences.calculateFloatRangeSize(newLower, upper, step, false);
         *    int loss = 0;
         *    int gain = 0;
         *    float delta = newLower - lower;
         *    if (size == 0 || ((delta % step) != 0)) {
         *      // invalidate everything - new or start point different
         * 	loss = size;
         * 	gain = newSize;
         *    } else if (newLower > lower) {
         *      // shrink -- chop off the front
         * 	loss = (int) delta / step;
         * 	if (loss > size)
         * 	    loss = size
         *    } else {
         *      // grow -- add to the beginning
         * 	gain = (int) -delta / step;
         *    }
         *    if (phase == TRIGGER_PHASE) {
         * 	lower = newLower;
         * 	size = newSize;
         *    }
         *    invalidate$range(0, loss, gain, phase);
         * }
         */
        private JCStatement makeInvalidateLower() {
            JCVariableDecl vNewLower = makeTmpVar("newLower", elemType, getLower());
            JCVariableDecl vNewSize = makeTmpVar("newSize", syms.intType,
                                        calculateSize(id(vNewLower), upper(), step()));
            JCVariableDecl vLoss = makeMutableTmpVar("loss", syms.intType, iZero());
            JCVariableDecl vGain = makeMutableTmpVar("gain", syms.intType, iZero());
            JCVariableDecl vDelta = makeTmpVar("delta", elemType, MINUS(id(vNewLower), lower()));

            return If (isSequenceValid(),
                      Block(
                        vNewLower,
                        If (AND(NE(step(), zero()), NE(lower(), id(vNewLower))),
                          Block(
                              vNewSize,
                              vLoss,
                              vGain,
                              vDelta,
                              If (OR(EQ(size(), iZero()), NE(makeBinary(JCTree.MOD, id(vDelta), step()), zero())),
                                  Block(
                                      Assign(vLoss, size()),
                                      Assign(vGain, id(vNewSize))),
                              If (GT(id(vNewLower), lower()),
                                  Block(
                                      Assign(vLoss, m().TypeCast(syms.intType, DIVstep(id(vDelta)))),
                                      If (GT(id(vLoss), size()),
                                         Assign(vLoss, size()))
                                      ),
                                  Assign(vGain, m().TypeCast(syms.intType, DIVstep(NEG(id(vDelta)))))
                              )),
                              If (IsTriggerPhase(),
                                  Block(
                                      Assign(lower(), id(vNewLower)),
                                      Assign(size(), id(vNewSize))
                                  )
                              ),
                              InvalidateCall(iZero(), id(vLoss), id(vGain))
                    )))
            );
        }

        /**
         * float newUpper = getUpper();
         * if (step != 0 && upper != newUpper) {
         *    int newSize = Sequences.calculateFloatRangeSize(lower, newUpper, step, false);
         *    int oldSize = size();
         *    if (phase == TRIGGER_PHASE) {
         *       upper = newUpper;
         *       size = newSize;
         *    }
         *    if (newSize >= oldSize)
         *       // grow
         *       invalidate$range(oldSize, oldSize, newSize-oldSize, phase);
         *    else
         *       // shrink
         *       invalidate$range(newSize, oldSize, 0, phase);
         * }
         */
        private JCStatement makeInvalidateUpper() {
            JCVariableDecl vNewUpper = makeTmpVar("newUpper", elemType, getUpper());
            JCVariableDecl vOldSize = makeTmpVar("oldSize", syms.intType, size());
            JCVariableDecl vNewSize = makeTmpVar("newSize", syms.intType,
                                        calculateSize(lower(), id(vNewUpper), step()));

            return If (isSequenceValid(),
                      Block(
                        vNewUpper,
                        If (AND(NE(step(), zero()), NE(upper(), id(vNewUpper))),
                            Block(
                                vNewSize,
                                vOldSize,
                                If (IsTriggerPhase(),
                                    Block(
                                        Assign(upper(), id(vNewUpper)),
                                        Assign(size(), id(vNewSize))
                                    )
                                ),
                                If (GE(id(vNewSize), id(vOldSize)),
                                    InvalidateCall(id(vOldSize), id(vOldSize), MINUS(id(vNewSize), id(vOldSize))),
                                    InvalidateCall(id(vNewSize), id(vOldSize), iZero()))
                    )))
            );
        }

        /**
         * float newStep = getStep();
         * if (step != newStep) {
         *    int newSize = Sequences.calculateFloatRangeSize(lower, upper, newStep, false);
         *    int oldSize = size();
         *    if (phase == TRIGGER_PHASE) {
         *       step = newStep;
         *       size = newSize;
         *    }
         *    // Invalidate everything
         *    invalidate$range(0, oldSize, newSize, phase);
         * }
         */
        private JCStatement makeInvalidateStep() {
            JCVariableDecl vNewStep = makeTmpVar("newStep", elemType, getStep());
            JCVariableDecl vOldSize = makeTmpVar("oldSize", syms.intType, size());
            JCVariableDecl vNewSize = makeTmpVar("newSize", syms.intType,
                                        calculateSize(lower(), upper(), id(vNewStep)));

            return If (isSequenceValid(),
                      Block(
                        vNewStep,
                        If (NE(step(), id(vNewStep)),
                          Block(
                              vNewSize,
                              vOldSize,
                              If (IsTriggerPhase(),
                                  Block(
                                      Assign(step(), id(vNewStep)),
                                      Assign(size(), id(vNewSize))
                                  )
                              ),
                              InvalidateCall(iZero(), id(vOldSize), id(vNewSize))
                    )))
            );
        }

        /**
         * float newLower = getLower();
         * float newUpper = getUpper();
         * float newStep = getStep();
         * int newSize = Sequences.calculateFloatRangeSize(newLower, newUpper, newStep, false);
         * if (phase == TRIGGER_PHASE) {
         *      lower = newLower;
         *      upper = newUpper ;
         *      step = newStep ;
         *      size = newSize;
         *      setSequenceValid();
         * }
         * // Invalidate: empty -> filled out range
         * invalidate$range(0, 0, newSize, phase);
         */
        private JCStatement makeInvalidateSize() {
            JCVariableDecl vNewLower = makeTmpVar("newLower", elemType, getLower());
            JCVariableDecl vNewUpper = makeTmpVar("newUpper", elemType, getUpper());
            JCVariableDecl vNewStep = makeTmpVar("newStep", elemType, getStep());
            JCVariableDecl vNewSize = makeTmpVar("newSize", syms.intType,
                                        calculateSize(id(vNewLower), id(vNewUpper), id(vNewStep)));
            ListBuffer<JCStatement> inits = ListBuffer.lb();
            inits.append(Assign(lower(), id(vNewLower)));
            inits.append(Assign(upper(), id(vNewUpper)));
            if (varStep != null) {
                inits.append(Assign(step(), id(vNewStep)));
            }
            inits.append(Assign(size(), id(vNewSize)));
            inits.append(setSequenceValid());

            return Block(
                    vNewLower,
                    vNewUpper,
                    vNewStep,
                    vNewSize,
                    If (IsTriggerPhase(),
                        m().Block(0L, inits.toList())
                    ),
                    InvalidateCall(iZero(), iZero(), id(vNewSize))
                   );
        }

        /**
         * Set invalidators for the synthetic support variables
         */
        void setupInvalidators() {
            addInvalidator(varLower.sym, makeInvalidateLower());
            addInvalidator(varUpper.sym, makeInvalidateUpper());
            if (varStep != null) {
                addInvalidator(varStep.sym, makeInvalidateStep());
            }
            addInvalidator(varSize.sym, makeInvalidateSize());
        }
    }

/* ***************************************************************************
 * Visitor methods -- implemented (alphabetical order)
 ****************************************************************************/

    public void visitBinary(JFXBinary tree) {
        result = (new BinaryOperationTranslator(tree.pos(), tree)).doit();
    }

    public void visitFunctionInvocation(final JFXFunctionInvocation tree) {
        if (tree == boundExpression && types.isSequence(targetType)) {
            // We want to translate to a bound sequence
            TODO("bound sequence", tree);
        }
        result = (ExpressionResult) (new BoundFunctionCallTranslator(tree)).doit();
    }

    public void visitIdent(JFXIdent tree) {
        final ExpressionResult exprResult = new BoundIdentTranslator(tree).doit();
        if (tree == boundExpression && types.isSequence(targetType)) {
            // We are translating to a bound sequence
            result = new BoundIdentSequenceTranslator(tree, exprResult).doit();
        } else {
            result = exprResult;
        }
    }

    public void visitIfExpression(JFXIfExpression tree) {
        final ExpressionResult exprResult = new IfExpressionTranslator(tree).doit();
        if (tree == boundExpression && types.isSequence(targetType)) {
            // We want to translate to a bound sequence
            TODO("bound sequence", tree);
        } else {
            result = exprResult;
        }
    }

    public void visitInstanceOf(JFXInstanceOf tree) {
        result = new InstanceOfTranslator(tree).doit();
    }

    public void visitInstanciate(JFXInstanciate tree) {
        result = new InstanciateTranslator(tree).doit();
    }

    public void visitLiteral(JFXLiteral tree) {
        // Just translate to literal value
        result = new ExpressionResult(translateLiteral(tree), tree.type);
    }

    public void visitParens(JFXParens tree) {
        result = translateBoundExpression(tree.expr, targetType, targetSymbol, isBidiBind);
    }

    public void visitSelect(JFXSelect tree) {
        if (tree == boundExpression && types.isSequence(targetType)) {
            // We want to translate to a bound sequence
            result = new BoundSelectSequenceTranslator(tree).doit();
        } else {
            result = new BoundSelectTranslator(tree).doit();
        }
    }

    public void visitSequenceEmpty(JFXSequenceEmpty tree) {
        result = new SequenceEmptyTranslator(tree).doit();
    }

    public void visitSequenceExplicit(JFXSequenceExplicit tree) {
        result = new BoundExplicitSequenceTranslator(tree).doit();
    }

    public void visitSequenceRange(JFXSequenceRange tree) {
        result = new BoundRangeSequenceTranslator(tree).doit();
    }

    public void visitStringExpression(JFXStringExpression tree) {
        result = new StringExpressionTranslator(tree).doit();
    }

    public void visitTimeLiteral(final JFXTimeLiteral tree) {
        result = new TimeLiteralTranslator(tree).doit();
   }

    public void visitTypeCast(final JFXTypeCast tree) {
        if (tree == boundExpression && types.isSequence(targetType)) {
            // We want to translate to a bound sequence
            TODO("bound sequence", tree);
        }
        result = new TypeCastTranslator(tree).doit();
    }

    public void visitUnary(JFXUnary tree) {
        if (tree == boundExpression && types.isSequence(targetType)) {
            // We want to translate to a bound sequence
            TODO("bound sequence", tree);
        }
        result = new UnaryOperationTranslator(tree).doit();
    }


/* ***************************************************************************
 * Visitor methods -- NOT implemented yet
 ****************************************************************************/

    JCExpression TODO(JFXTree tree) {
        return TODO("BIND functionality: " + tree.getClass().getSimpleName());
    }

    public void visitAssign(JFXAssign tree) {
        TODO(tree);
        //(tree.lhs);
        //(tree.rhs);
    }

    public void visitFunctionValue(JFXFunctionValue tree) {
        TODO(tree);
        for (JFXVar param : tree.getParams()) {
            //(param);
        }
        //(tree.getBodyExpression());
    }

    //@Override
    public void visitSequenceIndexed(JFXSequenceIndexed tree) {
        TODO(tree);
        //(that.getSequence());
        //(that.getIndex());
    }
    
    public void visitSequenceSlice(JFXSequenceSlice tree) {
        TODO(tree);
        //(that.getSequence());
        //(that.getFirstIndex());
        //(that.getLastIndex());
    }

    //@Override
    public void visitForExpression(JFXForExpression tree) {
        TODO(tree);
        for (ForExpressionInClauseTree cl : tree.getInClauses()) {
            JFXForExpressionInClause clause = (JFXForExpressionInClause)cl;
            //(clause);
        }
        //(that.getBodyExpression());
    }

    //@Override
    public void visitBlockExpression(JFXBlock tree) {
        TODO(tree);
        //(that.stats);
        //(that.value);
    }
    
    //@Override
    public void visitIndexof(JFXIndexof tree) {
        TODO(tree);
    }

    public void visitInterpolateValue(JFXInterpolateValue tree) {
        TODO(tree);
        //(that.attribute);
        //(that.value);
        if  (tree.interpolation != null) {
            //(that.interpolation);
        }
    }


    /***********************************************************************
     *
     * Utilities
     *s
     */

    protected String getSyntheticPrefix() {
        return "bfx$";
    }


    /***********************************************************************
     *
     * Moot visitors  (alphabetical order)
     *
     */

    private void wrong() {
        throw new AssertionError("should not be processed as part of a binding");
    }

    public void visitAssignop(JFXAssignOp tree) {
        wrong();
    }

    public void visitBreak(JFXBreak tree) {
        wrong();
    }

    public void visitContinue(JFXContinue tree) {
        wrong();
    }

    public void visitFunctionDefinition(JFXFunctionDefinition tree) {
        wrong();
    }

    public void visitInvalidate(JFXInvalidate tree) {
        wrong();
    }

    public void visitKeyFrameLiteral(JFXKeyFrameLiteral tree) {
        wrong();
    }

    public void visitReturn(JFXReturn tree) {
        wrong();
    }

    public void visitScript(JFXScript tree) {
        wrong();
    }

    public void visitSequenceDelete(JFXSequenceDelete tree) {
        wrong();
    }

    public void visitSequenceInsert(JFXSequenceInsert tree) {
        wrong();
    }

    public void visitSkip(JFXSkip tree) {
        wrong();
    }

    public void visitThrow(JFXThrow tree) {
        wrong();
    }

    public void visitTry(JFXTry tree) {
        wrong();
    }

    public void visitVar(JFXVar tree) {
        wrong();
    }

    public void visitVarInit(JFXVarInit tree) {
        wrong();
    }

    public void visitWhileLoop(JFXWhileLoop tree) {
        wrong();
    }
}
