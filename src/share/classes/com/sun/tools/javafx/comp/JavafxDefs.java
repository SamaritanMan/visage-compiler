/*
 * Copyright 2008-2009 Sun Microsystems, Inc.  All Rights Reserved.
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

import com.sun.javafx.runtime.Entry;
import com.sun.tools.mjavac.code.Type;
import com.sun.tools.mjavac.util.Context;
import com.sun.tools.mjavac.util.Name;
import com.sun.tools.javafx.code.JavafxSymtab;
import com.sun.tools.mjavac.code.Symbol;
import java.util.regex.Pattern;

/**
 * Shared definitions
 *
 * @author Robert Field
 */
public class JavafxDefs {

    /**
     * Method prefix strings for attributes
     */
    public static final String applyDefaults_AttributeMethodPrefix = "applyDefaults$";
    public static final String be_AttributeMethodPrefix = "be$";
    public static final String evaluate_AttributeMethodPrefix = "evaluate$";
    public static final String get_AttributeMethodPrefix = "get$";
    public static final String getElement_AttributeMethodPrefix = "get$";
    public static final String getMixin_AttributeMethodPrefix = "getMixin$";
    public static final String getVOFF_AttributeMethodPrefix = "getVOFF$";
    public static final String initVarBits_AttributeMethodPrefix = "initVarBits$";
    public static final String invalidate_AttributeMethodPrefix = "invalidate$";
    public static final String onReplace_AttributeMethodPrefix = "onReplace$";
    public static final String set_AttributeMethodPrefix = "set$";
    public static final String setMixin_AttributeMethodPrefix = "setMixin$";
    public static final String size_AttributeMethodPrefix = "size$";

    /**
     * Field prefix strings for attributes
     */
    public static final String saved_AttributeFieldNamePrefix = "saved$";

    /**
     * Class suffixes
     */
    public static final String mixinClassSuffix = "$Mixin";
    public static final String deprecatedInterfaceSuffix = "$Intf";
    public static final String scriptClassSuffix = "$Script";

    /**
     * Package name strings
     */
    public  static final String javaLang_PackageNameString = "java.lang";
    public  static final String runtime_PackageNameString = "com.sun.javafx.runtime";
    public  static final String annotation_PackageNameString = "com.sun.javafx.runtime.annotation";
    public  static final String functions_PackageNameString = "com.sun.javafx.functions";
    public  static final String sequence_PackageNameString = "com.sun.javafx.runtime.sequence";

    /**
     * Class name strings
     */
    public  static final String cSequences = sequence_PackageNameString + ".Sequences";
    public  static final String cSequence  = sequence_PackageNameString + ".Sequence";
    public  static final String cSequenceRef  = sequence_PackageNameString + ".SequenceRef";
    public  static final String cArraySequence  = sequence_PackageNameString + ".ArraySequence";

    private static final String cUtil = runtime_PackageNameString + ".Util";
    private static final String cChecks = runtime_PackageNameString + ".Checks";
    private static final String cFXBase = runtime_PackageNameString + ".FXBase";
    public static final String cFXObject = runtime_PackageNameString + ".FXObject";
    public static final String cFXMixin = runtime_PackageNameString + ".FXMixin";
    private static final String cFXVariable = runtime_PackageNameString + ".FXVariable";
    private static final String cPointer = runtime_PackageNameString + ".Pointer";

    private static final String cMath = javaLang_PackageNameString + ".Math";

    /**
     * Misc strings
     */
    public static final String boundFunctionDollarSuffix = "$$bound$";
    public static final String boundFunctionResult = "$$bound$result$";
    public static final String boundFunctionObjectParamPrefix = "$$boundInstance$";
    public static final String boundFunctionVarNumParamPrefix = "$$boundVarNum$";
    public static final String implFunctionSuffix = "$impl";
    public static final String attributeOldValueNameString = "varOldValue$";
    public static final String attributeNewValueNameString = "varNewValue$";
    public static final String fxBaseString = "com.sun.javafx.runtime.FXBase";
    public static final String typeInfosString = "com.sun.javafx.runtime.TypeInfo";
    public static final String internalRunFunctionNameString = Entry.entryMethodName();
    public static final String receiverNameString = "receiver$";
    public static final String addStaticDependentNameString = "addStaticDependent";
    public static final String makeAttributeMethodNameString = "makeAttribute";
    public static final String makeWithDefaultMethodNameString = "makeWithDefault";
    public static final String makeBijectiveMethodNameString = "makeBijective";
    public static final String invokeNameString = "invoke";
    public static final String lambdaNameString = "lambda";
    public static final String isInitializedNameString = "isInitialized";
    public static final String hasAnInitializerNameString = "hasAnInitializer";
    public static final String convertNumberSequence = "convertNumberSequence";
    public static final String bindingIdString = "id";
    public static final String varOffsetString = "VOFF$";
    public static final String varCountString = "VCNT$";
    public static final String varFlagsString = "VFLGS$";
    public static final String varDependentsManagerString = "DependentsManager$internal$";
    public static final String varValueString = "$";
    public static final String varMapString = "MAP$";
    public static final String varGetMapString = "GETMAP$";
    public static final String varOFF$valueString = "VOFF$value";

    public  static final String zeroDuration = "javafx.lang.Duration.$ZERO";

    public  static final Pattern DATETIME_FORMAT_PATTERN = Pattern.compile("%[<$0-9]*[tT]");

    public  static final char typeCharToEscape = '.';
    public  static final char escapeTypeChar = '_';

    static class RuntimeMethod {
        final String classString;
        final Name methodName;

        private RuntimeMethod(Name.Table names, String classString, String methodString) {
            this.classString = classString;
            this.methodName = names.fromString(methodString);
        }
    }

    /**
     * RuntimeMethod definitions
     */
    final RuntimeMethod TypeInfo_getTypeInfo;

    final RuntimeMethod Sequences_calculateIntRangeSize;
    final RuntimeMethod Sequences_calculateFloatRangeSize;
    final RuntimeMethod Sequences_convertCharToNumberSequence;
    final RuntimeMethod Sequences_convertNumberSequence;
    final RuntimeMethod Sequences_convertNumberToCharSequence;
    final RuntimeMethod Sequences_deleteIndexed;
    final RuntimeMethod Sequences_deleteSlice;
    final RuntimeMethod Sequences_deleteValue;
    final RuntimeMethod Sequences_deleteAll;
    final RuntimeMethod Sequences_forceNonNull;
    final RuntimeMethod Sequences_fromArray;
    final RuntimeMethod Sequences_getSingleValue;
    final RuntimeMethod Sequences_getNewElements;
    final RuntimeMethod Sequences_insert;
    final RuntimeMethod Sequences_insertBefore;
    final RuntimeMethod Sequences_range;
    final RuntimeMethod Sequences_rangeExclusive;
    final RuntimeMethod Sequences_replaceSlice;
    final RuntimeMethod Sequences_reverse;
    final RuntimeMethod Sequences_set;
    final RuntimeMethod Sequences_size;
    final RuntimeMethod Sequences_sizeOfNewElements;

    final RuntimeMethod SequencesRef_save;

    final RuntimeMethod Util_isEqual; //TODO: replace uses with Checks_equals

    final RuntimeMethod Checks_equals;
    final RuntimeMethod Checks_isNull;

    final RuntimeMethod Math_min;
    final RuntimeMethod Math_max;

    final RuntimeMethod FXBase_switchDependence;
    final RuntimeMethod FXBase_switchBiDiDependence;
    final RuntimeMethod FXBase_removeDependent;
    final RuntimeMethod FXBase_addDependent;

    final RuntimeMethod FXVariable_make;

    final RuntimeMethod Pointer_make;
    final RuntimeMethod Pointer_get;

    /**
     * Class-wide method Name definitions
     */
    final Name applyDefaults_ObjectMethodName;
    final Name count_ObjectMethodName;
    final Name get_ObjectMethodName;
    final Name invalidate_ObjectMethodName;
    final Name size_ObjectMethodName;
    final Name update_ObjectMethodName;

    /**
     * Method Name definitions
     */
    final Name incrementSharingMethodName;
    final Name makeAttributeMethodName;
    final Name makeMethodName;
    final Name needDefaultsMethodName;
    final Name sizeArrayMethodName;
    final Name sizeSequenceMethodName;
    final Name toArrayMethodName;

    final Name[] typedGetMethodName;
    final Name[] typedSetMethodName;


    /**
     * Method prefixes for attributes as Names
     * mostly for Name comparison
     */
    final Name applyDefaults_AttributeMethodPrefixName;
    final Name evaluate_AttributeMethodPrefixName;
    final Name getMixinAttributeMethodPrefixName;
    final Name getVOFFAttributeMethodPrefixName;
    final Name initVarBitsAttributeMethodPrefixName;
    final Name onReplaceAttributeMethodPrefixName;
    final Name setMixinAttributeMethodPrefixName;

    /**
     * Name definitions
     */
    public final Name fxObjectName;
    public final Name fxMixinName;
    public final Name fxBaseName;
    public final Name mixinSuffixName;
    public final Name lengthSuffixName;
    public final Name deprecatedInterfaceSuffixName;
    final Name scriptLevelAccessField;
    final Name scriptLevelAccessMethodPrefix;
    final Name durOpAdd;
    final Name durOpSub;
    final Name durOpMul;
    final Name durOpDiv;
    final Name durOpLE;
    final Name durOpLT;
    final Name durOpGE;
    final Name durOpGT;
    final Name userRunFunctionName;
    final Name internalRunFunctionName;
    final Name mainName;
    final Name receiverName;
    final Name initialize_FXObjectMethodName;
    final Name attributeNotifyDependentsName;
    final Name complete_FXObjectMethodName;
    final Name outerAccessorName;
    final Name getMethodName;
    final Name attributeSetMethodParamName;
    final Name getSliceMethodName;
    final Name defaultingTypeInfoFieldName;
    final Name invokeName;
    final Name lambdaName;
    final Name lengthName;
    final Name emptySequenceFieldString;
    final Name isInitializedName;
    final Name hasAnInitializerName;
    final Name bindingIdName;
    final Name varOffsetName;
    final Name varCountName;
    final Name toTestName;
    final Name toBeCastName;
    final Name idName;
    final Name arg0Name;
    final Name arg1Name;
    final Name moreArgsName;
    final Name dependentsName;
    final Name typeParamName;
    final Name initDefName;
    final Name postInitDefName;
    final Name javalangThreadName;
    final Name startName;
    final Name scriptClassSuffixName;
    final Name timeName;
    final Name valueOfName;
    final Name valuesName;
    final Name valueName;
    final Name targetName;
    final Name interpolateName;
    final Name initFXBaseName;
    final Name userInitName;
    final Name postInitName;
    final Name attributeGetMethodPrefixName;
    final Name attributeSetMethodPrefixName;
    final Name attributeBeMethodPrefixName;
    final Name attributeOldValueName;
    final Name attributeNewValueName;
    final Name onReplaceArgNameOld;
    final Name onReplaceArgNameNew;
    final Name sliceArgNameStartPos;
    final Name sliceArgNameEndPos;
    final Name sliceArgNameNewLength;
    final Name onReplaceArgNameFirstIndex;
    final Name onReplaceArgNameLastIndex;
    final Name onReplaceArgNameNewElements;
    final Name getArgNamePos;
    final Name invalidateArgNamePhase;

    final Name internalSuffixName;
    
    final Name varFlagActionTest;
    final Name varFlagActionChange;
    final Name varFlagRestrictSet;
    final Name varFlagIS_INVALID;
    final Name varFlagNEEDS_TRIGGER;
    final Name varFlagIS_BOUND;
    final Name varFlagIS_READONLY;
    final Name varFlagDEFAULT_APPLIED;
    final Name varFlagIS_INITIALIZED;    
    final Name varFlagVALIDITY_FLAGS;
    final Name varFlagIS_BOUND_INVALID;
    final Name varFlagIS_BOUND_DEFAULT_APPLIED;
    final Name varFlagINIT_NORMAL;
    final Name varFlagINIT_OBJ_LIT;
    final Name varFlagINIT_OBJ_LIT_DEFAULT;
    final Name varFlagINIT_READONLY;
    final Name varFlagINIT_BOUND;
    final Name varFlagINIT_BOUND_READONLY;
    final Name varFlagALL_FLAGS;

    public final Name varOFF$valueName;

    public final Name runtimePackageName;
    public final Name annotationPackageName;
    public final Name sequencePackageName;
    public final Name functionsPackageName;
    public final Name javaLangPackageName;
    public final Name boundFunctionResultName;

    public static final int TYPE_KIND_OBJECT = 0;
    public static final int TYPE_KIND_BOOLEAN = 1;
    public static final int TYPE_KIND_CHAR = 2;
    public static final int TYPE_KIND_BYTE = 3;
    public static final int TYPE_KIND_SHORT = 4;
    public static final int TYPE_KIND_INT = 5;
    public static final int TYPE_KIND_LONG = 6;
    public static final int TYPE_KIND_FLOAT = 7;
    public static final int TYPE_KIND_DOUBLE = 8;
    public static final int TYPE_KIND_SEQUENCE = 9;
    public static final int TYPE_KIND_COUNT = 10;

    static final String[] typePrefixes = new String[] { "Object", "Boolean", "Char", "Byte", "Short", "Int", "Long", "Float", "Double", "Sequence" };

    /**
     * Context set-up
     */
    public static final Context.Key<JavafxDefs> jfxDefsKey = new Context.Key<JavafxDefs>();

    private final Type[] realTypeByKind;

    public static JavafxDefs instance(Context context) {
        JavafxDefs instance = context.get(jfxDefsKey);
        if (instance == null) {
            instance = new JavafxDefs(context);
        }
        return instance;
    }

    protected JavafxDefs(Context context) {
        context.put(jfxDefsKey, this);
        final Name.Table names = Name.Table.instance(context);
        final JavafxSymtab syms = (JavafxSymtab)(JavafxSymtab.instance(context));

        durOpAdd = names.fromString("add");
        durOpSub = names.fromString("sub");
        durOpMul = names.fromString("mul");
        durOpDiv = names.fromString("div");
        durOpLE = names.fromString("le");
        durOpLT = names.fromString("lt");
        durOpGE = names.fromString("ge");
        durOpGT = names.fromString("gt");
        fxObjectName = names.fromString(cFXObject);
        fxMixinName = names.fromString(cFXMixin);
        fxBaseName = names.fromString(fxBaseString);
        mixinSuffixName = names.fromString(mixinClassSuffix);
        lengthSuffixName = names.fromString("$length");
        deprecatedInterfaceSuffixName = names.fromString(deprecatedInterfaceSuffix);
        userRunFunctionName = names.fromString("run");
        internalRunFunctionName = names.fromString(internalRunFunctionNameString);
        mainName = names.fromString("main");
        receiverName = names.fromString(receiverNameString);
        initialize_FXObjectMethodName = names.fromString("initialize$");
        attributeNotifyDependentsName = names.fromString("notifyDependents$");
        complete_FXObjectMethodName = names.fromString("complete$");
        outerAccessorName = names.fromString("accessOuter$");
        getMethodName = names.fromString("get");
        attributeSetMethodParamName = names.fromString("value$");
        getSliceMethodName = names.fromString("getSlice");
        sizeArrayMethodName = names.fromString("size");
        sizeSequenceMethodName = names.fromString("size");
        defaultingTypeInfoFieldName = names.fromString("$TYPE_INFO");
        needDefaultsMethodName = names.fromString("needDefault");
        toArrayMethodName = names.fromString("toArray");
        makeAttributeMethodName = names.fromString(makeAttributeMethodNameString);
        makeMethodName = names.fromString("make");
        invokeName = names.fromString(invokeNameString);
        lambdaName = names.fromString(lambdaNameString);
        lengthName = names.fromString("length");
        emptySequenceFieldString = names.fromString("emptySequence");
        isInitializedName = names.fromString(isInitializedNameString);
        hasAnInitializerName = names.fromString(hasAnInitializerNameString);
        bindingIdName = names.fromString(bindingIdString);
        varOffsetName = names.fromString(varOffsetString);
        varCountName = names.fromString(varCountString);
        scriptClassSuffixName = names.fromString(scriptClassSuffix);
        toTestName = names.fromString("toTest");
        toBeCastName = names.fromString("toBeCast");
        idName = names.fromString("id");
        arg0Name = names.fromString("arg$0");
        arg1Name = names.fromString("arg$1");
        moreArgsName = names.fromString("moreArgs");
        dependentsName = names.fromString("dependents");
        typeParamName = names.fromString("T");
        initDefName = names.fromString("$init$def$name");
        postInitDefName = names.fromString("$postinit$def$name");
        timeName = names.fromString("time");
        javalangThreadName = names.fromString("java.lang.Thread");
        startName = names.fromString("start");
        valueOfName = names.fromString("valueOf");
        valuesName = names.fromString("values");
        targetName = names.fromString("target");
        valueName = names.fromString("value");
        interpolateName = names.fromString("interpolate");
        initFXBaseName = names.fromString("initFXBase$");
        userInitName = names.fromString("userInit$");
        postInitName = names.fromString("postInit$");
        incrementSharingMethodName = names.fromString("incrementSharing");
        onReplaceArgNameOld = names.fromString("oldValue$");
        onReplaceArgNameNew = names.fromString("newValue$");
        sliceArgNameStartPos = names.fromString("startPos$");
        sliceArgNameEndPos = names.fromString("endPos$");
        sliceArgNameNewLength = names.fromString("newLength$");
        getArgNamePos = names.fromString("pos$");
        invalidateArgNamePhase = names.fromString("phase$");
        onReplaceArgNameFirstIndex = sliceArgNameStartPos;
        onReplaceArgNameLastIndex = sliceArgNameEndPos;
        onReplaceArgNameNewElements = names.fromString("$newElements$");
        internalSuffixName = names.fromString("$internal$");
        boundFunctionResultName = names.fromString(boundFunctionResult);
        attributeGetMethodPrefixName = names.fromString(get_AttributeMethodPrefix);
        get_ObjectMethodName = attributeGetMethodPrefixName;
        attributeSetMethodPrefixName = names.fromString(set_AttributeMethodPrefix);
        attributeBeMethodPrefixName = names.fromString(be_AttributeMethodPrefix);
        invalidate_ObjectMethodName = names.fromString(invalidate_AttributeMethodPrefix);
        onReplaceAttributeMethodPrefixName = names.fromString(onReplace_AttributeMethodPrefix);
        evaluate_AttributeMethodPrefixName = names.fromString(evaluate_AttributeMethodPrefix);
        getMixinAttributeMethodPrefixName = names.fromString(getMixin_AttributeMethodPrefix);
        getVOFFAttributeMethodPrefixName = names.fromString(getVOFF_AttributeMethodPrefix);
        setMixinAttributeMethodPrefixName = names.fromString(setMixin_AttributeMethodPrefix);
        initVarBitsAttributeMethodPrefixName = names.fromString(initVarBits_AttributeMethodPrefix);
        applyDefaults_ObjectMethodName = names.fromString(applyDefaults_AttributeMethodPrefix);
        applyDefaults_AttributeMethodPrefixName = names.fromString(applyDefaults_AttributeMethodPrefix);
        update_ObjectMethodName = names.fromString("update$");
        size_ObjectMethodName = names.fromString(size_AttributeMethodPrefix);
        count_ObjectMethodName = names.fromString("count$");
        attributeOldValueName =  names.fromString(attributeOldValueNameString);
        attributeNewValueName =  names.fromString(attributeNewValueNameString);
        scriptLevelAccessField = names.fromString("$scriptLevel$");
        scriptLevelAccessMethodPrefix = names.fromString("access$scriptLevel$");
        
        varFlagActionTest = names.fromString("varTestBits$");
        varFlagActionChange = names.fromString("varChangeBits$");
        varFlagRestrictSet = names.fromString("restrictSet$");
        
        varFlagIS_INVALID = names.fromString("VFLGS$IS_INVALID");
        varFlagNEEDS_TRIGGER = names.fromString("VFLGS$NEEDS_TRIGGER");
        varFlagIS_BOUND = names.fromString("VFLGS$IS_BOUND");
        varFlagIS_READONLY = names.fromString("VFLGS$IS_READONLY");
        varFlagDEFAULT_APPLIED = names.fromString("VFLGS$DEFAULT_APPLIED");
        varFlagIS_INITIALIZED = names.fromString("VFLGS$IS_INITIALIZED");
        varFlagVALIDITY_FLAGS = names.fromString("VFLGS$VALIDITY_FLAGS");
        varFlagIS_BOUND_INVALID = names.fromString("VFLGS$IS_BOUND_INVALID");
        varFlagIS_BOUND_DEFAULT_APPLIED = names.fromString("VFLGS$IS_BOUND_DEFAULT_APPLIED");
        varFlagINIT_NORMAL = names.fromString("VFLGS$INIT_NORMAL");
        varFlagINIT_OBJ_LIT = names.fromString("VFLGS$INIT_OBJ_LIT");
        varFlagINIT_OBJ_LIT_DEFAULT = names.fromString("VFLGS$INIT_OBJ_LIT_DEFAULT");
        varFlagINIT_READONLY = names.fromString("VFLGS$INIT_READONLY");
        varFlagINIT_BOUND = names.fromString("VFLGS$INIT_BOUND");
        varFlagINIT_BOUND_READONLY = names.fromString("VFLGS$INIT_BOUND_READONLY");
        varFlagALL_FLAGS = names.fromString("VFLGS$ALL_FLAGS");

        varOFF$valueName = names.fromString(varOFF$valueString);

        runtimePackageName = names.fromString(runtime_PackageNameString);
        annotationPackageName = names.fromString(annotation_PackageNameString);
        javaLangPackageName = names.fromString(javaLang_PackageNameString);
        sequencePackageName = names.fromString(sequence_PackageNameString);
        functionsPackageName = names.fromString(functions_PackageNameString);

        // Initialize RuntimeMethods
        TypeInfo_getTypeInfo = new RuntimeMethod(names, typeInfosString, "getTypeInfo");

        Sequences_convertNumberSequence = new RuntimeMethod(names, cSequences, "convertNumberSequence");
        Sequences_convertCharToNumberSequence = new RuntimeMethod(names, cSequences, "convertCharToNumberSequence");
        Sequences_convertNumberToCharSequence = new RuntimeMethod(names, cSequences, "convertNumberToCharSequence");
        Sequences_forceNonNull = new RuntimeMethod(names, cSequences, "forceNonNull");
        Sequences_fromArray = new RuntimeMethod(names, cSequences, "fromArray");
        Sequences_sizeOfNewElements = new RuntimeMethod(names, cSequences, "sizeOfNewElements");
        Sequences_getNewElements = new RuntimeMethod(names, cSequences, "getNewElements");
        Sequences_getSingleValue = new RuntimeMethod(names, cSequences, "getSingleValue");
        Sequences_range = new RuntimeMethod(names, cSequences, "range");
        Sequences_rangeExclusive = new RuntimeMethod(names, cSequences, "rangeExclusive");
        Sequences_size = new RuntimeMethod(names, cSequences, "size");
        Sequences_replaceSlice = new RuntimeMethod(names, cSequences, "replaceSlice");
        Sequences_reverse = new RuntimeMethod(names, cSequences, "reverse");
        Sequences_set = new RuntimeMethod(names, cSequences, "set");
        Sequences_insert = new RuntimeMethod(names, cSequences, "insert");
        Sequences_insertBefore = new RuntimeMethod(names, cSequences, "insertBefore");
        Sequences_deleteIndexed = new RuntimeMethod(names, cSequences, "deleteIndexed");
        Sequences_deleteSlice = new RuntimeMethod(names, cSequences, "deleteSlice");
        Sequences_deleteValue = new RuntimeMethod(names, cSequences, "deleteValue");
        Sequences_deleteAll = new RuntimeMethod(names, cSequences, "deleteAll");
        Sequences_calculateIntRangeSize = new RuntimeMethod(names, cSequences, "calculateIntRangeSize");
        Sequences_calculateFloatRangeSize = new RuntimeMethod(names, cSequences, "calculateFloatRangeSize");
        SequencesRef_save = new RuntimeMethod(names, cSequenceRef, "save");

        Util_isEqual = new RuntimeMethod(names, cUtil, "isEqual");

        Checks_equals = new RuntimeMethod(names, cChecks, "equals"); //TODO: looks like dup
        Checks_isNull = new RuntimeMethod(names, cChecks, "isNull");

        Math_min = new RuntimeMethod(names, cMath, "min");
        Math_max = new RuntimeMethod(names, cMath, "max");

        FXBase_switchDependence = new RuntimeMethod(names, cFXBase, "switchDependence$");
        FXBase_switchBiDiDependence = new RuntimeMethod(names, cFXBase, "switchBiDiDependence$");
        FXBase_removeDependent  = new RuntimeMethod(names, cFXBase, "removeDependent$");
        FXBase_addDependent     = new RuntimeMethod(names, cFXBase, "addDependent$");

        FXVariable_make = new RuntimeMethod(names, cFXVariable, "make");

        Pointer_make = new RuntimeMethod(names, cPointer, "make");
        Pointer_get = new RuntimeMethod(names, cPointer, "get");

        // Initialize per Kind names and types
        typedGetMethodName = new Name[TYPE_KIND_COUNT];
        typedSetMethodName = new Name[TYPE_KIND_COUNT];
        for (int kind = 0; kind < TYPE_KIND_COUNT; kind++) {
            typedGetMethodName[kind] = names.fromString("get" + accessorSuffixes[kind]);
            typedSetMethodName[kind] = names.fromString("set" + accessorSuffixes[kind]);
        }

        realTypeByKind = new Type[TYPE_KIND_COUNT];
        realTypeByKind[TYPE_KIND_OBJECT] = syms.objectType;
        realTypeByKind[TYPE_KIND_BOOLEAN] = syms.booleanType;
        realTypeByKind[TYPE_KIND_CHAR] = syms.charType;
        realTypeByKind[TYPE_KIND_BYTE] = syms.byteType;
        realTypeByKind[TYPE_KIND_SHORT] = syms.shortType;
        realTypeByKind[TYPE_KIND_INT] = syms.intType;
        realTypeByKind[TYPE_KIND_LONG] = syms.longType;
        realTypeByKind[TYPE_KIND_FLOAT] = syms.floatType;
        realTypeByKind[TYPE_KIND_DOUBLE] = syms.doubleType;
        realTypeByKind[TYPE_KIND_SEQUENCE] = syms.javafx_SequenceType;
    }

    static final String[] accessorSuffixes = new String[] { "", "AsBoolean", "AsChar", "AsByte", "AsShort", "AsInt", "AsLong", "AsFloat", "AsDouble", "AsSequence" };
    
    public static String getTypePrefix(int index) { return typePrefixes[index]; }

    public Name scriptLevelAccessMethod(Name.Table names, Symbol clazz) {
        StringBuilder buf = new StringBuilder();
        buf.append(scriptLevelAccessMethodPrefix);
        buf.append(clazz.getQualifiedName().toString().replace('.', '$'));
        buf.append('$');
        return names.fromString(buf);
 
    }
}
