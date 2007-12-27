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

tree grammar v3Walker;

options { 
   tokenVocab=v3;
   ASTLabelType=CommonTree;
   superClass = AbstractGeneratedTreeParser; 
}

@header {
package com.sun.tools.javafx.antlr;

import java.util.HashMap;
import java.util.Map;
import java.io.OutputStreamWriter;

import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javafx.tree.*;
import com.sun.javafx.api.tree.*;

import com.sun.tools.javac.code.*;
import com.sun.tools.javac.util.*;
import static com.sun.tools.javac.util.ListBuffer.lb;
import com.sun.javafx.api.JavafxBindStatus;
import static com.sun.javafx.api.JavafxBindStatus.*;

import org.antlr.runtime.*;
}
	
module returns [JCCompilationUnit result]
	: ^(MODULE packageDecl? moduleItems)		{ $result = F.TopLevel(noJCAnnotations(), $packageDecl.value, $moduleItems.items.toList()); }
       	;
packageDecl  returns [JCExpression value]
       	: ^(PACKAGE qualident)        			{ $value = $qualident.expr; }
	;
moduleItems  returns [ListBuffer<JCTree> items = new ListBuffer<JCTree>()]  
	: ( moduleItem					{ $items.append($moduleItem.value); }
	  )*
	;
moduleItem  returns [JCTree value]
       : importDecl 					{ $value = $importDecl.value; }
       | classDefinition 				{ $value = $classDefinition.value; }
       | statement      				{ $value = $statement.value; } 
       | expression 					{ $value = $expression.expr; } 
	;
importDecl  returns [JCTree value]
 	: ^(IMPORT importId)				{ $value = F.at(pos($IMPORT)).Import($importId.pid, false); }
	;
importId  returns [JCExpression pid]
 	: identifier					{ $pid = $identifier.expr; }
        | ^(DOT importId	( name 			{ $pid = F.at($name.pos).Select($importId.pid, $name.value); }
        			| STAR			{ $pid = F.at(pos($STAR)).Select($importId.pid, names.asterisk); }
        			)
           )		
	;
classDefinition  returns [JFXClassDeclaration value]
	: ^(CLASS classModifierFlags name supers? classMembers)
	  						{ $value = F.at(pos($CLASS)).ClassDeclaration(
	  						  F.at(pos($CLASS)).Modifiers($classModifierFlags.flags),
	  						  $name.value,
	                                	          $supers.ids.toList(), 
	                                	           $classMembers.mems.toList()); }
	;
supers  returns [ListBuffer<JCExpression> ids = new ListBuffer<JCExpression>()]
	: ^(EXTENDS (typeName           		{ $ids.append($typeName.expr); } )* )
	;	  					
classMembers  returns [ListBuffer<JCTree> mems = new ListBuffer<JCTree>()]
	: ( classMember					{ $mems.append($classMember.member); } )*
	;
classMember  returns [JCTree member]
	: initDefinition				{ $member = $initDefinition.value; } 
	| variableDeclaration 				{ $member = $variableDeclaration.value; } 
	| functionDefinition 				{ $member = $functionDefinition.value; } 
	;
functionDefinition  returns [JFXOperationDefinition value]
	: ^(FUNCTION name functionModifierFlags formalParameters type blockExpression?)
	    						{ $value = F.at(pos($FUNCTION)).OperationDefinition(
	    						  F.at(pos($FUNCTION)).Modifiers($functionModifierFlags.flags),
	    						  $name.value, $type.type, 
	    						  $formalParameters.params.toList(), $blockExpression.expr); }
	;
initDefinition  returns [JFXInitDefinition value]
	: ^(INIT block)					{ $value = F.at(pos($INIT)).InitDefinition($block.value); }
	;
functionModifierFlags  returns [long flags]
	: ^(MODIFIER accessModifier?  functionModifier?)
	 		 				{ $flags = $accessModifier.flag | $functionModifier.flag; }
	;
varModifierFlags  returns [long flags]
	: ^(MODIFIER accessModifier? varModifier?)
	 		 				{ $flags = $accessModifier.flag | $varModifier.flag; }
	;
classModifierFlags  returns [long flags]
	: ^(MODIFIER accessModifier? classModifier?)
	 		 				{ $flags = $accessModifier.flag | $classModifier.flag; }
	;
accessModifier   returns [long flag]
	:  PUBLIC          				{ flag = Flags.PUBLIC; }
	|  PRIVATE         				{ flag = Flags.PRIVATE; }
	|  PROTECTED       				{ flag = Flags.PROTECTED; } 
	;
functionModifier returns [long flag]
	:  ABSTRACT        				{ flag = Flags.ABSTRACT; }
	|  STATIC        				{ flag = Flags.STATIC; } 
	;
varModifier returns [long flag]
	:  READONLY        				{ flag = Flags.FINAL; } 
	|  STATIC        				{ flag = Flags.STATIC; } 
	;
classModifier returns [long flag]
	:  ABSTRACT        				{ flag = Flags.ABSTRACT; }
	;
memberSelector  returns [JFXMemberSelector value]
	: ^(MEMBERSELECTOR n1=name n2=name)		{ $value = F.at(pos($MEMBERSELECTOR)).MemberSelector($n1.value, $n2.value); } 
	;
formalParameters  returns [ListBuffer<JFXVar> params = new ListBuffer<JFXVar>()]
	: ^(LPAREN (formalParameter			{ params.append($formalParameter.var); } )* )
	;
formalParameter returns [JFXVar var]
	: ^(PARAM name type)				{ $var = F.at($name.pos).Param($name.value, $type.type); } 
	;
block  returns [JCBlock value]
@init { ListBuffer<JCStatement> stats = ListBuffer.<JCStatement>lb(); }
	: ^(BLOCK
		(	^(STATEMENT statement)		{ stats.append($statement.value); }	
		| 	^(EXPRESSION expression)	{ stats.append(F.Exec($expression.expr)); }
		)*
	    )
	;
blockExpression  returns [JFXBlockExpression expr]
@init { ListBuffer<JCStatement> stats = new ListBuffer<JCStatement>(); JCExpression val = null; }
	: ^(LBRACE 
		(	^(STATEMENT statement)		{ if (val != null) { stats.append(F.Exec(val)); val = null; }
	     					  	  stats.append($statement.value); }
		| 	^(EXPRESSION expression)	{ if (val != null) { stats.append(F.Exec(val)); }
	     					  	  val = $expression.expr; }
		)*
	    )
	;
variableDeclaration    returns [JCStatement value]
	: ^(VAR variableLabel varModifierFlags name type boundExpression? onChanges)
	    						{ $value = F.at(pos($VAR)).Var($name.value, 
	    							$type.type, 
	    							F.at($variableLabel.pos).Modifiers($varModifierFlags.flags),
	    							$variableLabel.local,
	    							$boundExpression.expr, 
	    							$boundExpression.status, 
	    							$onChanges.listb.toList()); }
	;
onChanges   returns [ListBuffer<JFXAbstractOnChange> listb = ListBuffer.<JFXAbstractOnChange>lb()]
	: ( onChangeClause				{ listb.append($onChangeClause.value); } )*
	;
onChangeClause     returns [JFXAbstractOnChange value]
	: ^(ON_REPLACE oldv=formalParameter? block)
							{ $value = F.at(pos($ON_REPLACE)).OnReplace($oldv.var, $block.value); }
	| ^(ON_REPLACE_ELEMENT index=formalParameter oldv=formalParameter? block)
							{ $value = F.at(pos($ON_REPLACE_ELEMENT)).OnReplaceElement($index.var, $oldv.var, $block.value); }
	| ^(ON_INSERT_ELEMENT index=formalParameter newv=formalParameter? block)
							{ $value = F.at(pos($ON_INSERT_ELEMENT)).OnInsertElement($index.var, $newv.var, $block.value); }
	| ^(ON_DELETE_ELEMENT index=formalParameter oldv=formalParameter? block)
							{ $value = F.at(pos($ON_DELETE_ELEMENT)).OnDeleteElement($index.var, $oldv.var, $block.value); }
	;
variableLabel    returns [boolean local, int pos]
	: VAR						{ $local = true; $pos = pos($VAR); }
	| LET						{ $local = true; $pos = pos($LET); }
	| ATTRIBUTE					{ $local = false; $pos = pos($ATTRIBUTE); }
	;
statement returns [JCStatement value]
	: variableDeclaration				{ $value = $variableDeclaration.value; }
	| functionDefinition 				{ $value = $functionDefinition.value; }
	| BREAK    					{ $value = F.at(pos($BREAK)).Break(null); }
	| CONTINUE  	 	 			{ $value = F.at(pos($CONTINUE)).Continue(null); }
       	| ^(THROW expression)	   			{ $value = F.at(pos($THROW)).Throw($expression.expr); } 
	| ^(WHILE expression block)			{ $value = F.at(pos($WHILE)).WhileLoop($expression.expr, $block.value); }
	| ^(INSERT e1=expression e2=expression)		{ $value = F.at(pos($INSERT)).SequenceInsert($e2.expr, $e1.expr); } 
	| ^(FROM e1=expression e2=expression)		{ $value = F.at(pos($FROM)).SequenceDelete($e2.expr,$e1.expr); } 
	| ^(DELETE expression)				{ $value = F.at(pos($DELETE)).SequenceDelete($expression.expr); } 
	| ^(RETURN expression?)				{ $value = F.at(pos($RETURN)).Return($expression.expr); } 
	| ^(TRY block catchClauses finallyClause?)	{ $value = F.at(pos($TRY)).Try($block.value, $catchClauses.caught.toList(), $finallyClause.value); }
       	;
catchClauses  returns [ListBuffer<JCCatch> caught = ListBuffer.lb()]
	: ( catchClause					{ $caught.append($catchClause.value); } )*
	;
catchClause  returns [JCCatch value]
	: ^(CATCH formalParameter block)		{ $value = F.at(pos($CATCH)).Catch($formalParameter.var, $block.value); } 
	;
finallyClause  returns [JCBlock value]
	: ^(FINALLY block)				{ $value = $block.value; }
	;
boundExpression   returns [JavafxBindStatus status, JCExpression expr]
@init { boolean isLazy = false; boolean isBidirectional = false; }
	: ^(BIND 				
	      (LAZY					{ isLazy = true; } )?
	      (INVERSE					{ isBidirectional = true; } )?
	      expression				{ $expr = $expression.expr; }
	      						{ $status = isBidirectional? isLazy? JavafxBindStatus.LAZY_BIDIBIND : JavafxBindStatus.BIDIBIND
	  									   : isLazy? JavafxBindStatus.LAZY_UNIDIBIND :  JavafxBindStatus.UNIDIBIND; }
	   )
	| ^(EXPRESSION expression)			{ $expr = $expression.expr; $status = UNBOUND; }
	;
expression  returns [JCExpression expr] 
       	: ^(FOR inClauses expression)			{ $expr = F.at(pos($FOR)).ForExpression($inClauses.clauses.toList(), $expression.expr); }
	| ^(IF econd=expression ethen=expression eelse=expression?)
							{ $expr = F.at(pos($IF)).Conditional($econd.expr, $ethen.expr, $eelse.expr); }
	| ^(EQ lhs=expression expression)		{ $expr = F.at(pos($EQ)).Assign($lhs.expr, $expression.expr); } 
	| ^(PLUSEQ lhs=expression expression) 		{ $expr = F.at(pos($PLUSEQ)).Assignop(JCTree.PLUS_ASG, $lhs.expr, $expression.expr); }
	| ^(SUBEQ lhs=expression expression) 		{ $expr = F.at(pos($SUBEQ)).Assignop(JCTree.MINUS_ASG, $lhs.expr, $expression.expr); }
	| ^(STAREQ lhs=expression expression) 		{ $expr = F.at(pos($STAREQ)).Assignop(JCTree.MUL_ASG, $lhs.expr, $expression.expr); }
	| ^(SLASHEQ lhs=expression expression) 		{ $expr = F.at(pos($SLASHEQ)).Assignop(JCTree.DIV_ASG, $lhs.expr, $expression.expr); }
	| ^(PERCENTEQ lhs=expression expression) 	{ $expr = F.at(pos($PERCENTEQ)).Assignop(JCTree.MOD_ASG, $lhs.expr, $expression.expr); }
	| ^(AND e1=expression e2=expression) 		{ $expr = F.at(pos($AND)).Binary(JCTree.AND, $e1.expr, $e2.expr); }
	| ^(OR e1=expression e2=expression) 		{ $expr = F.at(pos($OR)).Binary(JCTree.OR, $e1.expr, $e2.expr); } 
	| ^(INSTANCEOF expression typeName)		{ $expr = F.at(pos($INSTANCEOF)).TypeTest($expression.expr, $typeName.expr); }
	| ^(AS expression typeName)			{ $expr = F.at(pos($AS)).TypeCast($typeName.expr, $expression.expr); }   
	| ^(LTGT e1=expression e2=expression)		{ $expr = F.at(pos($LTGT)).Binary(JCTree.NE, $e1.expr, $e2.expr); }
	| ^(EQEQ e1=expression e2=expression)		{ $expr = F.at(pos($EQEQ)).Binary(JCTree.EQ, $e1.expr, $e2.expr); }
	| ^(LTEQ e1=expression e2=expression)		{ $expr = F.at(pos($LTEQ)).Binary(JCTree.LE, $e1.expr, $e2.expr); }
	| ^(GTEQ e1=expression e2=expression)		{ $expr = F.at(pos($GTEQ)).Binary(JCTree.GE, $e1.expr, $e2.expr); }
	| ^(LT   e1=expression e2=expression)		{ $expr = F.at(pos($LT))  .Binary(JCTree.LT, $e1.expr, $e2.expr); }
	| ^(GT   e1=expression e2=expression)		{ $expr = F.at(pos($GT))  .Binary(JCTree.GT, $e1.expr, $e2.expr); }
	| ^(PLUS e1=expression e2=expression)		{ $expr = F.at(pos($PLUS)).Binary(JCTree.PLUS , $e1.expr, $e2.expr); }
	| ^(SUB  e1=expression e2=expression)		{ $expr = F.at(pos($SUB)) .Binary(JCTree.MINUS, $e1.expr, $e2.expr); }
	| ^(STAR    e1=expression e2=expression)	{ $expr = F.at(pos($STAR))   .Binary(JCTree.MUL  , $e1.expr, $e2.expr); }
	| ^(SLASH   e1=expression e2=expression)	{ $expr = F.at(pos($SLASH))  .Binary(JCTree.DIV  , $e1.expr, $e2.expr); }
	| ^(PERCENT e1=expression e2=expression)	{ $expr = F.at(pos($PERCENT)).Binary(JCTree.MOD  , $e1.expr, $e2.expr); }   
	| ^(NEGATIVE expression)			{ $expr = F.at(pos($NEGATIVE)).Unary(JCTree.NEG, $expression.expr); }
	| ^(NOT expression)				{ $expr = F.at(pos($NOT)).Unary(JCTree.NOT, $expression.expr); }	
	| ^(SIZEOF expression)				{ $expr = F.at(pos($SIZEOF)).Unary(JavafxTag.SIZEOF, $expression.expr); }
	| ^(PLUSPLUS expression)   			{ $expr = F.at(pos($PLUSPLUS)).Unary(JCTree.PREINC, $expression.expr); }
	| ^(SUBSUB expression) 				{ $expr = F.at(pos($SUBSUB)).Unary(JCTree.PREDEC, $expression.expr); }
	| ^(POSTINCR expression)			{ $expr = F.at(pos($POSTINCR)).Unary(JCTree.POSTINC, $expression.expr); }
	| ^(POSTDECR expression)			{ $expr = F.at(pos($POSTDECR)).Unary(JCTree.POSTDEC, $expression.expr); }
	| ^(DOT expression name)			{ $expr = F.at(pos($DOT)).Select($expression.expr, $name.value); }
	| ^(FUNC_APPLY expression expressionList)	{ $expr = F.at(pos($FUNC_APPLY)).Apply(null, $expression.expr, $expressionList.args.toList()); } 
	| ^(SEQ_INDEX seq=expression idx=expression)	{ $expr = F.at(pos($SEQ_INDEX)).SequenceIndexed($seq.expr, $idx.expr); }
	| ^(OBJECT_LIT qualident objectLiteral)		{ $expr = F.at(pos($OBJECT_LIT)).Instanciate($qualident.expr, null, $objectLiteral.parts.toList()); } 
       	| ^(FUNC_EXPR formalParameters type blockExpression)
       							{ $expr = F.at(pos($FUNC_EXPR)).OperationValue($type.type, $formalParameters.params.toList(),
                                               								$blockExpression.expr); }
	| ^(NEW typeName expressionList)		{ $expr = F.at(pos($NEW)).Instanciate($typeName.expr, $expressionList.args.toList(), null); }
	| stringExpression				{ $expr = $stringExpression.expr; }
	| explicitSequenceExpression			{ $expr = $explicitSequenceExpression.expr; }
	| ^(DOTDOT from=expression to=expression step=expression? EXCLUSIVE?)
							{ $expr = F.at(pos($DOTDOT)).RangeSequence($from.expr, $to.expr, $step.expr, $EXCLUSIVE!=null); }
	| SEQ_EMPTY					{ $expr = F.at(pos($SEQ_EMPTY)).EmptySequence(); }
       	| THIS						{ $expr = F.at(pos($THIS)).Ident(names._this); }
       	| SUPER						{ $expr = F.at(pos($SUPER)).Ident(names._super); }
	| t=STRING_LITERAL				{ $expr = F.at(pos($t)).Literal(TypeTags.CLASS, $t.text); }
	| t=DECIMAL_LITERAL				{ $expr = F.at(pos($t)).Literal(TypeTags.INT, Convert.string2int($t.text, 10)); }
	| t=OCTAL_LITERAL				{ $expr = F.at(pos($t)).Literal(TypeTags.INT, Convert.string2int($t.text, 8)); }
	| t=HEX_LITERAL					{ $expr = F.at(pos($t)).Literal(TypeTags.INT, Convert.string2int($t.text, 16)); }
	| t=FLOATING_POINT_LITERAL 			{ $expr = F.at(pos($t)).Literal(TypeTags.DOUBLE, Double.valueOf($t.text)); }
	| t=TRUE   					{ $expr = F.at(pos($t)).Literal(TypeTags.BOOLEAN, 1); }
	| t=FALSE   					{ $expr = F.at(pos($t)).Literal(TypeTags.BOOLEAN, 0); }
	| t=NULL 					{ $expr = F.at(pos($t)).Literal(TypeTags.BOT, null); } 
	;
inClauses  returns [ListBuffer<JFXForExpressionInClause> clauses = ListBuffer.lb()]
	: ( inClause					{ clauses.append($inClause.value); } )*	
	;
inClause  returns [JFXForExpressionInClause value] 
	: ^(IN formalParameter se=expression we=expression?)
							{ $value = F.at(pos($IN)).InClause($formalParameter.var, $se.expr, $we.expr); }
	;
stringExpression  returns [JCExpression expr] 
@init { ListBuffer<JCExpression> strexp = new ListBuffer<JCExpression>(); }
	: ^(QUOTE_LBRACE_STRING_LITERAL			{ strexp.append(F.at(pos($QUOTE_LBRACE_STRING_LITERAL)).Literal(TypeTags.CLASS,
												 $QUOTE_LBRACE_STRING_LITERAL.text)); }
		  f1=stringFormat			{ strexp.append($f1.expr); }
		  e1=expression 			{ strexp.append($e1.expr); }
		  (  rl=RBRACE_LBRACE_STRING_LITERAL	{ strexp.append(F.at(pos($rl)).Literal(TypeTags.CLASS, $rl.text)); }
		     fn=stringFormat			{ strexp.append($fn.expr); }
		     en=expression 			{ strexp.append($en.expr); }
		  )*   
		  rq=RBRACE_QUOTE_STRING_LITERAL	{ strexp.append(F.at(pos($rq)).Literal(TypeTags.CLASS, $rq.text)); }
		  					{ $expr = F.at(pos($QUOTE_LBRACE_STRING_LITERAL)).StringExpression(strexp.toList()); }
	    )
	;
stringFormat  returns [JCExpression expr] 
	: fs=FORMAT_STRING_LITERAL			{ $expr = F.at(pos($fs)).Literal(TypeTags.CLASS, $fs.text); }
	| EMPTY_FORMAT_STRING				{ $expr = F.             Literal(TypeTags.CLASS, ""); }
	;
explicitSequenceExpression   returns [JFXSequenceExplicit expr]
@init { ListBuffer<JCExpression> exps = new ListBuffer<JCExpression>(); }
	: ^(SEQ_EXPLICIT   
	    ( expression 				{ exps.append($expression.expr); } )*
	   )						{ $expr = F.at(pos($SEQ_EXPLICIT)).ExplicitSequence(exps.toList()); }
	;
objectLiteral  returns [ListBuffer<JCTree> parts = ListBuffer.<JCTree>lb()]
	: ( objectLiteralPart  				{ $parts.append($objectLiteralPart.value); } ) * 
	;
objectLiteralPart  returns [JCTree value]
	: ^(OBJECT_LIT_PART name boundExpression)	{ $value = F.at(pos($OBJECT_LIT_PART)).ObjectLiteralPart($name.value,
								 $boundExpression.expr, $boundExpression.status); }
       	| variableDeclaration				{ $value = $variableDeclaration.value; }
       	| functionDefinition				{ $value = $functionDefinition.value; }
       	;
expressionList  returns [ListBuffer<JCExpression> args = new ListBuffer<JCExpression>()] 
	: ^(EXPR_LIST (expression			{ $args.append($expression.expr); }  )* )
	;
type  returns [JFXType type]
	: ^(TYPE_NAMED typeName cardinality)		{ $type = F.at(pos($TYPE_NAMED)).TypeClass($typeName.expr, $cardinality.ary); }
 	| ^(TYPE_FUNCTION typeArgList type cardinality)	{ $type = F.at(pos($TYPE_FUNCTION)).TypeFunctional($typeArgList.ptypes.toList(), $type.type, $cardinality.ary); }
 	| ^(TYPE_ANY cardinality)			{ $type = F.at(pos($TYPE_ANY)).TypeAny($cardinality.ary); } 
 	| TYPE_UNKNOWN					{ $type = F.TypeUnknown(); }
 	;
typeArgList   returns [ListBuffer<JFXType> ptypes = ListBuffer.<JFXType>lb(); ]
 	: (typeArg					{ ptypes.append($typeArg.type); } )*
	;
typeArg  returns [JFXType type]
 	: ^(COLON name? type)				{ $type = $type.type; }
 	;
cardinality returns [TypeTree.Cardinality ary]
	: RBRACKET					{ ary = TypeTree.Cardinality.ANY; }
	|                         			{ ary = TypeTree.Cardinality.SINGLETON; } 
	;	
typeName  returns [JCExpression expr]
	: ^(TYPE_ARG qualident genericArguments)	{ $expr = F.at(pos($TYPE_ARG)).TypeApply($qualident.expr, $genericArguments.exprbuff.toList()); }
	| qualident
	;
genericArguments  returns [ListBuffer<JCExpression> exprbuff = ListBuffer.<JCExpression>lb()]
	: ( genericArgument				{ $exprbuff.append($genericArgument.expr); } )* 
	;	
genericArgument  returns [JCExpression expr]
@init { BoundKind bk = BoundKind.UNBOUND; JCExpression texpr = null; }
	: typeName					{ $expr = $typeName.expr; }
	| ^(QUES (  ( EXTENDS 				{ bk = BoundKind.EXTENDS; }
		    | SUPER				{ bk = BoundKind.SUPER; }
		    ) 
		 typeName				{ texpr = $typeName.expr; }
	         )?					{ $expr = F.at(pos($QUES)).Wildcard(F.TypeBoundKind(bk), texpr); }
	    )
	;
qualident  returns [JCExpression expr]
	: name 				          	{ $expr = F.at($name.pos).Ident($name.value); } 
	| ^(DOT qualident name)				{ $expr = F.at(pos($DOT)).Select($qualident.expr, $name.value); } 
	;
identifier  returns [JCExpression expr]
	: name 				          	{ $expr = F.at($name.pos).Ident($name.value); } 
	;
name returns [Name value, int pos]
	: plainName					{ $value = $plainName.value; $pos = $plainName.pos; }
	| frenchName					{ $value = $frenchName.value; $pos = $frenchName.pos; }
	;
plainName returns [Name value, int pos]
	: IDENTIFIER					{ $value = Name.fromString(names, $IDENTIFIER.text); $pos = pos($IDENTIFIER); } 
	;
frenchName returns [Name value, int pos]
	: QUOTED_IDENTIFIER				{ $value = Name.fromString(names, $QUOTED_IDENTIFIER.text); $pos = pos($QUOTED_IDENTIFIER); } 
	;
