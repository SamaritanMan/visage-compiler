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

package org.visage.tools.comp;

import java.util.IdentityHashMap;
import java.util.Set;
import java.util.HashSet;

import javax.tools.JavaFileObject;
import com.sun.tools.mjavac.jvm.ClassReader;
import com.sun.tools.mjavac.code.*;
import com.sun.tools.mjavac.code.Type.*;
import com.sun.tools.mjavac.code.Symbol.*;
import com.sun.tools.mjavac.util.*;
import com.sun.tools.mjavac.util.List;

import static com.sun.tools.mjavac.code.Flags.*;
import static com.sun.tools.mjavac.code.Kinds.*;
import static com.sun.tools.mjavac.code.TypeTags.*;
import org.visage.tools.code.VisageClassSymbol;
import org.visage.tools.code.VisageSymtab;
import org.visage.tools.code.VisageFlags;
import org.visage.tools.code.VisageTypes;
import org.visage.tools.code.VisageVarSymbol;
import org.visage.tools.tree.VisageTreeMaker;
import org.visage.tools.util.MsgSym;

import org.visage.tools.main.VisageCompiler;

/** Provides operations to read a classfile into an internal
 *  representation. The internal representation is anchored in a
 *  VisageClassSymbol which contains in its scope symbol representations
 *  for all other definitions in the classfile. Top-level Classes themselves
 *  appear as members of the scopes of PackageSymbols.
 *
 *  We delegate actual classfile-reading to javac's ClassReader, and then
 *  translates the resulting ClassSymbol to VisageClassSymbol, doing some
 *  renaming etc to make the resulting Symbols and Types match those produced
 *  by the parser.  This munging is incomplete, and there are still places
 *  where the compiler needs to know if a class comes from the parser or a
 *  classfile; those places will hopefully become fewer.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
public class VisageClassReader extends ClassReader {
    protected static final Context.Key<ClassReader> backendClassReaderKey =
         new Context.Key<ClassReader>();

    private final VisageDefs defs;
    protected final VisageTypes visageTypes;
    protected final VisageTreeMaker visagemake;

    /** The raw class-reader, shared by the back-end. */
    public ClassReader jreader;

    private final Name functionClassPrefixName;
    private Context ctx;
    private Messages messages;
    
    public static void preRegister(final Context context, final ClassReader jreader) {
        context.put(backendClassReaderKey, jreader);
        Object instance = context.get(classReaderKey);
        if (instance instanceof VisageClassReader)
            ((VisageClassReader) instance).jreader = jreader;
        else
            preRegister(context);
    }
    public static void preRegister(final Context context) {
        context.put(classReaderKey, new Context.Factory<ClassReader>() {
	       public VisageClassReader make() {
		   VisageClassReader reader = new VisageClassReader(context, true);
                   reader.jreader = context.get(backendClassReaderKey);
                   return reader;
	       }
        });
    }

    public static VisageClassReader instance(Context context) {
        VisageClassReader instance = (VisageClassReader) context.get(classReaderKey);
        if (instance == null)
            instance = new VisageClassReader(context, true);
        return instance;
    }

    /** Construct a new class reader, optionally treated as the
     *  definitive classreader for this invocation.
     */
    protected VisageClassReader(Context context, boolean definitive) {
        super(context, definitive);
        defs = VisageDefs.instance(context);
        visageTypes = VisageTypes.instance(context);
        visagemake = VisageTreeMaker.instance(context);
        functionClassPrefixName = names.fromString(VisageSymtab.functionClassPrefix);
        ctx = context;
        messages = Messages.instance(context);
    }

    public Name.Table getNames() {
        return names;
    }
    
    /** Reassign names of classes that might have been loaded with
      * their flat names. */
    void fixupFullname (VisageClassSymbol cSym, ClassSymbol jsymbol) {
        if (cSym.fullname != jsymbol.fullname &&
                cSym.owner.kind == PCK && jsymbol.owner.kind == TYP) {
            cSym.owner.members().remove(cSym);
            cSym.name = jsymbol.name;
            ClassSymbol owner = enterClass(((ClassSymbol) jsymbol.owner).flatname);
            cSym.owner = owner;
            cSym.fullname = ClassSymbol.formFullName(cSym.name, owner);
        }
    }

    public VisageClassSymbol enterClass(ClassSymbol jsymbol) {
        Name className = jsymbol.flatname;
        boolean mixin = className.endsWith(defs.mixinClassSuffixName);
        if (mixin)
            className = className.subName(0, className.len - defs.mixinClassSuffixName.len);
        VisageClassSymbol cSym = (VisageClassSymbol) enterClass(className);
        //cSym.flags_field |= jsymbol.flags_field;
        if (mixin)
            cSym.flags_field |= VisageFlags.MIXIN;
        else {
            fixupFullname(cSym, jsymbol);
            cSym.jsymbol = jsymbol;
        }
        return cSym;
    }

    /** Define a new class given its name and owner.
     */
    @Override
    public ClassSymbol defineClass(Name name, Symbol owner) {
        ClassSymbol c = new VisageClassSymbol(0, name, owner);
        if (owner.kind == PCK)
            assert classes.get(c.flatname) == null : c;
        c.completer = this;
        return c;
    }

    /* FIXME: The re-written class-reader doesn't translate annotations yet.
 
    protected void attachAnnotations(final Symbol sym) {
        int numAttributes = nextChar();
        if (numAttributes != 0) {
            ListBuffer<CompoundAnnotationProxy> proxies =
                new ListBuffer<CompoundAnnotationProxy>();
            for (int i = 0; i<numAttributes; i++) {
                CompoundAnnotationProxy proxy = readCompoundAnnotation();
                if (proxy.type.tsym == syms.proprietaryType.tsym)
                    sym.flags_field |= PROPRIETARY;
                else {
                        proxies.append(proxy);
                }
            }
            annotate.later(new VisageAnnotationCompleter(sym, proxies.toList(), this));
        }
    }

    static public class VisageAnnotationCompleter extends AnnotationCompleter {
        VisageClassReader classReader;
        public VisageAnnotationCompleter(Symbol sym, List<CompoundAnnotationProxy> l, ClassReader classReader) {
            super(sym, l, classReader);
            this.classReader = (VisageClassReader)classReader;
        }
        // implement Annotate.Annotator.enterAnnotation()
        public void enterAnnotation() {
            JavaFileObject previousClassFile = classReader.currentClassFile;
            try {
                classReader.currentClassFile = classFile;
                List<Attribute.Compound> newList = deproxyCompoundList(l);
                VisageSymtab visageSyms = (VisageSymtab)classReader.syms;
                for (Attribute.Compound comp : newList) {
                }

                sym.attributes_field = ((sym.attributes_field == null)
                                        ? newList
                                        : newList.prependList(sym.attributes_field));
            } finally {
                classReader.currentClassFile = previousClassFile;
            }
        }
    }
    */

    /** Map javac Type/Symbol to visage Type/Symbol. */
    IdentityHashMap<Object,Object> typeMap = new IdentityHashMap<Object,Object>();
    
    /** Translate a List of raw JVM types to Visage types. */
    List<Type> translateTypes (List<Type> types) {
        if (types == null)
            return null;
        List<Type> ts = (List<Type>) typeMap.get(types);
        if (ts != null)
            return ts;
        ListBuffer<Type> rs = new ListBuffer<Type>();
        for (List<Type> t = types;
                 t.tail != null;
                 t = t.tail)
            rs.append(translateType(t.head));
        ts = rs.toList();
        typeMap.put(types, ts);
        return ts;
    }

    /** Translate raw JVM type to Visage type. */
    Type translateType (Type type) {
        if (type == null)
            return null;
        Type t = (Type) typeMap.get(type);
        if (t != null)
            return t;
        switch (type.tag) {
            case VOID:
                t = syms.voidType;
                break;
            case BOOLEAN:
                t = syms.booleanType;
                break;
            case CHAR:
                t = syms.charType;
                break;
            case BYTE:
                t = syms.byteType;
                break;
            case SHORT:
                t = syms.shortType;
                break;
            case INT:
                t = syms.intType;
                break;
            case LONG:
                t = syms.longType;
                break;
            case DOUBLE:
                t = syms.doubleType;
                break;
            case FLOAT:
                t = syms.floatType;
                break;
            case TYPEVAR:
                TypeVar tv = (TypeVar) type;
                TypeVar tx = new TypeVar(null, (Type) null, (Type) null);
                typeMap.put(type, tx); // In case of a cycle.
                tx.bound = translateType(tv.bound);
                tx.lower = translateType(tv.lower);
                tx.tsym = new TypeSymbol(0, tv.tsym.name, tx, translateSymbol(tv.tsym.owner));
                return tx;
            case FORALL:
                ForAll tf = (ForAll) type;
                t = new ForAll(translateTypes(tf.tvars), translateType(tf.qtype));
                break;
            case WILDCARD:
                WildcardType wt = (WildcardType) type;
                t = new WildcardType(translateType(wt.type), wt.kind,
                        translateTypeSymbol(wt.tsym));
                break;
            case CLASS:
                TypeSymbol tsym = type.tsym;
                if (tsym instanceof ClassSymbol) {
                    if (tsym.name.endsWith(defs.mixinClassSuffixName)) {
                        t = enterClass((ClassSymbol) tsym).type;
                        break;
                    }
                    final ClassType ctype = (ClassType) type;
                    if (ctype.isCompound()) {
                        t = types.makeCompoundType(translateTypes(ctype.interfaces_field), translateType(ctype.supertype_field));
                        break;
                    }
                    Name flatname = ((ClassSymbol) tsym).flatname;
                    if (flatname.startsWith(functionClassPrefixName)
                        && flatname != functionClassPrefixName) {
                        t = ((VisageSymtab) syms).makeFunctionType(translateTypes(ctype.typarams_field));
                        break;
                    }
                    TypeSymbol sym = translateTypeSymbol(tsym);
                    ClassType ntype;
                    if (tsym.type == type)
                        ntype = (ClassType) sym.type;
                    else
                        ntype = new ClassType(Type.noType, List.<Type>nil(), sym) {
                            boolean completed = false;
                            @Override
                            public Type getEnclosingType() {
                                if (!completed) {
                                    completed = true;
                                    tsym.complete();
                                    super.setEnclosingType(translateType(ctype.getEnclosingType()));
                                }
                                return super.getEnclosingType();
                            }
                            @Override
                            public void setEnclosingType(Type outer) {
                                throw new UnsupportedOperationException();
                            }
                            @Override
                            public boolean equals(Object t) {
                                return super.equals(t);
                            }

                            @Override
                            public int hashCode() {
                                return super.hashCode();
                            }
                        };
                    typeMap.put(type, ntype); // In case of a cycle.
                    ntype.typarams_field = translateTypes(type.getTypeArguments());
                    return ntype;
                }
                break;
            case ARRAY:
                t = new ArrayType(translateType(((ArrayType) type).elemtype), syms.arrayClass);
                break;
            case METHOD:
                t = new MethodType(translateTypes(type.getParameterTypes()),
                        translateType(type.getReturnType()),
                        translateTypes(type.getThrownTypes()),
                        syms.methodClass);
                break;
            default:
                t = type; // FIXME
        }
        typeMap.put(type, t);
        return t;
    }

    TypeSymbol translateTypeSymbol(TypeSymbol tsym) {
        if (tsym == syms.predefClass)
            return tsym;
        ClassSymbol csym = (ClassSymbol) tsym; // FIXME
        return enterClass(csym);
    }
    
    Symbol translateSymbol(Symbol sym) {
        if (sym == null)
            return null;
        Symbol s = (Symbol) typeMap.get(sym);
        if (s != null)
            return s;
        if (sym instanceof PackageSymbol)
            s = enterPackage(((PackageSymbol) sym).fullname);
        else if (sym instanceof MethodSymbol) {
            Symbol owner = translateSymbol(sym.owner);
            MethodSymbol m = translateMethodSymbol(sym.flags_field, sym, owner);
            ((ClassSymbol) owner).members_field.enter(m);
            s = m;
        }
        else
            s = translateTypeSymbol((TypeSymbol) sym);
        typeMap.put(sym, s);
        return s;
    }
    
    void popMethodTypeArg(MethodType type) {
        List<Type> argtypes = type.argtypes;
        ListBuffer<Type> lb = ListBuffer.<Type>lb();
        
        for (int i = 1; i < argtypes.size(); i++) {
            lb.append(argtypes.get(i));
        }
        
        type.argtypes = lb.toList();
    }
    
    MethodSymbol translateMethodSymbol(long flags, Symbol sym, Symbol owner) {
        Name name = sym.name;
        Type mtype = sym.type;
        String nameString = name.toString();
        
        int boundStringIndex = nameString.indexOf(VisageDefs.boundFunctionDollarSuffix);
        if (boundStringIndex != -1) {
            // this is a bound function
            // remove the bound suffix, and mark as bound
            nameString = nameString.substring(0, boundStringIndex);
            flags |= VisageFlags.BOUND;
        }
        VisageSymtab visageSyms = (VisageSymtab) this.syms;
        for (Attribute.Compound ann : sym.getAnnotationMirrors()) {
            if (ann.type.tsym.flatName() == visageSyms.visage_signatureAnnotationType.tsym.flatName()) {
                String sig = (String)ann.values.head.snd.getValue();
                signatureBuffer = new byte[sig.length()];
                try {
                    mtype = sigToType(names.fromString(sig));
                }
                catch (Exception e) {
                    throw new AssertionError("Bad Visage signature");
                }
            }
        }
        Type type = translateType(mtype);
        if (type instanceof MethodType) {
            boolean convertToStatic = false;
            
            if (nameString.endsWith(VisageDefs.implFunctionSuffix)) {
                nameString = nameString.substring(0, nameString.length() - VisageDefs.implFunctionSuffix.length());
                convertToStatic = true;
            }
            
            if (convertToStatic) {
                flags &= ~Flags.STATIC;
                popMethodTypeArg((MethodType)type);
            }
        }
    
        name = names.fromString(nameString);
        
        return new MethodSymbol(flags, name, type, owner);
    }

    // VSGC-2849 - Mixins: Change the mixin interface from $Intf to $Mixin.
    private void checkForIntfSymbol(Symbol sym) throws CompletionFailure {        
        if (sym.name.endsWith(defs.deprecatedInterfaceSuffixName)) {
            String fileString = ((ClassSymbol) sym).classfile.getName();
            String message = messages.getLocalizedString(MsgSym.MESSAGEPREFIX_COMPILER_MISC +
                                                            MsgSym.MESSAGE_DEPRECATED_INTERFACE_CLASS,
                                                         fileString);
            log.rawError(Position.NOPOS, message);
            throw new CompletionFailure(sym, message);
        }
    }

    @Override
    public void complete(Symbol sym) throws CompletionFailure {
        checkForIntfSymbol(sym);
        if (jreader.sourceCompleter == null)
           jreader.sourceCompleter = VisageCompiler.instance(ctx);
        if (sym instanceof PackageSymbol) {
            PackageSymbol psym = (PackageSymbol) sym;
            PackageSymbol jpackage;
            if (psym == syms.unnamedPackage)
                jpackage = jreader.syms.unnamedPackage;
            else
                jpackage = jreader.enterPackage(psym.fullname);
            jpackage.complete();
            if (psym.members_field == null) psym.members_field = new Scope(psym);
            for (Scope.Entry e = jpackage.members_field.elems;
                 e != null;  e = e.sibling) {
                 if (e.sym instanceof ClassSymbol) {
                     ClassSymbol jsym = (ClassSymbol) e.sym;
                     if (jsym.name.endsWith(defs.mixinClassSuffixName))
                         continue;
                     VisageClassSymbol csym = enterClass(jsym);
                     psym.members_field.enter(csym);
                     csym.classfile = jsym.classfile;
                     csym.jsymbol = jsym;
                 }
            }
            if (jpackage.exists())
                psym.flags_field |= EXISTS;
        } else {
            sym.owner.complete();
            VisageClassSymbol csym = (VisageClassSymbol) sym;
            ClassSymbol jsymbol = csym.jsymbol;
            if (jsymbol != null && jsymbol.classfile != null && 
                jsymbol.classfile.getKind() == JavaFileObject.Kind.SOURCE &&
                jsymbol.classfile.getName().endsWith(".visage")) {
                SourceCompleter visageSourceCompleter = VisageCompiler.instance(ctx);
                visageSourceCompleter.complete(csym);
                return;
            } else { 
                csym.jsymbol = jsymbol = jreader.loadClass(csym.flatname);
            }
            fixupFullname(csym, jsymbol);
            typeMap.put(jsymbol, csym);
            jsymbol.classfile = ((ClassSymbol) sym).classfile;
            
            ClassType ct = (ClassType)csym.type;
            ClassType jt = (ClassType)jsymbol.type;
            csym.members_field = new Scope(csym);

            // flags are derived from flag bits and access modifier annoations
            csym.flags_field = flagsFromAnnotationsAndFlags(jsymbol);
            
            ct.typarams_field = translateTypes(jt.typarams_field);
            ct.setEnclosingType(translateType(jt.getEnclosingType()));
            
            ct.supertype_field = translateType(jt.supertype_field);
            
            // VSGC-2841 - Mixins: Cannot find firePropertyChange method in SwingComboBox.visage
            if (ct.supertype_field != null && 
                ct.supertype_field.tsym != null &&
                ct.supertype_field.tsym.kind == TYP) {
                
            }
            
            ListBuffer<Type> interfaces = new ListBuffer<Type>();
            Type iface = null;
            if (jt.interfaces_field != null) { // true for ErrorType
                for (List<Type> it = jt.interfaces_field;
                     it.tail != null;
                     it = it.tail) {
                    Type itype = it.head;
                    checkForIntfSymbol(itype.tsym);
                    if (((ClassSymbol) itype.tsym).flatname == defs.cObjectName) {
                        csym.flags_field |= VisageFlags.VISAGE_CLASS;
                    } else if (((ClassSymbol) itype.tsym).flatname == defs.cMixinName) {
                        csym.flags_field |= VisageFlags.MIXIN | VisageFlags.VISAGE_CLASS;
                    } else if ((csym.fullname.len + defs.mixinClassSuffixName.len ==
                             ((ClassSymbol) itype.tsym).fullname.len) &&
                            ((ClassSymbol) itype.tsym).fullname.startsWith(csym.fullname) &&
                            itype.tsym.name.endsWith(defs.mixinClassSuffixName)) {
                        iface = itype;
                        iface.tsym.complete();
                        csym.flags_field |= VisageFlags.MIXIN | VisageFlags.VISAGE_CLASS;
                    } else {
                        itype = translateType(itype);
                        interfaces.append(itype);
                    }
                }
            }
           
            if (iface != null) {
                for (List<Type> it = ((ClassType) iface.tsym.type).interfaces_field;
                 it.tail != null;
                 it = it.tail) {
                    Type itype = it.head;
                    checkForIntfSymbol(itype.tsym);
                    if (((ClassSymbol) itype.tsym).flatname == defs.cObjectName) {
                        csym.flags_field |= VisageFlags.VISAGE_CLASS;
                    } else if (((ClassSymbol) itype.tsym).flatname == defs.cMixinName) {
                        csym.flags_field |= VisageFlags.MIXIN | VisageFlags.VISAGE_CLASS;
                    } else {
                        itype = translateType(itype);
                        interfaces.append(itype);
                    }
                }
            }
            ct.interfaces_field = interfaces.toList();

            // Now translate the members.
            // Do an initial "reverse" pass so we copy the order.
            List<Symbol> symlist = List.nil();
            for (Scope.Entry e = jsymbol.members_field.elems;
                 e != null;  e = e.sibling) {
                if ((e.sym.flags_field & SYNTHETIC) != 0)
                    continue;
                symlist = symlist.prepend(e.sym);
            }
            boolean isVisageClass = (csym.flags_field & VisageFlags.VISAGE_CLASS) != 0;
            boolean isMixinClass = (csym.flags_field & VisageFlags.MIXIN) != 0;
            
            VisageVarSymbol scriptAccessSymbol = isVisageClass ? visagemake.ScriptAccessSymbol(csym) : null;

            Set<Name> priorNames = new HashSet<Name>();
            handleSyms:
            for (List<Symbol> l = symlist; l.nonEmpty(); l=l.tail) {
                Symbol memsym = l.head;
                Name name = memsym.name;
                long flags = flagsFromAnnotationsAndFlags(memsym);
                if ((flags & PRIVATE) != 0)
                    continue;
                boolean sawSourceNameAnnotation = false;
                VisageSymtab visageSyms = (VisageSymtab) this.syms;
                for (Attribute.Compound a : memsym.getAnnotationMirrors()) {
                    if (a.type.tsym.flatName() == visageSyms.visage_staticAnnotationType.tsym.flatName()) {
                        flags |=  Flags.STATIC;
                    } else if (a.type.tsym.flatName() == visageSyms.visage_defAnnotationType.tsym.flatName()) {
                        flags |=  VisageFlags.IS_DEF;
                    } else if (a.type.tsym.flatName() == visageSyms.visage_defaultAnnotationType.tsym.flatName()) {
                        flags |=  VisageFlags.DEFAULT;
                    } else if (a.type.tsym.flatName() == visageSyms.visage_publicInitAnnotationType.tsym.flatName()) {
                        flags |=  VisageFlags.PUBLIC_INIT;
                    } else if (a.type.tsym.flatName() == visageSyms.visage_publicReadAnnotationType.tsym.flatName()) {
                        flags |=  VisageFlags.PUBLIC_READ;
                    } else if (a.type.tsym.flatName() == visageSyms.visage_inheritedAnnotationType.tsym.flatName()) {
                        continue handleSyms;
                    } else if (a.type.tsym.flatName() == visageSyms.visage_sourceNameAnnotationType.tsym.flatName()) {
                        Attribute aa = a.member(name.table.value);
                        Object sourceName = aa.getValue();
                        if (sourceName instanceof String) {
                            name = names.fromString((String) sourceName);
                            sawSourceNameAnnotation = true;
                        }
                    }
                }
                if (memsym instanceof MethodSymbol) {
                    MethodSymbol m = translateMethodSymbol(flags, memsym, csym);     
                    csym.members_field.enter(m);
                }
                else if (memsym instanceof VarSymbol) {
                    // Eliminate any duplicate value/location.
                    if (priorNames.contains(name))
                        continue;
                    Type otype = memsym.type;
                    Type type = translateType(otype);
                    VisageVarSymbol v;
                    if (scriptAccessSymbol != null && name == scriptAccessSymbol.name) {
                        v = scriptAccessSymbol;
                    } else {
                        v = new VisageVarSymbol(visageTypes, names, flags, name, type, csym);
                        csym.addVar(v, (flags & STATIC) != 0);
                    }
                    csym.members_field.enter(v);
                    if ((flags & VisageFlags.DEFAULT) != 0) {
                        csym.setDefaultVar(name);
                    }
                    priorNames.add(name);
                }
                else {
                    memsym.flags_field = flags;
                    csym.members_field.enter(translateSymbol(memsym));
                }
            }
        }
    }
    
    private long flagsFromAnnotationsAndFlags(Symbol sym) {
        long initialFlags = sym.flags_field;
        long nonAccessFlags = initialFlags & ~VisageFlags.VisageAccessFlags;
        long accessFlags = initialFlags & VisageFlags.VisageAccessFlags;
        VisageSymtab visageSyms = (VisageSymtab) this.syms;
        for (Attribute.Compound a : sym.getAnnotationMirrors()) {
            if (a.type.tsym.flatName() == visageSyms.visage_protectedAnnotationType.tsym.flatName()) {
                accessFlags = Flags.PROTECTED;
            } else if (a.type.tsym.flatName() == visageSyms.visage_packageAnnotationType.tsym.flatName()) {
                accessFlags = 0L;
            } else if (a.type.tsym.flatName() == visageSyms.visage_publicAnnotationType.tsym.flatName()) {
                accessFlags = Flags.PUBLIC;
            } else if (a.type.tsym.flatName() == visageSyms.visage_scriptPrivateAnnotationType.tsym.flatName()) {
                accessFlags = VisageFlags.SCRIPT_PRIVATE;
            }
        }
        if (accessFlags == 0L) {
            accessFlags = VisageFlags.PACKAGE_ACCESS;
        }
        return nonAccessFlags | accessFlags;
    }
}
