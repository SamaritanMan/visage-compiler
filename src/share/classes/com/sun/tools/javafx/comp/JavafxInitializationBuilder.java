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

import com.sun.tools.mjavac.code.*;
import com.sun.tools.mjavac.code.Scope.Entry;
import com.sun.tools.mjavac.code.Symbol.ClassSymbol;
import com.sun.tools.mjavac.code.Symbol.MethodSymbol;
import com.sun.tools.mjavac.code.Symbol.VarSymbol;
import com.sun.tools.mjavac.code.Type.*;
import com.sun.tools.mjavac.tree.JCTree;
import com.sun.tools.mjavac.tree.JCTree.*;
import com.sun.tools.mjavac.util.*;
import com.sun.tools.mjavac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javafx.code.JavafxFlags;
import com.sun.tools.javafx.code.JavafxSymtab;
import com.sun.tools.javafx.comp.JavafxAnalyzeClass.*;
import com.sun.tools.javafx.comp.JavafxAbstractTranslation.*;
import com.sun.tools.javafx.comp.JavafxAbstractTranslation.ExpressionResult.*;
import static com.sun.tools.javafx.comp.JavafxDefs.*;
import com.sun.tools.javafx.tree.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

/**
 * Build the representation(s) of a JavaFX class.  Includes class initialization, attribute and function proxies.
 * With support for mixins.
 *
 * @author Robert Field
 * @author Lubo Litchev
 * @author Per Bothner
 * @author Zhiqun Chen
 * @author Jim Laskey
 */
public class JavafxInitializationBuilder extends JavafxTranslationSupport {
    protected static final Context.Key<JavafxInitializationBuilder> javafxInitializationBuilderKey =
        new Context.Key<JavafxInitializationBuilder>();

    private final JavafxToJava toJava;
    private final JavafxClassReader reader;
    private final JavafxOptimizationStatistics optStat;

    public static class LiteralInitVarMap {
        private int count = 1;
        public Map<VarSymbol, Integer> varMap = new HashMap<VarSymbol, Integer>();
        public ListBuffer<VarSymbol> varList = ListBuffer.lb();

        public int addVar(VarSymbol sym) {
            Integer value = varMap.get(sym);

            if (value == null) {
                value = new Integer(count++);
                varMap.put(sym, value);
                varList.append(sym);
            }

            return value.intValue();
        }

        public int size() {
            return varMap.size();
        }
    }

    public static class LiteralInitClassMap {
        public Map<ClassSymbol, LiteralInitVarMap> classMap = new HashMap<ClassSymbol, LiteralInitVarMap>();

        public LiteralInitVarMap getVarMap(ClassSymbol sym) {
            LiteralInitVarMap map = classMap.get(sym);

            if (map == null) {
                map = new LiteralInitVarMap();
                classMap.put(sym, map);
            }

            return map;
        }

        public int size() {
            return classMap.size();
        }
    }

    public static JavafxInitializationBuilder instance(Context context) {
        JavafxInitializationBuilder instance = context.get(javafxInitializationBuilderKey);
        if (instance == null)
            instance = new JavafxInitializationBuilder(context);
        return instance;
    }

    protected JavafxInitializationBuilder(Context context) {
        super(context);

        context.put(javafxInitializationBuilderKey, this);

        toJava = JavafxToJava.instance(context);
        reader = (JavafxClassReader) JavafxClassReader.instance(context);
        optStat = JavafxOptimizationStatistics.instance(context);
    }

    /**
     * Hold the result of analyzing the class.
     * */
    static class JavafxClassModel {
        final Name interfaceName;
        final List<JCExpression> interfaces;
        final List<JCTree> iDefinitions;
        final List<JCTree> additionalClassMembers;
        final List<JCExpression> additionalImports;
        final Type superType;
        final ClassSymbol superClassSym;
        final List<ClassSymbol> superClasses;
        final List<ClassSymbol> immediateMixins;
        final List<ClassSymbol> allMixins;

        JavafxClassModel(
                Name interfaceName,
                List<JCExpression> interfaces,
                List<JCTree> iDefinitions,
                List<JCTree> addedClassMembers,
                List<JCExpression> additionalImports,
                Type superType,
                ClassSymbol superClassSym,
                List<ClassSymbol> superClasses,
                List<ClassSymbol> immediateMixins,
                List<ClassSymbol> allMixins) {
            this.interfaceName = interfaceName;
            this.interfaces = interfaces;
            this.iDefinitions = iDefinitions;
            this.additionalClassMembers = addedClassMembers;
            this.additionalImports = additionalImports;
            this.superType = superType;
            this.superClassSym = superClassSym;
            this.superClasses = superClasses;
            this.immediateMixins = immediateMixins;
            this.allMixins = allMixins;
        }
    }
    
    /**
     * Analyze the class.
     *
     * Determine what methods will be needed to access attributes.
     * Determine what methods will be needed to proxy to the static implementations of functions.
     * Determine what other misc fields and methods will be needed.
     * Create the corresponding interface.
     *
     * Return all this as a JavafxClassModel for use in translation.
     * */
   JavafxClassModel createJFXClassModel(JFXClassDeclaration cDecl,
           List<TranslatedVarInfo> translatedAttrInfo,
           List<TranslatedOverrideClassVarInfo> translatedOverrideAttrInfo,
           List<TranslatedFuncInfo> translatedFuncInfo,
           LiteralInitClassMap initClassMap,
           ListBuffer<JCStatement> translatedInitBlocks, ListBuffer<JCStatement> translatedPostInitBlocks) {

        DiagnosticPosition diagPos = cDecl.pos();
        Type superType = types.superType(cDecl);
        ClassSymbol outerTypeSym = outerTypeSymbol(cDecl); // null unless inner class with outer reference
        boolean isLibrary = toJava.getAttrEnv().toplevel.isLibrary;
        boolean isRunnable = toJava.getAttrEnv().toplevel.isRunnable;

        JavafxAnalyzeClass analysis = new JavafxAnalyzeClass(diagPos,
                cDecl.sym, translatedAttrInfo, translatedOverrideAttrInfo, translatedFuncInfo,
                names, types, defs, syms, reader, typeMorpher);
                
        List<VarInfo> classVarInfos = analysis.classVarInfos();
        List<VarInfo> scriptVarInfos = analysis.scriptVarInfos();
        List<FuncInfo> classFuncInfos = analysis.classFuncInfos();
        List<FuncInfo> scriptFuncInfos = analysis.scriptFuncInfos();
        
        boolean hasStatics = !scriptVarInfos.isEmpty() || !scriptFuncInfos.isEmpty();
        
        int classVarCount = analysis.getClassVarCount();
        int scriptVarCount = analysis.getScriptVarCount();
        List<MethodSymbol> needDispatch = analysis.needDispatch();
        ClassSymbol fxSuperClassSym = analysis.getFXSuperClassSym();
        List<ClassSymbol> superClasses = analysis.getSuperClasses();
        List<ClassSymbol> immediateMixinClasses = analysis.getImmediateMixins();
        List<ClassSymbol> allMixinClasses = analysis.getAllMixins();

        boolean isMixinClass = cDecl.isMixinClass();
        boolean isScriptClass = cDecl.isScriptClass();
        boolean isAnonClass = analysis.isAnonClass();
        boolean hasFxSuper = fxSuperClassSym != null;
        // Have to populate the var map for anon classes.
        // TODO: figure away to avoid this if not used (needs global knowledge.)
        LiteralInitVarMap varMap = isAnonClass ? initClassMap.getVarMap(analysis.getCurrentClassSymbol()) : null;

        ListBuffer<JCTree> cDefinitions = ListBuffer.lb();  // additional class members needed
        ListBuffer<JCTree> iDefinitions = ListBuffer.lb();

        JavaCodeMaker javaCodeMaker = new JavaCodeMaker(analysis, cDefinitions);
        
        if (!isMixinClass) {
            javaCodeMaker.makeAttributeNumbers(classVarInfos, classVarCount, varMap);
            javaCodeMaker.makeAttributeFields(classVarInfos);
            javaCodeMaker.makeAttributeAccessorMethods(classVarInfos);
            javaCodeMaker.makeVarNumMethods();

            JCStatement initMap = isAnonClass ? javaCodeMaker.makeInitVarMapInit(varMap) : null;

            if (outerTypeSym == null) {
                javaCodeMaker.makeJavaEntryConstructor(classVarInfos);
            } else {
                javaCodeMaker.makeOuterAccessorField(outerTypeSym);
                javaCodeMaker.makeOuterAccessorMethod(outerTypeSym);
            }

            javaCodeMaker.makeFunctionProxyMethods(needDispatch);
            javaCodeMaker.makeFXEntryConstructor(outerTypeSym);
            javaCodeMaker.makeInitMethod(defs.userInit_FXObjectMethodName, translatedInitBlocks, immediateMixinClasses);
            javaCodeMaker.makeInitMethod(defs.postInit_FXObjectMethodName, translatedPostInitBlocks, immediateMixinClasses);
            javaCodeMaker.gatherFunctions(classFuncInfos);

            if (isScriptClass && hasStatics) {
                ListBuffer<JCTree> sDefinitions = ListBuffer.lb();
                 
                // script-level into class X
                javaCodeMaker.makeAttributeFields(scriptVarInfos);
                javaCodeMaker.makeAttributeAccessorMethods(scriptVarInfos);
                javaCodeMaker.gatherFunctions(scriptFuncInfos);
                javaCodeMaker.makeInitClassMaps(initClassMap);

                // script-level into class X.X$Script
                javaCodeMaker.setContext(true, sDefinitions);
                javaCodeMaker.makeAttributeNumbers(scriptVarInfos, scriptVarCount, null);
                javaCodeMaker.makeVarNumMethods();
                javaCodeMaker.makeScriptLevelAccess(cDecl.sym, true, isRunnable);
                javaCodeMaker.setContext(false, cDefinitions);

                // script-level into class X
                javaCodeMaker.makeScriptLevelAccess(cDecl.sym, false, isRunnable);
                javaCodeMaker.makeInitStaticAttributesBlock(cDecl.sym, true, isLibrary ? scriptVarInfos : null, initMap);
                javaCodeMaker.makeScript(sDefinitions.toList());
            } else {
                javaCodeMaker.makeInitStaticAttributesBlock(cDecl.sym, false, null, initMap);
            }

            if (!hasFxSuper) {
                // Has a non-JavaFX super, so we can't use FXBase, therefore we need
                // to clone the necessary vars and methods.
                // This code must be after all methods have been added to cDefinitions,

                // A set of methods to exclude from cloning.
                HashSet<String> excludes = new HashSet<String>();

                // Exclude any methods generated by the init builder.
                for (JCTree member : cDefinitions) {
                    if (member.getTag() == JCTree.METHODDEF) {
                        JCMethodDecl jcmeth = (JCMethodDecl)member;
                        excludes.add(jcMethodDeclStr(jcmeth));
                     }
                }

                // Clone what is needed from FXBase/FXObject.
                javaCodeMaker.cloneFXBase(excludes);
            }

        } else {
            // Mixin class

            javaCodeMaker.makeAttributeFields(classVarInfos);

            // Static(script) vars are exposed in class
            javaCodeMaker.makeAttributeFields(scriptVarInfos);
            javaCodeMaker.makeAttributeAccessorMethods(scriptVarInfos);
            javaCodeMaker.makeVarNumMethods();
            javaCodeMaker.makeInitStaticAttributesBlock(cDecl.sym, false, null, null);

            javaCodeMaker.makeMixinAccessorMethods(classVarInfos);
            javaCodeMaker.makeInitMethod(defs.userInit_FXObjectMethodName, translatedInitBlocks, immediateMixinClasses);
            javaCodeMaker.makeInitMethod(defs.postInit_FXObjectMethodName, translatedPostInitBlocks, immediateMixinClasses);
            javaCodeMaker.gatherFunctions(classFuncInfos);
            
            javaCodeMaker.setContext(false, iDefinitions);
            javaCodeMaker.makeMemberVariableAccessorInterfaceMethods();
            javaCodeMaker.makeFunctionInterfaceMethods();
            javaCodeMaker.makeOuterAccessorInterfaceMembers();
            javaCodeMaker.setContext(false, cDefinitions);
        }

        Name interfaceName = isMixinClass ? interfaceName(cDecl) : null;

        return new JavafxClassModel(
                interfaceName,
                makeImplementingInterfaces(diagPos, cDecl, immediateMixinClasses),
                iDefinitions.toList(),
                cDefinitions.toList(),
                makeAdditionalImports(diagPos, cDecl, immediateMixinClasses),
                superType,
                fxSuperClassSym,
                superClasses,
                immediateMixinClasses,
                allMixinClasses);
    }

    //
    // Build a string that can be compared against MethodSymbol.toString()
    //
    private static String jcMethodDeclStr(JCMethodDecl meth) {
        String str = meth.name.toString() + "(";
        boolean needsComma = false;
        for (JCVariableDecl varDecl : meth.getParameters()) {
            if (needsComma) str += ",";
            str += varDecl.vartype.toString();
            needsComma = true;
        }
        str += ")";
        return str;
    }

    private List<JCExpression> makeImplementingInterfaces(DiagnosticPosition diagPos,
            JFXClassDeclaration cDecl,
            List<ClassSymbol> baseInterfaces) {
        ListBuffer<JCExpression> implementing = ListBuffer.lb();

        if (cDecl.isMixinClass()) {
            implementing.append(makeIdentifier(diagPos, cFXObject));
            implementing.append(makeIdentifier(diagPos, cFXMixin));
        } else {
            implementing.append(makeIdentifier(diagPos, cFXObject));
        }

        for (JFXExpression intf : cDecl.getImplementing()) {
            implementing.append(makeType(diagPos, intf.type, false));
        }

        for (ClassSymbol baseClass : baseInterfaces) {
            implementing.append(makeType(diagPos, baseClass.type, true));
        }
        return implementing.toList();
    }

    private List<JCExpression> makeAdditionalImports(DiagnosticPosition diagPos, JFXClassDeclaration cDecl, List<ClassSymbol> baseInterfaces) {
        // Add import statements for all the base classes and basClass $Mixin(s).
        // There might be references to them when the methods/attributes are rolled up.
        ListBuffer<JCExpression> additionalImports = new ListBuffer<JCExpression>();
        for (ClassSymbol baseClass : baseInterfaces) {
            if (baseClass.type != null && baseClass.type.tsym != null &&
                    baseClass.type.tsym.packge() != cDecl.sym.packge() &&     // Work around javac bug (CR 6695838)
                    baseClass.type.tsym.packge() != syms.unnamedPackage) {    // Work around javac bug. the visitImport of Attr
                // is casting to JCFieldAcces, but if you have imported an
                // JCIdent only a ClassCastException is thrown.
                additionalImports.append(makeType( diagPos,baseClass.type, false));
                additionalImports.append(makeType( diagPos,baseClass.type, true));
            }
        }
        return additionalImports.toList();
    }

    // Add the methods and field for accessing the outer members. Also add a constructor with an extra parameter
    // to handle the instantiation of the classes that access outer members
    private ClassSymbol outerTypeSymbol(JFXClassDeclaration cdecl) {
        if (cdecl.sym != null && toJava.getHasOuters().contains(cdecl.sym)) {
            Symbol typeOwner = cdecl.sym.owner;
            while (typeOwner != null && typeOwner.kind != Kinds.TYP) {
                typeOwner = typeOwner.owner;
            }

            if (typeOwner != null) {
                // Only return an interface class if it's a mixin.
                return !isMixinClass((ClassSymbol)typeOwner) ? (ClassSymbol)typeOwner.type.tsym :
                        reader.jreader.enterClass(names.fromString(typeOwner.type.toString() + mixinClassSuffix));
            }
        }
        return null;
    }

    protected String getSyntheticPrefix() {
        return "ifx$";
    }

    //-----------------------------------------------------------------------------------------------------------------------------
    //
    // This class is used to simplify the construction of java code in the
    // initialization builder.
    //
    class JavaCodeMaker extends JavaTreeBuilder {
        // The current class analysis/
        private final JavafxAnalyzeClass analysis;
        private ListBuffer<JCTree> definitions;
        private Name scriptName;
        private ClassSymbol scriptClassSymbol;
        private ClassType scriptClassType;
        private final boolean isBoundFuncClass;

        JavaCodeMaker(JavafxAnalyzeClass analysis, ListBuffer<JCTree> definitions) {
            super(analysis.getCurrentClassPos(), analysis.getCurrentClassDecl(), false);
            this.analysis = analysis;
            this.definitions = definitions;
            this.scriptName = analysis.getCurrentClassDecl().getName().append(defs.scriptClassSuffixName);
            this.scriptClassSymbol = makeClassSymbol(Flags.STATIC | Flags.PUBLIC, this.scriptName, getCurrentClassSymbol());
            this.scriptClassType = (ClassType)this.scriptClassSymbol.type;

            Symbol owner = getCurrentOwner().owner;
            this.isBoundFuncClass = (owner instanceof MethodSymbol) &&
                                (owner.flags() & JavafxFlags.BOUND) != 0L;
        }
        
        //
        // Method for changing the current definition list.
        //
        public void setContext(boolean isScript, ListBuffer<JCTree> definitions) {
            setIsScript(isScript);
            this.definitions = definitions;
        }
        
        //
        // This method returns the current owner symbol.
        //
        ClassSymbol getCurrentOwner() {
            return isScript() ? scriptClassSymbol : analysis.getCurrentClassSymbol();
        }
        
        //
        // Methods for adding a new definitions.
        //
        private void addDefinition(JCTree member) {
            if (member != null) {
                definitions.append(member);
            }
        }
        private void addDefinitions(List<JCTree> members) {
            if (members != null) {
                definitions.appendList(members);
            }
        }

        //
        // Methods for managing the current diagnostic position.
        //
        private void setDiagPos(VarInfo ai) { setDiagPos(ai.pos()); }
        private void resetDiagPos() { setDiagPos(analysis.getCurrentClassPos()); }

        //
        // Returns the current class symbol.
        //
        public ClassSymbol getCurrentClassSymbol() {
            return analysis.getCurrentClassSymbol();
        }

        //
        // Argument ids
        //
        JCIdent varNumArg() {
            return makeMethodArg(defs.varNum_ArgName, syms.intType);
        }

        JCIdent updateInstanceArg() {
            return makeMethodArg(defs.updateInstance_ArgName, syms.javafx_FXObjectType);
        }

        JCIdent objArg() {
            return makeMethodArg(defs.obj_ArgName, syms.objectType);
        }

        //
        // Returns true if the sym is the current class symbol.
        //
        public boolean isCurrentClassSymbol(Symbol sym) {
            return analysis.isCurrentClassSymbol(sym);
        }
        
        //
        // Return raw flags for current class.
        //
        public long rawFlags() {
            return isMixinClass() ? (Flags.STATIC | Flags.PUBLIC) : Flags.PUBLIC;
        }
        
        //
        // Create a method symbol.
        //
        public MethodSymbol makeMethodSymbol(long flags, Type returnType, Name methodName, List<Type> argTypes) {
            MethodType methodType = new MethodType(argTypes, returnType, List.<Type>nil(), syms.methodClass);
            return new MethodSymbol(flags, methodName, methodType, getCurrentOwner());
        }

        //
        // Create a var symbol.
        //
        public VarSymbol makeVarSymbol(long flags, Type type, Name varName) {
            return new VarSymbol(flags, varName, type, getCurrentOwner());
        }
        
        //
        // This method creates a member field field.
        //
        private JCVariableDecl makeField(long flags, Type varType, Name name, JCExpression varInit) {
            VarSymbol varSym = makeVarSymbol(flags, varType, name);
            return Var(flags, varType, name, varInit, varSym);
        }

        //
        //
        // This method generates a simple java integer field then adds to the buffer.
        //
        private JCVariableDecl addSimpleIntVariable(long flags, Name name, int value) {
            return makeField(flags, syms.intType, name, Int(value));
        }

        //
        // This method generates a java field for a varInfo.
        //
        private JCVariableDecl makeVariableField(VarInfo varInfo, JCModifiers mods, Type varType, Name name, JCExpression varInit) {
            setDiagPos(varInfo);
            // Get the var symbol.
            VarSymbol varSym = varInfo.getSymbol();
            // Construct the variable itself.
            JCVariableDecl var = Var(mods, makeType(varType), name, varInit, varSym);
            // Update the statistics.
            optStat.recordClassVar(varSym);
            optStat.recordConcreteField();

            return var;
        }

        //
        // Build the location and value field for each attribute.
        //
        public void makeAttributeFields(List<? extends VarInfo> attrInfos) {
            for (VarInfo ai : attrInfos) {
                // Only process attributes declared in this class (includes mixins.)
                if (ai.needsCloning() && !ai.isOverride()) {
                    // Set the current diagnostic position.
                    setDiagPos(ai);
                    // Grab the variable symbol.
                    VarSymbol varSym = ai.getSymbol();
                    // The fields need to be available to reflection.
                    // TODO deal with defs.
                    JCModifiers mods = m().Modifiers(Flags.PUBLIC | (ai.getFlags() & Flags.STATIC));

                    // Apply annotations, if current class then add source annotations.
                    if (isCurrentClassSymbol(varSym.owner)) {
                        List<JCAnnotation> annotations = List.<JCAnnotation>of(m().Annotation(
                                makeIdentifier(diagPos, JavafxSymtab.sourceNameAnnotationClassNameString),
                                List.<JCExpression>of(String(varSym.name.toString()))));
                        mods = addAccessAnnotationModifiers(diagPos, varSym.flags(), mods, annotations);
                    } else {
                        mods = addInheritedAnnotationModifiers(diagPos, varSym.flags(), mods);
                    }

                    // Construct the value field
                    addDefinition(makeVariableField(ai, mods, ai.getRealType(), attributeValueName(varSym),
                                                    needsDefaultValue(ai.getVMI()) && !(ai.isSequence() && ai.hasBoundDefinition()) ? 
                                                          JavafxInitializationBuilder.this.makeDefaultValue(diagPos, ai.getVMI())
                                                        : null));
                }
            }
        }
        
        
        //
        // This method constructs modifiers for getters/setters and proxies.
        //
        private JCModifiers proxyModifiers(VarInfo ai, boolean isAbstract) {
            // Copy old flags from VarInfo.
            long oldFlags = ai.getFlags();
            
            // Determine new flags.
            long newFlags = (oldFlags & Flags.STATIC) | Flags.PUBLIC;
            if (isAbstract) {
                newFlags|= Flags.ABSTRACT;
            } else if (isMixinClass()) {
                newFlags|= Flags.STATIC;
            }

            // Set up basic flags.
            JCModifiers mods = m().Modifiers(newFlags);

            // If var is in current class.
            if (isCurrentClassSymbol(ai.getSymbol().owner)) {
                // Use local access modifiers.
                mods = addAccessAnnotationModifiers(ai.pos(), oldFlags, mods);
            } else {
                // Use inherited modifiers.
                mods = addInheritedAnnotationModifiers(ai.pos(), oldFlags, mods);
            }

            return mods;
        }

        //
        // Return a receiver$, scriptLevelAccess$() or null depending on the context.
        //
        private JCExpression getReceiver(VarInfo varInfo) {
            return getReceiver(varInfo.getSymbol());
        }
        private JCExpression getReceiverOrThis(VarInfo varInfo) {
            return getReceiverOrThis(varInfo.getSymbol());
        }

        //
        // This method gathers all the translated functions in funcInfos.
        //
        public void gatherFunctions(List<FuncInfo> funcInfos) {
            for (FuncInfo func : funcInfos) {
                if (func instanceof TranslatedFuncInfo) {
                    addDefinitions(((TranslatedFuncInfo)func).jcFunction());
                }
            }
        }
        
        //
        // This class is designed to reduce the repetitiveness of constructing methods.
        //
        public class MethodBuilder {
            // Name of method to generate.
            protected Name methodName;
            // Method return type.
            protected Type returnType;
            // True if the return type is void.
            protected boolean isVoidReturnType;
            // True if we're to stop the build.
            protected boolean stopBuild = false;
            // True if body is required.
            protected boolean needsBody = true;
            // Cached method symbol.
            MethodSymbol methodSymbol = null;
            
            // Grab the super class.
            ClassSymbol superClassSym = analysis.getFXSuperClassSym();
            // Grab the mixin classes.
            public List<ClassSymbol> immediateMixinClasses = analysis.getImmediateMixins();
            
            // Stack of nested statements.
            protected Stack<ListBuffer<JCStatement>> stmtsStack = new Stack<ListBuffer<JCStatement>>();
            // Current set of statements.
            protected ListBuffer<JCStatement> stmts = ListBuffer.lb();
            
            void buildIf(boolean condition) {
                stopBuild = !condition;
            }
            
            // List of parameter types.
            ListBuffer<Type> paramTypes = ListBuffer.lb();
            // List of parameter names.
            ListBuffer<Name> paramNames = ListBuffer.lb();
            
            MethodBuilder(Name methodName, Type returnType) {
                this.methodName = methodName;
                this.returnType = returnType;
                this.isVoidReturnType = returnType == syms.voidType;
            }
            
            // This method saves the current list of statements and starts a new one.
            public void beginBlock() {
                stmtsStack.push(stmts);
                stmts = ListBuffer.lb();
            }
            
            // This method restores the previous list of statements and returns the current
            // list of statements in a block.
            public JCBlock endBlock() {
                return Block(endBlockAsList());
            }
            
            // This method restores the previous list of statements and returns the current
            // list of statements.
            public List<JCStatement> endBlockAsList() {
                assert !stmtsStack.empty() : "MethodBuilder: mismatched blocks";
                List<JCStatement> result = stmts.toList();
                stmts = stmtsStack.pop();
                return result;
            }
            
            // This method restores the previous list of statements and returns the current
            // list buffer of statements.
            public ListBuffer<JCStatement> endBlockAsBuffer() {
                assert !stmtsStack.empty() : "MethodBuilder: mismatched blocks";
                ListBuffer<JCStatement> result = stmts;
                stmts = stmtsStack.pop();
                return result;
            }
            
            // This method adds a new statement to the current lists of statements.
            public void addStmt(JCStatement stmt) {
                if (stmt != null) {
                    stmts.append(stmt);
                }
            }
            
            // This method adds several statements to the current lists of statements.
            public void addStmts(List<JCStatement> list) {
                stmts.appendList(list);
            }
            public void addStmts(ListBuffer<JCStatement> list) {
                stmts.appendList(list.toList());
            }
            
            // This method adds a new parameter type and name to the current method.
            public void addParam(Type type, Name name) {
                paramTypes.append(type);
                paramNames.append(name);
            }

            // This method adds a new parameter type and name to the current method.
            public void addParam(JCIdent arg) {
                addParam(arg.type, arg.name);
            }

            // This method returns all the parameters for the current method as a
            // list of JCVariableDecl.
            protected List<JCVariableDecl> paramList() {
                return paramList(false);
            }
            protected List<JCVariableDecl> paramList(boolean isAbstract) {
                Iterator<Type> typeIter = paramTypes.iterator();
                Iterator<Name> nameIter = paramNames.iterator();
                ListBuffer<JCVariableDecl> params = ListBuffer.lb();
                
                if (isMixinClass() && !isAbstract) {
                    params.append(ReceiverParam(getCurrentClassDecl()));
                }
     
                while (typeIter.hasNext() && nameIter.hasNext()) {
                    params.append(Param(typeIter.next(), nameIter.next()));
                }
                
                return params.toList();
            }
            
            
            // This method returns all the parameters for the current method as a
            // list of JCExpression.
            protected List<JCExpression> argList() {
                ListBuffer<JCExpression> args = ListBuffer.lb();
                
                for (Name name : paramNames) {
                    args.append(id(name));
                }
                
                return args.toList();
            }
            
            // This method generates a method symbol for the current method.
            protected MethodSymbol methodSymbol(boolean isAbstract) {
                if (methodSymbol == null) {
                    ListBuffer<Type> argtypes = ListBuffer.lb();
                    
                    if (isMixinClass() && !isAbstract) {
                        argtypes.append(getCurrentOwner().type);
                    }
                    
                    for (Type type : paramTypes) {
                        argtypes.append(type);
                    }
                    
                    methodSymbol = makeMethodSymbol(rawFlags(), returnType, methodName, argtypes.toList());
                }
                
                return methodSymbol;
            }

            // This method generates a call to the mixin symbol.
            public void callMixin(ClassSymbol mixin) {
                JCExpression receiver = id(isMixinClass() ? defs.receiverName : names._this);
                List<JCExpression> mixinArgs = List.<JCExpression>of(receiver).appendList(argList());
                JCExpression selector = makeType(mixin.type, false);
 
                if (isVoidReturnType) {
                    addStmt(CallStmt(selector, methodName, mixinArgs));
                } else {
                    addStmt(Return(Call(selector, methodName, mixinArgs)));
                }
            }

            // This method generates all the calls for immediate mixins.
            public void callMixins() {
                for (ClassSymbol mixin : immediateMixinClasses) {
                    callMixin(mixin);
                }
            }
            
            // This method generates the call for the super class.
            public void callSuper() {
                if (superClassSym != null && !isMixinClass()) {
                    List<JCExpression> superArgs = argList();
                    
                    if (isVoidReturnType) {
                        addStmt(CallStmt(id(names._super), methodName, superArgs));
                    } else {
                        addStmt(Return(Call(id(names._super), methodName, superArgs)));
                    }
                 }
            }
            
            // Return the method flags.
            public JCModifiers flags() {
                return m().Modifiers(rawFlags());
            }
            
            // Driver method to construct the current method.
            public void build() {
                // Initialize for method.
                initialize();
                
                // Generate the code.
                if (needsBody && !stopBuild) generate();
                
                // Produce no method if generate indicates stopBuild.
                if (!stopBuild) {
                    // Record method.
                    optStat.recordProxyMethod();
    
                    // Construct method.
                    addDefinition(Method(flags(),
                                             returnType,
                                             methodName,
                                             paramList(!needsBody),
                                             needsBody ? stmts.toList() : null,
                                             methodSymbol(!needsBody)));
                }
            }

            // This method generates the statements for the method.
            public void generate() {
                // Reset diagnostic position to current class.
                resetDiagPos();
                // Emit prologue statements.
                prologue();
                
                // Reset diagnostic position to current class.
                resetDiagPos();
                // Emit method body.
                body();
                
                // Reset diagnostic position to current class.
                resetDiagPos();
                // Emit epilog statements.
                epilogue();
            }

            
            // This method contains any code to initialize the builder.
            public void initialize() {
            }
            
            // This method generates any code needed before the body of the method.
            public void prologue() {
            }
             
            // This method generates any code needed after the body of the method.
            public void epilogue() {
            }
            
            // This method generates the body of the method.
            public void body() {
                statements();
            }
            
            // This method generates specialized code for the body.
            public void statements() {
            }            
        }
        
        //
        // This class is designed to generate a method whose body is a static
        // utility.
        //
        public class StaticMethodBuilder extends MethodBuilder {
            StaticMethodBuilder(Name methodName, Type returnType) {
                super(methodName, returnType);
            }
            
            // Return the method flags.
            @Override
            public JCModifiers flags() {
                return m().Modifiers(Flags.STATIC | Flags.PUBLIC);
            }
        }
      
        //
        // This class is designed to generate a method whose body is a var
        // accessor.
        //
        public class VarAccessorMethodBuilder extends MethodBuilder {
            // Current var info.
            protected VarInfo varInfo;
            // Symbol used on the method.
            protected VarSymbol varSym;
            // Symbol used when accessing the variable.
            protected VarSymbol proxyVarSym;
            // Name of var field.
            protected Name varName;
            // Is a sequence type.
            protected boolean isSequence;
            // Real type of the var.
            protected Type type;
            // Element type of the var.
            protected Type elementType;

            
            VarAccessorMethodBuilder(Name methodName, Type returnType, VarInfo varInfo, boolean needsBody) {
                super(methodName, returnType);
                this.varInfo = varInfo;
                this.needsBody = needsBody;
                this.varSym = varInfo.getSymbol();
                this.proxyVarSym = varInfo.proxyVarSym();
                this.varName = attributeValueName(proxyVarSym);
                this.isSequence = varInfo.isSequence();
                this.type = varInfo.getRealType();
                this.elementType = isSequence ? varInfo.getElementType() : null;
                this.needsBody = needsBody;
            }
            
            // Return the method flags.
            @Override
            public JCModifiers flags() {
                return proxyModifiers(varInfo, !needsBody);
            }
        }
        
        //
        // This class is designed to generate a method whose body is a mixin var
        // accessor.
        //
        public class MixinMethodBuilder extends MethodBuilder {
            // Current var info.
            protected VarInfo varInfo;
            // Symbol used on the method.
            protected VarSymbol varSym;
            // Symbol used when accessing the variable.
            protected VarSymbol proxyVarSym;
            // Name of var field.
            protected Name varName;
            // Real type of the var.
            protected Type type;
            // Is a sequence type.
            protected boolean isSequence;
            
            MixinMethodBuilder(Name methodName, Type returnType, VarInfo varInfo) {
                super(methodName, returnType);
                this.varInfo = varInfo;
                this.varSym = varInfo.getSymbol();
                this.proxyVarSym = varInfo.proxyVarSym();
                this.varName = attributeValueName(proxyVarSym);
                this.type = varInfo.getRealType();
                this.isSequence = varInfo.isSequence();
            }
        }
        
        //
        // This class is designed to generate a method whose body is switched
        // on var offsets.
        //
        public class VarCaseMethodBuilder extends MethodBuilder {
            protected List<VarInfo> attrInfos =  List.<VarInfo>nil();
            protected int varCount = 0;
            protected VarInfo varInfo;
        
            VarCaseMethodBuilder(Name methodName, Type returnType, List<VarInfo> attrInfos, int varCount) {
                super(methodName, returnType);
                this.attrInfos = attrInfos;
                this.varCount = varCount;
                addParam(varNumArg());
            }
                        
            // Specialized body the handles offset cases.
            @Override
            public void body() {
                if (varCount != 0) {
                    // Prepare to accumulate cases.
                    ListBuffer<JCCase> cases = ListBuffer.lb();
                    // Iterate thru each var.
                    for (VarInfo ai : attrInfos) {
                        // Set to the var position.
                        setDiagPos(ai.pos());
                         
                        // Constrain the var.
                        if (ai.needsCloning() && !ai.isOverride()) {
                            // Construct the case.
                            beginBlock();
                            
                            // Generate statements.
                            varInfo = ai;
                            statements();
    
                            // case tag number
                            JCExpression tag = Int(ai.getEnumeration() - varCount);
    
                            // Add the case, something like:
                            // case i: return get$var();
                            cases.append(m().Case(tag, endBlockAsList()));
                        }
                    }
                    
                    // Add statement if there were some cases.
                    if (cases.nonEmpty()) {
                        // varNum - VCNT$
                        JCExpression tagExpr = MINUS(varNumArg(), id(defs.count_FXObjectFieldName));
                        // Construct and add: switch(varNum - VCNT$) { ... }
                        addStmt(m().Switch(tagExpr, cases.toList()));
                    }
                    
                    // Call the super version.
                    callSuper();
                    
                    // Control build.
                    buildIf(stmts.nonEmpty());
                }
            }
        }
        
        //
        // This method returns true if there is a default statement for a given var.
        //
        public boolean hasDefaultInitStatement(VarInfo varInfo) {
            return varInfo.getDefaultInitStatement() != null ||
                   (varInfo.onReplaceAsInline() != null && (varInfo.hasBoundDefinition() || !varInfo.isOverride()));
        }
        

        //
        // This method returns the default statement for a given var.
        //
        public JCStatement getDefaultInitStatement(VarInfo varInfo) {
            JCStatement init = varInfo.getDefaultInitStatement();
            
            if (init == null || varInfo.hasBoundDefinition()) {
                VarSymbol varSym = varInfo.getSymbol();
                
                // If we need to prime the on replace trigger.
                if (varInfo.onReplaceAsInline() != null) {
                    if (varInfo.hasBoundDefinition()) {
                        if (!varInfo.isSequence()) {
                            init = CallStmt(getReceiver(), attributeGetterName(varSym));
                        } else {
                            init = CallStmt(getReceiver(), attributeSizeName(varSym));
                        }
                    } else if(!varInfo.isOverride()) {
                        JCStatement onReplaceCall;
                        if (!varInfo.isSequence()) {
                            onReplaceCall = CallStmt(getReceiver(), attributeOnReplaceName(varSym),
                                            makeMixinSafeVarValue(varSym), makeMixinSafeVarValue(varSym));
                        } else {
                            onReplaceCall = CallStmt(getReceiver(), attributeOnReplaceName(varSym),
                                            Int(0), Int(0), Int(0));
                        }
                        JCStatement flagSet = FlagChangeStmt(varSym, defs.varFlagDEFAULT_APPLIED, defs.varFlagDEFAULT_APPLIED);
                        init = Block(flagSet, onReplaceCall);
                    }
                }
            }

            return init;
        }
        
        //
        // Determine if this override needs an invalidate method
        // Must be in sync with makeInvalidateAccessorMethod
        //
        private boolean needOverrideInvalidateAccessorMethod(VarInfo varInfo) {
            if (varInfo.isMixinVar() || varInfo.onReplace() != null) {
                // based on makeInvalidateAccessorMethod
                return true;
            } else if (varInfo.hasBoundDefinition()) {
                return false;
            } else {
                if (varInfo instanceof TranslatedVarInfoBase) {
                    return ((TranslatedVarInfoBase) varInfo).boundBinders().size() != 0;
                } else {
                    return false;
                }
            }
        }

        //
        // This method returns the correct expression for accessing a value depending if in a mixin or not.
        //
        private JCExpression makeMixinSafeVarValue(VarSymbol varSym) {
           if (isMixinClass() && JavafxAnalyzeClass.isMixinClass(varSym.owner)) {
               return Call(getReceiver(varSym), attributeGetMixinName(varSym));
           } else if (varSym.isStatic()) {
               return Select(makeType(varSym.owner.type, false), attributeValueName(varSym));
           }
           
           return id(attributeValueName(varSym));
        }

                
        //-----------------------------------------------------------------------------------------------------------------------------
        //
        // Sequence var accessors.
        //
        
        //
        // This method constructs the getter method for a sequence attribute.
        //
        private void makeSeqGetterAccessorMethod(VarInfo varInfo, boolean needsBody) {
            VarAccessorMethodBuilder vamb = new VarAccessorMethodBuilder(attributeGetterName(varInfo.getSymbol()),
                                                                         varInfo.getRealType(),
                                                                         varInfo, needsBody) {
                @Override
                public void statements() {
                    JCStatement initIf = null;
                    if (!varInfo.isStatic()) {
                        // Prepare to accumulate body of if.
                        beginBlock();

                        // applyDefaults$(VOFF$var)
                        addStmt(CallStmt(getReceiver(), defs.applyDefaults_FXObjectMethodName, Offset(varSym)));

                        // Is it uninitialized (and not bound)
                        JCExpression initCondition = FlagTest(proxyVarSym, defs.varFlagIS_BOUND_DEFAULT_APPLIED, null);

                        // if (uninitialized) { applyDefaults$(VOFF$var); }
                        initIf = If(initCondition, endBlock(), null);
                    }

                    if (isBoundFuncClass && ((varInfo.getFlags() & Flags.PARAMETER) != 0L)) {
                            // Prepare to accumulate body of if.
                            beginBlock();

                            /*
                             * if "foo" is the variable name, then we generate
                             *
                             *     be$(varNum, $$boundInstance$foo.get($$boundVarNum$foo));
                             *
                             * With be$(int, Object), we need not worry about type conversion.
                             */
                            JCExpression get$call = Call(
                                    id(boundFunctionObjectParamName(varSym.name)),
                                    defs.get_FXObjectMethodName,
                                    id(boundFunctionVarNumParamName(varSym.name)));

                            addStmt(CallStmt(getReceiver(),
                                    defs.be_AttributeMethodPrefixName,
                                    Offset(varSym), get$call));

                            // Is it invalid?
                            JCExpression condition = FlagTest(proxyVarSym, defs.varFlagIS_BOUND_INVALID, defs.varFlagIS_BOUND_INVALID);

                            // if (invalid) { set$var(init/bound expression); }
                            addStmt(If(condition, endBlock(), initIf));
                    } else if (varInfo.isMixinVar()) {
                        assert false : "Mixin sequences not implemented";
                    } else if (varInfo.hasBoundDefinition()) {  
                        // Begin if block.
                        beginBlock();

                        // Be sure the sequence is initialized before returning the SequenceRef -- call the size accessor to initialize
                        addStmt(CallStmt(getReceiver(), attributeSizeName(varSym)));
                        
                        // seq$ = new SequenceRef(<<typeinfo T>>, this, VOFF$seq);
                        JCExpression receiver = getReceiverOrThis(proxyVarSym);

                        List<JCExpression> args = List.<JCExpression>of(TypeInfo(diagPos, elementType), receiver, Offset(varSym));
                        JCExpression newExpr = m().NewClass(null, null, makeType(types.erasure(syms.javafx_SequenceRefType)), args, null);
                        addStmt(Stmt(m().Assign(id(varName), newExpr)));
                        
                        // If (seq$ == null) { seq$ = new SequenceRef(<<typeinfo T>>, this, VOFF$seq); }
                        addStmt(If(EQnull(id(varName)),
                                endBlock()));
                    } else {
                        addStmt(initIf);
                    }
                    
                    // Construct and add: return $var;
                    addStmt(Return(id(varName)));
                }
            };

            vamb.build();
        }
        
        //
        // This method constructs the get position method for a sequence attribute.
        //
        private void makeSeqGetPosAccessorMethod(VarInfo varInfo, boolean needsBody) {
            VarAccessorMethodBuilder vamb = new VarAccessorMethodBuilder(attributeGetterName(varInfo.getSymbol()),
                                                                         varInfo.getElementType(),
                                                                         varInfo, needsBody) {
                @Override
                public void initialize() {
                    addParam(posArg());
                }
                
                @Override
                public void statements() {
                    if (varInfo.isMixinVar()) {
                        TODO("mixin sequences");
                    } else if (varInfo.hasBoundDefinition()) {
                        if (isBoundFuncClass && ((varInfo.getFlags() & Flags.PARAMETER) != 0L)) {
                            JCExpression apply = Call(
                                    id(boundFunctionObjectParamName(varSym.name)),
                                    defs.getElement_FXObjectMethodName,
                                    id(boundFunctionVarNumParamName(varSym.name)),
                                    posArg());
                            addStmt(Return(castFromObject(apply, varInfo.getElementType())));
                        } else {
                            addStmt(varInfo.boundElementGetter());
                        }
                    } else {
                        // Construct and add: return $var.get(pos$);
                        addStmt(Return(Call(id(varName), defs.get_SequenceMethodName, posArg())));
                    }
                }
            };

            vamb.build();
        }

        
        //
        // This method constructs the getter method for a sequence attribute.
        //
        private void makeSeqGetSizeAccessorMethod(VarInfo varInfo, boolean needsBody) {
            VarAccessorMethodBuilder vamb = new VarAccessorMethodBuilder(attributeSizeName(varInfo.getSymbol()),
                                                                         syms.intType,
                                                                         varInfo, needsBody) {
                @Override
                public void statements() {
                    if (varInfo.isMixinVar()) {
                        assert false : "Mixin sequences not implemented";
                    } else if (varInfo.hasBoundDefinition()) {
                        if (isBoundFuncClass && ((varInfo.getFlags() & Flags.PARAMETER) != 0L)) {
                            JCExpression apply = Call(
                                    id(boundFunctionObjectParamName(varSym.name)),
                                    defs.size_FXObjectMethodName,
                                    id(boundFunctionVarNumParamName(varSym.name)));
                            addStmt(Return(apply));
                        } else {
                            addStmt(varInfo.boundSizeGetter());
                        }
                    } else {
                        // Construct and add: return $var.size();
                        addStmt(Return(Call(id(varName), defs.size_SequenceMethodName)));
                    }
                }
            };

            vamb.build();
        }

        //
        // This method constructs the be method for a sequence attribute.
        //
        private void makeSeqBeAccessorMethod(VarInfo varInfo, boolean needsBody) {
            VarAccessorMethodBuilder vamb = new VarAccessorMethodBuilder(attributeBeName(varInfo.getSymbol()),
                                                                         varInfo.getRealType(),
                                                                         varInfo, needsBody) {
                @Override
                public void initialize() {
                    addParam(type, defs.varNewValue_ArgName);
                }
                
                @Override
                public void statements() {
                    // FIXME - Do the right thing.
                    // T varOldValue$ = $var;
                    addStmt(Var(Flags.FINAL, type, defs.varOldValue_LocalVarName, id(varName)));
    
                    // $var = value
                    addStmt(Stmt(m().Assign(id(varName), id(defs.varNewValue_ArgName))));
    
                    // return $var;
                    addStmt(Return(id(varName)));
                }
            };

            vamb.build();
        }

        //
        // This method constructs the invalidate method for a sequence attribute.
        //
        private void makeSeqInvalidateAccessorMethod(VarInfo varInfo, boolean needsBody) {
            VarAccessorMethodBuilder vamb = new VarAccessorMethodBuilder(attributeInvalidateName(varInfo.getSymbol()),
                                                                         syms.voidType,
                                                                         varInfo, needsBody) {
                @Override
                public void initialize() {
                    addParam(startPosArg());
                    addParam(endPosArg());
                    addParam(newLengthArg());
                    addParam(phaseArg());
                }
                                                                         
                @Override
                public void statements() {
                    // Handle invalidators if present.
                    List<BindeeInvalidator> invalidatees = varInfo.boundInvalidatees();
                    boolean hasInvalidators = !invalidatees.isEmpty();
                     
                    if (hasInvalidators) {
                        // Insert invalidators.
                        for (BindeeInvalidator invalidator : invalidatees) {
                            addStmt(invalidator.invalidator);
                        }
                    }

                    if (varInfo.isOverride()) {
                        // Call super first.
                        callSuper();
                    }

                    // Mixin invalidate$
                    if (!isMixinClass() && varInfo.isMixinVar()) {
                        // Mixin.invalidate$var(this, phase$);
                        callMixin((ClassSymbol)varSym.owner);
                    }
                    
                    // Add on-invalidate trigger if any
                    if (varInfo.onInvalidate() != null) {
                        addStmt(varInfo.onInvalidateAsInline());
                    }

                    // Hack for bug in boundBinders() which includes invalidators
                    if (!hasInvalidators) for (VarInfo otherVar : varInfo.boundBinders()) {
                        // invalidate$var(phase$);
                        if (!otherVar.isSequence()) {
                            addStmt(CallStmt(getReceiver(), attributeInvalidateName(otherVar.getSymbol()), phaseArg()));
                        } else {
                            addStmt(CallStmt(getReceiver(), attributeInvalidateName(otherVar.getSymbol()),
                                             startPosArg(),
                                             endPosArg(),
                                             newLengthArg(),
                                             phaseArg()));
                        }
                    }
                    
                    // Invalidate back to inverse.
                    if (varInfo.hasBoundDefinition() && varInfo.hasBiDiBoundDefinition()) {
                        for (VarSymbol bindeeSym : varInfo.boundBindees()) {
                            if (!types.isSequence(bindeeSym.type)) {
                                addStmt(CallStmt(getReceiver(), attributeInvalidateName(bindeeSym), phaseArg()));
                            } else {
                                addStmt(CallStmt(getReceiver(bindeeSym), attributeInvalidateName(bindeeSym),
                                                 startPosArg(),
                                                 endPosArg(),
                                                 newLengthArg(),
                                                 phaseArg()));
                            }
                            // rest are duplicates.
                            break;
                        }
                    }
                    
                    boolean isSuperVarInfo = varInfo instanceof SuperClassVarInfo;

                    if (isSuperVarInfo) {
                        callSuper();
                    } else if (!varInfo.isOverride()) {
                        // notifyDependents(VOFF$var, phase$);
                        addStmt(CallStmt(getReceiver(varInfo), defs.notifyDependents_FXObjectMethodName, Offset(proxyVarSym),
                                startPosArg(), endPosArg(), newLengthArg(),
                                phaseArg()));
                    } 

                    //TODO: remove this -- currently disabled
                    // Doing this save violates first phase invalidation safety by making calls to size and element getter.
                    // Saving away of a SequenceRef should now be avoided, so this shouldn't be needed
                    if (false && varInfo.hasBoundDefinition()) {
                        // Begin seq save block.
                        beginBlock();
                        // seq$.save()
                        addStmt(CallStmt(defs.SequencesRef_save, id(varName)));
                        // seq$ = null;
                        addStmt(Stmt(m().Assign(id(varName), Null())));
                        // If (seq$ != null) { seq$.save(); seq$ = null; }
                        addStmt(If(NEnull(id(varName)),
                                endBlock()));
                    }
                    
                    if (varInfo.onReplace() != null) {
                        // Begin the get$ block.
                        beginBlock();
                        
                        // Call the onReplace$var to force evaluation.
                        addStmt(CallStmt(getReceiver(), attributeOnReplaceName(proxyVarSym),
                                                        startPosArg(),
                                                        endPosArg(),
                                                        newLengthArg()));
                            
                        // phase$ == VFLGS$NEEDS_TRIGGER
                        JCExpression ifTriggerPhase = EQ(phaseArg(), id(defs.varFlagNEEDS_TRIGGER));
                       
                        // if (phase$ == VFLGS$NEEDS_TRIGGER) { get$var(); }
                        addStmt(If(ifTriggerPhase,
                                endBlock()));
                    }
                }
            };

            vamb.build();
        }

        //
        // This method constructs the onreplace$ method for a sequence attribute.
        //
        private void makeSeqOnReplaceAccessorMethod(VarInfo varInfo, boolean needsBody) {
            VarAccessorMethodBuilder vamb = new VarAccessorMethodBuilder(attributeOnReplaceName(varInfo.getSymbol()),
                                                                         syms.voidType,
                                                                         varInfo, needsBody) {
                Name oldValueName;
                Name newValueName;
                Name firstIndexName;
                Name lastIndexName;
                Name newElementsName;
                Name newLengthName;
                Name savedName;

                @Override
                public void initialize() {
                    JFXOnReplace onReplace = varInfo.onReplace();
                    
                    newValueName = defs.varNewValue_ArgName;
                    firstIndexName = paramStartPosName(onReplace);
                    lastIndexName = paramEndPosName(onReplace);
                    newLengthName = paramNewElementsLengthName(onReplace);

                    addParam(syms.intType, firstIndexName);
                    addParam(syms.intType, lastIndexName);
                    addParam(syms.intType, newLengthName);
                    if (onReplace != null && onReplace.getOldValue() != null) {
                        savedName = attributeSavedName(varInfo.getSymbol());
                        addDefinition(Var(varInfo.isStatic() ? Flags.STATIC : 0, type, savedName,
                                accessEmptySequence(diagPos, varInfo.getElementType())));
                    }
                }
                
                @Override
                public void statements() {
                    // Forward to the mixin.
                    // Call super first.
                    if (varInfo.isOverride()) {
                        callSuper();
                    }

                    // Fetch the on replace statement or null.
                    JFXOnReplace onReplace = varInfo.onReplace();
    
                    if (!isMixinClass() && varInfo.isMixinVar()) {
                        // Mixin.onReplace$var(this, oldValue, newValue);
                        callMixin((ClassSymbol)varSym.owner);
                    }
                    if (!isMixinClass() && onReplace != null && !varInfo.isStatic()) {
                        addStmt(Var(Flags.FINAL, id(interfaceName(getCurrentClassDecl())), defs.receiverName, id(names._this)));
                    }

                    if (onReplace != null) {
                        JFXVar lastIndex = varInfo.onReplace().getLastIndex();
                        JFXVar newElements = varInfo.onReplace().getNewElements();
                        if (lastIndex != null && varInfo.onReplace().getEndKind() == JFXSequenceSlice.END_INCLUSIVE) {
                            addStmt(Var(syms.intType, lastIndex.name,
                                    MINUS(endPosArg(), Int(1))));
                        }
                        if (savedName != null) {
                            addStmt(Var(type, onReplace.getOldValue().getName(), id(savedName)));
                            addStmt(Stmt(m().Assign(id(savedName),
                                    Call(getReceiver(), attributeGetterName(varInfo.getSymbol())))));
                            addStmt(CallStmt(id(savedName), defs.incrementSharing_SequenceMethodName));
                        }
                        if (newElements != null
                                && (newElements.sym.flags_field & JavafxFlags.VARUSE_OPT_TRIGGER) == 0) {
                                   JCExpression seq = savedName != null ? id(savedName)
                                           : Call(getReceiver(), attributeGetterName(varInfo.getSymbol()));
                                   JCExpression init = Call(defs.Sequences_getNewElements,
                                           seq, id(firstIndexName), id(newLengthName));
                            addStmt(Var(newElements.type, newElements.name, init));
                        }
                    }
                    
                    // Need to capture init state if has trigger.
                    if (onReplace != null) {
                        // Insert the trigger.
                        addStmt(varInfo.onReplaceAsInline());
                    }
                }
            };

            vamb.build();
        }

        //-----------------------------------------------------------------------------------------------------------------------------
        //
        // Normal var accessors.
        //
        
        //
        // This method constructs the getter method for the specified attribute.
        //
        private void makeGetterAccessorMethod(VarInfo varInfo, boolean needsBody) {
            VarAccessorMethodBuilder vamb = new VarAccessorMethodBuilder(attributeGetterName(varInfo.getSymbol()),
                                                                         varInfo.getRealType(),
                                                                         varInfo, needsBody) {
                @Override
                public void statements() {
                    if (varInfo.isBareSynth()) {
                        // return bound-expression
                        addStmts(varInfo.boundPreface());
                        addStmt(Return(varInfo.boundInit()));
                    } else {
                        JCStatement initIf = null;
                        if (!varInfo.isStatic()) {
                            // Prepare to accumulate body of if.
                            beginBlock();
    
                                // applyDefaults$(VOFF$var)
                            addStmt(CallStmt(getReceiver(), defs.applyDefaults_FXObjectMethodName, Offset(varSym)));
    
                            // Is it uninitialized (and not bound)
                            JCExpression initCondition = FlagTest(proxyVarSym, defs.varFlagIS_BOUND_DEFAULT_APPLIED, null);
    
                            // if (uninitialized) { applyDefaults$(VOFF$var); }
                            initIf = If(initCondition,
                                    endBlock());
                        }

                        if (isBoundFuncClass && ((varInfo.getFlags() & Flags.PARAMETER) != 0L)) {
                            // Prepare to accumulate body of if.
                            beginBlock();

                            /*
                             * if "foo" is the variable name, then we generate
                             *
                             *     be$(varNum, $$boundInstance$foo.get($$boundVarNum$foo));
                             *
                             * With be$(int, Object), we need not worry about type conversion.
                             */
                            JCExpression get$call = Call(
                                    id(boundFunctionObjectParamName(varSym.name)),
                                    defs.get_FXObjectMethodName,
                                    id(boundFunctionVarNumParamName(varSym.name)));

                            addStmt(CallStmt(getReceiver(),
                                    defs.be_AttributeMethodPrefixName,
                                    Offset(varSym),
                                    get$call));

                            // Is it invalid?
                            JCExpression condition = FlagTest(proxyVarSym, defs.varFlagIS_BOUND_INVALID, defs.varFlagIS_BOUND_INVALID);

                            // if (invalid) { set$var(init/bound expression); }
                            addStmt(If(condition, 
                                    endBlock(),
                                    initIf));

                        } else if (varInfo.hasBoundDefinition() || varInfo.isMixinVar()) {
                            // Prepare to accumulate body of if.
                            beginBlock();
                            
                            // Set to new value.
                            if (varInfo.isMixinVar()) {
                                // Mixin.evaluate$var(this);
                                addStmt(makeSuperCall((ClassSymbol)varSym.owner, attributeEvaluateName(varSym), id(names._this)));
                                // Make valid.
                                addStmt(FlagChangeStmt(proxyVarSym, defs.varFlagVALIDITY_FLAGS, null));
                            } else {
                                assert varInfo.boundInit() != null : "Oops! No boundInit.  varInfo = " + varInfo + ", preface = " + varInfo.boundPreface();

                                // set$var(init/bound expression)
                                addStmts(varInfo.boundPreface());
                                JCExpression initValue = varInfo.boundInit();
                                if (varInfo.isInitWithBoundFuncResult()) {
                                    // We have a Pointer - need to do Pointer.get() and cast the result
                                    initValue = castFromObject(Call(initValue, defs.get_PointerMethodName), varSym.type);
                                }
                                addStmt(CallStmt(getReceiver(), attributeBeName(varSym), initValue));
                            }
                          
                            // Is it bound and invalid?
                            JCExpression condition = FlagTest(proxyVarSym, defs.varFlagIS_BOUND_INVALID, defs.varFlagIS_BOUND_INVALID);
                            
                            // if (bound and invalid) { set$var(init/bound expression); }
                            addStmt(If(condition, 
                                    endBlock(),
                                    initIf));
                        } else {
                            addStmt(initIf);
                        }
    
                        // Construct and add: return $var;
                        addStmt(Return(id(varName)));
                    }
                }
            };

            vamb.build();
        }

        //
        // This method constructs the setter method for the specified attribute.
        //
        private void makeSetterAccessorMethod(VarInfo varInfo, boolean needsBody) {
            VarAccessorMethodBuilder vamb = new VarAccessorMethodBuilder(attributeSetterName(varInfo.getSymbol()),
                                                                         varInfo.getRealType(),
                                                                         varInfo, needsBody) {
                @Override
                public void initialize() {
                    addParam(type, defs.varNewValue_ArgName);
                    buildIf(!varInfo.isDef() && !varInfo.isBareSynth());
                }

                @Override
                public void statements() {
                    // Restrict setting.
                    addStmt(CallStmt(getReceiver(varSym), defs.varFlagRestrictSet, Offset(varSym)));

                    addStmt(FlagChangeStmt(varSym, null, defs.varFlagIS_INITIALIZED));

                    if (varInfo.hasBoundDefinition() && varInfo.hasBiDiBoundDefinition()) {
                        // Begin bidi block.
                        beginBlock();
                        // Preface to setter.
                        addStmts(varInfo.boundInvSetterPreface());
                        // Test to see if bound.
                        JCExpression ifBoundTest = FlagTest(varSym, defs.varFlagIS_BOUND, defs.varFlagIS_BOUND);
                        // if (!isBound$(VOFF$var)) { set$other(inv bound expression); }
                        addStmt(If(ifBoundTest,
                                endBlock()));
                    }
                    
                    // be$var(value)
                    addStmt(CallStmt(getReceiver(), attributeBeName(varSym), id(defs.varNewValue_ArgName)));
                    // return $var;
                    addStmt(Return(id(varName)));
                }
            };

            vamb.build();
        }

        //
        // This method constructs the be method for the specified attribute.
        //
        private void makeBeAccessorMethod(VarInfo varInfo, boolean needsBody) {
            VarAccessorMethodBuilder vamb = new VarAccessorMethodBuilder(attributeBeName(varInfo.getSymbol()),
                                                                         varInfo.getRealType(),
                                                                         varInfo, needsBody) {
                @Override
                public void initialize() {
                    addParam(type, defs.varNewValue_ArgName);
                    buildIf(!varInfo.isBareSynth());
                }
                
                @Override
                public void statements() {
                    // T varOldValue$ = $var;
                    addStmt(Var(Flags.FINAL, type, defs.varOldValue_LocalVarName, id(varName)));
    
                    // Prepare to accumulate then statements.
                    beginBlock();
    
                    // setValid(VFLGS$IS_INVALID);
                    addStmt(FlagChangeStmt(proxyVarSym, null, defs.varFlagDEFAULT_APPLIED));

                    // invalidate$(VFLGS$IS_INVALID)
                    addStmt(CallStmt(getReceiver(), attributeInvalidateName(varSym), id(defs.varFlagIS_INVALID)));
    
                    // $var = value
                    addStmt(Stmt(m().Assign(id(varName), id(defs.varNewValue_ArgName))));
    
                    // setValid(VFLGS$IS_INVALID);
                    addStmt(FlagChangeStmt(proxyVarSym, defs.varFlagIS_INVALID, null));

                    // invalidate$(VFLGS$NEEDS_TRIGGER)
                    addStmt(CallStmt(getReceiver(), attributeInvalidateName(varSym), id(defs.varFlagNEEDS_TRIGGER)));

                    // setValid(VFLGS$NEEDS_TRIGGER); and set as initialized;
                    addStmt(FlagChangeStmt(proxyVarSym, defs.varFlagNEEDS_TRIGGER, null));
    
                    // onReplace$(varOldValue$, varNewValue$)
                    addStmt(CallStmt(getReceiver(), attributeOnReplaceName(varSym), id(defs.varOldValue_LocalVarName), id(defs.varNewValue_ArgName)));
    
                    // varOldValue$ != varNewValue$
                    // or !varOldValue$.isEquals(varNewValue$) test for Objects and Sequences
                    JCExpression testExpr = type.isPrimitive() ?
                        NE(id(defs.varOldValue_LocalVarName), id(defs.varNewValue_ArgName))
                      : NOT(Call(defs.Util_isEqual, id(defs.varOldValue_LocalVarName), id(defs.varNewValue_ArgName)));
                    testExpr = OR(testExpr, FlagTest(proxyVarSym, defs.varFlagDEFAULT_APPLIED, null));
                    
                    // End of then block.
                    JCBlock thenBlock = endBlock();
    
                    // Prepare to accumulate else statements.
                    beginBlock();
    
                    // setValid(VFLGS$VALIDITY_FLAGS);
                    addStmt(FlagChangeStmt(proxyVarSym, defs.varFlagVALIDITY_FLAGS, defs.varFlagDEFAULT_APPLIED));
                    
                    // End of else block.
                    JCBlock elseBlock = endBlock();
    
                    // if (varOldValue$ != varNewValue$) { handle change }
                    addStmt(If(testExpr, 
                            thenBlock,
                            elseBlock));
   
                    // return $var;
                    addStmt(Return(id(varName)));
                }
            };

            vamb.build();
        }

        //
        // This method constructs the invalidate method for the specified attribute.
        //
        private void makeInvalidateAccessorMethod(VarInfo varInfo, boolean needsBody) {
            VarAccessorMethodBuilder vamb = new VarAccessorMethodBuilder(attributeInvalidateName(varInfo.getSymbol()),
                                                                         syms.voidType,
                                                                         varInfo, needsBody) {
                @Override
                public void initialize() {
                    addParam(phaseArg());
                }
                                                                         
                @Override
                public void statements() {
                    // Handle invalidators if present.
                    List<BindeeInvalidator> invalidatees = varInfo.boundInvalidatees();
                    boolean hasInvalidators = !invalidatees.isEmpty();
                     
                    if (hasInvalidators) {
                        // Insert invalidators.
                        for (BindeeInvalidator invalidator : invalidatees) {
                            addStmt(invalidator.invalidator);
                        }
                        
                        return;
                    }
                    
                    // Prepare to accumulate if statements.
                    beginBlock();
    
                    if (varInfo.isOverride()) {
                        // Call super first.
                        callSuper();
                    }

                    // Mixin invalidate$
                    if (!isMixinClass() && varInfo.isMixinVar()) {
                        // Mixin.invalidate$var(this, phase$);
                        callMixin((ClassSymbol)varSym.owner);
                    }

                    // Add on-invalidate trigger if any
                    if (varInfo.onInvalidate() != null) {
                        addStmt(varInfo.onInvalidateAsInline());
                    }
                    
                    for (VarInfo otherVar : varInfo.boundBinders()) {
                        // invalidate$var(phase$);
                        if (!otherVar.isSequence()) {
                            addStmt(CallStmt(getReceiver(), attributeInvalidateName(otherVar.getSymbol()), phaseArg()));
                        }
                    }
                    
                    // Invalidate back to inverse.
                    if (varInfo.hasBoundDefinition() && varInfo.hasBiDiBoundDefinition()) {
                        for (VarSymbol bindeeSym : varInfo.boundBindees()) {
                            addStmt(CallStmt(getReceiver(), attributeInvalidateName(bindeeSym), phaseArg()));
                            break;
                        }
                    }
                    
                    boolean isSuperVarInfo = varInfo instanceof SuperClassVarInfo;

                    if (isSuperVarInfo) {
                        callSuper();
                    } else if (!varInfo.isOverride()) {
                        // notifyDependents(VOFF$var, phase$);
                        addStmt(CallStmt(getReceiver(varInfo), defs.notifyDependents_FXObjectMethodName, Offset(proxyVarSym), phaseArg()));
                    } 
                    
                    // isValid
                    JCExpression ifValidTest;
                    if (isSuperVarInfo || varInfo.isOverride()) {
                        ifValidTest = FlagTest(proxyVarSym, phaseArg(), phaseArg());
                    } else {
                        ifValidTest = FlagChange(proxyVarSym, null, phaseArg());
                    }
                    
                    // if (!isValidValue$(VOFF$var)) { ... invalidate  code ... }
                    addStmt( If (NOT(ifValidTest),
                            endBlock()));
                    
                    if (varInfo.onReplace() != null) {
                        // Begin the get$ block.
                        beginBlock();
                        
                        // Call the get$var to force evaluation.
                        addStmt(CallStmt(getReceiver(), attributeGetterName(proxyVarSym)));
                            
                        // phase$ == VFLGS$NEEDS_TRIGGER
                        JCExpression ifTriggerPhase = EQ(phaseArg(), id(defs.varFlagNEEDS_TRIGGER));
                       
                        // if (phase$ == VFLGS$NEEDS_TRIGGER) { get$var(); }
                        addStmt(If(ifTriggerPhase,
                                endBlock()));
                    }
                }
            };

            vamb.build();
        }

        //
        // This method constructs the onreplace$ method for the specified attribute.
        //
        private void makeOnReplaceAccessorMethod(VarInfo varInfo, boolean needsBody) {
            VarAccessorMethodBuilder vamb = new VarAccessorMethodBuilder(attributeOnReplaceName(varInfo.getSymbol()),
                                                                         syms.voidType,
                                                                         varInfo, needsBody) {
                Name oldValueName;
                Name newValueName;

                @Override
                public void initialize() {
                    JFXOnReplace onReplace = varInfo.onReplace();
                    
                    oldValueName = paramOldValueName(onReplace);
                    newValueName = paramNewValueName(onReplace);
                    
                    addParam(type, oldValueName);
                    addParam(type, newValueName);
                    
                    buildIf(!varInfo.isBareSynth());
                }
                
                @Override
                public void statements() {
                    // Call super first.
                    if (varInfo.isOverride()) {
                        callSuper();
                    }

                    // Fetch the on replace statement or null.
                    JCStatement onReplace = varInfo.onReplaceAsInline();
    
                    // Forward to the mixin.
                    if (!isMixinClass() && varInfo.isMixinVar()) {
                        // Mixin.onReplace$var(this, oldValue, newValue);
                        callMixin((ClassSymbol)varSym.owner);
                    }
                    if (!isMixinClass() && onReplace != null && !varInfo.isStatic()) {
                        addStmt(Var(Flags.FINAL, id(interfaceName(getCurrentClassDecl())), defs.receiverName, id(names._this)));
                    }
                    
                    // Need to capture init state if has trigger.
                    if (onReplace != null) {
                        // Insert the trigger.
                        addStmt(onReplace);
                    }
                }
            };

            vamb.build();
        }

        //-----------------------------------------------------------------------------------------------------------------------------
        //
        // Mixin var accessors.
        //
        
        //
        // This method constructs a getMixin$ method.
        //
        private void makeGetMixinAccessorMethod(VarInfo varInfo, boolean needsBody) {
            VarAccessorMethodBuilder vamb = new VarAccessorMethodBuilder(attributeGetMixinName(varInfo.getSymbol()),
                                                                         varInfo.getRealType(),
                                                                         varInfo, needsBody) {
                @Override
                public void statements() {
                    // Construct and add: return $var;
                    addStmt(Return(id(varName)));
                }
            };
             
            vamb.build();
        }

        //
        // This method constructs a getVOFF$ method.
        //
        private void makeGetVOFFAccessorMethod(VarInfo varInfo, boolean needsBody) {
            VarAccessorMethodBuilder vamb = new VarAccessorMethodBuilder(attributeGetVOFFName(varInfo.getSymbol()),
                                                                         syms.intType,
                                                                         varInfo, needsBody) {
                @Override
                public void statements() {
                    addStmt(Return(id(attributeOffsetName(proxyVarSym))));
                }
            };
             
            vamb.build();
        }
        
        //
        // This method constructs a setMixin$ method.
        //
        private void makeSetMixinAccessorMethod(VarInfo varInfo, boolean needsBody) {
            VarAccessorMethodBuilder vamb = new VarAccessorMethodBuilder(attributeGetMixinName(varInfo.getSymbol()),
                                                                         varInfo.getRealType(),
                                                                         varInfo, needsBody) {
                @Override
                public void initialize() {
                    addParam(type, defs.varNewValue_ArgName);
                }
                
                @Override
                public void statements() {
                    // Construct and add: return $var;
                    addStmt(Return(m().Assign(id(varName), id(defs.varNewValue_ArgName))));
                }
            };
             
            vamb.build();
        }
        
        //
        // This method constructs a mixin applyDefault method.
        //
        private void makeMixinApplyDefaultsMethod(VarInfo varInfo) {
            MixinMethodBuilder mmb = new MixinMethodBuilder(attributeApplyDefaultsName(varInfo.getSymbol()), syms.voidType, varInfo) {
                @Override
                public void initialize() {
                    buildIf(isCurrentClassSymbol(varSym.owner) || hasDefaultInitStatement(varInfo));
                }                

                @Override
                public void statements() {
                    if (!varInfo.isBareSynth()) {
                        // Get body of applyDefaults$.
                        addStmt(makeApplyDefaultsStatement(varInfo, true));
                    }
                }
            };
             
            mmb.build();
        }

        //
        // This method constructs a mixin initVarBits method.
        //
        private void makeMixinInitVarBitsMethod(VarInfo varInfo) {
            MixinMethodBuilder mmb = new MixinMethodBuilder(attributeInitVarBitsName(varInfo.getSymbol()), syms.intType, varInfo) {
                @Override
                public void initialize() {
                    buildIf(isCurrentClassSymbol(varSym.owner));
                }                

                @Override
                public void statements() {
                    boolean isBound = varInfo.hasBoundDefinition();
                    boolean isReadonly = varInfo.isDef() || (isBound && !varInfo.hasBiDiBoundDefinition());
                    Name setBits = null;
                    
                    if (isReadonly && isBound) {
                        setBits = defs.varFlagINIT_BOUND_READONLY;
                    } else if (isReadonly) {
                        setBits = defs.varFlagINIT_READONLY;
                    } else if (isBound) {
                        setBits = defs.varFlagINIT_BOUND;
                    }
                    
                    if (setBits != null) {
                        addStmt(Return(id(setBits)));
                    } else if (!varInfo.isOverride() || hasDefaultInitStatement(varInfo)) {
                        addStmt(Return(id(defs.varFlagINIT_NORMAL)));
                    } else {
                        buildIf(false);
                    }
                }
            };
             
            mmb.build();
        }

        //
        // This method constructs a mixin evaluate method.
        //
        private void makeMixinEvaluateAccessorMethod(VarInfo varInfo) {
            MixinMethodBuilder mmb = new MixinMethodBuilder(attributeEvaluateName(varInfo.getSymbol()), syms.voidType, varInfo) {
                @Override
                public void initialize() {
                    buildIf(isCurrentClassSymbol(varSym.owner) || varInfo.hasBoundDefinition());
                }                

                @Override
                public void statements() {
                    if (varInfo.hasBoundDefinition() && !varInfo.isBareSynth()) {
                        // set$var(init/bound expression)
                        addStmts(varInfo.boundPreface());
                        addStmt(CallStmt(getReceiver(), attributeBeName(varSym), varInfo.boundInit()));
                    } 
                }
            };
             
            mmb.build();
        }

        //
        // This method constructs a mixin invalidate method.
        //
        private void makeMixinInvalidateAccessorMethod(VarInfo varInfo) {
            MixinMethodBuilder mmb = new MixinMethodBuilder(attributeInvalidateName(varInfo.getSymbol()), syms.voidType, varInfo) {
                @Override
                public void initialize() {
                    if (isSequence) {
                        addParam(startPosArg());
                        addParam(endPosArg());
                        addParam(newLengthArg());
                    }
                    addParam(phaseArg());
                    buildIf(varInfo instanceof TranslatedVarInfoBase);
                }
                                                                         
                @Override
                public void statements() {
                    // Call super first.
                    if (varInfo.isOverride()) {
                       callSuper();
                    }
                    
                    // Invalidate other mixin vars.
                    for (VarInfo otherVar : varInfo.boundBinders()) {
                        // FIXME - do the right thing.
                        if (!otherVar.isSequence()) {
                            addStmt(CallStmt(getReceiver(), attributeInvalidateName(otherVar.getSymbol()), phaseArg()));
                        } else if (isSequence) {
                            addStmt(CallStmt(getReceiver(), attributeInvalidateName(otherVar.getSymbol()),
                                             startPosArg(),
                                             endPosArg(),
                                             newLengthArg(),
                                             phaseArg()));
                        } else {
                            addStmt(CallStmt(getReceiver(), attributeInvalidateName(otherVar.getSymbol()),
                                             Int(-1),
                                             Int(-1),
                                             Int(-1),
                                             phaseArg()));
                        }
                    }
      
                    // Add on-invalidate trigger if any
                    if (varInfo.onInvalidate() != null) {
                        addStmt(varInfo.onInvalidateAsInline());
                    }
                }
            };
             
            mmb.build();
        
        }

        //-----------------------------------------------------------------------------------------------------------------------------
        
        //
        // This method constructs the accessor methods for an attribute.
        //
        public void makeAnAttributeAccessorMethods(VarInfo ai, boolean needsBody) {
            setDiagPos(ai.pos());

            if (ai.useAccessors()) {
                if (ai.isSequence()) {
                     if (!ai.isOverride()) {
                        makeSeqGetterAccessorMethod(ai, needsBody);
                        makeSeqGetPosAccessorMethod(ai, needsBody);
                        makeSeqGetSizeAccessorMethod(ai, needsBody);
                        makeSeqBeAccessorMethod(ai, needsBody);
                        makeSeqInvalidateAccessorMethod(ai, needsBody);
                        makeSeqOnReplaceAccessorMethod(ai, needsBody);
                    } else if (needsBody) {
                        if (ai.hasInitializer()) {
                            // Bound or not, we need getter & setter on override since we
                            // may be switching between bound and non-bound or visa versa
                            makeSeqGetterAccessorMethod(ai, needsBody);
                            makeSeqGetPosAccessorMethod(ai, needsBody);
                            makeSeqGetSizeAccessorMethod(ai, needsBody);
                            makeSeqBeAccessorMethod(ai, needsBody);
                        }
                        if (needOverrideInvalidateAccessorMethod(ai)) {
                            makeSeqInvalidateAccessorMethod(ai, needsBody);
                        }
                        if (ai.onReplace() != null || ai.isMixinVar()) {
                            makeSeqOnReplaceAccessorMethod(ai, needsBody);
                        }
                    }
               } else {
                    if (!ai.isOverride()) {
                        makeGetterAccessorMethod(ai, needsBody);
                        makeSetterAccessorMethod(ai, needsBody);
                        makeBeAccessorMethod(ai, needsBody);
                        makeInvalidateAccessorMethod(ai, needsBody);
                        makeOnReplaceAccessorMethod(ai, needsBody);
                    } else if (needsBody) {
                        if (ai.hasInitializer()) {
                            // Bound or not, we need getter & setter on override since we
                            // may be switching between bound and non-bound or visa versa
                            makeGetterAccessorMethod(ai, needsBody);
                            makeSetterAccessorMethod(ai, needsBody);
                        }
                        if (needOverrideInvalidateAccessorMethod(ai)) {
                            makeInvalidateAccessorMethod(ai, needsBody);
                        }
                        if (ai.onReplace() != null || ai.isMixinVar()) {
                            makeOnReplaceAccessorMethod(ai, needsBody);
                        }
                    }
                }
                
                if (ai.isMixinVar()) {
                    makeGetMixinAccessorMethod(ai, needsBody);
                    makeGetVOFFAccessorMethod(ai, needsBody);
                    makeSetMixinAccessorMethod(ai, needsBody);
                }
            }
        }
        
        //
        // This method constructs the accessor methods for each attribute.
        //
        public void makeAttributeAccessorMethods(List<VarInfo> attrInfos) {
            for (VarInfo ai : attrInfos) {
                // Only create accessors for declared and proxied vars.
                if (ai.needsCloning()) {
                    makeAnAttributeAccessorMethods(ai, true);
                } else {
                    // If a super has binders we need to emit an overriding invalidate$.
                    if (ai.boundBinders().size() != 0) {
                        makeInvalidateAccessorMethod(ai, true);
                    }
                }
            }
        }

        //
        // This method constructs the abstract interfaces for the accessors in
        // a mixin class.
        //
        public void makeMemberVariableAccessorInterfaceMethods() {
            // TranslatedVarInfo for the current class.
            List<TranslatedVarInfo> translatedAttrInfo = analysis.getTranslatedAttrInfo();

            // Only for vars within the class.
            for (VarInfo ai : translatedAttrInfo) {
                if (!ai.isStatic()) {
                    makeAnAttributeAccessorMethods(ai, false);
                }
            }
        }

        //
        // This method constructs mixin accessor methods.
        //
        private void makeMixinAccessorMethods(List<? extends VarInfo> attrInfos) {
            for (VarInfo ai : attrInfos) {
                // Set diagnostic position for attribute.
                setDiagPos(ai.pos());
                
                if (ai.needsCloning()) {
                    makeMixinApplyDefaultsMethod(ai);
                    makeMixinInitVarBitsMethod(ai);
                    makeMixinEvaluateAccessorMethod(ai);
                    makeMixinInvalidateAccessorMethod(ai);
                    makeOnReplaceAccessorMethod(ai, true);
                }
            }
        }

        //
        // This method generates an enumeration for each of the instance attributes
        // of the class.
        //
        public void makeAttributeNumbers(List<VarInfo> attrInfos, int varCount, LiteralInitVarMap varMap) {
            // Reset diagnostic position to current class.
            resetDiagPos();

            // Construct a static count variable (VCNT$), -1 indicates count has not been initialized.
            addDefinition(addSimpleIntVariable(Flags.STATIC | Flags.PUBLIC, defs.count_FXObjectFieldName, -1));

            // Construct a static count accessor method (VCNT$)
            makeVCNT$(attrInfos, varCount);

            // Construct a virtual count accessor method (count$)
            makecount$();

            // Accumulate variable numbering.
            for (VarInfo ai : attrInfos) {
                // Only variables actually declared.
                if (ai.needsCloning() && !ai.isOverride()) {
                    // Set diagnostic position for attribute.
                    setDiagPos(ai.pos());

                    // Construct offset var.
                    Name name = attributeOffsetName(ai.getSymbol());
                    // Construct and add: public static int VOFF$name = n;
                    
                    addDefinition(makeField(Flags.STATIC | Flags.PUBLIC, syms.intType, name, null));
                }

                // Add to var map if an anon class.
                // Exclude the bogus $internal$ fields of FXBase/FXObject
                if (varMap != null && !ai.getSymbol().name.endsWith(defs.internalSuffixName)) {
                    varMap.addVar(ai.getSymbol());
                }
            }
        }

        //
        // The method constructs the VCNT$ method for the current class.
        //
        public void makeVCNT$(final List<VarInfo> attrInfos, final int varCount) {
            StaticMethodBuilder smb = new StaticMethodBuilder(defs.count_FXObjectFieldName, syms.intType) {
                @Override
                public void statements() {
                    // Start if block.
                    beginBlock();
        
                    // VCNT$ = super.VCNT$() + n  or VCNT$ = n;
                    JCExpression setVCNT$Expr = superClassSym == null ?  Int(varCount) :
                                                                         PLUS(
                                                                                    Call(makeType(superClassSym.type), defs.count_FXObjectFieldName),
                                                                                    Int(varCount));
                    Name countName = names.fromString("$count");
                    // final int $count = VCNT$ = super.VCNT$() + n;
                    addStmt(makeField(Flags.FINAL, syms.intType, countName, m().Assign(id(defs.count_FXObjectFieldName), setVCNT$Expr)));
        
                    for (VarInfo ai : attrInfos) {
                        // Only variables actually declared.
                        if (ai.needsCloning() && !ai.isOverride()) {
                            // Set diagnostic position for attribute.
                            setDiagPos(ai.pos());
                            // Offset var name.
                            Name name = attributeOffsetName(ai.getSymbol());
                            // VCNT$ - n + i;
                            JCExpression setVOFF$Expr = PLUS(id(countName), Int(ai.getEnumeration() - varCount));
                            // VOFF$var = VCNT$ - n + i;
                            addStmt(Stmt(m().Assign(id(name), setVOFF$Expr)));
                        }
                    }
        
                    // VCNT$ == -1
                    JCExpression condition = EQ(id(defs.count_FXObjectFieldName), Int(-1));
                    // if (VCNT$ == -1) { ...
                    addStmt(If(condition,
                            endBlock()));
                    // return VCNT$;
                    addStmt(Return(id(defs.count_FXObjectFieldName)));
                }
            };
            
            smb.build();
        }

        //
        // The method constructs the count$ method for the current class.
        //
        public void makecount$() {
            MethodBuilder smb = new MethodBuilder(defs.count_FXObjectMethodName, syms.intType) {
                @Override
                public void statements() {
                    // Construct and add: return VCNT$();
                    addStmt(Return(Call(defs.count_FXObjectFieldName)));
                }
            };
            
            smb.build();
        }
        
        //
        // Clones a field declared in FXBase as an non-static field.  It also creates
        // FXObject accessor method.
        //
        private void cloneFXBaseVar(VarSymbol var, HashSet<String> excludes) {
            // Var name as a string.
            String str = var.name.toString();
            // Var modifier flags.
            long flags = var.flags();
            
            // If it's an excluded name or a static then skip it.
            if (excludes.contains(str) ||
                (flags & (Flags.SYNTHETIC | Flags.STATIC)) != 0) {
                return;
            }
            
            // Var FX type.
            Type type = var.asType();
            
            // Clone the var.
            addDefinition(Var(flags, type, names.fromString(str), null, var));
            
            // Construct the getter.
            ListBuffer<JCStatement> stmts = ListBuffer.lb();
            Name name = names.fromString("get" + str);
            stmts.append(Return(id(var)));
            // public int getVar { return Var; }
            MethodSymbol getMethSym = makeMethodSymbol(flags, type, name, List.<Type>nil());
            JCMethodDecl getMeth = Method(flags, type, name, List.<JCVariableDecl>nil(), stmts.toList(), getMethSym);
            // Add to definitions.
            addDefinition(getMeth);
            // Add to the exclusion set.
            excludes.add(jcMethodDeclStr(getMeth));

            // Construct the setter.
            stmts = ListBuffer.lb();
            name = names.fromString("set" + str);
            Name argName = names.fromString("value");
            JCVariableDecl arg = Param(type, argName);
            stmts.append(m().Exec(m().Assign(id(var), id(argName))));
            // public void setVar(final int value) { Var = value; }
            MethodSymbol setMethSym = makeMethodSymbol(flags, syms.voidType, name, List.<Type>of(type));
            JCMethodDecl setMeth = Method(flags, syms.voidType, name, List.<JCVariableDecl>of(arg), stmts.toList(), setMethSym);
            // Add to definitions.
            addDefinition(setMeth);
            // Add to the exclusion set.
            excludes.add(jcMethodDeclStr(setMeth));
        }

        //
        // Clones a method declared as an FXObject interface to call the static 
        // equivalent in FXBase. 
        //
        private void cloneFXBaseMethod(MethodSymbol method, HashSet<String> excludes) {
            // Method modifier flags.
            long flags = method.flags();
            
            // If it's an excluded name or a static then skip it.
            if (excludes.contains(method.toString()) ||
                (flags & (Flags.SYNTHETIC | Flags.STATIC)) != 0) {
                return;
            }

            // List of arguments to new method.
            ListBuffer<JCVariableDecl> args = ListBuffer.lb();
            // List of arguments to call supporting FXBase method.
            ListBuffer<JCExpression> callArgs = ListBuffer.lb();
            // Add this to to the call.
            callArgs.append(id(names._this));
            
            // Add arguments to both lists.
            for (VarSymbol argSym : method.getParameters()) {
                Type argType = argSym.asType();
                args.append(Param(argSym.asType(), argSym.name));
                callArgs.append(id(argSym));
            }

            // Buffer for statements.
            ListBuffer<JCStatement> stmts = ListBuffer.lb();
            // Method return type.
            Type returnType = method.getReturnType();
            // Basic call to supporting FXBase method.
            JCExpression fxBaseCall = Call(makeType(syms.javafx_FXBaseType), method.name, callArgs);
           
            // Exec or return based on return type.
            if (returnType == syms.voidType) {
                stmts.append(Stmt(fxBaseCall));
            } else {
                stmts.append(Return(fxBaseCall));
            }
    
            //  public type meth$(t0 arg0, ...) { return FXBase.meth$(this, arg0, ...); }
            addDefinition(Method(Flags.PUBLIC, returnType, method.name, args.toList(), stmts.toList(), method));
        }

        //
        // This method clones the contents of FXBase and FXObject when inheriting
        // from a java class.
        //
        public void cloneFXBase(HashSet<String> excludes) {
            // Reset diagnostic position to current class.
            resetDiagPos();

            // Retrieve FXBase and FXObject.
            ClassSymbol fxBaseSym = (ClassSymbol)syms.javafx_FXBaseType.tsym;
            ClassSymbol fxObjectSym = (ClassSymbol)syms.javafx_FXObjectType.tsym;
            Entry e;

            // Clone the vars in FXBase.
            for (e = fxBaseSym.members().elems; e != null && e.sym != null; e = e.sibling) {
                if (e.sym instanceof VarSymbol) {
                     cloneFXBaseVar((VarSymbol)e.sym, excludes);
                }
            }

            // Clone the interfaces in FXObject.
            for (e = fxObjectSym.members().elems; e != null && e.sym != null; e = e.sibling) {
                if (e.sym instanceof MethodSymbol) {
                     cloneFXBaseMethod((MethodSymbol)e.sym, excludes);
                }
            }
        }

        //
        // This method constructs the statements needed to apply defaults to a given var.
        //
        private JCStatement makeApplyDefaultsStatement(VarInfo ai, boolean isMixinClass) {
            // Assume the worst.
            JCStatement stmt = null;
            // Get init statement.
            JCStatement init = getDefaultInitStatement(ai);

            if (init != null) {
                // a default exists, either on the direct attribute or on an override
                stmt = init;
            } else if (!isMixinClass) {
                if (ai.isMixinVar()) {
                    // Fetch the attribute symbol.
                    VarSymbol varSym = ai.getSymbol();
                    // Construct the name of the method.
                    Name methodName = attributeApplyDefaultsName(varSym);
                    // Include defaults for mixins into real classes.
                    // Return immediately -- don't wrap in a test and set of IS_INITIALIZED, because it would double wrap and fail
                    return makeSuperCall((ClassSymbol)varSym.owner, methodName, id(names._this));
               } else if (ai instanceof TranslatedVarInfoBase) {
                    //TODO: see SequenceVariable.setDefault() and JFXC-885
                    // setDefault() isn't really done for sequences
                   /**
                    if (!ai.isSequence()) {
                        // Make .setDefault() if Location (without clearing initialize bit) to fire trigger.
                        JCStatement setter = makeSetDefaultStatement(ai);
                        if (setter != null) {
                            stmt = setter;
                        }
                    }
                    * ***/
                }
            }
            return stmt;
        }
        
        //-----------------------------------------------------------------------------------------------------------------------------
        //
        // VarNum method generation.
        //
        
        //
        // This method coordinates the generation of instance level varnum methods.
        //
        public void makeVarNumMethods() {
            final HashMap<VarSymbol, HashMap<VarSymbol, HashSet<VarInfo>>> updateMap =
                isScript() ? analysis.getScriptUpdateMap() : analysis.getClassUpdateMap();
            final List<VarInfo> varInfos = isScript() ? analysis.scriptVarInfos() : analysis.classVarInfos();
            final int varCount = isScript() ? analysis.getScriptVarCount() : analysis.getClassVarCount();

            if (!isMixinClass()) {
                makeApplyDefaultsMethod(varInfos, varCount);
            }
            
            makeUpdateMethod(updateMap, false);
            makeUpdateMethod(updateMap, true);
            
            if (!isMixinClass() && varCount > 0) {
                makeGetMethod(varInfos, varCount);
                makeGetPosMethod(varInfos, varCount);
                makeSizeMethod(varInfos, varCount);
                makeSetMethod(varInfos, varCount);
                makeBeMethod(varInfos, varCount);
                makeInvalidateMethod(varInfos, varCount);
                makeInitVarBitsMethod(varInfos);
            }
        }

        //
        // This method constructs the current class's applyDefaults$ method.
        //
        public void makeApplyDefaultsMethod(final List<VarInfo> attrInfos, final int count) {
            MethodBuilder mb = new MethodBuilder(defs.applyDefaults_FXObjectMethodName, syms.voidType) {
                @Override
                public void initialize() {
                    addParam(varNumArg());
                }
                
                // Return the method flags.
                @Override
                public JCModifiers flags() {
                    return m().Modifiers(Flags.PUBLIC);
                }
                
                @Override
                public void statements() {
                    // Prepare to accumulate cases.
                    ListBuffer<JCCase> cases = ListBuffer.lb();
                     // Prepare to accumulate overrides.
                    ListBuffer<JCStatement> overrides = ListBuffer.lb();

                    if (!isMixinClass()) {
                        JCExpression receiverType = isScript() ? id(getCurrentClassDecl().getName().append(defs.scriptClassSuffixName)) :
                                                               id(interfaceName(getCurrentClassDecl()));
                        addStmt(Var(Flags.FINAL, receiverType, defs.receiverName, id(names._this)));
                    }
        
                    // Gather the instance attributes.
                    for (VarInfo ai : attrInfos) {
                        // Only declared attributes with default expressions.
                        if (ai.needsCloning() && !ai.isOverride()) {
                            // Begin the var case.
                            beginBlock();
                            
                            if (!ai.isBareSynth()) {
                                // Set initialized flag if need be.
                                if (ai.hasInitializer()) {
                                    addStmt(FlagChangeStmt(ai.proxyVarSym(), null, defs.varFlagIS_INITIALIZED));
                                }
                                // Get body of applyDefaults$.
                                addStmt(makeApplyDefaultsStatement(ai, isMixinClass()));
                            }
                            // return
                            addStmt(Return(null));
                            
                            // case tag number
                            JCExpression tag = Int(ai.getEnumeration() - count);
                             // Add the case, something like:
                            // case i: applyDefaults$var(); return;j
                            cases.append(m().Case(tag, endBlockAsList()));
                        } else if (ai.hasInitializer()) {
                            // Begin the override case.
                            beginBlock();
                            
                            if (!ai.isBareSynth()) {
                                // Set initialized flag.
                                addStmt(FlagChangeStmt(ai.proxyVarSym(), null, defs.varFlagIS_INITIALIZED));
                                // Get body of applyDefaults$.
                                addStmt(makeApplyDefaultsStatement(ai, isMixinClass()));
                                // return
                            }
                            addStmt(Return(null));
                            
                            // varNum == VOFF$var
                            JCExpression isRightVarExpr = EQ(varNumArg(), id(attributeOffsetName(ai.getSymbol())));
                            // if (varNum == VOFF$var) { init; return; }
                            overrides.append(If(isRightVarExpr, endBlock()));
                        }
                    }
        
                    // Reset diagnostic position to current class.
                    resetDiagPos();
        
                    // Has some defaults.
                    boolean hasDefaults = cases.nonEmpty() || overrides.nonEmpty();
                    
                    // Begin overall block.
                    beginBlock();
        
                    // If there were some location vars.
                    if (cases.nonEmpty()) {
                        // varNum - VCNT$
                        JCExpression tagExpr = MINUS(varNumArg(), id(defs.count_FXObjectFieldName));
                        // Construct and add: switch(varNum - VCNT$) { ... }
                        addStmt(m().Switch(tagExpr, cases.toList()));
                    }
        
                    // Add overrides.
                    addStmts(overrides.toList());
                    
                    // Call super if necessary.
                    callSuper();
                    
                    // if (!default_applied)
                    JCExpression ifExpr = FlagTest(varNumArg(), defs.varFlagDEFAULT_APPLIED, null);
                    // if (!default_applied) { body } 
                    addStmt(If(ifExpr, endBlock()));
                    
                    buildIf(hasDefaults);
                }
            };
            
            mb.build();
            
        }
        
        //
        // This method sets the initial var flags.
        //
        private void makeInitVarBitsMethod(final List<VarInfo> attrInfos) {
            MethodBuilder mb = new MethodBuilder(defs.initVarBits_FXObjectMethodName, syms.voidType) {

                @Override
                public void statements() {
                    callSuper();
                    
                    // Set var flags when necessary. 
                    for (VarInfo ai : attrInfos) {
                        setDiagPos(ai.pos());
                        // Only declared attributes with default expressions.
                        if (ai.needsCloning()) {
                            VarSymbol proxyVarSym = ai.proxyVarSym();
                            if (!isMixinClass() && ai.isMixinVar() && !(ai.hasOverrideVar() && hasDefaultInitStatement(ai))) {
                                JCExpression mixinCall = Call(makeType(proxyVarSym.owner.type, false), attributeInitVarBitsName(proxyVarSym), id(names._this));
                                addStmt(FlagChangeStmt(proxyVarSym, id(defs.varFlagALL_FLAGS), mixinCall));
                            } else {
                                boolean isBound = ai.hasBoundDefinition();
                                boolean isReadonly = ai.isDef() || (isBound && !ai.hasBiDiBoundDefinition());
                                Name setBits = null;
                                
                                if (isReadonly && isBound) {
                                    setBits = defs.varFlagINIT_BOUND_READONLY;
                                } else if (isReadonly) {
                                    setBits = defs.varFlagINIT_READONLY;
                                } else if (isBound) {
                                    setBits = defs.varFlagINIT_BOUND;
                                }
                                
                                if (setBits != null) {
                                    addStmt(FlagChangeStmt(proxyVarSym, defs.varFlagALL_FLAGS, setBits));
                                } else if (ai.isOverride() && hasDefaultInitStatement(ai)) {
                                    addStmt(FlagChangeStmt(proxyVarSym, defs.varFlagALL_FLAGS, null));
                                }
                            }
                        }
                    }
                    
                    buildIf(stmts.size() != 1);
                }
            };
            
            mb.build();
        }
    
        //
        // This method constructs the current class's update$ method.
        //
        public void makeUpdateMethod(final HashMap<VarSymbol, HashMap<VarSymbol, HashSet<VarInfo>>> updateMap, final boolean isSequenceVersion) {
            MethodBuilder mb = new MethodBuilder(defs.update_FXObjectMethodName, syms.voidType) {
                @Override
                public void initialize() {
                    addParam(updateInstanceArg());
                    addParam(varNumArg());
                    if (isSequenceVersion) {
                        addParam(startPosArg());
                        addParam(endPosArg());
                        addParam(newLengthArg());
                    }
                    addParam(phaseArg());
                }
            
                @Override
                public void statements() {
                    /*
                     * For bound functions, we generate a local class. If the current
                     * class is such a class, we need to invalidate the synthetic
                     * bound function param instance fields from the input FXObject
                     * and varNum pairs.
                     */
                    if (isBoundFuncClass) {
                        MethodSymbol msym = (MethodSymbol) getCurrentClassSymbol().owner;
                        List<VarSymbol> params = msym.params();

                        /*
                         * For each bound function param "foo", we generate
                         *
                         *     if (varNum$ == $$boundVarNum$foo && instance$ == $$boundInstance$foo) {
                         *          invalidate$local_klass2$foo(phase$);
                         *          // or sequence version of invalidate..
                         *     }
                         */
                        JCStatement ifReferenceStmt = null;
                        for (VarSymbol param : params) {
                            beginBlock();
                            // varNum$ == $$boundVarNum$foo
                            JCExpression varNumCond = EQ(varNumArg(), id(boundFunctionVarNumParamName(param.name)));
                            // instance$ == $$boundInstance$foo
                            JCExpression objCond = EQ(updateInstanceArg(), id(boundFunctionObjectParamName(param.name)));
                            // && of above two conditions
                            JCExpression ifReferenceCond = AND(varNumCond, objCond);
                            // invalidate the synthetic instance field for this param

                            if (isSequenceVersion) {
                                if (types.isSequence(param.type)) {
                                    // Sequence: update$ is only used on select, so, for sequences, we can just pass through
                                    addStmt(CallStmt(getReceiver(), attributeInvalidateName(param),
                                            startPosArg(),
                                            endPosArg(),
                                            newLengthArg(),
                                            phaseArg()));
                                }
                            } else {
                                if (!types.isSequence(param.type)) {
                                    // Non-sequence
                                    addStmt(CallStmt(getReceiver(), attributeInvalidateName(param), phaseArg()));
                                }
                            }
                            ifReferenceStmt = If(ifReferenceCond, 
                                    endBlock(),
                                    ifReferenceStmt);
                            addStmt(ifReferenceStmt);
                        }
                    }
                    // Loop for instance symbol.
                    for (VarSymbol instanceVar : updateMap.keySet()) {
                        HashMap<VarSymbol, HashSet<VarInfo>> instanceMap = updateMap.get(instanceVar);
                        beginBlock();

                        // Loop for reference symbol.
                        JCStatement ifReferenceStmt = null;
                        for (VarSymbol referenceVar : instanceMap.keySet()) {
                            HashSet<VarInfo> referenceSet = instanceMap.get(referenceVar);
                            beginBlock();
                             
                            // Loop for local vars.
                            for (VarInfo varInfo : referenceSet) {
                                VarSymbol proxyVarSym = varInfo.proxyVarSym();
                                if (isSequenceVersion) {
                                    if (varInfo.isSequence()) {
                                        // Sequence: update$ is only used on select, so, for sequences, we can just pass through
                                        addStmt(CallStmt(getReceiver(), attributeInvalidateName(proxyVarSym),
                                                startPosArg(),
                                                endPosArg(),
                                                newLengthArg(),
                                                phaseArg()));
                                    }
                                } else {
                                    if (!varInfo.isSequence()) {
                                        // Non-sequence
                                        addStmt(CallStmt(getReceiver(), attributeInvalidateName(proxyVarSym), phaseArg()));
                                    }
                                }
                            }

                            // Reference the class with the instance, if it is script-level append the suffix
                            JCExpression ifReferenceCond = EQ(varNumArg(), Offset(referenceVar));
                            ifReferenceStmt = If(ifReferenceCond, 
                                    endBlock(),
                                    ifReferenceStmt);
                        }
                        addStmt(ifReferenceStmt);
                        
                        JCExpression ifInstanceCond = EQ(updateInstanceArg(), makeMixinSafeVarValue(instanceVar));
                        addStmt(If(ifInstanceCond,
                                endBlock()));
                    }
                    
                    if (stmts.nonEmpty()) {
                        callSuper();
                    }
                    
                    callMixins();
                    
                    buildIf(stmts.nonEmpty());
                }
            };
            
            mb.build();
         }
        
        //
        // This method constructs the current class's get$ method.
        //
        public void makeGetMethod(List<VarInfo> attrInfos, int varCount) {
            VarCaseMethodBuilder vcmb = new VarCaseMethodBuilder(defs.get_FXObjectMethodName, syms.objectType,
                                                                 attrInfos, varCount) {
                @Override
                public void statements() {
                    // get$var()
                    JCExpression getterExp = Call(attributeGetterName(varInfo.getSymbol()));
                    // return get$var()
                    addStmt(Return(getterExp));
                }
            };
            
            vcmb.build();
        }
        
        //
        // This method constructs the current class's get$(varnum, pos) method.
        //
        public void makeGetPosMethod(List<VarInfo> attrInfos, int varCount) {
            VarCaseMethodBuilder vcmb = new VarCaseMethodBuilder(defs.get_FXObjectMethodName, syms.objectType,
                                                                 attrInfos, varCount) {
                @Override
                public void initialize() {
                    addParam(posArg());
                }
                
                @Override
                public void statements() {
                    if (varInfo.isSequence()) {
                        // return get$var(pos$)
                        addStmt(Return(Call(attributeGetterName(varInfo.getSymbol()), posArg())));
                    } else {
                        addStmt(Return(Null()));
                    }
                }
            };
            
            vcmb.build();
        }
         
        //
        // This method constructs the current class's size$(varnum) method.
        //
        public void makeSizeMethod(List<VarInfo> attrInfos, int varCount) {
            VarCaseMethodBuilder vcmb = new VarCaseMethodBuilder(defs.size_FXObjectMethodName, syms.intType,
                                                                 attrInfos, varCount) {
                @Override
                public void statements() {
                    if (varInfo.isSequence()) {
                        // return size$var()
                        addStmt(Return(Call(attributeSizeName(varInfo.getSymbol()))));
                    } else {
                        addStmt(Return(Int(0)));
                    }
                }
            };
            
            vcmb.build();
        }
       
        //
        // This method constructs the current class's set$ method.
        //
        public void makeSetMethod(List<VarInfo> attrInfos, int varCount) {
            VarCaseMethodBuilder vcmb = new VarCaseMethodBuilder(defs.set_AttributeMethodPrefixName, syms.voidType,
                                                                 attrInfos, varCount) {
                @Override
                public void initialize() {
                    addParam(objArg());
                 }
                
                @Override
                public void statements() {
                    if (!varInfo.isDef() && !varInfo.isBareSynth()) {
                         // (type)object$
                        JCExpression objCast = typeCast(diagPos, varInfo.getRealType(), syms.objectType, objArg());
                        if (varInfo.isSequence()) {
                            addStmt(CallStmt(defs.Sequences_set, id(names._this), Offset(varInfo.getSymbol()), objCast));
                        } else {
                            // set$var((type)object$)
                            addStmt(CallStmt(attributeSetterName(varInfo.getSymbol()), objCast));
                        }
                    }
                    // return
                    addStmt(Return(null));
                }
            };
            
            vcmb.build();
        }
       
        //
        // This method constructs the current class's be$ method.
        //
        public void makeBeMethod(List<VarInfo> attrInfos, int varCount) {
            VarCaseMethodBuilder vcmb = new VarCaseMethodBuilder(defs.be_AttributeMethodPrefixName, syms.voidType,
                                                                 attrInfos, varCount) {
                @Override
                public void initialize() {
                    addParam(objArg());
                }
  
                @Override
                public void statements() {
                    if (!varInfo.isBareSynth()) {
                        // (type)object$
                        JCExpression objCast = typeCast(diagPos, varInfo.getRealType(), syms.objectType, objArg());
                        // be$var((type)object$)
                        addStmt(CallStmt(attributeBeName(varInfo.getSymbol()), objCast));
                    }
                    // return
                    addStmt(Return(null));
                }
            };

            vcmb.build();
        }
         
        //
        // This method constructs the current class's invalidate$(varnum, ...) method.
        //
        public void makeInvalidateMethod(List<VarInfo> attrInfos, int varCount) {
            VarCaseMethodBuilder vcmb = new VarCaseMethodBuilder(defs.invalidate_FXObjectMethodName, syms.voidType,
                                                                 attrInfos, varCount) {
                @Override
                public void initialize() {
                    addParam(startPosArg());
                    addParam(endPosArg());
                    addParam(newLengthArg());
                    addParam(phaseArg());
                }
                
                @Override
                public void statements() {
                    // FIXME - do the right thing.
                    if (varInfo.isSequence()) {
                        addStmt(CallStmt(attributeInvalidateName(varInfo.getSymbol()),
                                startPosArg(), endPosArg(), newLengthArg(), phaseArg()));
                    } else {
                        addStmt(CallStmt(attributeInvalidateName(varInfo.getSymbol()), phaseArg()));
                    }
                    
                    addStmt(Return(null));
                }
            };
            
            vcmb.build();
        }

        //
        // This method constructs the initializer for a var map.
        //
        public JCExpression makeInitVarMapExpression(ClassSymbol cSym, LiteralInitVarMap varMap) {
            // Reset diagnostic position to current class.
            resetDiagPos();

             // Build up the argument list for the call.
            ListBuffer<JCExpression> args = ListBuffer.lb();
            // X.VCNT$()
            args.append(Call(makeType(cSym.type), defs.count_FXObjectFieldName));

            // For each var declared in order (to make the switch tags align to the vars.)
            for (VarSymbol vSym : varMap.varList.toList()) {
                // ..., X.VOFF$x, ...

                args.append(Select(makeType(cSym.type), attributeOffsetName(vSym)));
            }

            // FXBase.makeInitMap$(X.VCNT$(), X.VOFF$a, ...)
            return Call(defs.FXBase_makeInitMap, args);
        }

        //
        // This method constructs a single var map declaration.
        //
        private JCVariableDecl makeInitVarMapDecl(ClassSymbol cSym, LiteralInitVarMap varMap) {
            // Reset diagnostic position to current class.
            resetDiagPos();
            // Fetch name of map.
            Name mapName = varMapName(cSym);
            // static short[] Map$X;
            return makeField(Flags.STATIC, syms.javafx_ShortArray, mapName, null);
        }

        //
        // This method constructs a single var map initial value.
        //
        public JCStatement makeInitVarMapInit(LiteralInitVarMap varMap) {
            // Get current class symbol.
            ClassSymbol cSym = getCurrentClassSymbol();
            // Reset diagnostic position to current class.
            resetDiagPos();
            // Fetch name of map.
            Name mapName = varMapName(cSym);
            // Map$X = FXBase.makeInitMap$(X.VCNT$(), X.VOFF$a, ...);
            return Stmt(m().Assign(id(mapName), makeInitVarMapExpression(cSym, varMap)));
        }

        //
        // This method constructs declarations for var maps used by literal initializers.
        //
        public void makeInitClassMaps(LiteralInitClassMap initClassMap) {
            // Reset diagnostic position to current class.
            resetDiagPos();

            // For each class initialized in the current class.
            for (ClassSymbol cSym : initClassMap.classMap.keySet()) {
                // Get the var map for the referencing class.
                LiteralInitVarMap varMap = initClassMap.classMap.get(cSym);
                // Add to var.
                addDefinition(makeInitVarMapDecl(cSym, varMap));

                // Fetch name of map.
                Name mapName = varMapName(cSym);
                // Prepare to accumulate statements.
                ListBuffer<JCStatement> stmts = ListBuffer.lb();

                if (analysis.isAnonClass(cSym)) {
                    // Construct and add: return MAP$X;
                    stmts.append(Return(id(mapName)));
                } else {
                    // MAP$X == null
                    JCExpression condition = EQnull(id(mapName));
                    // MAP$X = FXBase.makeInitMap$(X.VCNT$, X.VOFF$a, ...)
                    JCExpression assignExpr = m().Assign(id(mapName), makeInitVarMapExpression(cSym, varMap));
                    // Construct and add: return MAP$X == null ? (MAP$X = FXBase.makeInitMap$(X.VCNT$, X.VOFF$a, ...)) : MAP$X;
                    stmts.append(Return(m().Conditional(condition, assignExpr, id(mapName))));
                }
                
                // Construct the method symbol.
                MethodSymbol methSym = makeMethodSymbol(Flags.PUBLIC | Flags.STATIC,
                                                        syms.javafx_ShortArray,
                                                        varGetMapName(cSym),
                                                        List.<Type>nil());

                // Construct lazy accessor method.
                JCMethodDecl method = Method(Flags.PUBLIC | Flags.STATIC,
                                                 syms.javafx_ShortArray,
                                                 varGetMapName(cSym),
                                                 List.<JCVariableDecl>nil(),
                                                 stmts.toList(), 
                                                 methSym);
                                                 
                // Add method.
                addDefinition(method);
            }
        }


        //
        // This method constructs a super call with appropriate arguments.
        //
        private JCStatement makeSuperCall(ClassSymbol cSym, Name name, JCExpression... args) {
            ListBuffer<JCExpression> buffer = ListBuffer.lb();
            
            for (JCExpression arg : args) {
                buffer.append(arg);
            }
            
            return makeSuperCall(cSym, name, buffer.toList());
        }
        private JCStatement makeSuperCall(ClassSymbol cSym, Name name, List<JCExpression> args) {
            // If this is from a mixin class then we need to use receiver$ otherwise this.
            boolean fromMixinClass = isMixinClass();
            // If this is to a mixin class then we need to use receiver$ otherwise this.
            boolean toMixinClass = JavafxAnalyzeClass.isMixinClass(cSym);
            // If this class doesn't have a javafx super then punt to FXBase.
            boolean toFXBase = cSym == null;

            // Add in the receiver if necessary.
            if (toMixinClass || toFXBase) {
                // Determine the receiver name.
                Name receiver = fromMixinClass ? defs.receiverName : names._this;
                args.prepend(id(receiver));
            }

            // Determine the selector.
            JCExpression selector;
            if (toMixinClass) {
                selector = makeType(cSym.type, false);
            } else if (toFXBase) {
                selector = makeType(syms.javafx_FXBaseType, false);
            } else {
                selector = id(names._super);
            }

            // Construct the call.
            return CallStmt(selector, name, args);
        }

        //
        // Construct the static block for setting defaults
        //
        public void makeInitStaticAttributesBlock(ClassSymbol sym, boolean isScriptLevel, List<VarInfo> attrInfo, JCStatement initMap) {
            // Buffer for init statements.
            ListBuffer<JCStatement> stmts = ListBuffer.lb();
    
            // Initialize the var map for anon class.
            if (initMap != null) {
                stmts.append(initMap);
            }

            if (isScriptLevel) {
                stmts.append(CallStmt(scriptLevelAccessMethod(sym)));
            }
            
            if (attrInfo != null) {
                stmts.append(CallStmt(Call(scriptLevelAccessMethod(sym)), defs.applyDefaults_FXObjectMethodName));
            }
             
            addDefinition(m().Block(Flags.STATIC, stmts.toList()));
        }

        //
        // This method generates the code for a userInit or postInit method.
        public void makeInitMethod(Name methName, ListBuffer<JCStatement> translatedInitBlocks, List<ClassSymbol> immediateMixinClasses) {
            ClassSymbol superClassSym = analysis.getFXSuperClassSym();
           
            // Only create method if necessary (rely on FXBase.)
            if (translatedInitBlocks.nonEmpty() || immediateMixinClasses.nonEmpty() || isMixinClass()) {
                List<JCVariableDecl> receiverVarDeclList;
                MethodSymbol methSym;
                ListBuffer<JCStatement> stmts = ListBuffer.lb();
    
                // Mixin super calls will be handled when inserted into real classes.
                if (!isMixinClass()) {
                    //TODO:
                    // Some implementation code is still generated assuming a receiver parameter.  Until this is fixed
                    //    var receiver = this;
                    
                    stmts.append(Var(Flags.FINAL, id(interfaceName(getCurrentClassDecl())), defs.receiverName, id(names._this)));
                    receiverVarDeclList = List.<JCVariableDecl>nil();
    
                    if (superClassSym != null) {
                        stmts.append(CallStmt(id(names._super), methName));
                    }
    
                    for (ClassSymbol mixinClassSym : immediateMixinClasses) {
                        JCExpression selector = makeType(mixinClassSym.type, false);
                        stmts.append(CallStmt(selector, methName,  m().TypeCast(makeType(mixinClassSym), id(names._this))));
                    }
                    
                    methSym = makeMethodSymbol(rawFlags(), syms.voidType, methName, List.<Type>nil());
                } else {
                    receiverVarDeclList = List.<JCVariableDecl>of(ReceiverParam(getCurrentClassDecl()));
                    methSym = makeMethodSymbol(rawFlags(), syms.voidType, methName, List.<Type>of(getCurrentClassSymbol().type));
                }
    
                stmts.appendList(translatedInitBlocks);
                addDefinition(Method(rawFlags(),
                                         syms.voidType,
                                         methName,
                                         receiverVarDeclList,
                                         stmts.toList(),
                                         methSym));
            }
        }

        private void makeConstructor(List<JCVariableDecl> params, List<Type> types, List<JCStatement> cStats) {
            resetDiagPos();
            addDefinition(Method(Flags.PUBLIC,
                          syms.voidType,
                          names.init,
                          params,
                          cStats,
                          makeMethodSymbol(Flags.PUBLIC, syms.voidType, names.init, types)));
    
        }
        
        //
        // Make a constructor to be called by Java code.
        // Simply pass up to super, unless this is the last JavaFX class, in which case add object initialization
        //
        public void makeJavaEntryConstructor(List<VarInfo> varInfos) {
            //    public Foo() {
            //        this(false);
            //        initialize$();
            //        // register dependencies for bound function class
            //    }
            ListBuffer<JCStatement> stmts = ListBuffer.lb();
            stmts.append(CallStmt(names._this, Boolean(false)));
            stmts.append(CallStmt(defs.initialize_FXObjectMethodName));
            if (isBoundFuncClass) {
                /*
                 * For each bound function param (FXObject+varNum pair), at the
                 * end of object creation register "this" as a dependent by
                 * calling FXBase.addDependent$ method:
                 *
                 *     FXBase.addDependent$(boundFuncObjParam1, boundFunctionVarNumParam1, this);
                 *     FXBase.addDependent$(boundFuncObjParam1, boundFunctionVarNumParam1, this);
                 *     ....
                 */
                for (VarInfo vi : varInfos) {
                    if ((vi.getFlags() & Flags.PARAMETER) != 0L) {
                        // call FXBase.addDependent$(FXObject, int, FXObject)
                        Symbol varSym = vi.getSymbol();
                        stmts.append(CallStmt(
                                defs.FXBase_addDependent,
                                id(boundFunctionObjectParamName(varSym.name)),
                                id(boundFunctionVarNumParamName(varSym.name)),
                                id(names._this)));
                    }
                }
            }
            makeConstructor(List.<JCVariableDecl>nil(), List.<Type>nil(), stmts.toList());
        }

        //
        // Make a constructor to be called by JavaFX code.
        //
        public void makeFXEntryConstructor(ClassSymbol outerTypeSym) {
            ListBuffer<JCStatement> stmts = ListBuffer.lb();
            Name dummyParamName = names.fromString("dummy");
    
            // call the FX version of the constructor in the superclass
            //    public Foo(boolean dummy) {
            //        super(dummy);
            //    }
            if (analysis.getFXSuperClassSym() != null) {
                stmts.append(CallStmt(names._super, id(dummyParamName)));
            } else {
                stmts.append(CallStmt(defs.initFXBase_MethodName));
            }
    
            // Construct the parameters
            ListBuffer<JCVariableDecl> params = ListBuffer.lb();
            ListBuffer<Type> types = ListBuffer.lb();
            if (outerTypeSym != null) {
                // add a parameter and a statement to constructor for the outer instance reference
                params.append(Param(outerTypeSym.type, defs.outerAccessor_FXObjectFieldName) );
                types.append(outerTypeSym.type);
                JCFieldAccess cSelect = m().Select(id(names._this), defs.outerAccessor_FXObjectFieldName);
                stmts.append(Stmt(m().Assign(cSelect, id(defs.outerAccessor_FXObjectFieldName))));
            }
            params.append(Param(syms.booleanType, dummyParamName));
            types.append(syms.booleanType);
    
            makeConstructor(params.toList(), types.toList(), stmts.toList());
        }
    

        //
        // Make the field for accessing the outer members
        //
        public void makeOuterAccessorField(ClassSymbol outerTypeSym) {
            resetDiagPos();
            // Create the field to store the outer instance reference
            addDefinition(makeField(Flags.PUBLIC, outerTypeSym.type, defs.outerAccessor_FXObjectFieldName, null));
        }
    
        //
        // Make the method for accessing the outer members
        //
        public void makeOuterAccessorMethod(ClassSymbol outerTypeSym) {
            resetDiagPos();
            ListBuffer<JCStatement> stmts = ListBuffer.lb();

            VarSymbol vs = new VarSymbol(Flags.PUBLIC, defs.outerAccessor_FXObjectFieldName, outerTypeSym.type, getCurrentClassSymbol());
            stmts.append(Return(id(vs)));
            MethodSymbol methSym = makeMethodSymbol(Flags.PUBLIC, outerTypeSym.type, defs.outerAccessor_MethodName, List.<Type>nil());
            addDefinition(Method(Flags.PUBLIC, outerTypeSym.type, defs.outerAccessor_MethodName, List.<JCVariableDecl>nil(), stmts.toList(), methSym));
        }
        
        //
        // Make a method body which redirects to the actual implementation in a static method of the defining class.
        //
        private List<JCStatement> makeDispatchBody(MethodSymbol sym, boolean isBound, boolean isStatic) {
            ClassSymbol cSym = analysis.getCurrentClassSymbol();
            ListBuffer<JCExpression> args = ListBuffer.lb();
            if (!isStatic) {
                // Add the this argument, so the static implementation method is invoked
                args.append(m().TypeCast(id(interfaceName(getCurrentClassDecl())), id(names._this)));
            }
            for (VarSymbol var : sym.params) {
                args.append(id(var));
            }
            JCExpression receiver = isCurrentClassSymbol(sym.owner) ? null : makeType(sym.owner.type, false);
            JCExpression expr = Call(receiver, functionName(sym, !isStatic, isBound), args);
            JCStatement stmt = (sym.getReturnType() == syms.voidType) ? m().Exec(expr) : Return(expr);
            return List.<JCStatement>of(stmt);
        }

        //
        // Make a method from a MethodSymbol and an optional method body.
        // Make a bound version if "isBound" is set.
        //
        private void appendMethodClones(MethodSymbol sym, boolean needsBody) {
            resetDiagPos();
            //TODO: static test is broken
            boolean isBound = (sym.flags() & JavafxFlags.BOUND) != 0;
            List<JCStatement> body = needsBody ? makeDispatchBody(sym, isBound, (sym.flags() & Flags.STATIC) != 0) : null;
            // build the parameter list
            
            ListBuffer<JCVariableDecl> params = ListBuffer.lb();
            for (VarSymbol vsym : sym.getParameters()) {
                if (isBound) {
                    TODO("bound appendMethodClones");
                }
                params.append(m().VarDef(m().Modifiers(0L), vsym.name, makeType(vsym.asType()), null));
            }
    
            // make the method
            JCModifiers mods = m().Modifiers(Flags.PUBLIC | (body == null? Flags.ABSTRACT : 0L));
            
            if (isCurrentClassSymbol(sym.owner))
                mods = addAccessAnnotationModifiers(diagPos, sym.flags(), mods);
            else
                mods = addInheritedAnnotationModifiers(diagPos, sym.flags(), mods);
                
            Name functionName = functionName(sym, false, isBound);
            MethodType methType = (MethodType)sym.type;
            
            addDefinition(Method(
                          mods,
                          makeReturnTypeTree(diagPos, sym, isBound),
                          functionName,
                          params.toList(),
                          body,
                          makeMethodSymbol(mods.flags, methType.restype, functionName, methType.argtypes)));
            if (needsBody) {
                optStat.recordProxyMethod();
            }
        }
    
    
        //
        // Add proxies which redirect to the static implementation for every concrete method
        //
        public void makeFunctionProxyMethods(List<MethodSymbol> needDispatch) {
            for (MethodSymbol sym : needDispatch) {
                appendMethodClones(sym, true);
            }
        }

        //
        // Add interface declarations for declared methods.
        //
        public void makeFunctionInterfaceMethods() {
            for (JFXTree def : getCurrentClassDecl().getMembers()) {
                if (def.getFXTag() == JavafxTag.FUNCTION_DEF) {
                    JFXFunctionDefinition func = (JFXFunctionDefinition) def;
                    MethodSymbol sym = func.sym;
                    
                    if ((sym.flags() & (Flags.SYNTHETIC | Flags.STATIC | Flags.PRIVATE)) == 0) {
                        appendMethodClones(sym, false);
                    }
                }
            }
        }

        //
        // This method constructs a script class.
        //
        public void makeScript(List<JCTree> definitions) {
            JCModifiers classMods = m().Modifiers(Flags.PUBLIC | Flags.STATIC);
            classMods = addAccessAnnotationModifiers(diagPos, 0, classMods);
            JCClassDecl script = m().ClassDef(
                    classMods,
                    scriptName,
                    List.<JCTypeParameter>nil(),
                    makeType(syms.javafx_FXBaseType),
                    List.<JCExpression>of(makeType(syms.javafx_FXObjectType)),
                    definitions);
            script.sym = scriptClassSymbol;
        
            membersToSymbol(script);

            addDefinition(script);
        }
                
        //
        // Methods for accessing the outer members.
        //
        public void makeOuterAccessorInterfaceMembers() {
            ClassSymbol cSym = getCurrentClassSymbol();
            
            if (cSym != null && toJava.getHasOuters().contains(cSym)) {
                Symbol typeOwner = cSym.owner;
                
                while (typeOwner != null && typeOwner.kind != Kinds.TYP) {
                    typeOwner = typeOwner.owner;
                }
    
                if (typeOwner != null) {
                    ClassSymbol returnSym = reader.enterClass(names.fromString(typeOwner.type.toString() + mixinClassSuffix));
                    JCMethodDecl accessorMethod = Method(
                            Flags.PUBLIC,
                            returnSym.type,
                            defs.outerAccessor_MethodName,
                            List.<JCVariableDecl>nil(),
                            null,
                            makeMethodSymbol(Flags.PUBLIC, returnSym.type, defs.outerAccessor_MethodName, List.<Type>nil()));
                    addDefinition(accessorMethod);
                    optStat.recordProxyMethod();
                }
            }
        }

        //
        // Add definitions to class to access the script-level sole instance.
        //
        public void makeScriptLevelAccess(ClassSymbol sym, boolean scriptLevel, boolean isRunnable) {
            if (!scriptLevel) {
                // sole instance field
                addDefinition(makeField(Flags.PRIVATE | Flags.STATIC, scriptClassType, defs.scriptLevelAccess_FXObjectFieldName, null));
            }
            
            ListBuffer<JCStatement> stmts = ListBuffer.lb();
            long flags = Flags.PUBLIC;
            
            if (scriptLevel) {
                stmts.append(Return(id(names._this)));
            } else {
                // class needs to be static.
                flags |= Flags.STATIC;
                
                // sole instance lazy creation method
                JCExpression condition = EQnull(id(defs.scriptLevelAccess_FXObjectFieldName));
    
                JCExpression assignExpr = m().Assign(
                        id(defs.scriptLevelAccess_FXObjectFieldName),
                        m().NewClass(null, null, id(scriptName), List.<JCExpression>nil(), null));
    
                ListBuffer<JCStatement> ifStmts = ListBuffer.lb();
                ifStmts.append(Stmt(assignExpr));
    
                stmts.append(If(condition, Block(ifStmts)));
                stmts.append(Return(id(defs.scriptLevelAccess_FXObjectFieldName)));
            }
            
            MethodSymbol methSym = makeMethodSymbol(flags, scriptClassType, scriptLevelAccessMethod(sym), List.<Type>nil());
            addDefinition(Method(flags, scriptClassType, scriptLevelAccessMethod(sym), List.<JCVariableDecl>nil(), stmts.toList(), methSym));
        }
    }
}
