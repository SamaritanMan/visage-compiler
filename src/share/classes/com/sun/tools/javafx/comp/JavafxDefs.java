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

package com.sun.tools.javafx.comp;

import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javafx.code.JavafxVarSymbol;

/**
 * Shared definitions
 *
 * @author Robert Field
 */
public class JavafxDefs {

    /**
     * static string definitions
     */
    public static final String boundFunctionSuffix = "$$bound"; // Add an extra $ character so it doesn't get confused with an inner class named $bound
    public static final String boundFunctionDollarSuffix = "$$bound$";
    public static final String implFunctionSuffix = "$impl";
    public static final String attributeGetMethodNamePrefix = "get$";
    public static final String attributeInitMethodNamePrefix = "init$";
    public static final String interfaceSuffix = "$Intf";
    public static final String equalsMethodString = "com.sun.javafx.runtime.Checks.equals";
    public static final String isNullMethodString = "com.sun.javafx.runtime.Checks.isNull";
    public static final String startMethodString = "com.sun.javafx.runtime.Entry.start";
    
    public static final String fxObjectString = "com.sun.javafx.runtime.FXObject";
    public static final String runMethodString = "javafx$run$";
    public static final String receiverNameString = "receiver$";
    public static final String initializeNameString ="initialize$";
    public static final String getMethodNameString = "get";
    public static final String setMethodNameString ="set";
    public static final String addStaticDependentNameString = "addStaticDependent";
    public static final String addDynamicDependentNameString = "addDynamicDependent";
    public static final String clearDynamicDependenciesNameString = "clearDynamicDependencies";
    public static final String makeMethodNameString = "make";
    public static final String invokeNameString = "invoke";
    public static final String lambdaNameString = "lambda";
    
    public char typeCharToEscape = '.';
    public char escapeTypeChar = '_';
     
    /**
     * Name definitions
     */
    final Name runMethodName;
    final Name receiverName;
    final Name initializeName;
    final Name getMethodName;
    final Name getSliceMethodName;
    final Name replaceSliceMethodName;
    final Name setMethodName;
    final Name addStaticDependentName;
    final Name addDynamicDependentName;
    final Name clearDynamicDependenciesName;
    final Name makeMethodName;
    final Name invokeName;
    final Name lambdaName;
    final Name computeValueName;
    final Name initDefName;
    final Name postInitDefName;
    final Name[] locationGetMethodName;
    final Name[] locationSetMethodName;
    final Name[] getPreviousMethodName;

    /**
     * Context set-up
     */
    protected static final Context.Key<JavafxDefs> jfxDefsKey = new Context.Key<JavafxDefs>();

    public static JavafxDefs instance(Context context) {
        JavafxDefs instance = context.get(jfxDefsKey);
        if (instance == null) {
            instance = new JavafxDefs(context);
        }
        return instance;
    }

    protected JavafxDefs(Context context) {
        context.put(jfxDefsKey, this);
        Name.Table names = Name.Table.instance(context);

        runMethodName = names.fromString(runMethodString);
        receiverName = names.fromString(receiverNameString);
        initializeName = names.fromString(initializeNameString);
        getMethodName = Name.fromString(names, getMethodNameString);
        getSliceMethodName = names.fromString("getSlice");
        replaceSliceMethodName = names.fromString("replaceSlice");
        setMethodName = Name.fromString(names, setMethodNameString);
        addStaticDependentName = names.fromString(addStaticDependentNameString);
        addDynamicDependentName = names.fromString(addDynamicDependentNameString);
        clearDynamicDependenciesName = names.fromString(clearDynamicDependenciesNameString);
        makeMethodName = Name.fromString(names, makeMethodNameString);
        invokeName = names.fromString(invokeNameString);
        lambdaName = names.fromString(lambdaNameString);
        computeValueName = names.fromString("computeValue");
        initDefName = names.fromString("$init$def$name");
        postInitDefName = names.fromString("$postinit$def$name");
        locationGetMethodName = new Name[JavafxVarSymbol.accessorSuffixes.length];
        locationSetMethodName = new Name[JavafxVarSymbol.accessorSuffixes.length];
        getPreviousMethodName = new Name[JavafxVarSymbol.accessorSuffixes.length];
        for (int i=0; i< JavafxVarSymbol.accessorSuffixes.length; i++) {
            locationGetMethodName[i] = names.fromString("get" + JavafxVarSymbol.accessorSuffixes[i]);
            locationSetMethodName[i] = names.fromString("set" + JavafxVarSymbol.accessorSuffixes[i]);
            getPreviousMethodName[i] = names.fromString("getPrevious" + JavafxVarSymbol.accessorSuffixes[i]);
        }
    }
}
