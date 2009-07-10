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

import java.util.HashMap;
import java.util.Map;

import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.MethodType;
import com.sun.tools.javac.util.*;
import com.sun.tools.javafx.code.JavafxSymtab;
import com.sun.tools.javafx.code.JavafxTypes;
import com.sun.tools.javafx.code.JavafxVarSymbol;
import com.sun.tools.javafx.code.JavafxClassSymbol;

import static com.sun.tools.javafx.code.JavafxVarSymbol.*;
import static com.sun.tools.javafx.comp.JavafxTypeMorpher.VarRepresentation.*;
import static com.sun.tools.javafx.comp.JavafxDefs.locationPackageNameString;
import static com.sun.tools.javafx.comp.JavafxDefs.sequencePackageNameString;
import static com.sun.tools.javafx.code.JavafxFlags.*;
import static com.sun.tools.javac.code.Flags.*;


/**
 *
 * @author Robert Field
 */
public class JavafxTypeMorpher {
    protected static final Context.Key<JavafxTypeMorpher> typeMorpherKey =
            new Context.Key<JavafxTypeMorpher>();

    private final JavafxDefs defs;
    private final Name.Table names;
    final JavafxClassReader reader;
    private final JavafxSymtab syms;
    private final JavafxTypes types;

    public final LocationNameSymType[] locationNCT;
    public final LocationNameSymType[] variableNCT;
    public final LocationNameSymType[] constantLocationNCT;
    public final LocationNameSymType   baseLocation;
    public final LocationNameSymType abstractBoundComprehension;

    private final Object[] defaultValueByKind;

    public class LocationNameSymType {
        public final Name name;
        public final ClassSymbol sym;
        public final Type type;
        private LocationNameSymType(Name name) {
            this.name = name;
            sym = reader.jreader.enterClass(name);
            type = sym.type;
        }
        private LocationNameSymType(String which) {
            this(locationPackageNameString, which);
        }
        private LocationNameSymType(String pkg, String which) {
            this(names.fromString(pkg + "." + which));
        }
    }

    enum VarRepresentation {
        NeverLocation,
        SlackerLocation,
        AlwaysLocation;

        boolean isSlacker() { return this==SlackerLocation; }
        boolean possiblyLocation() { return this!=NeverLocation; }
    }

    private Map<Symbol, VarMorphInfo> vmiMap = new HashMap<Symbol, VarMorphInfo>();

    public class VarMorphInfo extends TypeMorphInfo {
        private final Symbol sym;
        private final VarRepresentation representation;

        VarMorphInfo(Symbol sym) {
            super((sym.kind == Kinds.MTH)? ((MethodType)sym.type).getReturnType() : sym.type);
            this.sym = sym;
            this.representation = computeRepresentation(sym);
        }

        Symbol getSymbol() {
            return sym;
        }

        VarRepresentation representation() {
            return representation;
        }

        boolean useAccessors() {
            return isFXMemberVariable() && !isSequence();
        }

        boolean isMemberVariable() {
            return sym.owner.kind == Kinds.TYP;
        }

        boolean isFXMemberVariable() {
            return isMemberVariable() && JavafxTypeMorpher.this.types.isJFXClass(sym.owner);
        }

    }

    public class TypeMorphInfo {
        private Type realType;
        private final Type morphedVariableType;
        private final Type morphedLocationType;
        private int typeKind;
        private Type elementType = null;

        TypeMorphInfo(Type symType) {
            TypeSymbol realTsym = symType.tsym;
            //check if symbol is already a Location, needed for source class
            assert
                (realTsym != variableNCT[TYPE_KIND_OBJECT].sym) &&
                (realTsym != variableNCT[TYPE_KIND_BOOLEAN].sym) &&
                (realTsym != variableNCT[TYPE_KIND_CHAR].sym) &&
                (realTsym != variableNCT[TYPE_KIND_BYTE].sym) &&
                (realTsym != variableNCT[TYPE_KIND_SHORT].sym) &&
                (realTsym != variableNCT[TYPE_KIND_INT].sym) &&
                (realTsym != variableNCT[TYPE_KIND_LONG].sym) &&
                (realTsym != variableNCT[TYPE_KIND_FLOAT].sym) &&
                (realTsym != variableNCT[TYPE_KIND_DOUBLE].sym) &&
                (realTsym != variableNCT[TYPE_KIND_SEQUENCE].sym) : "Locations should have been converted";

            this.realType = symType;

            if (symType.isPrimitive()) {
                typeKind = kindFromPrimitiveType(realTsym);
            } else {
                if (isSequence()) {
                    typeKind = TYPE_KIND_SEQUENCE;
                    elementType = types.elementType(symType);
                } else {
                    typeKind = TYPE_KIND_OBJECT;
                    elementType = realType;
                }
            }

            // must be called AFTER typeKind and realType are set in vsym
            this.morphedVariableType = symType == syms.voidType ? symType : generifyIfNeeded(variableType(typeKind), this);
            this.morphedLocationType = symType == syms.voidType ? symType : generifyIfNeeded(locationType(typeKind), this);
        }

        protected boolean isSequence() {
            return types.isSequence(realType);
        }

        public Type getRealType() { return realType; }
        public Type getRealBoxedType() { return types.boxedTypeOrType(realType); }
        public Type getRealFXType() { return (realType.isPrimitive() && typeKind==TYPE_KIND_OBJECT)? types.boxedTypeOrType(realType) : realType; }

        public Type getLocationType() { return morphedLocationType; }
        public Type getVariableType() { return morphedVariableType; }
        public Type getConstantLocationType() { return generifyIfNeeded(constantLocationNCT[typeKind].type, this); }
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

        defs = JavafxDefs.instance(context);
        syms = (JavafxSymtab)(JavafxSymtab.instance(context));
        types = JavafxTypes.instance(context);
        names = Name.Table.instance(context);
        reader = JavafxClassReader.instance(context);

        variableNCT = new LocationNameSymType[TYPE_KIND_COUNT];
        locationNCT = new LocationNameSymType[TYPE_KIND_COUNT];
        constantLocationNCT = new LocationNameSymType[TYPE_KIND_COUNT];
        abstractBoundComprehension = new LocationNameSymType(sequencePackageNameString, "AbstractBoundComprehension");

        for (int kind = 0; kind < TYPE_KIND_COUNT; ++kind) {
            variableNCT[kind] = new LocationNameSymType(defs.locationVariableName[kind]);
            locationNCT[kind] = new LocationNameSymType(defs.locationInterfaceName[kind]);
            constantLocationNCT[kind] = new LocationNameSymType(JavafxVarSymbol.getTypePrefix(kind) + "Constant");
        }

        baseLocation = new LocationNameSymType("Location");

        defaultValueByKind = new Object[TYPE_KIND_COUNT];
        defaultValueByKind[TYPE_KIND_OBJECT] = null;
        defaultValueByKind[TYPE_KIND_BOOLEAN] = 0;
        defaultValueByKind[TYPE_KIND_CHAR] = 0;
        defaultValueByKind[TYPE_KIND_BYTE] = 0;
        defaultValueByKind[TYPE_KIND_SHORT] = 0;
        defaultValueByKind[TYPE_KIND_INT] = 0;
        defaultValueByKind[TYPE_KIND_LONG] = 0L;
        defaultValueByKind[TYPE_KIND_FLOAT] = (float)0.0;
        defaultValueByKind[TYPE_KIND_DOUBLE] = 0.0;
        defaultValueByKind[TYPE_KIND_SEQUENCE] = null; // Empty sequence done programatically
    }

    public int kindFromPrimitiveType(TypeSymbol tsym) {
        if (tsym == syms.booleanType.tsym) return TYPE_KIND_BOOLEAN;
        if (tsym == syms.charType.tsym) return TYPE_KIND_CHAR;
        if (tsym == syms.byteType.tsym) return TYPE_KIND_BYTE;
        if (tsym == syms.shortType.tsym) return TYPE_KIND_SHORT;
        if (tsym == syms.intType.tsym) return TYPE_KIND_INT;
        if (tsym == syms.longType.tsym) return TYPE_KIND_LONG;
        if (tsym == syms.floatType.tsym) return TYPE_KIND_FLOAT;
        if (tsym == syms.doubleType.tsym) return TYPE_KIND_DOUBLE;
        assert false : "should not reach here";
        return TYPE_KIND_OBJECT;
    }

    private VarRepresentation computeRepresentation(Symbol sym) {
        if (sym.kind == Kinds.VAR) {
            final Symbol owner = sym.owner;
            final long flags = sym.flags();
            final boolean isMemberVar = owner.kind == Kinds.TYP;
            final boolean isMixinVar = isMemberVar && (owner.flags() & MIXIN) != 0;
            final boolean isLocalVar = !isMemberVar;
            final boolean isAssignedTo = (flags & (VARUSE_INIT_ASSIGNED_TO | VARUSE_ASSIGNED_TO)) != 0;
            final boolean isParameter = (flags & Flags.PARAMETER) != 0;
            final boolean isStatic = (flags & Flags.STATIC) != 0;
            final boolean hasInnerAccess = (flags & VARUSE_INNER_ACCESS) != 0;
            final boolean hasOnReplace = (flags & VARUSE_HAS_ON_REPLACE) != 0;
            final boolean usedInBind = (flags & (VARUSE_USED_IN_BIND|VARUSE_BOUND_INIT)) != 0;
            final boolean canWriteOutsideScript = (flags & (PUBLIC | PROTECTED | PACKAGE_ACCESS)) != 0L && (flags & IS_DEF) == 0L;
            final boolean canOverrideOutsideScript = canWriteOutsideScript;
            final boolean isOverriden = (flags & VARUSE_OVERRIDDEN) != 0L;
            final boolean readIsScriptPrivate = (flags & (PUBLIC | PROTECTED | PACKAGE_ACCESS | PUBLIC_READ | PUBLIC_INIT)) == 0L;

            if (sym.flatName() == names._super || sym.flatName() == names._this) {
                // 'this' and 'super' can't be made into Locations
                return NeverLocation;
            }
            if (isMemberVar && !types.isJFXClass(owner)) {
                // Java fields are never Locations
                return NeverLocation;
            }
            if (!isParameter && !isMemberVar && hasInnerAccess) {
                // Local variables must be Locations if they are accessed within an inner class
                // Because of proper sequencing, this is true even if the var isn't assigned to
                //TODO: optimize the cases where the initializer does not depend on other local vars
                return AlwaysLocation;
            }
            if (isParameter) {
                // Otherwise parameters are Locations only if in bound contexts, for-loops induction vars, bound function params
                return (flags & VARUSE_BOUND_DEFINITION) != 0? AlwaysLocation : NeverLocation;
            }
            if(hasOnReplace && (!isMemberVar || isStatic || isMixinVar)) {
                // Local vars with on-replace always need to be Locations, member vars have on-replace in-lined
                return AlwaysLocation;
            }
            if (types.isSequence(sym.type)) {
                // for a sequence to be modified it must be a Location
                //TODO: check for sequence variables which are never modified (no insert, delete, assignment, etc)
                //NOTE: If we do optimize the never-modified case, then we need
                //   to explicitly do incrementSharing() on initialization.

                return AlwaysLocation;
            }
            if (sym.type instanceof MethodType) {
                // Function values have wierd behavior, we just don't want to go there
                return AlwaysLocation;
            }
            if( (flags & (VARUSE_SELF_REFERENCE | VARUSE_IS_INITIALIZED_USED)) != 0 ) {
                // Reference to the var within its own initializer requires a Location.
                // To be able to use isInitialized()  requires a Location.
                return AlwaysLocation;
            }
            if ((flags & VARUSE_BOUND_DEFINITION) != 0) {
                if (isStatic || hasOnReplace || isLocalVar || isMixinVar || (flags & (VARUSE_SELF_REFERENCE | VARUSE_INIT_HAS_SIDE_EFFECTS_OR_NASTY)) != 0L) {
                    return AlwaysLocation;
                } else if (readIsScriptPrivate && !isOverriden && !canOverrideOutsideScript && (flags & (VARUSE_USED_IN_BIND | VARUSE_OBJ_LIT_INIT)) == 0L ) {
                    return NeverLocation;
                } else {
                    return SlackerLocation;
                }
            }

            if (isMemberVar) {  // class or script var
                /*
                To be able to elide member vars we need to know that
                (1) the var will not be defined by a bound expression
                (2) it will not have an 'on replace' on its definition/override
                (3a) it will either never be used in a bind or
                (3b) its value will not change after initialization.

                For (1) and (2) the var must not, within the script, be defined by a bound expression or have an 'on replace'
                and, so that these cannot occur outside the script, its base access must be script-private (it can be
                public-read or public-init) or it must be a 'def'

                Additionally, for (3), either (3a) must hold by virtue of it not being used in a bind within the script and
                its access is script-private (no public-* either)
                or, (3b) holds, the value isn't assigned by the script after initialization and the access prevents this
                from occurring externally ('def' or script-private with optional public-read or public-init).

                The (1) and (2) checks are handled by general checks (above).
                */

                // Overridden variables need to have Locations since overridden on-replace expects it.
                // It is OK that the overridden analsysis is across all files in the compile since
                // this is moot for script-private and unwritable-outside-script variables cannot be
                // overridden except in the script.
                //TODO: Once the bind translation is smart about collapsing expressions, then making unchanging values Locations is wrong.
                // vars which are used in a bind should be Locations (even if never changed) since otherwise they will dynamically be turned to Locations
                if (!isOverriden && !usedInBind) {

                    // (3a) check.  Not used in bind has already been checked (above).
                    // Check that it is not accessible outside the script (so noone else can bind it).
                    if (readIsScriptPrivate) {
                        return NeverLocation;
                    }

                    // (3b) check.  No assignments ((no longer) except in init{}) and
                    // permissions such that this can't be done externally, or it is a 'def'.
                    //JFXC-2026 : Elide unassigned and externally unassignable member vars
                    //JFXC-2103 -- allow public-init
                    if (!isAssignedTo && !canWriteOutsideScript) {
                        return NeverLocation;
                    }
                }

                return SlackerLocation;
            }
        }
        return NeverLocation;
    }

    Type variableType(int typeKind) {
        return variableNCT[typeKind].type;
    }

    Type locationType(int typeKind) {
        return locationNCT[typeKind].type;
    }

    /** Add type parameters.
     * Returns a bogus hybrid front-end/back-end Type that is only meaningful
     * as an argument to makeTypeTree.
     * FIXME when translation creates attributes trees.
     */
    Type generifyIfNeeded(Type aLocationType, TypeMorphInfo tmi) {
        Type newType;
        Type elemType = tmi.getElementType();
        if ((tmi.getTypeKind() == TYPE_KIND_OBJECT ||
                tmi.getTypeKind() == TYPE_KIND_SEQUENCE) ) {
            if (elemType == null) {
                /* handles library which doesn't have element type */
                elemType = syms.objectType;
            }
            else
                elemType = types.boxedTypeOrType(elemType);
            List<Type> actuals = List.of(elemType);
            Type clazzOuter = variableType(tmi.getTypeKind()).getEnclosingType();

            List<Type> newActuals = List.nil();
            for (Type t : actuals) {
                if ((t.tsym instanceof ClassSymbol) &&
                        (t.tsym.flags_field & MIXIN) != 0) {
                    String str = t.tsym.name.toString().replace("$", ".");
                    ClassSymbol csym = new JavafxClassSymbol(0, names.fromString(str), t.tsym.owner);
                    csym.flags_field |= MIXIN;
                    Type tp = new ClassType(Type.noType, List.<Type>nil(), csym);
                    newActuals = newActuals.append(tp);
                    break;
                }

                newActuals = newActuals.append(t);
            }

            newType = new ClassType(clazzOuter, newActuals, aLocationType.tsym);
        } else {
            newType = aLocationType;
        }
        return newType;
    }
}
