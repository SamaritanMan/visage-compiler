/*
 * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file tree accompanied this code.
 *
 * This code is distributed in the hope tree it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file tree
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

import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javafx.code.JavafxSymtab;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.MethodType;
import com.sun.tools.javac.jvm.ClassReader;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Log;

import com.sun.javafx.api.JavafxBindStatus;
import com.sun.tools.javafx.code.JavafxVarSymbol;
import com.sun.tools.javafx.tree.*;
import static com.sun.tools.javafx.code.JavafxVarSymbol.*;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javafx.comp.JavafxToJava.JCExpressionTupple;
import static com.sun.tools.javafx.comp.JavafxDefs.*;

import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Robert Field
 */
public class JavafxTypeMorpher {
    protected static final Context.Key<JavafxTypeMorpher> typeMorpherKey =
            new Context.Key<JavafxTypeMorpher>();
    
    private final Name.Table names;
    final ClassReader reader;
    private final TreeMaker make;
    private final JavafxSymtab syms;
    private final Log log;
    private final JavafxToJava toJava;
    private final Types types;
    private final JavafxInitializationBuilder initBuilder;
    
    public static final String locationPackageName = "com.sun.javafx.runtime.location.";
    
    public LocationNameSymType[] varLocation;
    public LocationNameSymType[] exprLocation;
    public LocationNameSymType[] declLocation;
    public LocationNameSymType[] bindingExpression;
    public LocationNameSymType[] holder;
    public LocationNameSymType   baseLocation;
    
    private final Type[] realTypeByKind;
    private final Object[] defaultValueByKind;
    
    final Name getMethodName;
    final Name setMethodName;
    private final Name makeMethodName;
    private final Name makeLazyMethodName;
    
    public class LocationNameSymType {
        public final Name name;
        public final ClassSymbol sym;
        public final Type type;
        LocationNameSymType(String which) {
            name = Name.fromString(names, locationPackageName + which);
            sym = reader.enterClass(name);
            type = sym.type;      
        }
    }
    
    private Map<Symbol, VarMorphInfo> vmiMap = new HashMap<Symbol, VarMorphInfo>();
    
    class VarMorphInfo extends TypeMorphInfo {
        private final Symbol sym;
        private final boolean isMethod;
        private boolean mustMorph = false;
        private boolean haveDeterminedMorphability = false;
        private boolean isBoundTo = false;
        private boolean isAssignedTo = false;
        
        VarMorphInfo(Symbol sym) {
            super((sym.kind == Kinds.MTH)? ((MethodType)sym.type).getReturnType() : sym.type);
            this.sym = sym;
            this.isMethod = (sym.kind == Kinds.MTH);
        }
        
        private boolean isAttribute() {
            if (isMethod) {
                return false;
            }
            Symbol owner = getSymbol().owner;
            if (owner.kind == Kinds.MTH) {
                return false; // local var
            } else if (owner.kind == Kinds.TYP) {
                if (getSymbol() instanceof JavafxVarSymbol) {
                    return true; // we made it, soassume it is from a JavaFX class
                } else if (!sym.toString().equals("super") && !sym.toString().equals("this")) {
                    //TODO: temp hack until the MI init code is in place
                    ClassSymbol klass = (ClassSymbol)owner;
		    if (klass.sourcefile != null) {
			String source = klass.sourcefile.getName();
			String extension = source.substring(source.length()-3);
			return extension.equals(".fx");
		    }
                }
            }
            // what is this?
            return false;
        }

        public boolean mustMorph() {
            if (!haveDeterminedMorphability()) {
                if (((getSymbol().flags() & Flags.PARAMETER) == 0) && (isBoundTo() || isAttribute() || isSequence())) {
                    // Must be a Location if it is bound, or a sequence,
                    // and, at least for now if it is an attribute.
                    // However, if this is a parameter it is either synthetic
                    // (like a loop or formal var) so we don't want to morph
                    // or it is a function param.  If we do bound param we
                    // will need to change this.
                    //TODO: should be a Location if there is a trigger on it
                    //System.err.println("Will morph: " + varSymbol);
                    markMustMorph();
                }
                markDeterminedMorphability();
            }
            return mustMorph;
        }
        
        private void markMustMorph() { mustMorph = true; }
        private void markDeterminedMorphability() { haveDeterminedMorphability = true; }
        private boolean haveDeterminedMorphability() { return haveDeterminedMorphability; }
        public boolean isBoundTo() { return isBoundTo; }
        public boolean isAssignedTo() { return isAssignedTo; }
        public void markBoundTo() { this.isBoundTo = true; haveDeterminedMorphability = false; }
        public void markAssignedTo() { this.isAssignedTo = true; }

        public Symbol getSymbol() {
            return sym;
        }
    }
    
    class TypeMorphInfo {
        final Type realType;
        final Type morphedType;
        int typeKind;
        Type elementType = null;
        
        TypeMorphInfo(Type symType) {
            TypeSymbol realTsym = symType.tsym;
            //check if symbol is already a Location, needed for source class
            Type locationType = null;
            if (realTsym == declLocation[TYPE_KIND_OBJECT].sym) {
                locationType = ((ClassType) symType).typarams_field.head;
                typeKind = TYPE_KIND_OBJECT;
            } else if (realTsym == declLocation[TYPE_KIND_SEQUENCE].sym) {
                locationType = ((ClassType) symType).typarams_field.head;
                typeKind = TYPE_KIND_SEQUENCE;
            } else if (realTsym == declLocation[TYPE_KIND_BOOLEAN].sym) {
                locationType = syms.booleanType;
                typeKind = TYPE_KIND_BOOLEAN;
            } else if (realTsym == declLocation[TYPE_KIND_DOUBLE].sym) {
                locationType = syms.doubleType;
                typeKind = TYPE_KIND_DOUBLE;
            } else if (realTsym == declLocation[TYPE_KIND_INT].sym) {
                locationType = syms.intType;
                typeKind = TYPE_KIND_INT;
            }
            if (locationType != null) {
                // External module with a Location type
                this.morphedType = symType;
                this.realType = locationType;
            } else {
                this.realType = symType;

                if (symType.isPrimitive()) {
                    if (realTsym == syms.doubleType.tsym) {
                        typeKind = TYPE_KIND_DOUBLE;
                    } else if (realTsym == syms.intType.tsym) {
                        typeKind = TYPE_KIND_INT;
                    } else if (realTsym == syms.booleanType.tsym) {
                        typeKind = TYPE_KIND_BOOLEAN;
                    } else {
                        assert false : "should not reach here";
                        typeKind = TYPE_KIND_OBJECT;
                    }
                } else {
                    if (isSequence()) {
                        typeKind = TYPE_KIND_SEQUENCE;
                        elementType = symType.getTypeArguments().head;
                    } else {
                        typeKind = TYPE_KIND_OBJECT;
                    }
                }

                // must be called AFTER typeKind and realType are set in vsym
                this.morphedType = symType == syms.voidType? symType : generifyIfNeeded(declLocationType(typeKind), this);
            }
        }
        
        protected boolean isSequence() {
            return types.erasure(realType) == syms.javafx_SequenceTypeErasure;
        }

        public Type getRealType() { return realType; }
        public Type getMorphedType() { return morphedType; }
        public Type getBindingExpressionType() { return generifyIfNeeded(bindingExpressionType(typeKind), this); }
        public Type getHolderType() { return generifyIfNeeded(holderType(typeKind), this); }
        public Object getDefaultValue() { return defaultValueByKind[typeKind]; }
        public Type getElementType() { return elementType; }

        public int getTypeKind() { return typeKind; }
    }
    
    VarMorphInfo varMorphInfo(Symbol sym) {
        VarMorphInfo vmi = vmiMap.get(sym);
        if (vmi == null) {
            vmi = new VarMorphInfo(sym);
            vmiMap.put(sym, vmi);
        }
        return vmi;
    }
    
    TypeMorphInfo typeMorphInfo(Type type) {
        return new TypeMorphInfo(type);
    }
    
    public static JavafxTypeMorpher instance(Context context) {
        JavafxTypeMorpher instance = context.get(typeMorpherKey);
        if (instance == null)
            instance = new JavafxTypeMorpher(context);
        return instance;
    }
    
    protected JavafxTypeMorpher(Context context) {
        context.put(typeMorpherKey, this);
        
        syms = (JavafxSymtab)(JavafxSymtab.instance(context));
        types = Types.instance(context);
        names = Name.Table.instance(context);
        reader = ClassReader.instance(context);
        make = (JavafxTreeMaker)JavafxTreeMaker.instance(context);
        log = Log.instance(context);
        toJava = JavafxToJava.instance(context);
        initBuilder = JavafxInitializationBuilder.instance(context);

        String[] locClass = new String[TYPE_KIND_COUNT];
        locClass[TYPE_KIND_OBJECT] = "Object";
        locClass[TYPE_KIND_DOUBLE] = "Double";
        locClass[TYPE_KIND_BOOLEAN] = "Boolean";
        locClass[TYPE_KIND_INT] = "Int";
        locClass[TYPE_KIND_SEQUENCE] = "Sequence";

        varLocation = new LocationNameSymType[TYPE_KIND_COUNT];
        exprLocation = new LocationNameSymType[TYPE_KIND_COUNT];
        declLocation = new LocationNameSymType[TYPE_KIND_COUNT];
        bindingExpression = new LocationNameSymType[TYPE_KIND_COUNT];
        holder = new LocationNameSymType[TYPE_KIND_COUNT];

        for (int kind = 0; kind < TYPE_KIND_COUNT; ++kind) {
            varLocation[kind]  = new LocationNameSymType(locClass[kind] + "Var");
            exprLocation[kind] = new LocationNameSymType(locClass[kind] + "Expression");
            declLocation[kind] = new LocationNameSymType(locClass[kind] + "Location");
            bindingExpression[kind] = new LocationNameSymType(locClass[kind] + "BindingExpression");
            holder[kind] = new LocationNameSymType(locClass[kind] + "LocationHolder");
        }
        
        baseLocation = new LocationNameSymType("Location");
                
        realTypeByKind = new Type[TYPE_KIND_COUNT];
        realTypeByKind[TYPE_KIND_OBJECT] = syms.objectType;
        realTypeByKind[TYPE_KIND_DOUBLE] = syms.doubleType;
        realTypeByKind[TYPE_KIND_BOOLEAN] = syms.booleanType;
        realTypeByKind[TYPE_KIND_INT] = syms.intType;
        realTypeByKind[TYPE_KIND_SEQUENCE] = syms.javafx_SequenceType;
        
        defaultValueByKind = new Object[TYPE_KIND_COUNT];
        defaultValueByKind[TYPE_KIND_OBJECT] = null;
        defaultValueByKind[TYPE_KIND_DOUBLE] = new Double(0.0);
        defaultValueByKind[TYPE_KIND_BOOLEAN] = new Integer(0);
        defaultValueByKind[TYPE_KIND_INT] = new Integer(0);
        defaultValueByKind[TYPE_KIND_SEQUENCE] = null; //TODO: empty sequence
        
        getMethodName = Name.fromString(names, "get");
        setMethodName = Name.fromString(names, "set");
        makeMethodName = Name.fromString(names, "make");
        makeLazyMethodName = Name.fromString(names, "makeLazy");
    }
       
    Type declLocationType(int typeKind) {
        return declLocation[typeKind].type;
    }
    
    Type varLocationType(int typeKind) {
        return varLocation[typeKind].type;
    }
    
    Type expressionLocationType(int typeKind) {
        return exprLocation[typeKind].type;
    }

    Type bindingExpressionType(int typeKind) {
        return bindingExpression[typeKind].type;
    }
    
    Type holderType(int typeKind) {
        return holder[typeKind].type;
    }
    
    JCExpression sharedLocationId(DiagnosticPosition diagPos, int typeKind, LocationNameSymType[] lnsta) {
        LocationNameSymType lnst = lnsta[typeKind];
        return ((JavafxTreeMaker)make).at(diagPos).Identifier(lnst.name);
    }
    
    JCExpression castToReal(Type realType, JCExpression expr) {
        // cast the expression so that boxing works
        JCExpression typeExpr = make.Type(realType);
        JCExpression result = make.TypeCast(typeExpr, expr);     
        return result;
    }
    
    private Type generifyIfNeeded(Type aLocationType, TypeMorphInfo tmi) {
        Type newType;
        if (tmi.getTypeKind() == TYPE_KIND_OBJECT || 
                tmi.getTypeKind() == TYPE_KIND_SEQUENCE) {
            List<Type> actuals = tmi.getTypeKind() == TYPE_KIND_SEQUENCE?
                  tmi.getRealType().getTypeArguments()
                : List.of(tmi.getRealType());
            Type clazzOuter = declLocationType(tmi.getTypeKind()).getEnclosingType();

            List<Type> newActuals = List.<Type>nil();
            actualsLabel: for (Type t : actuals) {
                if ((t.tsym instanceof ClassSymbol) &&
                        initBuilder.isJFXClass((ClassSymbol)t.tsym)) {
                    String str = t.tsym.flatName().toString().replace("$", ".");
                    String strLookFor = str + interfaceSuffix;
                    Type tp = reader.enterClass(names.fromString(strLookFor)).type;
                    if (tp != null) {
                        tp.tsym.completer = null;
                        newActuals = newActuals.append(tp);
                        break actualsLabel;
                    }
                }
                
                newActuals = newActuals.append(t);
            }
 
            newType = new ClassType(clazzOuter, newActuals, aLocationType.tsym);
        } else {
            newType = aLocationType;
        }
        return newType;
    }
        
    public JCExpression convertVariableReference(DiagnosticPosition diagPos,
                                                                                            JCExpression varRef, Symbol sym, boolean staticReference,
                                                                                            JavafxBindStatus bindContext, boolean inLHS) {
        
        JCExpression expr = varRef;

        if (sym instanceof VarSymbol) {
             VarSymbol vsym = (VarSymbol) sym;
            VarMorphInfo vmi = varMorphInfo(vsym);
            if (toJava.shouldMorph(vmi)) {
                if (sym.owner.kind == Kinds.TYP && !staticReference) {
                    // this is a non-static reference to an attribute, use the get$ form
                    assert varRef.getTag() == JCTree.SELECT : "attribute must be accessed through receiver";
                    JCFieldAccess select = (JCFieldAccess) varRef;
                    Name attrAccessName = names.fromString(attributeGetMethodNamePrefix + select.name.toString());
                    select = make.at(diagPos).Select(select.getExpression(), attrAccessName);
                    List<JCExpression> emptyArgs = List.nil();
                    expr = make.at(diagPos).Apply(null, select, emptyArgs);
                }   
                if (bindContext.isBidiBind()) {
                    // already in correct form-- leave it
                } else if (inLHS) {
                    // immediate left-hand side -- leave it
                } else {
                    // non-bind context -- want v1.get()
                    JCFieldAccess getSelect = make.Select(expr, getMethodName);
                    List<JCExpression> getArgs = List.nil();
                    expr = make.at(diagPos).Apply(null, getSelect, getArgs);
                }
            }
        } else if (sym instanceof MethodSymbol) {
            if (staticReference) {
                Name name = toJava.functionName((MethodSymbol)sym);
                if (expr.getTag() == JCTree.SELECT) {
                    JCFieldAccess select = (JCFieldAccess) expr;
                    expr = make.at(diagPos).Select(select.getExpression(), name);
                } else if (expr.getTag() == JCTree.IDENT) {
                    expr = make.at(diagPos).Ident(name);
                }
            }
        }
         return expr;
    }
    
    private void pretty(JCTree tree) {
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
    
    private void fxPretty(JCTree tree) {
        OutputStreamWriter osw = new OutputStreamWriter(System.out);
        Pretty pretty = new JavafxPretty(osw, false);
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
    
    public JCExpression morphAssign(DiagnosticPosition diagPos, VarSymbol vsym, JCExpression lhs, JCExpression rhs) {
        if (vsym != null) {
            VarMorphInfo vmi = varMorphInfo(vsym);
            if (toJava.shouldMorph(vmi)) {     
                JCFieldAccess setSelect = make.Select(lhs, setMethodName);
                List<JCExpression> setArgs = List.of(rhs);
                return make.at(diagPos).Apply(null, setSelect, setArgs);
            }
        }
        return make.at(diagPos).Assign(lhs, rhs); // make a new one so we are non-destructive
    }        
    
    /**
     * assignment of a sequence element --  s[i]=8
     */
    public JCExpression morphSequenceIndexedAssign(
            DiagnosticPosition diagPos, 
            JCExpression seq, 
            JCExpression index, 
            JCExpression value) {
        JCFieldAccess select = make.Select(seq, setMethodName);
        List<JCExpression> args = List.of(index, value);
        return make.at(diagPos).Apply(null, select, args);
    }        

    /**
     * access of a sequence element (RHS) --  s[i]
     */
    public JCExpression morphSequenceIndexedAccess(
            DiagnosticPosition diagPos, 
            JCExpression seq, 
            JCExpression index) {
        JCFieldAccess select = make.at(diagPos).Select(seq, getMethodName);
        List<JCExpression> args = List.of(index);
        return make.at(diagPos).Apply(null, select, args);
    }        

    //========================================================================================================================
    // Bound Expression support
    //========================================================================================================================
    
    /** Make an attributed tree representing a literal. This will be 
     *  a Literal node.
     *  @param type       The literal's type.
     *  @param value      The literal's value.
     */
    JCExpression makeLit(Type type, Object value, DiagnosticPosition diagPos) {
        int tag = value==null? TypeTags.BOT : type.tag;
        return make.at(diagPos).Literal(tag, value).setType(type.constType(value));
    }
    
    public BindAnalysis bindAnalysis(JCExpression expr) {
        return new BindAnalysis(expr);
    }
    
    class BindAnalysis extends JavafxTreeScanner {

        boolean iterationSeen = false;
        boolean assignmentSeen = false;
        boolean javaCallSeen = false;
        boolean fxCallSeen = false;
        boolean exceptionHandlingSeen = false;
        boolean foreachSeen = false;
        boolean nonTerminalReturnSeen = false;
        private JCReturn terminalReturn = null;

        BindAnalysis(JCExpression expr) {
            if (expr.getTag() == JavafxTag.BLOCK_EXPRESSION) {
                JFXBlockExpression bexpr = (JFXBlockExpression)expr;
                if (bexpr.value == null) {
                    JCStatement last = bexpr.getStatements().last();
                    if (last != null && last.getTag() == JavafxTag.RETURN)
                        terminalReturn = (JCReturn)last;
                }
            }
            scan(expr);
        }
        
        boolean isBindPermeable() {
            return !(iterationSeen || assignmentSeen || exceptionHandlingSeen || nonTerminalReturnSeen);
        }
        
        @Override
        public void visitApply(JCMethodInvocation tree) {
            super.visitApply(tree);
            Symbol msym =toJava.expressionSymbol(tree.getMethodSelect());
            if (msym != null && initBuilder.isJFXClass(msym.owner)) {
                fxCallSeen = true;
            } else {
                javaCallSeen = true;
            }
        }
        
        @Override
        public void visitForExpression(JFXForExpression tree) {
            super.visitForExpression(tree);
            foreachSeen = true;
        }

        @Override
        public void visitWhileLoop(JCWhileLoop tree) {
            super.visitWhileLoop(tree);
            iterationSeen = true;
        }

        @Override
        public void visitBreak(JCBreak tree) {
            super.visitBreak(tree);
            iterationSeen = true;
        }

        @Override
        public void visitContinue(JCContinue tree) {
            super.visitContinue(tree);
            iterationSeen = true;
        }

        @Override
        public void visitReturn(JCReturn tree) {
            super.visitReturn(tree);
            if (terminalReturn != tree) {
                nonTerminalReturnSeen = true;
            }
        }

        @Override
        public void visitTry(JCTry tree) {
            super.visitTry(tree);
            exceptionHandlingSeen = true;
        }

        @Override
        public void visitThrow(JCThrow tree) {
            super.visitThrow(tree);
            exceptionHandlingSeen = true;
        }

        @Override
        public void visitAssign(JCAssign tree) {
            super.visitAssign(tree);
            assignmentSeen = true;
        }

        @Override
        public void visitAssignop(JCAssignOp tree) {
            super.visitAssignop(tree);
            assignmentSeen = true;
        }

        @Override
        public void visitClassDeclaration(JFXClassDeclaration that) {
            // don't decend into classes
        }
    }
    
    /**
     * Build a list of dependencies for an expression.  It should include
     * any references to variables (which are Locations) but exclude
     * any that are defined internal to the expression.
     * The resulting list is translated.
     */
    List<JCExpression> buildDependencies(JCExpression expr) {
        final Map<VarSymbol, JCExpression> refMap = new HashMap<VarSymbol, JCExpression>();
        final Set<VarSymbol> internalSet = new HashSet<VarSymbol>();
        
        JavafxTreeScanner ts = new JavafxTreeScanner() {
            @Override
            public void visitIdent(JCIdent tree)   {
                if (tree.sym instanceof VarSymbol) {
                    VarSymbol ivsym = (VarSymbol)tree.sym;
                    if (ivsym.name != names._this && ivsym.name != names._super) {
                        VarMorphInfo vmi = varMorphInfo(ivsym);
                        if (toJava.shouldMorph(vmi)) {
                            refMap.put(ivsym, tree);
                        }
                    }
                }
            }
            @Override
            public void visitSelect(JCFieldAccess tree)   {
                super.visitSelect(tree);
                if (tree.sym instanceof VarSymbol) {
                    VarSymbol ivsym = (VarSymbol)tree.sym;
                    VarMorphInfo vmi = varMorphInfo(ivsym);
                    if (toJava.shouldMorph(vmi)) {
                        JCExpression expr = tree.getExpression();
                        Symbol exprSym = toJava.expressionSymbol(expr);
                        if (exprSym != null && internalSet.contains(exprSym)) { //TODO: fix this
                            log.warning(tree, "javafx.not.implemented", "dependency not generated: ", tree);
                        } else {
                            refMap.put(ivsym, tree); 
                        }
                    }
                }
            }
            @Override
            public void visitVar(JFXVar tree)   {
                if (tree.sym instanceof VarSymbol) {
                    VarSymbol vvsym = tree.sym;
                    internalSet.add(vvsym);
                }
            }
        };
        ts.scan(expr);
        
        ListBuffer<JCExpression> depend = ListBuffer.<JCExpression>lb();
        for (Map.Entry<VarSymbol, JCExpression> ref : refMap.entrySet()) {
            if (!internalSet.contains(ref.getKey())) {
                depend.append( toJava.translateLHS( ref.getValue() ) );
            }
        }
        
        return depend.toList();
    }
    
    public JCExpressionTupple buildDefinitionalAssignment(DiagnosticPosition diagPos,
                    VarMorphInfo vmi, JCExpression fxInit, JCExpression translatedInit,
                    JavafxBindStatus bindStatus, boolean isAttribute) {
        JCExpressionTupple ret;
        JCExpression initExpr = (translatedInit != null)? 
                translatedInit : 
                vmi.getTypeKind() == TYPE_KIND_SEQUENCE? 
                      toJava.makeEmptySeuenceCreator(diagPos, vmi.getElementType())
                    : makeLit(vmi.getRealType(), vmi.getDefaultValue(), diagPos);
        ret = new JCExpressionTupple(initExpr, null);
        if (bindStatus.isUnidiBind()) {
            JCStatement stmt = make.at(diagPos).Return(translatedInit);
            ret = buildExpressionAndDependencies(vmi, fxInit, stmt, bindStatus, isAttribute);
        } else if (!bindStatus.isBidiBind()) {
            initExpr = makeCall(vmi, diagPos, List.of(initExpr), varLocation, makeMethodName);
            ret = new JCExpressionTupple(initExpr, null);
        }
        return ret;
    }
    
    JCExpressionTupple buildExpressionAndDependencies(TypeMorphInfo tmi, 
            JCExpression fxInit, JCStatement stmt, JavafxBindStatus bindStatus, boolean isAttribute) {
        DiagnosticPosition diagPos = fxInit.pos();

        JCExpression newExpr = buildExpressionClass(diagPos, tmi,  stmt);
        List<JCExpression> dependencies = buildDependencies(fxInit);
        
        ListBuffer<JCExpression> argValues = ListBuffer.lb();
        argValues.append(newExpr);
        if (!isAttribute) {
            argValues.appendList(dependencies);
        }
        
        JCExpression makeExpr = makeExpressionLocation(diagPos, tmi,  bindStatus, argValues.toList());
        return new JCExpressionTupple(makeExpr, isAttribute ? dependencies : null);
    }
    
    JCExpression buildExpression(TypeMorphInfo tmi, 
            JCExpression fxInit, JCStatement stmt, JavafxBindStatus bindStatus) {
        DiagnosticPosition diagPos = fxInit.pos();

        List<JCExpression> dependencies = buildDependencies(fxInit);
        if (false && dependencies.size() == 0) { //TODO: block expression return would have to be dealt with
            return makeCall(tmi, diagPos, List.of(toJava.translate(fxInit)), varLocation, makeMethodName);
        } else {
            JCExpression newExpr = buildExpressionClass(diagPos, tmi, stmt);

            ListBuffer<JCExpression> argValues = ListBuffer.lb();
            argValues.append(newExpr);
            argValues.appendList(dependencies);

            return makeExpressionLocation(diagPos, tmi, bindStatus, argValues.toList());
        }
    }
    
    JCExpression makeExpressionLocation(DiagnosticPosition diagPos, TypeMorphInfo tmi,  JavafxBindStatus bindStatus, List<JCExpression> makeArgs) {
        Name makeName = bindStatus.isLazy()? makeLazyMethodName : makeMethodName;
        return makeCall(tmi, diagPos, makeArgs, exprLocation, makeName);
    }
    
    JCExpression buildExpressionClass(DiagnosticPosition diagPos, TypeMorphInfo tmi,  JCStatement stmt) {
        JCBlock body = stmt.getTag() == JavafxTag.BLOCK? 
            (JCBlock)stmt :
            make.at(diagPos).Block(0, List.<JCStatement>of(stmt));
        
        JCMethodDecl getMethod = make.at(diagPos).MethodDef(
                make.at(diagPos).Modifiers(Flags.PUBLIC), 
                getMethodName, 
                toJava.makeTypeTree(tmi.getRealType(), diagPos, true), 
                List.<JCTypeParameter>nil(), 
                List.<JCVariableDecl>nil(), 
                List.<JCExpression>nil(), 
                body, 
                null); 
        
        toJava.bindingExpressionDefs.append(getMethod);
        JCClassDecl anon = make.at(diagPos).AnonymousClassDef(
                    make.at(diagPos).Modifiers(0), 
                    toJava.bindingExpressionDefs.toList());
        toJava.bindingExpressionDefs = null;
        return make.at(diagPos).NewClass(
                null,                       // enclosing
                List.<JCExpression>nil(),   // type args
                // class name
                toJava.makeTypeTree(tmi.getBindingExpressionType(), diagPos),
                List.<JCExpression>nil(),   // args
                anon);
    }

    private JCExpression makeCall(TypeMorphInfo tmi, 
                                  DiagnosticPosition diagPos,
                                  List<JCExpression> makeArgs,
                                  LocationNameSymType[] lnsta,
                                  Name makeName) {
        JCExpression locationTypeExp = sharedLocationId(diagPos, tmi.getTypeKind(), lnsta);
        JCFieldAccess makeSelect = make.at(diagPos).Select(locationTypeExp, makeName);
        List<JCExpression> typeArgs = null;
        if (tmi.getTypeKind() == TYPE_KIND_OBJECT) {
            typeArgs = List.of(toJava.makeTypeTree(tmi.getRealType(), diagPos, true));
        }
        return make.at(diagPos).Apply(typeArgs, makeSelect, makeArgs);
    }
}
