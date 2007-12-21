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

grammar v3;

options { 
   output=AST;
   ASTLabelType=CommonTree;
   superClass = AbstractGeneratedParser; 
}
 
tokens {
   // these tokens can start a statement/definition -- can insert semi-colons before
   SEMI_INSERT_START;
   ABSTRACT='abstract';
   ASSERT='assert';
   ATTRIBUTE='attribute';
   BIND='bind';
   BREAK='break';
   CLASS='class';
   CONTINUE='continue';
   DELETE='delete';
   FALSE='false';
   FOR='for';
   FOREACH='foreach';
   FUNCTION='function';
   IF='if';
   IMPORT='import';
   INIT='init';
   INSERT='insert';
   LET='let';
   NEW='new';
   NOT='not';
   NULL='null';
   PACKAGE='package';
   PRIVATE='private';
   PROTECTED='protected';
   PUBLIC='public';
   READONLY='readonly';
   RETURN='return';
   SUPER='super';
   SIZEOF='sizeof';
   STATIC='static';
   THIS='this';
   THROW='throw';
   TRY='try';
   TRUE='true';
   VAR='var';
   WHILE='while';
   
   POUND='#';
   LPAREN='(';
   LBRACKET='[';
   PLUSPLUS='++';
   SUBSUB='--';
   PIPE='|';
   SEMI_INSERT_END;

   // cannot start a statement
   AFTER='after';
   AND='and';
   AS='as';
   BEFORE='before';
   BY='by';
   CATCH='catch';
   DO='do';
   DUR='dur';
   EASEBOTH='easeboth';
   EASEIN='easein';
   EASEOUT='easeout';
   ELSE='else';
   EXCLUSIVE='exclusive';
   EXTENDS='extends';
   FINALLY='finally';
   FIRST='first';
   FROM='from';
   IN='in';
   INDEXOF='indexof';
   INSTANCEOF='instanceof';
   INTO='into';
   INVERSE='inverse';
   LAST='last';
   LAZY='lazy';
   LINEAR='linear';
   MOTION='motion';
   ON='on';
   OR='or';
   ORDER='order';
   REPLACE='replace';
   REVERSE='reverse';
   STEP='step';
   THEN='then';
   TYPEOF='typeof';
   WITH='with';
   WHERE='where';
   
   DOTDOT='..';
   RPAREN=')';
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
   SUB='-';
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

   // these are imaginary tokens
   MODULE;
   MODIFIER;
   MEMBERSELECTOR;
   PARAM;
   FUNCTIONEXPR;
   STATEMENT;
   EXPRESSION;
   BLOCK;
   ON_REPLACE;
   ON_REPLACE_ELEMENT;
   ON_INSERT_ELEMENT;
   ON_DELETE_ELEMENT;
   POSTINCR;
   POSTDECR;
   SEQ_INDEX;
   OBJECT_LIT;
   EMPTY_FORMAT_STRING;
   TYPE_NAMED;
   TYPE_FUNCTION;
   TYPE_ANY;
   TYPE_ARG;
}

@lexer::header {
package com.sun.tools.javafx.antlr;

import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Log;
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

@members {
        public v2Parser(Context context, CharSequence content) {
           this(new CommonTokenStream(new v2Lexer(context, new ANTLRStringStream(content.toString()))));
           initialize(context);
    	}
}

@lexer::members {
    /** The log to be used for error diagnostics.
     */
    private Log log;
    
    static final byte NO_INSERT_SEMI = 0; // default
    static final byte INSERT_SEMI = 1; 
    static final byte IGNORE_FOR_SEMI = 2; 
    static final byte[] semiKind = new byte[LAST_TOKEN];
    { 
      for (int i = SEMI_INSERT_START; i < SEMI_INSERT_END; ++i) {
          semiKind[i] = INSERT_SEMI;
      }
      semiKind[RBRACE] = INSERT_SEMI;
      semiKind[STRING_LITERAL] = INSERT_SEMI;
      semiKind[QUOTE_LBRACE_STRING_LITERAL] = INSERT_SEMI;
      semiKind[QUOTED_IDENTIFIER] = INSERT_SEMI;
      semiKind[DECIMAL_LITERAL] = INSERT_SEMI;
      semiKind[OCTAL_LITERAL] = INSERT_SEMI;
      semiKind[HEX_LITERAL] = INSERT_SEMI;
      semiKind[FLOATING_POINT_LITERAL] = INSERT_SEMI;
      semiKind[IDENTIFIER] = INSERT_SEMI;
      
      semiKind[WS] = IGNORE_FOR_SEMI;
      semiKind[COMMENT] = IGNORE_FOR_SEMI;
      semiKind[LINE_COMMENT] = IGNORE_FOR_SEMI;
    }
      
    int previousTokenType = -1;
    final Token syntheticSemi = new CommonToken(SEMI);
    List tokens = new ArrayList();
    
    public v2Lexer(Context context, CharStream input) {
    	this(input);
        this.log = Log.instance(context);
    }
    
    /** Allow emitting more than one token from a lexer rule
     */
    public void emit(Token token) {
        int ttype = token.getType();
        //System.err.println("::: " + ttype + " --- " + token.getText());
        if (previousTokenType == RBRACE && (ttype == EOF || semiKind[ttype] == INSERT_SEMI)) {
            this.token = syntheticSemi;
            tokens.add(syntheticSemi);
            //System.err.println("INSERTING in front of: " + ttype);
        } else {
            this.token = token;
        }
    	tokens.add(token);

        if (ttype != EOF && semiKind[ttype] != IGNORE_FOR_SEMI) {
            previousTokenType = ttype;
        }
    }
    public Token nextToken() {
        if ( tokens.size() > 0 ) {
            return (Token)tokens.remove(0);
        }
    	super.nextToken();
        if ( tokens.size()==0 ) {
            emit(Token.EOF_TOKEN);
        }
        Token cur = (Token)tokens.remove(0);
        return cur;
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
    
    void processString() {
    	setText(StringLiteralProcessor.convert( log, getCharIndex(), getText() ));
    }

    // quote context --
    static final int CUR_QUOTE_CTX	= 0;	// 0 = use current quote context
    static final int SNG_QUOTE_CTX	= 1;	// 1 = single quote quote context
    static final int DBL_QUOTE_CTX	= 2;	// 2 = double quote quote context
 }

/*------------------------------------------------------------------
 * LEXER RULES
 *------------------------------------------------------------------*/

STRING_LITERAL  		: '"' DoubleQuoteBody '"'  	{ processString(); }
				| '\'' SingleQuoteBody '\''  	{ processString(); }
				;
// String Expression token implementation
QUOTE_LBRACE_STRING_LITERAL 	: '"' DoubleQuoteBody '{'   	{ processString(); }
				  NextIsPercent[DBL_QUOTE_CTX] 
				| '\'' SingleQuoteBody '{'   	{ processString(); }
				  NextIsPercent[SNG_QUOTE_CTX] 
				;
LBRACE				: '{'				{ BraceQuoteTracker.enterBrace(0, false); } 
				;
RBRACE_QUOTE_STRING_LITERAL 	:				{ BraceQuoteTracker.rightBraceLikeQuote(DBL_QUOTE_CTX) }?=>
				  '}' DoubleQuoteBody '"'	{ BraceQuoteTracker.leaveBrace(); 
				         			  BraceQuoteTracker.leaveQuote(); 
				         			  processString(); }
				|				{ BraceQuoteTracker.rightBraceLikeQuote(SNG_QUOTE_CTX) }?=>
				  '}' SingleQuoteBody '\''	{ BraceQuoteTracker.leaveBrace(); 
				         			  BraceQuoteTracker.leaveQuote(); 
				         			  processString(); }
				;
RBRACE_LBRACE_STRING_LITERAL 	:				{ BraceQuoteTracker.rightBraceLikeQuote(DBL_QUOTE_CTX) }?=>
				  '}' DoubleQuoteBody '{'	{ BraceQuoteTracker.leaveBrace(); 
				         			  processString(); }
				   NextIsPercent[CUR_QUOTE_CTX]	
				|				{ BraceQuoteTracker.rightBraceLikeQuote(SNG_QUOTE_CTX) }?=>
				  '}' SingleQuoteBody '{'	{ BraceQuoteTracker.leaveBrace(); 
				         			  processString(); }
				   NextIsPercent[CUR_QUOTE_CTX]	
				;
RBRACE				:				{ !BraceQuoteTracker.rightBraceLikeQuote(CUR_QUOTE_CTX) }?=>
				  '}'				{ BraceQuoteTracker.leaveBrace(); }
				;
fragment
DoubleQuoteBody  :	 (~('{' |'"'|'\\')|'\\' .)*  ;

fragment
SingleQuoteBody  :	 (~('{' |'\''|'\\')|'\\' .)*  ;

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
 
DECIMAL_LITERAL : ('0' | '1'..'9' '0'..'9'*) ;

OCTAL_LITERAL : '0' ('0'..'7')+ ;

HEX_LITERAL : '0' ('x'|'X') HexDigit+    			{ setText(getText().substring(2, getText().length())); };

fragment
HexDigit : ('0'..'9'|'a'..'f'|'A'..'F') ;

FLOATING_POINT_LITERAL
    :     d=DECIMAL_LITERAL RangeDots
    	  	{
    	  		$d.setType(DECIMAL_LITERAL);
    	  		emit($d);
          		$RangeDots.setType(DOTDOT);
    	  		emit($RangeDots);
    	  	}
    |     d=OCTAL_LITERAL RangeDots
    	  	{
    	  		$d.setType(OCTAL_LITERAL);
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

LAST_TOKEN
    :	'~~~~~~~~' {false}? '~~~~~~~~'
    ;
    	
/*------------------------------------------------------------------
 * PARSER RULES
 *------------------------------------------------------------------*/

module
	: packageDecl? moduleItems EOF 		-> ^(MODULE packageDecl? moduleItems)
       	;
packageDecl 
       	: PACKAGE qualident SEMI         	-> qualident
	;
moduleItems    
	: moduleItem? (SEMI moduleItem? )*	-> moduleItem*
	;
moduleItem 
	: importDecl 				-> importDecl
	| classDefinition 			-> classDefinition
	| statement      			-> statement
	| expression 				-> expression
	;
importDecl
 	: IMPORT importId			-> ^(IMPORT importId)
	;
importId
 	: ( identifier				-> identifier)
                 ( DOT name			-> ^(DOT $importId name) )* 
                 ( DOT STAR			-> ^(DOT $importId STAR) )?  
	;
classDefinition 
	: classModifierFlags  CLASS name supers LBRACE classMembers RBRACE
	  					-> ^(CLASS classModifierFlags name supers classMembers)
	;
supers 
	: (EXTENDS typeName
           ( COMMA typeName )* 
	  )?
	  					-> ^(EXTENDS typeName+)
	;
classMembers 
	: classMember ?
	  (SEMI
	     classMember ?
	  ) *					-> classMember*
	;
classMember
	: initDefinition	
	| variableDeclaration 
	| functionDefinition 
	;
functionDefinition
	: functionModifierFlags FUNCTION name formalParameters typeReference  
	    					-> ^(FUNCTION name functionModifierFlags formalParameters typeReference)
	;
initDefinition
	: INIT block 				-> ^(INIT block)
	;
functionModifierFlags
	: accessModifier functionModifier?	-> ^(MODIFIER accessModifier  functionModifier?)
	| functionModifier accessModifier?	-> ^(MODIFIER accessModifier? functionModifier?)
	| 					-> ^(MODIFIER)
	;
varModifierFlags
	: accessModifier varModifier?		-> ^(MODIFIER accessModifier  varModifier?)
	| varModifier accessModifier?		-> ^(MODIFIER accessModifier? varModifier?)
	| 					-> ^(MODIFIER)
	;
classModifierFlags
	: accessModifier classModifier?		-> ^(MODIFIER accessModifier  classModifier?)
	| classModifier accessModifier?		-> ^(MODIFIER accessModifier? classModifier?)
	| 					-> ^(MODIFIER)
	;
accessModifier 
	:  PUBLIC          			
	|  PRIVATE         			
	|  PROTECTED       			
	;
functionModifier 
	:  ABSTRACT        			
	|  STATIC        			
	;
varModifier 
	:  READONLY        			
	|  STATIC        			
	;
classModifier 
	:  ABSTRACT        			
	;
memberSelector
	: name DOT name				-> ^(MEMBERSELECTOR name+)
	;
formalParameters
	: LPAREN ( formalParameter (COMMA formalParameter)* )?  RPAREN
						-> ^(LPAREN formalParameter*)
	;
formalParameter
	: name typeReference			-> ^(PARAM name typeReference)
	;
block
	: LBRACE blockComponent?
	   (SEMI blockComponent?) *
	  RBRACE				-> ^(BLOCK blockComponent*)
	;
blockExpression 
	: LBRACE blockComponent?
	   (SEMI blockComponent?) *
	  RBRACE				-> ^(LBRACE blockComponent*)
	;
blockComponent
	: statement				-> ^(STATEMENT statement)
	| expression				-> ^(EXPRESSION expression)
	;
statement 
	: variableDeclaration	
	| functionDefinition 
	| insertStatement 	
	| deleteStatement 
 	| whileStatement
	| BREAK    		
	| CONTINUE  	 	 	
       	| THROW expression 	   	
       	| returnStatement 		
       	| tryStatement			
       	;
variableDeclaration   
	: varModifierFlags variableLabel  name  typeReference (EQ boundExpression)? onChangeClause*
	    					-> ^(VAR variableLabel varModifierFlags name typeReference boundExpression? onChangeClause*)
	;
onChangeClause  
	: ON REPLACE (LPAREN oldv=formalParameter RPAREN)? block
						-> ^(ON_REPLACE $oldv block)
	| ON REPLACE LBRACKET index=formalParameter RBRACKET (LPAREN oldv=formalParameter RPAREN)? block
						-> ^(ON_REPLACE_ELEMENT $index $oldv? block)
	| ON INSERT LBRACKET index=formalParameter RBRACKET (LPAREN newv=formalParameter RPAREN)? block
						-> ^(ON_INSERT_ELEMENT $index $newv? block)
	| ON DELETE LBRACKET index=formalParameter RBRACKET (LPAREN oldv=formalParameter RPAREN)? block
						-> ^(ON_DELETE_ELEMENT $index $oldv? block)
	;
variableLabel 
	: VAR	
	| LET	
	| ATTRIBUTE	
	;
whileStatement
	: WHILE LPAREN expression RPAREN block	-> ^(WHILE expression block)
	;
insertStatement  
	: INSERT e1=expression INTO e2=expression 
						-> ^(INSERT $e1 $e2)
	;
deleteStatement  
	: DELETE  e1=expression  
	   ( FROM e2=expression 		-> ^(FROM $e1 $e2)
	   | /* indexed and whole cases */	-> ^(DELETE $e2)
	   )
	;
returnStatement
	: RETURN expression?			-> ^(RETURN expression?)
	;
tryStatement
	: TRY block 			
	   ( finallyClause
	   | catchClause+ finallyClause?   
	   ) 					-> ^(TRY block catchClause+ finallyClause?)
	;
finallyClause
	: FINALLY block				-> ^(FINALLY block)
	;
catchClause
	: CATCH LPAREN formalParameter RPAREN block
						-> ^(CATCH formalParameter block)
	;
boundExpression 
	: BIND LAZY? expression (WITH INVERSE)?
						-> ^(BIND LAZY? INVERSE? expression)
	| expression				-> ^(EXPRESSION expression)
	;
expression 
       	: blockExpression
       	| ifExpression   		
       	| forExpression   	
       	| newExpression 	
	| assignmentExpression	 
      	;
forExpression
	: FOR LPAREN inClause (COMMA inClause)* RPAREN expression
								-> ^(FOR inClause* expression)
	;
inClause
	: formalParameter IN expression (WHERE expression)?	-> ^(IN formalParameter IN expression expression?)
	;
ifExpression 
	: IF LPAREN econd=expression  RPAREN 
		THEN?  ethen=expression  elseClause 		-> ^(IF $econd $ethen elseClause)
	;
elseClause
	: (ELSE)=>  ELSE  expression				-> expression
	| /*nada*/ 						->
	;
assignmentExpression  
	: assignmentOpExpression 
		( (EQ)=>   EQ  expression			-> ^(EQ assignmentOpExpression expression)
		|						-> assignmentOpExpression
		)
	;
assignmentOpExpression 
	: e1=andExpression					
	   (   PLUSEQ   e2=expression				-> ^(PLUSEQ $e1 $e2) 
	   |   SUBEQ   e2=expression				-> ^(SUBEQ $e1 $e2) 
	   |   STAREQ   e2=expression				-> ^(STAREQ $e1 $e2) 
	   |   SLASHEQ   e2=expression				-> ^(SLASHEQ $e1 $e2) 
	   |   PERCENTEQ   e2=expression			-> ^(PERCENTEQ $e1 $e2) 
	   |							-> $e1
	   )
	;
assignmentOperator 
	: PLUSEQ   
	|    		
	|    
	|    
	| PERCENTEQ   			
	;
andExpression  
	: e1=orExpression					
	   (   AND   e2=orExpression				-> ^(AND $e1 $e2) 
	   |							-> $e1
	   )
	;
orExpression  
	: e1=typeExpression				
	   (   OR   e2=typeExpression				-> ^(OR $e1 $e2) 
	   |							-> $e1
	   )
	;
typeExpression 
	: relationalExpression		
	   (   INSTANCEOF itn=typeName				-> ^(INSTANCEOF relationalExpression $itn)
	   |   AS atn=typeName					-> ^(AS relationalExpression $atn)
	   | 							-> relationalExpression
	   )
	;
relationalExpression  
	: additiveExpression					-> additiveExpression
	   (   LTGT   e=additiveExpression			-> ^(LTGT $relationalExpression $e)
	   |   EQEQ   e=additiveExpression			-> ^(EQEQ $relationalExpression $e)
	   |   LTEQ   e=additiveExpression			-> ^(LTEQ $relationalExpression $e)
	   |   GTEQ   e=additiveExpression			-> ^(GTEQ $relationalExpression $e)
	   |   LT     e=additiveExpression			-> ^(LT   $relationalExpression $e)
	   |   GT     e=additiveExpression			-> ^(GT   $relationalExpression $e)
	   ) * ;
additiveExpression 
	: multiplicativeExpression				-> multiplicativeExpression
	   (   PLUS   e=multiplicativeExpression		-> ^(PLUS $additiveExpression $e)
	   |   SUB    e=multiplicativeExpression		-> ^(SUB  $additiveExpression $e)
	   ) * ;
multiplicativeExpression
	: ( unaryExpression					-> unaryExpression )
	   (   STAR    e=unaryExpression			-> ^(STAR    $multiplicativeExpression $e)
	   |   SLASH   e=unaryExpression			-> ^(SLASH   $multiplicativeExpression $e)
	   |   PERCENT e=unaryExpression			-> ^(PERCENT $multiplicativeExpression $e)
	   ) * 
	;
//TODO: POUND QUES TYPEOF REVERSE
unaryExpression 
	: suffixedExpression					-> suffixedExpression
	| SUB      e=unaryExpression				-> ^(SUB $e)
	| NOT      e=unaryExpression				-> ^(NOT $e)		
	| SIZEOF   e=unaryExpression				-> ^(SIZEOF $e)	
	| PLUSPLUS e=unaryExpression				-> ^(PLUSPLUS $e)   	
	| SUBSUB   e=unaryExpression				-> ^(SUBSUB $e) 
	;
suffixedExpression 
	: postfixExpression
		( PLUSPLUS					-> ^(POSTINCR postfixExpression)
		| SUBSUB					-> ^(POSTDECR postfixExpression)
		|						-> postfixExpression
		)
	;
postfixExpression 
	: primaryExpression 					-> primaryExpression
	   ( DOT ( name						-> ^(DOT $postfixExpression name)
//TODO:		 | CLASS   					
	         )   
	   | LPAREN expressionListOpt RPAREN   			-> ^(LPAREN $postfixExpression expressionListOpt)
	   | LBRACKET expression RBRACKET			-> ^(SEQ_INDEX $postfixExpression expression)
	   ) * 
	;
primaryExpression  
	: qualident					
		( LBRACE  objectLiteralPart* RBRACE 		-> ^(OBJECT_LIT qualident objectLiteralPart*)
		|						-> qualident
		)
       	| THIS							-> THIS
       	| SUPER							-> SUPER
       	| stringExpression 					-> stringExpression
       	| bracketExpression 					-> bracketExpression
       	| literal 						-> literal
      	| functionExpression					-> functionExpression
       	| LPAREN expression RPAREN				-> expression
       	;
functionExpression  
	: FUNCTION formalParameters typeReference blockExpression
								-> ^(FUNCTIONEXPR formalParameters typeReference blockExpression)
	;
newExpression 
	: NEW  typeName  
		( (LPAREN)=>LPAREN expressionListOpt  RPAREN
		)?
								-> ^(NEW typeName expressionListOpt?)
	;
objectLiteralPart  
	: name COLON  boundExpression (COMMA | SEMI)?		-> ^(COLON name boundExpression)
       	| variableDeclaration	(COMMA | SEMI)?			-> variableDeclaration
       	| functionDefinition 	(COMMA | SEMI)?			-> functionDefinition
       	;
stringExpression  
	: QUOTE_LBRACE_STRING_LITERAL
	  stringFormat	
	  expression 	
	  stringExpressionInner*   
	  RBRACE_QUOTE_STRING_LITERAL				-> ^(QUOTE_LBRACE_STRING_LITERAL 
	  								stringFormat expression 
	  								stringExpressionInner* 
	  								RBRACE_QUOTE_STRING_LITERAL)
	;
stringExpressionInner
	: RBRACE_LBRACE_STRING_LITERAL stringFormat expression 			
	;
stringFormat  
	: FORMAT_STRING_LITERAL			-> FORMAT_STRING_LITERAL
	| /* no formar */			-> EMPTY_FORMAT_STRING
	;
bracketExpression  
	: LBRACKET   
	    ( /*nada*/				-> ^(LBRACKET)
	    | e1=expression 	
	     	(   /*nada*/			-> ^(LBRACKET expression*)
	     	| COMMA 
	     	   (   /*nada*/			-> ^(LBRACKET expression*)
            	| e2=expression 		
	     	         (COMMA expression)*
                          COMMA?
	     	       				-> ^(LBRACKET expression*)
                    )
	     	| DOTDOT   dd=expression	
	     	    ( STEP st=expression )?
	     	    EXCLUSIVE?
	     					-> ^(DOTDOT $e1 $dd $st EXCLUSIVE)
	     	)   
	    )
	  RBRACKET 
	;
expressionListOpt  
	: (expression (COMMA expression)*)?	-> expression*
	;
type 
	: typeName cardinality			-> ^(TYPE_NAMED typeName cardinality)
 	| FUNCTION LPAREN typeArgList
          	   RPAREN typeReference 
          	   	cardinality	//TODO: this introduces an ambiguity: return cardinality vs type cardinality
          	   				-> ^(TYPE_FUNCTION typeArgList typeReference cardinality)
 	| STAR cardinality			-> ^(TYPE_ANY cardinality)
 	;
typeArgList
 	: (typeArg (COMMA typeArg)* )?		-> typeArg*
	;
typeArg 
 	: ( COLON type		
 	  | name COLON type		
 	  | name			
 	  )					-> ^(COLON name? type?)
 	;
typeReference 
 	: COLON type				-> type
 	| /*nada*/				->
 	;
cardinality 
	: (LBRACKET)=>LBRACKET RBRACKET 	-> RBRACKET
	|                         		->
	;
literal  
	: STRING_LITERAL	
	| DECIMAL_LITERAL	
	| OCTAL_LITERAL		
	| HEX_LITERAL			
	| FLOATING_POINT_LITERAL 
	| TRUE   		
	| FALSE   		
	| NULL 		
	;
	
typeName  
	: qualident 		
		(LT typeArgument (COMMA typeArgument)* GT
						-> ^(TYPE_ARG qualident typeArgument+)
		|				-> qualident
		)
	;
typeArgument 
	: typeName				-> typeName
	| QUES (  ( EXTENDS 		
		  | SUPER		
		  ) 
		 typeName		
	       )?				-> ^(QUES EXTENDS? SUPER? typeName?)
	;
	
qualident returns [JCExpression expr]
	: n0=plainName            	{ $expr = F.at($n0.pos).Ident($n0.value); } 
         ( (DOT)=> DOT nn=plainName     { $expr = F.at($nn.pos).Select($expr, $nn.value); } 
         ) *  
	| frenchName			{ $expr = F.at($frenchName.pos).Identifier($frenchName.value); }
         ( (DOT)=> DOT nn=plainName     { $expr = F.at($nn.pos).Select($expr, $nn.value); } 
         ) *  
	;
identifier  returns [JCIdent expr]
	: name              		{ $expr = F.at($name.pos).Ident($name.value); } 
	;
name returns [Name value, int pos]
	: plainName			{ $value = $plainName.value; $pos = $plainName.pos; }
	| frenchName			{ $value = $frenchName.value; $pos = $frenchName.pos; }
	;
plainName returns [Name value, int pos]
	: IDENTIFIER			{ $value = Name.fromString(names, $IDENTIFIER.text); $pos = pos($IDENTIFIER); } 
	;
frenchName returns [Name value, int pos]
	: QUOTED_IDENTIFIER		{ $value = Name.fromString(names, $QUOTED_IDENTIFIER.text); $pos = pos($QUOTED_IDENTIFIER); } 
	;
