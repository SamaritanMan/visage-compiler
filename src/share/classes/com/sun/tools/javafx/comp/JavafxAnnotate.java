/*
 * Copyright 2003-2007 Sun Microsystems, Inc.  All Rights Reserved.
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

import com.sun.tools.javac.util.*;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.tree.JCTree.*;

import com.sun.tools.javafx.tree.JavafxTreeMaker;
import com.sun.tools.javafx.comp.JavafxAttr.Sequenceness;
import com.sun.tools.javafx.util.MsgSym;

/** Enter annotations on symbols.  Annotations accumulate in a queue,
 *  which is processed at the top level of any set of recursive calls
 *  requesting it be processed.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
public class JavafxAnnotate {
    protected static final Context.Key<JavafxAnnotate> javafxAnnotateKey =
	new Context.Key<JavafxAnnotate>();

    public static JavafxAnnotate instance(Context context) {
	JavafxAnnotate instance = context.get(javafxAnnotateKey);
	if (instance == null)
	    instance = new JavafxAnnotate(context);
	return instance;
    }

    final JavafxAttr attr;
    final TreeMaker make;
    final Log log;
    final Symtab syms;
    final Name.Table names;
    final JavafxResolve rs;
    final Types types;
//TODO:?    final ConstFold cfolder;
    final JavafxCheck chk;

    protected JavafxAnnotate(Context context) {
	context.put(javafxAnnotateKey, this);
	attr = JavafxAttr.instance(context);
	make = JavafxTreeMaker.instance(context);
	log = Log.instance(context);
	syms = Symtab.instance(context);
	names = Name.Table.instance(context);
	rs = JavafxResolve.instance(context);
	types = Types.instance(context);
//TODO:?	cfolder = ConstFold.instance(context);
	chk = (JavafxCheck)JavafxCheck.instance(context);
    }

/* ********************************************************************
 * Queue maintenance
 *********************************************************************/

    private int enterCount = 0;

    ListBuffer<Annotator> q = new ListBuffer<Annotator>();

    public void later(Annotator a) {
	q.append(a);
    }

    public void earlier(Annotator a) {
	q.prepend(a);
    }

    /** Called when the Enter phase starts. */
    public void enterStart() {
        enterCount++;
    }

    /** Called after the Enter phase completes. */
    public void enterDone() {
        enterCount--;
	flush();
    }

    public void flush() {
	if (enterCount != 0) return;
        enterCount++;
	try {
	    while (q.nonEmpty())
                q.next().enterAnnotation();
	} finally {
            enterCount--;
	}
    }

    /** A client that has annotations to add registers an annotator,
     *  the method it will use to add the annotation.  There are no
     *  parameters; any needed data should be captured by the
     *  Annotator.
     */
    public interface Annotator {
	void enterAnnotation();
	String toString();
    }


/* ********************************************************************
 * Compute an attribute from its annotation.
 *********************************************************************/

    /** Process a single compound annotation, returning its
     *  Attribute. Used from MemberEnter for attaching the attributes
     *  to the annotated symbol.
     */
    Attribute.Compound enterAnnotation(JCAnnotation a,
                                       Type expected,
				       JavafxEnv<JavafxAttrContext> env) {
	// The annotation might have had its type attributed (but not checked)
	// by attr.attribAnnotationTypes during MemberEnter, in which case we do not
	// need to do it again.
	Type at = (a.annotationType.type != null ? a.annotationType.type
		  : attr.attribType(a.annotationType, env));
	a.type = chk.checkType(a.annotationType.pos(), at, expected, Sequenceness.DISALLOWED);
	if (a.type.isErroneous())
	    return new Attribute.Compound(a.type, List.<Pair<MethodSymbol,Attribute>>nil());
	if ((a.type.tsym.flags() & Flags.ANNOTATION) == 0) {
	    log.error(a.annotationType.pos(), 
                      "not.annotation.type", a.type.toString());
	    return new Attribute.Compound(a.type, List.<Pair<MethodSymbol,Attribute>>nil());
	}
	List<JCExpression> args = a.args;
	if (args.length() == 1 && args.head.getTag() != JCTree.ASSIGN) {
	    // special case: elided "value=" assumed
	    args.head = make.at(args.head.pos).
		Assign(make.Ident(names.value), args.head);
	}
	ListBuffer<Pair<MethodSymbol,Attribute>> buf =
	    new ListBuffer<Pair<MethodSymbol,Attribute>>();
	for (List<JCExpression> tl = args; tl.nonEmpty(); tl = tl.tail) {
	    JCExpression t = tl.head;
	    if (t.getTag() != JCTree.ASSIGN) {
		log.error(t.pos(), MsgSym.MESSAGE_ANNOTATION_VALUE_MUST_BE_NAME_VALUE);
                continue;
	    }
	    JCAssign assign = (JCAssign)t;
	    if (assign.lhs.getTag() != JCTree.IDENT) {
		log.error(t.pos(), MsgSym.MESSAGE_ANNOTATION_VALUE_MUST_BE_NAME_VALUE);
                continue;
	    }
	    JCIdent left = (JCIdent)assign.lhs;
	    Symbol method = rs.resolveQualifiedMethod(left.pos(),
						      env,
						      a.type,
						      left.name,
						      rs.newMethTemplate(List.<Type>nil(), null));
	    left.sym = method;
	    left.type = method.type;
	    if (method.owner != a.type.tsym)
		log.error(left.pos(), MsgSym.MESSAGE_NO_ANNOTATION_MEMBER, left.name, a.type);
	    Type result = method.type.getReturnType();
	    Attribute value = enterAttributeValue(result, assign.rhs, env);
	    if (!method.type.isErroneous())
                buf.append(new Pair<MethodSymbol,Attribute>
                           ((MethodSymbol)method, value));
	}
	return new Attribute.Compound(a.type, buf.toList());
    }

    Attribute enterAttributeValue(Type expected,
				  JCExpression tree,
				  JavafxEnv<JavafxAttrContext> env) {
	if (expected.isPrimitive() || types.isSameType(expected, syms.stringType)) {
	    Type result = attr.attribExpr(tree, env, expected);
            if (result.isErroneous())
		return new Attribute.Error(expected);
	    if (result.constValue() == null) {
                log.error(tree.pos(), MsgSym.MESSAGE_ATTRIBUTE_VALUE_MUST_BE_CONSTANT);
		return new Attribute.Error(expected);
	    }
//TODO:?            result = cfolder.coerce(result, expected);
	    return new Attribute.Constant(expected, result.constValue());
	}
	if (expected.tsym == syms.classType.tsym) {
	    Type result = attr.attribExpr(tree, env, expected);
            if (result.isErroneous())
		return new Attribute.Error(expected);
	    if (TreeInfo.name(tree) != names._class) {
		log.error(tree.pos(), MsgSym.MESSAGE_ANNOTATION_VALUE_MUST_BE_CLASS_LITERAL);
		return new Attribute.Error(expected);
	    }
	    return new Attribute.Class(types,
				       (((JCFieldAccess) tree).selected).type);
	}
	if ((expected.tsym.flags() & Flags.ANNOTATION) != 0) {
	    if (tree.getTag() != JCTree.ANNOTATION) {
		log.error(tree.pos(), MsgSym.MESSAGE_ANNOTATION_VALUE_MUST_BE_ANNOTATION);
                expected = syms.errorType;
	    }
	    return enterAnnotation((JCAnnotation)tree, expected, env);
	}
	if (expected.tag == TypeTags.ARRAY) { // should really be isArray()
	    if (tree.getTag() != JCTree.NEWARRAY) {
		tree = make.at(tree.pos).
		    NewArray(null, List.<JCExpression>nil(), List.of(tree));
	    }
	    JCNewArray na = (JCNewArray)tree;
	    if (na.elemtype != null) {
		log.error(na.elemtype.pos(), MsgSym.MESSAGE_NEW_NOT_ALLOWED_IN_ANNOTATION);
		return new Attribute.Error(expected);
	    }
	    ListBuffer<Attribute> buf = new ListBuffer<Attribute>();
	    for (List<JCExpression> l = na.elems; l.nonEmpty(); l=l.tail) {
		buf.append(enterAttributeValue(types.elemtype(expected),
					       l.head,
					       env));
	    }
	    return new Attribute.
		Array(expected, buf.toArray(new Attribute[buf.length()]));
	}
	if (expected.tag == TypeTags.CLASS &&
            (expected.tsym.flags() & Flags.ENUM) != 0) {
	    attr.attribExpr(tree, env, expected);
	    Symbol sym = TreeInfo.symbol(tree);
	    if (sym == null ||
		TreeInfo.nonstaticSelect(tree) ||
		sym.kind != Kinds.VAR ||
		(sym.flags() & Flags.ENUM) == 0) {
		log.error(tree.pos(), MsgSym.MESSAGE_ENUM_ANNOTATION_MUST_BE_ENUM_CONSTANT);
		return new Attribute.Error(expected);
	    }
	    VarSymbol enumerator = (VarSymbol) sym;
	    return new Attribute.Enum(expected, enumerator);
	}
	if (!expected.isErroneous())
            log.error(tree.pos(), MsgSym.MESSAGE_ANNOTATION_VALUE_NOT_ALLOWABLE_TYPE);
	return new Attribute.Error(attr.attribExpr(tree, env, expected));
    }
}
