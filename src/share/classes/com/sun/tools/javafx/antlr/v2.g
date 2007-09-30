grammar v2;

options { superClass = AbstractGeneratedParser; }
 
tokens {
   ABSTRACT='abstract';
   AFTER='after';
   AND='and';
   AS='as';
   ASSERT='assert';
   ATTRIBUTE='attribute';
   BEFORE='before';
   BIND='bind';
   BREAK='break';
   BY='by';
   CATCH='catch';
   CLASS='class';
   CONTINUE='continue';
   DELETE='delete';
   DO='do';
   DUR='dur';
   EASEBOTH='easeboth';
   EASEIN='easein';
   EASEOUT='easeout';
   ELSE='else';
   EXTENDS='extends';
   FALSE='false';
   FINALLY='finally';
   FIRST='first';
   FOREACH='foreach';
   FROM='from';
   FUNCTION='function';
   IF='if';
   IMPLEMENTS='implements';
   IMPORT='import';
   IN='in';
   INDEX='index';
   INDEXOF='indexof';
   INIT='init';
   INSTANCEOF='instanceof';
   INSERT='insert';
   INTO='into';
   INVERSE='inverse';
   LAST='last';
   LAZY='lazy';
   LET='let';
   LINEAR='linear';
   MOTION='motion';
   NEW='new';
   NOT='not';
   NULL='null';
   OPERATION='operation';
   ON='on';
   OR='or';
   ORDER='order';
   PACKAGE='package';
   PRIVATE='private';
   PROTECTED='protected';
   PUBLIC='public';
   READONLY='readonly';
   RETURN='return';
   REPLACE='replace';
   REVERSE='reverse';
   SELECT='select';
   SUPER='super';
   SIZEOF='sizeof';
   STAYS='stays';
   STATIC='static';
   THEN='then';
   THIS='this';
   THROW='throw';
   TIE='tie';
   TRY='try';
   TRUE='true';
   TYPE='type';  
   TYPEOF='typeof';
   VAR='var';
   WITH='with';
   WHILE='while';
   WHERE='where';
   
   BAR='|';
   POUND='#';
   DOTDOT='..';
   LPAREN='(';
   RPAREN=')';
   LBRACKET='[';
   RBRACKET=']';
   SEMI=';';
   COMMA=',';
   DOT='.';
   EQEQ='==';
   EQ='=';
   GT='>';
   LT='<';
   LTGT='<>';
   LTEQ='<=';
   GTEQ='>=';
   PLUS='+';
   PLUSPLUS='++';
   SUB='-';
   SUBSUB='--';
   STAR='*';
   SLASH='/';
   PERCENT='%';
   PLUSEQ='+=';
   SUBEQ='-=';
   STAREQ='*=';
   SLASHEQ='/=';
   PERCENTEQ='%=';
   COLON=':';
   QUES='?';
}

@lexer::header {
package com.sun.tools.javafx.antlr;
}

@header {
package com.sun.tools.javafx.antlr;

import java.util.HashMap;
import java.util.Map;
import java.io.OutputStreamWriter;

import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javafx.tree.*;

import com.sun.tools.javac.code.*;
import com.sun.tools.javac.util.*;
import static com.sun.tools.javac.util.ListBuffer.lb;
import com.sun.tools.javafx.code.JavafxBindStatus;
import static com.sun.tools.javafx.code.JavafxBindStatus.*;

import org.antlr.runtime.*;
}

@members {
        public v2Parser(Context context, CharSequence content) {
           this(new CommonTokenStream(new v2Lexer(new ANTLRStringStream(content.toString()))));
           initialize(context);
    	}
}

@lexer::members {
    /** Allow emitting more than one token from a lexer rule
     */
    List tokens = new ArrayList();
    public void emit(Token token) {
            this.token = token;
        	tokens.add(token);
    }
    public Token nextToken() {
    	super.nextToken();
        if ( tokens.size()==0 ) {
            return Token.EOF_TOKEN;
        }
        return (Token)tokens.remove(0);
    }
    
    /** Track "He{"l{"l"}o"} world" quotes
     */
    private static class BraceQuoteTracker {
        private static BraceQuoteTracker quoteStack = null;
        private int braceDepth;
        private char quote;
        private boolean percentIsFormat;
        private BraceQuoteTracker next;
        private BraceQuoteTracker(BraceQuoteTracker prev, char quote, boolean percentIsFormat) {
            this.quote = quote;
            this.percentIsFormat = percentIsFormat;
            this.braceDepth = 1;
            this.next = prev;
        }
        static void enterBrace(int quote, boolean percentIsFormat) {
            if (quote == 0) {  // exisiting string expression or non string expression
                if (quoteStack != null) {
                    ++quoteStack.braceDepth;
                    quoteStack.percentIsFormat = percentIsFormat;
                }
            } else {
                quoteStack = new BraceQuoteTracker(quoteStack, (char)quote, percentIsFormat); // push
            }
        }
        /** Return quote kind if we are reentering a quote
         * */
        static char leaveBrace() {
            if (quoteStack != null && --quoteStack.braceDepth == 0) {
                return quoteStack.quote;
            }
            return 0;
        }
        static boolean rightBraceLikeQuote(int quote) {
            return quoteStack != null && quoteStack.braceDepth == 1 && (quote == 0 || quoteStack.quote == (char)quote);
        }
        static void leaveQuote() {
            assert (quoteStack != null && quoteStack.braceDepth == 0);
            quoteStack = quoteStack.next; // pop
        }
        static boolean percentIsFormat() {
            return quoteStack != null && quoteStack.percentIsFormat;
        }
        static void resetPercentIsFormat() {
            quoteStack.percentIsFormat = false;
        }
        static boolean inBraceQuote() {
            return quoteStack != null;
        }
    }
    
    void removeQuotes() {
    	setText(getText().substring(1, getText().length()-1));
    }
    
    // quote context --
    static final int CUR_QUOTE_CTX	= 0;	// 0 = use current quote context
    static final int SNG_QUOTE_CTX	= 1;	// 1 = single quote quote context
    static final int DBL_QUOTE_CTX	= 2;	// 2 = double quote quote context
 }

/*------------------------------------------------------------------
 * LEXER RULES
 *------------------------------------------------------------------*/

STRING_LITERAL  		: '"' (~('{' |'"'))* '"'  	{ removeQuotes(); }
				| '\'' (~('{' |'\''))* '\''  	{ removeQuotes(); }
				;
// String Expression token implementation
QUOTE_LBRACE_STRING_LITERAL 	: '"' (~('{' |'"'))* '{'   	{ removeQuotes(); }
				  NextIsPercent[DBL_QUOTE_CTX] 
				| '\'' (~('{' |'\''))* '{'   	{ removeQuotes(); }
				  NextIsPercent[SNG_QUOTE_CTX] 
				;
LBRACE				: '{'				{ BraceQuoteTracker.enterBrace(0, false); } 
				;
RBRACE_QUOTE_STRING_LITERAL 	:				{ BraceQuoteTracker.rightBraceLikeQuote(DBL_QUOTE_CTX) }?=>
				  '}' (~('{' |'"'))* '"'	{ BraceQuoteTracker.leaveBrace(); 
				         			  BraceQuoteTracker.leaveQuote(); 
				         			  removeQuotes(); }
				|				{ BraceQuoteTracker.rightBraceLikeQuote(SNG_QUOTE_CTX) }?=>
				  '}' (~('{' |'\''))* '\''	{ BraceQuoteTracker.leaveBrace(); 
				         			  BraceQuoteTracker.leaveQuote(); 
				         			  removeQuotes(); }
				;
RBRACE_LBRACE_STRING_LITERAL 	:				{ BraceQuoteTracker.rightBraceLikeQuote(DBL_QUOTE_CTX) }?=>
				  '}' (~('{' |'"'))* '{'	{ BraceQuoteTracker.leaveBrace(); 
				         			  removeQuotes(); }
				   NextIsPercent[CUR_QUOTE_CTX]	
				|				{ BraceQuoteTracker.rightBraceLikeQuote(SNG_QUOTE_CTX) }?=>
				  '}' (~('{' |'\''))* '{'	{ BraceQuoteTracker.leaveBrace(); 
				         			  removeQuotes(); }
				   NextIsPercent[CUR_QUOTE_CTX]	
				;
RBRACE				:				{ !BraceQuoteTracker.rightBraceLikeQuote(CUR_QUOTE_CTX) }?=>
				  '}'				{ BraceQuoteTracker.leaveBrace(); }
				;
fragment
NextIsPercent[int quoteContext]
	 			: ((' '|'\r'|'\t'|'\u000C'|'\n')* '%')=>
	 							{ BraceQuoteTracker.enterBrace(quoteContext, true); }
				|				{ BraceQuoteTracker.enterBrace(quoteContext, false); }
				;
FORMAT_STRING_LITERAL		: 				{ BraceQuoteTracker.percentIsFormat() }?=>
				  '%' (~' ')* 			{ BraceQuoteTracker.resetPercentIsFormat(); }
				;
 
QUOTED_IDENTIFIER 
		:	'<<' (~'>'| '>' ~'>')* '>'* '>>'   	{ setText(getText().substring(2, getText().length()-2)); };
 
INTEGER_LITERAL : Digits ;

FLOATING_POINT_LITERAL
    :     d=Digits RangeDots
    	  	{
    	  		$d.setType(INTEGER_LITERAL);
    	  		emit($d);
          		$RangeDots.setType(DOTDOT);
    	  		emit($RangeDots);
    	  	}
    |	  Digits '.' (Digits)? (Exponent)? 
    | '.' Digits (Exponent)? 
    |     Digits Exponent
    ;

fragment
RangeDots 
	:	'..'
	;
fragment
Digits	:	('0'..'9')+ 
        ;
fragment
Exponent : 	('e'|'E') ('+'|'-')? Digits
        ;

IDENTIFIER 
    :   Letter (Letter|JavaIDDigit)*
    ;

fragment
Letter
    :  '\u0024' |
       '\u0041'..'\u005a' |
       '\u005f' |
       '\u0061'..'\u007a' |
       '\u00c0'..'\u00d6' |
       '\u00d8'..'\u00f6' |
       '\u00f8'..'\u00ff' |
       '\u0100'..'\u1fff' |
       '\u3040'..'\u318f' |
       '\u3300'..'\u337f' |
       '\u3400'..'\u3d2d' |
       '\u4e00'..'\u9fff' |
       '\uf900'..'\ufaff'
    ;

fragment
JavaIDDigit
    :  '\u0030'..'\u0039' |
       '\u0660'..'\u0669' |
       '\u06f0'..'\u06f9' |
       '\u0966'..'\u096f' |
       '\u09e6'..'\u09ef' |
       '\u0a66'..'\u0a6f' |
       '\u0ae6'..'\u0aef' |
       '\u0b66'..'\u0b6f' |
       '\u0be7'..'\u0bef' |
       '\u0c66'..'\u0c6f' |
       '\u0ce6'..'\u0cef' |
       '\u0d66'..'\u0d6f' |
       '\u0e50'..'\u0e59' |
       '\u0ed0'..'\u0ed9' |
       '\u1040'..'\u1049'
   ;

WS  :  (' '|'\r'|'\t'|'\u000C'|'\n') {$channel=HIDDEN;}
    ;

COMMENT
    :   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
    ;

LINE_COMMENT
    : '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
    ;

/*------------------------------------------------------------------
 * PARSER RULES
 *------------------------------------------------------------------*/

module returns [JCCompilationUnit result]
       : packageDecl? moduleItems EOF 		{ $result = F.TopLevel(noJCAnnotations(), $packageDecl.value, $moduleItems.items.toList()); };
packageDecl returns [JCExpression value]
       : PACKAGE qualident SEMI         	{ $value = $qualident.expr; };
moduleItems returns [ListBuffer<JCTree> items = new ListBuffer<JCTree>()]
       	: (moduleItem                   	{ items.append($moduleItem.value); } )*  ;
moduleItem returns [JCTree value]
       : importDecl				{ $value = $importDecl.value; }
       | classDefinition 			{ $value = $classDefinition.value; }
       | statement      			{ $value = $statement.value; } 
       | expression SEMI			{ $value = F.Exec($expression.expr); } 
       ;
importDecl returns [JCTree value]
@init { JCExpression pid = null; }
        : IMPORT  identifier			{ pid = $identifier.expr; }
                 ( DOT name			{ pid = F.at($name.pos).Select(pid, $name.value); } )* 
                 ( DOT STAR			{ pid = F.at(pos($STAR)).Select(pid, names.asterisk); } )? SEMI 
          					{ $value = F.at(pos($IMPORT)).Import(pid, false); } ;
classDefinition returns [JFXClassDeclaration value]
	: modifierFlags  CLASS name supers interfaces LBRACE classMembers RBRACE 
	  					{ $value = F.at(pos($CLASS)).ClassDeclaration($modifierFlags.mods, $name.value,
	                                	                $supers.ids.toList(), $interfaces.ids.toList(), 
	                                	                $classMembers.mems.toList()); } ;
supers returns [ListBuffer<JCExpression> ids = new ListBuffer<JCExpression>()]
	: (EXTENDS id1=qualident           	{ $ids.append($id1.expr); }
           ( COMMA idn=qualident           	{ $ids.append($idn.expr); } )* 
	  )?
	;
interfaces returns [ListBuffer<JCExpression> ids = new ListBuffer<JCExpression>()]
	: (IMPLEMENTS id1=qualident           	{ $ids.append($id1.expr); }
           ( COMMA idn=qualident         	{ $ids.append($idn.expr); } )* 
	  )?
	;
classMembers returns [ListBuffer<JCTree> mems = new ListBuffer<JCTree>()]
	: ( ad1=variableDeclaration SEMI       	{ $mems.append($ad1.value); }
	  |  fd1=functionDefinition     	{ $mems.append($fd1.value); }
	  ) *   
	  (initDefinition	     		{ $mems.append($initDefinition.value); }			
	    ( ad2=variableDeclaration SEMI	{ $mems.append($ad2.value); }
	    | fd2=functionDefinition     	{ $mems.append($fd2.value); }
	    ) *   
	  )?
	;
functionDefinition  returns [JFXOperationDefinition value]
	: modifierFlags functionLabel name 
	    formalParameters  typeReference  
	    blockExpression 			{ $value = F.at($functionLabel.pos).OperationDefinition($modifierFlags.mods,
	    						$name.value, $typeReference.type, 
	    						$formalParameters.params.toList(), $blockExpression.expr); }
	;
functionLabel    returns [int pos]
	: FUNCTION				{ $pos = pos($FUNCTION); }
	| OPERATION				{ $pos = pos($OPERATION); }
	;
initDefinition  returns [JFXInitDefinition value]
	: INIT block 				{ $value = F.at(pos($INIT)).InitDefinition($block.value); }
	;
modifierFlags returns [JCModifiers mods]
@init { long flags = 0; }
	: ( om1=otherModifier 			{ flags |= $om1.flags; }
		( am1=accessModifier  		{ flags |= $am1.flags; }  )?    
	  | am2=accessModifier 			{ flags |= $am2.flags; }
		( om2=otherModifier  		{ flags |= $om2.flags; }  )?  
	  ) ?         				{ $mods = F.Modifiers(flags); } 
	;
accessModifier returns [long flags = 0]
	: (PUBLIC          			{ flags |= Flags.PUBLIC; }
	|  PRIVATE         			{ flags |= Flags.PRIVATE; }
	|  PROTECTED       			{ flags |= Flags.PROTECTED; } ) ;
otherModifier returns [long flags = 0]
	: (ABSTRACT        			{ flags |= Flags.ABSTRACT; }
	|  READONLY        			{ flags |= Flags.FINAL; } 
	|  STATIC        			{ flags |= Flags.STATIC; } ) ;
memberSelector returns [JFXMemberSelector value]
	: name1=name   DOT   name2=name		{ $value = F.at($name1.pos).MemberSelector($name1.value, $name2.value); } 
	;
formalParameters returns [ListBuffer<JFXVar> params = new ListBuffer<JFXVar>()]
	: LPAREN  ( fp0=formalParameter		{ params.append($fp0.var); }
	          ( COMMA   fpn=formalParameter	{ params.append($fpn.var); } )* )?  RPAREN 
	;
formalParameter returns [JFXVar var]
	: name typeReference			{ $var = F.at($name.pos).Param($name.value, $typeReference.type); } 
	;
block returns [JCBlock value]
@init 		{ ListBuffer<JCStatement> stats = new ListBuffer<JCStatement>();
	 	}
	: LBRACE
	  (    statement			{ stats.append($statement.value); } 
	  |    expression SEMI			{ stats.append(F.Exec($expression.expr)); } 
	  )*
	  RBRACE				{ $value = F.at(pos($LBRACE)).Block(0L, stats.toList()); }
	;
functionExpression  returns [JFXOperationValue expr]
       : FUNCTION   formalParameters   typeReference blockExpression {
   $expr = F.at(pos($FUNCTION)).OperationValue($typeReference.type, 
                                               $formalParameters.params.toList(),
                                               $blockExpression.expr);
   };
operationExpression  returns [JFXOperationValue expr]
       : OPERATION   formalParameters   typeReference blockExpression {
   $expr = F.at(pos($OPERATION)).OperationValue($typeReference.type, 
                                                $formalParameters.params.toList(),
                                                $blockExpression.expr);
   };

blockExpression returns [JFXBlockExpression expr = null]
@init { ListBuffer<JCStatement> stats = new ListBuffer<JCStatement>(); }
	: LBRACE (statements[stats])? RBRACE	{ $expr = F.at(pos($LBRACE)).BlockExpression(0L, stats.toList(), 
						 					     $statements.expr); }
	;
statements [ListBuffer<JCStatement> stats]  returns [JCExpression expr = null]
	: statement				{ stats.append($statement.value); }
		(sts1=statements[stats]		{ $expr = $sts1.expr; } )?
	| expression
	        (SEMI  (			{ stats.append(F.Exec($expression.expr)); }
	         	 stsn=statements[stats]	{ $expr = $stsn.expr; }
		       | /*no_statements*/	{ $expr = $expression.expr; }
		      )
		 | /*no_semi*/			{ $expr = $expression.expr; }
		 )
	;
statement returns [JCStatement value]
	: variableDeclaration SEMI		{ $value = $variableDeclaration.value; }
	| functionDefinition			{ $value = $functionDefinition.value; }
	| insertStatement SEMI			{ $value = $insertStatement.value; }
	| deleteStatement SEMI			{ $value = $deleteStatement.value; }
        | WHILE LPAREN expression RPAREN block	{ $value = F.at(pos($WHILE)).WhileLoop($expression.expr, $block.value); }
	| BREAK SEMI   				{ $value = F.at(pos($BREAK)).Break(null); }
	| CONTINUE  SEMI 	 		{ $value = F.at(pos($CONTINUE)).Continue(null); }
       	| THROW expression SEMI	   		{ $value = F.at(pos($THROW)).Throw($expression.expr); } 
       	| returnStatement SEMI			{ $value = $returnStatement.value; }
       	| tryStatement				{ $value = $tryStatement.value; } 
	| SEMI					{ $value = F.at(pos($SEMI)).Skip(); } 
       	;
variableDeclaration   returns [JCStatement value]
	: modifierFlags variableLabel  name  typeReference  eqBoundExpressionOpt onChanges
	    					{ $value = F.at($variableLabel.pos).Var($name.value, 
	    							$typeReference.type, 
	    							$modifierFlags.mods,
	    							$eqBoundExpressionOpt.expr, 
	    							$eqBoundExpressionOpt.status, 
	    							$onChanges.listb.toList()); }
	;
eqBoundExpressionOpt   returns [JavafxBindStatus status, JCExpression expr]
	: EQ boundExpression	 		{ $expr = $boundExpression.expr; $status = $boundExpression.status; }
	| /*nada*/		 		{ $expr = null; $status = UNBOUND; }
	;
onChanges   returns [ListBuffer<JFXAbstractOnChange> listb = ListBuffer.<JFXAbstractOnChange>lb()]
	: ( onChangeClause			{ listb.append($onChangeClause.value); } )*
	;
onChangeClause   returns [JFXAbstractOnChange value]
	: ON REPLACE (LPAREN oldv=formalParameter RPAREN)? block
						{ $value = F.at(pos($ON)).OnReplace($oldv.var, $block.value); }
	| ON REPLACE LBRACKET index=formalParameter RBRACKET (LPAREN oldv=formalParameter RPAREN)? block
						{ $value = F.at(pos($ON)).OnReplaceElement($index.var, $oldv.var, $block.value); }
	| ON INSERT LBRACKET index=formalParameter RBRACKET (LPAREN newv=formalParameter RPAREN)? block
						{ $value = F.at(pos($ON)).OnInsertElement($index.var, $newv.var, $block.value); }
	| ON DELETE LBRACKET index=formalParameter RBRACKET (LPAREN oldv=formalParameter RPAREN)? block
						{ $value = F.at(pos($ON)).OnDeleteElement($index.var, $oldv.var, $block.value); }
	;
variableLabel    returns [int pos]
	: VAR					{ $pos = pos($VAR); }
	| LET					{ $pos = pos($LET); }
	| ATTRIBUTE				{ $pos = pos($ATTRIBUTE); }
	;
insertStatement   returns [JCStatement value]
	: INSERT e1=expression  INTO e2=expression 
						{ $value = F.at(pos($INSERT)).SequenceInsert($e2.expr, $e1.expr); } 
	;
deleteStatement   returns [JCStatement value]
	: DELETE  e1=expression  
	   ( FROM e2=expression 		{ $value = F.at(pos($DELETE)).SequenceDelete($e2.expr,$e1.expr); } 
	   | /* indexed and whole cases */	{ $value = F.at(pos($DELETE)).SequenceDelete($e1.expr); } 
	   )
	;
returnStatement   returns [JCStatement value]
@init { JCExpression expr = null; }
	: RETURN (expression 			{ expr = $expression.expr; } )?  
	   					{ $value = F.at(pos($RETURN)).Return(expr); } 
	;
tryStatement   returns [JCStatement value]
@init	{	JCBlock body;
		ListBuffer<JCCatch> catchers = new ListBuffer<JCCatch>();
		JCBlock finalBlock = null;
	}
	: TRY   tb=block 			{ body = $tb.value; }
	   ( FINALLY   fb1=block		{ finalBlock = $fb1.value; } 
	   |    (catchClause 			{ catchers.append($catchClause.value); } )+   
	        (FINALLY  fb2=block		{ finalBlock = $fb2.value; } )?   
	   ) 					{ $value = F.at(pos($TRY)).Try(body, catchers.toList(), finalBlock); }
	;
catchClause    returns [JCCatch value]
	: CATCH  LPAREN  formalParameter
	  RPAREN   block 			{ $value = F.at(pos($CATCH)).Catch($formalParameter.var, $block.value); } 
	;
boundExpression   returns [JavafxBindStatus status, JCExpression expr]
@init { boolean isLazy = false; }
	: ( BIND 				
	      (LAZY				{ isLazy = true; } )?
	      e1=expression			{ $expr = $e1.expr; }
	      (WITH INVERSE			{ $status = isLazy? JavafxBindStatus.LAZY_BIDIBIND :  JavafxBindStatus.BIDIBIND; }
	      |					{ $status = isLazy? JavafxBindStatus.LAZY_UNIDIBIND :  JavafxBindStatus.UNIDIBIND; }
	      )
	  )
	| e2=expression				{ $expr = $e2.expr; $status = UNBOUND; }
	;
expression returns [JCExpression expr] 
       	: ifExpression   					{ $expr = $ifExpression.expr; }  
       	| forExpression   					{ $expr = $forExpression.expr; }  
       	| suffixedExpression					{ $expr = $suffixedExpression.expr; }  
//     	| LPAREN  typeName  RPAREN   suffixedExpression     //FIXME: CAST
      	;
forExpression   returns [JCExpression expr] 
@init { ListBuffer<JFXForExpressionInClause> clauses = ListBuffer.lb(); }
	: FOREACH   LPAREN  
		in1=inClause					{ clauses.append($in1.value); }	       
		( COMMA in2=inClause				{ clauses.append($in2.value); } )*	       
	        RPAREN be=expression 				{ $expr = F.at(pos($FOREACH)).ForExpression(clauses.toList(), $be.expr); }
	;
inClause   returns [JFXForExpressionInClause value] 
@init { JFXVar var; }
	: formalParameter IN se=expression   (WHERE  we=expression)?
	          						{ $value = F.at(pos($IN)).InClause($formalParameter.var, $se.expr, $we.expr); }
	;
ifExpression  returns [JCExpression expr] 
	: IF econd=expression   THEN  ethen=expression   
	  (ELSE  eelse=expression)?				{ JCExpression elsepart = $eelse.expr;
	  							  if (elsepart == null) elsepart = F.at(pos($IF)).Literal(TypeTags.BOT, null);
	  							  $expr = F.at(pos($IF)).Conditional($econd.expr, $ethen.expr, elsepart); }
	;
suffixedExpression  returns [JCExpression expr] 
	: e1=assignmentExpression				{ $expr = $e1.expr; }
	   (PLUSPLUS | SUBSUB) ?   //TODO
	;
assignmentExpression  returns [JCExpression expr] 
	: e1=assignmentOpExpression				{ $expr = $e1.expr; }
	   (   EQ   e2=assignmentOpExpression			{ $expr = F.at(pos($EQ)).Assign($expr, $e2.expr); }   ) ? ;
assignmentOpExpression  returns [JCExpression expr] 
	: e1=andExpression					{ $expr = $e1.expr; }
	   (   assignmentOperator   e2=andExpression		{ $expr = F.Assignop($assignmentOperator.optag,
	   													$expr, $e2.expr); }   ) ? ;
andExpression  returns [JCExpression expr] 
	: e1=orExpression					{ $expr = $e1.expr; }
	   (   AND   e2=orExpression				{ $expr = F.at(pos($AND)).Binary(JCTree.AND, $expr, $e2.expr); }   ) * ;
orExpression  returns [JCExpression expr] 
	: e1=instanceOfExpression				{ $expr = $e1.expr; }
	   (   OR   e2=instanceOfExpression			{ $expr = F.at(pos($OR)).Binary(JCTree.OR, $expr, $e2.expr); }    ) * ;
instanceOfExpression  returns [JCExpression expr] 
	: e1=relationalExpression				{ $expr = $e1.expr; }
	   (   INSTANCEOF identifier				{ $expr = F.at(pos($INSTANCEOF)).Binary(JCTree.TYPETEST, $expr, 
	   													 $identifier.expr); }   ) ? ;
relationalExpression  returns [JCExpression expr] 
	: e1=additiveExpression					{ $expr = $e1.expr; }
	   (   LTGT   e=additiveExpression			{ $expr = F.at(pos($LTGT)).Binary(JCTree.NE, $expr, $e.expr); }
	   |   EQEQ   e=additiveExpression			{ $expr = F.at(pos($EQEQ)).Binary(JCTree.EQ, $expr, $e.expr); }
	   |   LTEQ   e=additiveExpression			{ $expr = F.at(pos($LTEQ)).Binary(JCTree.LE, $expr, $e.expr); }
	   |   GTEQ   e=additiveExpression			{ $expr = F.at(pos($GTEQ)).Binary(JCTree.GE, $expr, $e.expr); }
	   |   LT     e=additiveExpression			{ $expr = F.at(pos($LT))  .Binary(JCTree.LT, $expr, $e.expr); }
	   |   GT     e=additiveExpression			{ $expr = F.at(pos($GT))  .Binary(JCTree.GT, $expr, $e.expr); }
	   |   IN     e=additiveExpression			{ /* $expr = F.at(pos($IN  )).Binary(JavaFXTag.IN, $expr, $e2.expr); */ }
	   ) * ;
additiveExpression  returns [JCExpression expr] 
	: e1=multiplicativeExpression				{ $expr = $e1.expr; }
	   (   PLUS   e=multiplicativeExpression		{ $expr = F.at(pos($PLUS)).Binary(JCTree.PLUS , $expr, $e.expr); }
	   |   SUB    e=multiplicativeExpression		{ $expr = F.at(pos($SUB)) .Binary(JCTree.MINUS, $expr, $e.expr); }
	   ) * ;
multiplicativeExpression  returns [JCExpression expr] 
	: e1=unaryExpression					{ $expr = $e1.expr; }
	   (   STAR    e=unaryExpression			{ $expr = F.at(pos($STAR))   .Binary(JCTree.MUL  , $expr, $e.expr); }
	   |   SLASH   e=unaryExpression			{ $expr = F.at(pos($SLASH))  .Binary(JCTree.DIV  , $expr, $e.expr); }
	   |   PERCENT e=unaryExpression			{ $expr = F.at(pos($PERCENT)).Binary(JCTree.MOD  , $expr, $e.expr); }   
	   ) * ;
unaryExpression  returns [JCExpression expr] 
	: postfixExpression					{ $expr = $postfixExpression.expr; }
	| unaryOperator   postfixExpression			{ $expr = F.Unary($unaryOperator.optag, $postfixExpression.expr); }
	;
postfixExpression  returns [JCExpression expr] 
	: primaryExpression 					{ $expr = $primaryExpression.expr; }
	   ( DOT ( CLASS   					//TODO
	         | name   					{ $expr = F.at(pos($DOT)).Select($expr, $name.value); }
	            ( LPAREN expressionListOpt RPAREN   	{ $expr = F.at(pos($LPAREN)).Apply(null, $expr, $expressionListOpt.args.toList()); } ) *
	         )   
	   | LBRACKET expression  RBRACKET			{ $expr = F.at(pos($LBRACKET)).SequenceIndexed($expr, $expression.expr); }
	   ) * 
	;
primaryExpression  returns [JCExpression expr] 
	: newExpression 					{ $expr = $newExpression.expr; }
	| identifier LBRACE  objectLiteral RBRACE 		{ $expr = F.at(pos($LBRACE)).PureObjectLiteral($identifier.expr, $objectLiteral.parts.toList()); } 
       	| THIS							{ $expr = F.at(pos($THIS)).Identifier(names._this); }
       	| SUPER							{ $expr = F.at(pos($SUPER)).Identifier(names._super); }
       	| identifier 						{ $expr = $identifier.expr; }
       		( LPAREN   expressionListOpt   RPAREN   	{ $expr = F.at(pos($LPAREN)).Apply(null, $expr, $expressionListOpt.args.toList()); } )*
       	| stringExpression 					{ $expr = $stringExpression.expr; }
       	| bracketExpression 					{ $expr = $bracketExpression.expr; }
       	| literal 						{ $expr = $literal.expr; }
       	| blockExpression					{ $expr = $blockExpression.expr; }
       	| functionExpression					{ $expr = $functionExpression.expr; }
       	| operationExpression					{ $expr = $operationExpression.expr; }
       	| LPAREN expression RPAREN				{ $expr = F.at(pos($LPAREN)).Parens($expression.expr); }
       	;
newExpression  returns [JCExpression expr] 
@init { ListBuffer<JCExpression> args = null; }
	: NEW  qualident  
		( LPAREN   expressionListOpt   RPAREN 		{ args = $expressionListOpt.args; } )?
								{ $expr = F.at(pos($NEW)).Instanciate(null, null, $qualident.expr, 
												(args==null? new ListBuffer<JCExpression>() : args).toList(), null); }
		   //TODO: need anonymous subclasses
	;
objectLiteral  returns [ListBuffer<JCStatement> parts = new ListBuffer<JCStatement>()]
	: ( objectLiteralPart  					{ $parts.append($objectLiteralPart.value); } ) * 
	;
objectLiteralPart  returns [JFXStatement value]
	: name COLON  boundExpression (COMMA | SEMI)?	{ $value = F.at(pos($COLON)).ObjectLiteralPart($name.value, $boundExpression.expr, $boundExpression.status); }
       	| variableDeclaration
       	| functionDefinition 
       	;
stringExpression  returns [JCExpression expr] 
@init { ListBuffer<JCExpression> strexp = new ListBuffer<JCExpression>(); }
	: ql=QUOTE_LBRACE_STRING_LITERAL	{ strexp.append(F.at(pos($ql)).Literal(TypeTags.CLASS, $ql.text)); }
	  f1=stringFormat			{ strexp.append($f1.expr); }
	  e1=expression 			{ strexp.append($e1.expr); }
	  (  rl=RBRACE_LBRACE_STRING_LITERAL	{ strexp.append(F.at(pos($rl)).Literal(TypeTags.CLASS, $rl.text)); }
	     fn=stringFormat			{ strexp.append($fn.expr); }
	     en=expression 			{ strexp.append($en.expr); }
	  )*   
	  rq=RBRACE_QUOTE_STRING_LITERAL	{ strexp.append(F.at(pos($rq)).Literal(TypeTags.CLASS, $rq.text)); }
	  					{ $expr = F.at(pos($ql)).StringExpression(strexp.toList()); }
	;
stringFormat  returns [JCExpression expr] 
	: fs=FORMAT_STRING_LITERAL		{ $expr = F.at(pos($fs)).Literal(TypeTags.CLASS, $fs.text); }
	| /* no formar */			{ $expr = F.             Literal(TypeTags.CLASS, ""); }
	;
bracketExpression   returns [JFXAbstractSequenceCreator expr]
@init { ListBuffer<JCExpression> exps = new ListBuffer<JCExpression>(); }
	: LBRACKET   
	    ( /*nada*/				{ $expr = F.at(pos($LBRACKET)).EmptySequence(); }
	    | e1=expression 			{ exps.append($e1.expr); }
	     	(   /*nada*/			{ $expr = F.at(pos($LBRACKET)).ExplicitSequence(exps.toList()); }
	     	| COMMA e2=expression 		{ exps.append($e2.expr); }
	     	    (
//	     	      DOTDOT dds=expression	{  }
//	     	    | 			
	     	      (COMMA  en=expression	{ exps.append($en.expr); } )*
	     	    				{ $expr = F.at(pos($LBRACKET)).ExplicitSequence(exps.toList()); }
	     	    )
	     	| DOTDOT   dd=expression	{ $expr = F.at(pos($LBRACKET)).RangeSequence($e1.expr, $dd.expr); }
	     	)   
	    )
	  RBRACKET 
	;
expressionListOpt  returns [ListBuffer<JCExpression> args = new ListBuffer<JCExpression>()] 
	: ( e1=expression		{ $args.append($e1.expr); }
	    (COMMA   e=expression	{ $args.append($e.expr); }  )* )? ;
unaryOperator  returns [int optag]
	: POUND				{ $optag = 0; } //TODO
	| QUES   			{ $optag = 0; } //TODO
	| SUB   			{ $optag = JCTree.NEG; } 
	| NOT   			{ $optag = JCTree.NOT; } 
	| SIZEOF   			{ $optag = 0; } //TODO
	| TYPEOF   			{ $optag = 0; } //TODO
	| REVERSE   			{ $optag = 0; } //TODO
	| PLUSPLUS   			{ $optag = 0; } //TODO
	| SUBSUB 			{ $optag = 0; } //TODO
	;
assignmentOperator  returns [int optag]
	: PLUSEQ   			{ $optag = JCTree.PLUS_ASG; } 
	| SUBEQ   			{ $optag = JCTree.MINUS_ASG; } 
	| STAREQ   			{ $optag = JCTree.MUL_ASG; } 
	| SLASHEQ   			{ $optag = JCTree.DIV_ASG; } 
	| PERCENTEQ   			{ $optag = JCTree.MOD_ASG; } 
	;
type returns [JFXType type]
	: typeName ccn=cardinalityConstraint		{ $type = F.TypeClass($typeName.expr, $ccn.ary); }

        | FUNCTION
          { ListBuffer<JFXType> ptypes = new ListBuffer<JFXType>(); }
          LPAREN (pt0=type		{ ptypes.append($pt0.type); }
	          ( COMMA ptn=type	{ ptypes.append($ptn.type); } )* )?
          RPAREN ret=type
          { $type = F.at(pos($FUNCTION)).TypeFunctional(ptypes.toList(), $ret.type, $ccn.ary); }
        | STAR ccs=cardinalityConstraint 	{ $type = F.at(pos($STAR)).TypeAny($ccs.ary); } 
        ;
typeReference returns [JFXType type]
        : COLON type						{ $type = $type.type; }
 	| /*nada*/						{ $type = F.TypeUnknown(); }
        ;
cardinalityConstraint returns [int ary]
	:  LBRACKET   RBRACKET    	{ ary = JFXType.CARDINALITY_ANY; }
	|                         	{ ary = JFXType.CARDINALITY_SINGLETON; } 
	;
literal  returns [JCExpression expr]
	: t=STRING_LITERAL		{ $expr = F.at(pos($t)).Literal(TypeTags.CLASS, $t.text); }
	| t=INTEGER_LITERAL		{ $expr = F.at(pos($t)).Literal(TypeTags.INT, Convert.string2int($t.text, 10)); }
	| t=FLOATING_POINT_LITERAL 	{ $expr = F.at(pos($t)).Literal(TypeTags.DOUBLE, Double.valueOf($t.text)); }
	| t=TRUE   			{ $expr = F.at(pos($t)).Literal(TypeTags.BOOLEAN, 1); }
	| t=FALSE   			{ $expr = F.at(pos($t)).Literal(TypeTags.BOOLEAN, 0); }
	| t=NULL 			{ $expr = F.at(pos($t)).Literal(TypeTags.BOT, null); } 
	;
typeName  returns [JCExpression expr]
       : qualident            		{ $expr = $qualident.expr; } 
       ;
qualident returns [JCExpression expr]
       : identifier            		{ $expr = $identifier.expr; }
         ( DOT name     		{ $expr = F.at($name.pos).Select($expr, $name.value); } 
         ) *  ;
identifier  returns [JCIdent expr]
	: name              		{ $expr = F.at($name.pos).Ident($name.value); } 
	;
name returns [Name value, int pos]
	: tokid=( QUOTED_IDENTIFIER | IDENTIFIER ) { $value = Name.fromString(names, $tokid.text); $pos = pos($tokid); } 
	;
