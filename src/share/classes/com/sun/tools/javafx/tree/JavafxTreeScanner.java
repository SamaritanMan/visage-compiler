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

package com.sun.tools.javafx.tree;

import com.sun.tools.javac.util.*;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCStatement;

/**
 *
 * @author Robert Field
 */
public class JavafxTreeScanner extends TreeScanner implements JavafxVisitor {

    public JavafxTreeScanner() {
    }

    /** Visitor method: Scan a single node.
   */
    public void scan(JCTree tree) {
	if(tree!=null) tree.accept(this);
    }

    /** Visitor method: scan a list of nodes.
     */
    public void scan(List<? extends JCTree> trees) {
	if (trees != null)
	for (List<? extends JCTree> l = trees; l.nonEmpty(); l = l.tail)
	    scan(l.head);
    }


/* ***************************************************************************
 * Visitor methods
 ****************************************************************************/
    // Begin JavaFX trees
    
    @Override
    public void visitClassDeclaration(JFXClassDeclaration that) {
        that.mods.accept(this);
        for (JCTree member : that.defs) {
            member.accept(this);
        }
    }
    
    @Override
    public void visitOperationValue(JFXOperationValue that) {
        for (JFXVar param : that.getParameters()) {
            param.accept((JavafxVisitor)this);
        }
        that.getBodyExpression().accept((JavafxVisitor)this);
    }

    @Override
    public void visitOperationDefinition(JFXOperationDefinition that) {
        that.getModifiers().accept(this);
        that.getJFXReturnType().accept((JavafxVisitor)this);
        visitOperationValue(that.operation);
    }

    @Override
    public void visitInitDefinition(JFXInitDefinition that) {
        that.getBody().accept(this);
    }

    @Override
    public void visitDoLater(JFXDoLater that) {
        that.getBody().accept(this);
    }

    @Override
    public void visitMemberSelector(JFXMemberSelector that) {
    }
    
    @Override
    public void visitSequenceEmpty(JFXSequenceEmpty that) {
    }
    
    @Override
    public void visitSequenceRange(JFXSequenceRange that) {
        that.getLower().accept(this);
        that.getUpper().accept(this);
    }
    
    @Override
    public void visitSequenceExplicit(JFXSequenceExplicit that) {
        for (JCExpression expr : that.getItems()) {
            expr.accept(this);
        }
    }

    @Override
    public void visitSequenceIndexed(JFXSequenceIndexed that) {
        that.getSequence().accept(this);
        that.getIndex().accept(this);
    }
    
    @Override
    public void visitSequenceInsert(JFXSequenceInsert that) {
        scan(that.getSequence());
        scan(that.getElement());
    }
    
    @Override
    public void visitSequenceDelete(JFXSequenceDelete that) {
        scan(that.getSequence());
        scan(that.getElement());
    }

    @Override
    public void visitStringExpression(JFXStringExpression that) {
        List<JCExpression> parts = that.getParts();
        parts = parts.tail;
        while (parts.nonEmpty()) {
            parts = parts.tail;
            parts.head.accept(this);
            parts = parts.tail;
            parts = parts.tail;
        }
    }
    
    @Override
    public void visitPureObjectLiteral(JFXPureObjectLiteral that) {
        that.getIdentifier().accept(this);
        for (JCStatement part : that.getParts()) {
            if (part != null) {
                part.accept(this);
            }
        }
    }
    
    @Override
    public void visitSetAttributeToObjectBeingInitialized(JFXSetAttributeToObjectBeingInitialized that) {
    }
    
    @Override
    public void visitObjectLiteralPart(JFXObjectLiteralPart that) {
        that.getExpression().accept(this);
    }  
    
    @Override
    public void visitTypeAny(JFXTypeAny that) {
    }
    
    @Override
    public void visitTypeClass(JFXTypeClass that) {
    }
    
    @Override
    public void visitTypeFunctional(JFXTypeFunctional that) {
        for (JCTree param : that.getParameters()) {
            param.accept(this);
        }
        that.getReturnType().accept((JavafxVisitor)this);
    }
    
    @Override
    public void visitTypeUnknown(JFXTypeUnknown that) {
    }
    
    @Override
    public void visitVar(JFXVar tree) {
        scan(tree.getJFXType());
	scan(tree.mods);
	scan(tree.init);
    }
    
    public void visitAbstractOnChange(JFXAbstractOnChange tree) {
	scan(tree.getIndex());
	scan(tree.getOldValue());  
        scan(tree.getBody());
    }
    
    @Override
    public void visitOnReplace(JFXOnReplace tree) {
        visitAbstractOnChange(tree);
    }
    
    @Override
    public void visitOnReplaceElement(JFXOnReplaceElement tree) {
        visitAbstractOnChange(tree);
    }
    
    @Override
    public void visitOnInsertElement(JFXOnInsertElement tree) {
        visitAbstractOnChange(tree);
    }
    
    @Override
    public void visitOnDeleteElement(JFXOnDeleteElement tree) {
        visitAbstractOnChange(tree);
    }
    
    @Override
    public void visitOnDeleteAll(JFXOnDeleteAll tree) {
        visitAbstractOnChange(tree);
    }
    
    @Override
    public void visitForExpression(JFXForExpression that) {
        for (JFXForExpressionInClause clause : that.getInClauses()) {
            clause.accept((JavafxVisitor)this);
        }
        that.getBodyExpression().accept(this);
    }
    
    @Override
    public void visitForExpressionInClause(JFXForExpressionInClause that) {
        that.getVar().accept((JavafxVisitor)this);
        that.getSequenceExpression().accept(this);
        if (that.getWhereExpression() != null) {
            that.getWhereExpression().accept(this);
        }
    }
    
    @Override
    public void visitInstanciate(JFXInstanciate that) {
        visitNewClass(that);
    }
        
    @Override
    public void visitBlockExpression(JFXBlockExpression that) {
        scan(that.stats);
        scan(that.value);
    }

    @Override
    public void visitTree(JCTree that) {
        assert false : "Should not be here!!!";
    }
}
