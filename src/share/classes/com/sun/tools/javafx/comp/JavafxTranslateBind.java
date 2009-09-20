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
import com.sun.tools.javafx.comp.JavafxAbstractTranslation.ExpressionResult;
import com.sun.tools.mjavac.code.Kinds;
import com.sun.tools.mjavac.code.Symbol;
import com.sun.tools.mjavac.code.Symbol.VarSymbol;
import com.sun.tools.mjavac.code.Type;
import com.sun.tools.mjavac.code.TypeTags;
import com.sun.tools.mjavac.tree.JCTree;
import com.sun.tools.mjavac.tree.JCTree.*;
import com.sun.tools.mjavac.util.List;
import com.sun.tools.mjavac.util.ListBuffer;
import com.sun.tools.mjavac.util.Context;
import com.sun.tools.mjavac.util.Name;

/**
 * Translate bind expressions into code in bind defining methods
 * 
 * @author Robert Field
 */
public class JavafxTranslateBind extends JavafxAbstractTranslation<ExpressionResult> implements JavafxVisitor {

    protected static final Context.Key<JavafxTranslateBind> jfxBoundTranslation =
        new Context.Key<JavafxTranslateBind>();

    Symbol targetSymbol;

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

    ExpressionResult translate(JFXExpression expr, Symbol targetSymbol) {
        this.targetSymbol = targetSymbol;
        return translate(expr);
    }

/* ***************************************************************************
 * Visitor methods -- implemented (alphabetical order)
 ****************************************************************************/

    public void visitBinary(JFXBinary tree) {
        result = (new BinaryOperationTranslator(tree.pos(), tree)).doit();
    }

    //TODO: merge with JavafxToJava version
    public void visitFunctionInvocation(final JFXFunctionInvocation tree) {
        result = (new FunctionCallTranslator(tree) {
            private Name funcName = null;

            List<JCExpression> translateExpressions(List<JFXExpression> trees) {
                ListBuffer<JCExpression> list = ListBuffer.lb();
                for (JFXExpression expr : trees) {
                    list.append(translateExpr(expr));
                }
                return list.toList();
            }

            protected ExpressionResult doit() {
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

                return (ExpressionResult) new NullCheckTranslator(diagPos, toCheckOrNull, returnType, knownNonNull) {

                    JCExpression translateToCheck(JFXExpression expr) {
                        JCExpression trans;
                        if (renameToSuper || superCall) {
                           trans = id(names._super);
                        } else if (renameToThis || thisCall) {
                           trans = id(names._this);
                        } else if (superToStatic) {
                            trans = staticReference(msym);
                        } else if (selector != null && !useInvoke && msym != null && msym.isStatic()) {
                            //TODO: clean this up -- handles referencing a static function via an instance
                            trans = staticReference(msym);
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

                            trans = translateExpr(expr);
                            if (expr.type.isPrimitive()) {
                                // Java doesn't allow calls directly on a primitive, wrap it
                                trans = makeBox(diagPos, trans, expr.type);
                            }
                        }
                        return trans;
                    }

                    @Override
                    JCExpression fullExpression( JCExpression mungedToCheckTranslated) {
                        ListBuffer<JCExpression> targs = ListBuffer.lb();
                        JCExpression condition = null;

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
                            //TODO
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
                                JFXExpression arg = l.head;
                                JCExpression targ;
                                if (magicIsInitializedFunction) {
                                    //TODO: in theory, this could have side-effects (but only in theory)
                                    //TODO: Lombard
                                    targ = translateExpr(l.head, formal);
                                } else {
                                    if (arg instanceof JFXIdent) {
                                        Symbol sym = ((JFXIdent) arg).sym;
                                        JCVariableDecl oldVar = makeTmpVar("old", formal, id(attributeValueName(sym)));
                                        JCVariableDecl newVar = makeTmpVar("new", formal, call(attributeGetterName(sym)));
                                        addPreface(oldVar);
                                        addPreface(newVar);
                                        addBindee((VarSymbol)sym);

                                        // oldArg != newArg
                                        JCExpression compare = makeNotEqual(id(oldVar), id(newVar));
                                        // concatenate with OR --  oldArg1 != newArg1 || oldArg2 != newArg2
                                        condition = condition == null ? compare : makeBinary(JCTree.OR, condition, compare);

                                        targ = id(newVar);
                                    } else {
                                        targ = preserveSideEffects(formal, l.head, translateExpr(arg, formal));
                                    }
                                }
                                targs.append(targ);
                            }
                        }
                        JCExpression tc = mungedToCheckTranslated;
                        if (funcName != null) {
                            // add the selector name back
                            tc = select(tc, funcName);
                        }
                        JCMethodInvocation app =  m().Apply(translateExpressions(tree.typeargs), tc, targs.toList());

                        JCExpression full = callBound ?
                              null //TODO
                            : app;
                        if (useInvoke) {
                            if (tree.type.tag != TypeTags.VOID) {
                                full = castFromObject(full, tree.type);
                            }
                        }
                        if (condition != null) {
                            full = m().Conditional(condition, full, id(attributeValueName(targetSymbol)));
                        }
                        return full;
                    }
                }.doit();
            }

        }).doit();
    }

    public void visitIdent(JFXIdent tree) {
        IdentTranslator tor = new IdentTranslator(tree);
        if (tree.sym instanceof VarSymbol) {
            tor.addBindee((VarSymbol) tree.sym);
        }
        result = tor.doit();
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
        private final Type targetType;
        private final JCVariableDecl resVar;

        IfExpressionTranslator(JFXIfExpression tree) {
            super(tree.pos());
            this.tree = tree;
            this.targetType = tree.type;
            this.resVar = makeTmpVar("res", targetType, null);
        }

        JCStatement side(JFXExpression expr) {
            ExpressionResult res = translateToExpressionResult(expr, targetType);
            addBindees(res.bindees());
            return m().Block(0L, res.statements().append(makeExec(m().Assign(id(resVar), res.expr()))));
        }

        protected ExpressionResult doit() {
            JCExpression cond = translateExpr(tree.getCondition());
            addPreface(resVar);
            addPreface(m().If(
                    cond,
                    side(tree.getTrueExpression()),
                    side(tree.getFalseExpression())));
            return toResult( id(resVar) );
        }
    }

    public void visitIfExpression(JFXIfExpression tree) {
        result = new IfExpressionTranslator(tree).doit();
    }

    public void visitLiteral(JFXLiteral tree) {
        // Just translate to literal value
        result = new ExpressionResult(translateLiteral(tree));
    }

    public void visitParens(JFXParens tree) {
        result = translate(tree.expr);
    }

    public void visitUnary(JFXUnary tree) {
        result = new UnaryOperationTranslator(tree).doit();
    }


/* ***************************************************************************
 * Visitor methods -- NOT implemented yet
 ****************************************************************************/

    void TODO() {
        throw new RuntimeException("Not yet implemented bind functionality");
    }

    public void visitAssign(JFXAssign tree) {
        TODO();
        translate(tree.lhs);
        translate(tree.rhs);
    }

    public void visitTypeCast(JFXTypeCast tree) {
        TODO();
        //(tree.clazz);
        translate(tree.expr);
    }

    public void visitInstanceOf(JFXInstanceOf tree) {
        TODO();
        translate(tree.expr);
        //(tree.clazz);
    }

    public void visitSelect(JFXSelect tree) {
        TODO();
        translate(tree.selected);
    }

    //@Override
    public void visitFunctionValue(JFXFunctionValue tree) {
        TODO();
        for (JFXVar param : tree.getParams()) {
            translate(param);
        }
        translate(tree.getBodyExpression());
    }

    //@Override
    public void visitSequenceEmpty(JFXSequenceEmpty that) {
        TODO();
    }
    
    //@Override
    public void visitSequenceRange(JFXSequenceRange that) {
        TODO();
        translate( that.getLower() );
        translate( that.getUpper() );
        translate( that.getStepOrNull() );
    }
    
    //@Override
    public void visitSequenceExplicit(JFXSequenceExplicit that) {
        TODO();
        translate( that.getItems() );
    }

    //@Override
    public void visitSequenceIndexed(JFXSequenceIndexed that) {
        TODO();
        translate(that.getSequence());
        translate(that.getIndex());
    }
    
    public void visitSequenceSlice(JFXSequenceSlice that) {
        TODO();
        translate(that.getSequence());
        translate(that.getFirstIndex());
        translate(that.getLastIndex());
    }

    public void visitStringExpression(JFXStringExpression that) {
        TODO();
        List<JFXExpression> parts = that.getParts();
        parts = parts.tail;
        while (parts.nonEmpty()) {
            parts = parts.tail;
            translate(parts.head);
            parts = parts.tail;
            parts = parts.tail;
        }
    }
    
    //@Override
    public void visitInstanciate(JFXInstanciate tree) {
        TODO();
       translate(tree.getIdentifier());
       translate(tree.getArgs());
       translate(tree.getParts());
       translate(tree.getLocalvars());
       translate(tree.getClassBody());
    }
    
    
    //@Override
    public void visitForExpression(JFXForExpression that) {
        TODO();
        for (ForExpressionInClauseTree cl : that.getInClauses()) {
            JFXForExpressionInClause clause = (JFXForExpressionInClause)cl;
            //(clause);
        }
        translate(that.getBodyExpression());
    }

    //@Override
    public void visitBlockExpression(JFXBlock that) {
        TODO();
        translate(that.stats);
        translate(that.value);
    }
    
    //@Override
    public void visitIndexof(JFXIndexof that) {
        TODO();
    }

    public void visitTimeLiteral(JFXTimeLiteral tree) {
        TODO();
    }

    public void visitInterpolateValue(JFXInterpolateValue that) {
        TODO();
        translate(that.attribute);
        translate(that.value);
        if  (that.interpolation != null) {
            translate(that.interpolation);
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

    public void visitAssignop(JFXAssignOp tree) {
        assert false : "should not be processed as part of a binding";
    }

    public void visitBreak(JFXBreak tree) {
        assert false : "should not be processed as part of a binding";
    }

    public void visitClassDeclaration(JFXClassDeclaration tree) {
        assert false : "should not be processed as part of a binding";
    }

    public void visitContinue(JFXContinue tree) {
        assert false : "should not be processed as part of a binding";
    }

    public void visitFunctionDefinition(JFXFunctionDefinition tree) {
        assert false : "should not be processed as part of a binding";
    }

    public void visitInitDefinition(JFXInitDefinition tree) {
        assert false : "should not be processed as part of a binding";
    }

    public void visitKeyFrameLiteral(JFXKeyFrameLiteral tree) {
        assert false : "should not be processed as part of a binding";
    }

    public void visitOnReplace(JFXOnReplace tree) {
        assert false : "should not be processed as part of a binding";
    }

    public void visitOverrideClassVar(JFXOverrideClassVar tree) {
        assert false : "should not be processed as part of a binding";
    }

    public void visitPostInitDefinition(JFXPostInitDefinition tree) {
        assert false : "should not be processed as part of a binding";
    }

    public void visitReturn(JFXReturn tree) {
        assert false : "should not be processed as part of a binding";
    }

    public void visitScript(JFXScript tree) {
        assert false : "should not be processed as part of a binding";
    }

    public void visitSequenceDelete(JFXSequenceDelete tree) {
        assert false : "should not be processed as part of a binding";
    }

    public void visitSequenceInsert(JFXSequenceInsert tree) {
        assert false : "should not be processed as part of a binding";
    }

    public void visitSkip(JFXSkip tree) {
        assert false : "should not be processed as part of a binding";
    }

    public void visitThrow(JFXThrow tree) {
        assert false : "should not be processed as part of a binding";
    }

    public void visitTry(JFXTry tree) {
        assert false : "should not be processed as part of a binding";
    }

    public void visitVar(JFXVar tree) {
        assert false : "should not be processed as part of a binding";
    }

    public void visitVarScriptInit(JFXVarScriptInit tree) {
        assert false : "should not be processed as part of a binding";
    }

    public void visitWhileLoop(JFXWhileLoop tree) {
        assert false : "should not be processed as part of a binding";
    }
}
