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

import com.sun.tools.mjavac.code.*;
import com.sun.tools.mjavac.jvm.*;
import com.sun.tools.mjavac.util.*;
import com.sun.tools.mjavac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.mjavac.code.Symbol.*;
import com.sun.tools.mjavac.code.Type.*;

import static com.sun.tools.mjavac.code.Flags.*;
import static com.sun.tools.mjavac.code.Kinds.*;
import static com.sun.tools.mjavac.code.TypeTags.*;

import org.visage.tools.tree.*;
import org.visage.tools.code.VisageClassSymbol;
import org.visage.tools.code.VisageFlags;
import org.visage.tools.code.VisageSymtab;
import org.visage.tools.code.VisageTypes;
import org.visage.tools.code.VisageVarSymbol;
import org.visage.tools.util.MsgSym;

import javax.tools.JavaFileObject;
import java.util.Set;
import java.util.HashSet;

/**
 * Add local declarations to current environment.
 * The main entry point is {@code memberEnter}, which is called from
 * {@link VisageAttr} when {@code visit}-ing a tree that contains local
 * declarations.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
public class VisageMemberEnter extends VisageTreeScanner implements VisageVisitor, Completer {

    protected static final Context.Key<VisageMemberEnter> visageMemberEnterKey =
            new Context.Key<VisageMemberEnter>();
    protected final static boolean checkClash = true;
    private final Name.Table names;
    private final VisageEnter enter;
    private final Log log;
    private final VisageCheck chk;
    private final VisageAttr attr;
    private final VisageSymtab syms;
    private final VisageTreeMaker visagemake;
    private final ClassReader reader;
    private final VisageTodo todo;
    private final VisageAnnotate annotate;
    private final VisageTypes types;
    private final Target target;
    private final VisageBoundContextAnalysis boundAnalysis;

    public static VisageMemberEnter instance(Context context) {
        VisageMemberEnter instance = context.get(visageMemberEnterKey);
        if (instance == null) {
            instance = new VisageMemberEnter(context);
        }
        return instance;
    }

    protected VisageMemberEnter(Context context) {
        context.put(visageMemberEnterKey, this);
        names = Name.Table.instance(context);
        enter = VisageEnter.instance(context);
        log = Log.instance(context);
        chk = (VisageCheck) VisageCheck.instance(context);
        attr = VisageAttr.instance(context);
        syms = (VisageSymtab) VisageSymtab.instance(context);
        visagemake = (VisageTreeMaker) VisageTreeMaker.instance(context);
        reader = VisageClassReader.instance(context);
        boundAnalysis = VisageBoundContextAnalysis.instance(context);
        todo = VisageTodo.instance(context);
        annotate = VisageAnnotate.instance(context);
        types = VisageTypes.instance(context);
        target = Target.instance(context);
    }
    /** A queue for classes whose members still need to be entered into the
     *  symbol table.
     */
    ListBuffer<VisageEnv<VisageAttrContext>> halfcompleted = new ListBuffer<VisageEnv<VisageAttrContext>>();
    /** Set to true only when the first of a set of classes is
     *  processed from the halfcompleted queue.
     */
    boolean isFirst = true;
    /** A flag to disable completion from time to time during member
     *  enter, as we only need to look up types.  This avoids
     *  unnecessarily deep recursion.
     */
    boolean completionEnabled = true;

    /* ---------- Processing import clauses ----------------
     */
    /** Import all classes of a class or package on demand.
     *  @param pos           Position to be used for error reporting.
     *  @param tsym          The class or package the members of which are imported.
     *  @param toScope   The (import) scope in which imported classes
     *               are entered.
     */
    void importAll(int pos,
            final TypeSymbol tsym,
            VisageEnv<VisageAttrContext> env) {
        // Check that packages imported from exist (JLS ???).
        if (tsym.kind == PCK && tsym.members().elems == null && !tsym.exists()) {
            // If we can't find java.lang, exit immediately.
            if (((PackageSymbol) tsym).fullname.equals(names.java_lang)) {
                JCDiagnostic msg = JCDiagnostic.fragment(MsgSym.MESSAGE_FATAL_ERR_NO_JAVA_LANG);
                throw new FatalError(msg);
            } else {
                log.error(pos, MsgSym.MESSAGE_DOES_NOT_EXIST, tsym);
            }
        }
        final Scope fromScope = tsym.members();
        final Scope toScope = env.toplevel.starImportScope;
        for (Scope.Entry e = fromScope.elems; e != null; e = e.sibling) {
            if (e.sym.kind == TYP && !toScope.includes(e.sym)) {
                toScope.enter(e.sym, fromScope);
            }
        }
    }

    /** Import all static members of a class or package on demand.
     *  @param pos           Position to be used for error reporting.
     *  @param tsym          The class or package the members of which are imported.
     *  @param toScope   The (import) scope in which imported classes
     *               are entered.
     */
    private void importStaticAll(int pos,
            final TypeSymbol tsym,
            VisageEnv<VisageAttrContext> env) {
        final JavaFileObject sourcefile = env.toplevel.sourcefile;
        final Scope toScope = env.toplevel.starImportScope;
        final PackageSymbol packge = env.toplevel.packge;
        final TypeSymbol origin = tsym;

        // enter imported types immediately
        new Object() {

            Set<Symbol> processed = new HashSet<Symbol>();

            void importFrom(TypeSymbol tsym) {
                if (tsym == null || !processed.add(tsym)) {
                    return;
                }

                if (tsym instanceof ClassSymbol) {
                    // also import inherited names
                    if (tsym instanceof VisageClassSymbol) {
                        for (Type superType : types.supertypes(tsym.type)) {
                            importFrom(superType.tsym);
                        }
                    } else {
                        importFrom(types.supertype(tsym.type).tsym);
                    }
                    for (Type t : types.interfaces(tsym.type)) {
                        importFrom(t.tsym);
                    }
                }

                final Scope fromScope = tsym.members();
                for (Scope.Entry e = fromScope.elems; e != null; e = e.sibling) {
                    Symbol sym = e.sym;
                    if (sym.kind == TYP
                            && (sym.flags() & STATIC) != 0
                            && staticImportAccessible(sym, packge)
                            && sym.isMemberOf(origin, types)
                            && !toScope.includes(sym)) {
                        toScope.enter(sym, fromScope, origin.members());
                    }
                }
            }
        }.importFrom(tsym);

        // enter non-types before annotations that might use them
        annotate.earlier(new VisageAnnotate.Annotator() {

            Set<Symbol> processed = new HashSet<Symbol>();

            @Override
            public String toString() {
                return "import static " + tsym + ".*" + " in " + sourcefile;
            }

            void importFrom(TypeSymbol tsym) {
                if (tsym == null || !processed.add(tsym)) {
                    return;
                }

                if (tsym instanceof ClassSymbol) {
                    // also import inherited names
                    if (tsym instanceof VisageClassSymbol) {
                        for (Type superType : types.supertypes(tsym.type)) {
                            importFrom(superType.tsym);
                        }
                    } else {
                        importFrom(types.supertype(tsym.type).tsym);
                    }
                    for (Type t : types.interfaces(tsym.type)) {
                        importFrom(t.tsym);
                    }
                }

                final Scope fromScope = tsym.members();
                for (Scope.Entry e = fromScope.elems; e != null; e = e.sibling) {
                    Symbol sym = e.sym;
                    if (sym.isStatic() && sym.kind != TYP
                            && staticImportAccessible(sym, packge)
                            && !toScope.includes(sym)
                            && sym.isMemberOf(origin, types)) {
                        toScope.enter(sym, fromScope, origin.members());
                    }
                }
            }

            public void enterAnnotation() {
                importFrom(tsym);
            }
        });
    }

    // is the sym accessible everywhere in packge?
    boolean staticImportAccessible(Symbol sym, PackageSymbol packge) {
        // because the PACKAGE_ACCESS bit is too high for the switch, test it later
        int flags = (short) (sym.flags() & Flags.AccessFlags);
        switch (flags) {
            default:
            case PUBLIC:
                return true;
            case PRIVATE:
                return false;
            case 0:
                // 'package' vs script-private
                return (flags & VisageFlags.SCRIPT_PRIVATE) == 0;
            case PROTECTED:
                return sym.packge() == packge;
        }
    }

    /** Import statics types of a given name.  Non-types are handled in Attr.
     *  @param pos           Position to be used for error reporting.
     *  @param tsym          The class from which the name is imported.
     *  @param name          The (simple) name being imported.
     *  @param env           The environment containing the named import
     *                  scope to add to.
     */
    private void importNamedStatic(final DiagnosticPosition pos,
            final TypeSymbol tsym,
            final Name name,
            final VisageEnv<VisageAttrContext> env) {
        if (tsym.kind != TYP) {
            log.error(pos, MsgSym.MESSAGE_STATIC_IMP_ONLY_CLASSES_AND_INTERFACES);
            return;
        }

        final Scope toScope = env.toplevel.namedImportScope;
        final PackageSymbol packge = env.toplevel.packge;
        final TypeSymbol origin = tsym;

        // enter imported types immediately
        new Object() {

            Set<Symbol> processed = new HashSet<Symbol>();

            void importFrom(TypeSymbol tsym) {
                if (tsym == null || !processed.add(tsym)) {
                    return;
                }

                // also import inherited names
                if (tsym instanceof VisageClassSymbol) {
                    for (Type superType : types.supertypes(tsym.type)) {
                        importFrom(superType.tsym);
                    }
                } else {
                    importFrom(types.supertype(tsym.type).tsym);
                }
                for (Type t : types.interfaces(tsym.type)) {
                    importFrom(t.tsym);
                }

                for (Scope.Entry e = tsym.members().lookup(name);
                        e.scope != null;
                        e = e.next()) {
                    Symbol sym = e.sym;
                    if (sym.isStatic()
                            && sym.kind == TYP
                            && staticImportAccessible(sym, packge)
                            && sym.isMemberOf(origin, types)
                            && chk.checkUniqueStaticImport(pos, sym, toScope)) {
                        toScope.enter(sym, sym.owner.members(), origin.members());
                    }
                }
            }
        }.importFrom(tsym);

        // enter non-types before annotations that might use them
        annotate.earlier(new VisageAnnotate.Annotator() {

            Set<Symbol> processed = new HashSet<Symbol>();
            boolean found = false;

            @Override
            public String toString() {
                return "import static " + tsym + "." + name;
            }

            void importFrom(TypeSymbol tsym) {
                if (tsym == null || !processed.add(tsym)) {
                    return;
                }

                // also import inherited names
                if (tsym instanceof VisageClassSymbol) {
                    for (Type superType : types.supertypes(tsym.type)) {
                        importFrom(superType.tsym);
                    }
                } else {
                    importFrom(types.supertype(tsym.type).tsym);
                }
                for (Type t : types.interfaces(tsym.type)) {
                    importFrom(t.tsym);
                }

                for (Scope.Entry e = tsym.members().lookup(name);
                        e.scope != null;
                        e = e.next()) {
                    Symbol sym = e.sym;
                    if (sym.isStatic()
                            && staticImportAccessible(sym, packge)
                            && sym.isMemberOf(origin, types)) {
                        found = true;
                        if (sym.kind == MTH
                                || sym.kind != TYP && chk.checkUniqueStaticImport(pos, sym, toScope)) {
                            toScope.enter(sym, sym.owner.members(), origin.members());
                        }
                    }
                }
            }

            public void enterAnnotation() {
                JavaFileObject prev = log.useSource(env.toplevel.sourcefile);
                try {
                    importFrom(tsym);
                    if (!found) {
                        log.error(pos, MsgSym.MESSAGE_CANNOT_RESOLVE_LOCATION,
                                JCDiagnostic.fragment(MsgSym.KINDNAME_STATIC),
                                name, "", "", VisageResolve.typeKindName(tsym.type),
                                tsym.type);
                    }
                } finally {
                    log.useSource(prev);
                }
            }
        });
    }

    /** Import given class.
     *  @param pos           Position to be used for error reporting.
     *  @param tsym          The class to be imported.
     *  @param env           The environment containing the named import
     *                  scope to add to.
     */
    void importNamed(DiagnosticPosition pos, Symbol tsym, VisageEnv<VisageAttrContext> env) {
        if (tsym.kind == TYP
                && chk.checkUniqueImport(pos, tsym, env.toplevel.namedImportScope)) {
            env.toplevel.namedImportScope.enter(tsym, tsym.owner.members());
        }
    }

    private static void importNamed(Symbol tsym, Scope scope) {
        scope.enter(tsym, tsym.owner.members());
    }

    public static void importPredefs(VisageSymtab syms, Scope scope) {
        // Import-on-demand the Visage types
        importNamed(syms.objectType.tsym, scope);
        importNamed(syms.visage_BooleanType.tsym, scope);
        importNamed(syms.visage_CharacterType.tsym, scope);
        importNamed(syms.visage_ByteType.tsym, scope);
        importNamed(syms.visage_ShortType.tsym, scope);
        importNamed(syms.visage_IntegerType.tsym, scope);
        importNamed(syms.visage_LongType.tsym, scope);
        importNamed(syms.visage_FloatType.tsym, scope);
        importNamed(syms.visage_DoubleType.tsym, scope);
        importNamed(syms.visage_StringType.tsym, scope);
        importNamed(syms.visage_DurationType.tsym, scope);
        importNamed(syms.visage_LengthType.tsym, scope);
        importNamed(syms.visage_AngleType.tsym, scope);
        importNamed(syms.visage_ColorType.tsym, scope);
        importNamed(syms.visage_RuntimeType.tsym, scope);
    }

    /* ********************************************************************
     * Visitor methods for member enter
     *********************************************************************/
    /** Visitor argument: the current environment
     */
    protected VisageEnv<VisageAttrContext> env;

    /** Enter field and method definitions and process import
     *  clauses, catching any completion failure exceptions.
     */
    void memberEnter(VisageTree tree, VisageEnv<VisageAttrContext> env) {
        VisageEnv<VisageAttrContext> prevEnv = this.env;
        try {
            this.env = env;
            if (tree != null) {
                tree.accept(this);
            }
        } catch (CompletionFailure ex) {
            chk.completionError(tree.pos(), ex);
        } finally {
            this.env = prevEnv;
        }
    }

    /** Enter members from a list of trees.
     */
    void memberEnter(List<? extends VisageTree> trees, VisageEnv<VisageAttrContext> env) {
        for (List<? extends VisageTree> l = trees; l.nonEmpty(); l = l.tail) {
            memberEnter(l.head, env);
        }
    }

    @Override
    public void visitTree(VisageTree tree) {
        if (tree instanceof VisageBlock) {
            visitBlockExpression((VisageBlock) tree);
        } else {
            super.visitTree(tree); //super.visitTree is a no-op because scan is overridden!!
        }
    }

    @Override
    public void visitErroneous(VisageErroneous tree) {
        memberEnter(tree.getErrorTrees(), env);
    }

    @Override
    public void visitScript(VisageScript tree) {
        if (tree.isEntered) {
            // we must have already processed this toplevel
            return;
        }
        tree.isEntered = true;

        // check that no class exists with same fully qualified name as
        // toplevel package
        if (checkClash && tree.pid != null) {
            Symbol p = tree.packge;
            while (p.owner != syms.rootPackage) {
                p.owner.complete(); // enter all class members of p
                if (syms.classes.get(p.getQualifiedName()) != null) {
                    log.error(tree.pos,
                            MsgSym.MESSAGE_PKG_CLASHES_WITH_CLASS_OF_SAME_NAME,
                            p);
                }
                p = p.owner;
            }
        }

        importStaticAll(-1, syms.visage_AutoImportRuntimeType.tsym, env);

        // Process all import clauses.
        memberEnter(tree.defs, env);
    }

    @Override
    public void visitImport(VisageImport tree) {
        VisageExpression imp = tree.qualid;
        Name name = VisageTreeInfo.name(imp);

        // Create a local environment pointing to this tree to disable
        // effects of other imports in Resolve.findGlobalType
        VisageEnv<VisageAttrContext> localEnv = env.dup(tree);

        // Attribute qualifying package or class.
        //
        boolean all = false;

        // Attribute qualifying package or class and all descendants
        //
        boolean allAndSundry = false;

        if (imp instanceof VisageSelect) {

            if (name == names.asterisk) {

                all = true;
                imp = ((VisageSelect) imp).getExpression();

            } else if (name.contentEquals("**")) {

                allAndSundry = true;

                // TODO: Implement .**
                // Just cause an assertion error so that we locate this code quickly
                //
                assert (allAndSundry == false);

            }
        }
        if (all) {
            TypeSymbol p = attr.attribTree(imp,
                    localEnv,
                    TYP | PCK,
                    Type.noType).tsym;
            // Import on demand.
            chk.checkCanonical(imp);
            if (p instanceof ClassSymbol) {
                importStaticAll(tree.pos, p, env);
            } else {
                importAll(tree.pos, p, env);
            }
            return;
        } else if (imp instanceof VisageSelect) {
            VisageSelect s = (VisageSelect) imp;
            TypeSymbol p = attr.attribTree(s.selected,
                    localEnv,
                    (TYP | PCK),
                    Type.noType).tsym;
            // Named type import.
            if (p instanceof ClassSymbol) {
                chk.checkCanonical(s.selected);
                importNamedStatic(tree.pos(), p, name, localEnv);
                return;
            }
        }
        TypeSymbol c = attribImportType(imp, localEnv).tsym;
        chk.checkCanonical(imp);
        importNamed(tree.pos(), c, env);
    }

    /** Create a fresh environment for method bodies.
     *  @param tree     The method definition.
     *  @param env      The environment current outside of the method definition.
     */
    public VisageEnv<VisageAttrContext> methodEnv(VisageFunctionDefinition tree, VisageEnv<VisageAttrContext> env) {
        Scope localScope = new Scope(tree.sym);
        localScope.next = env.info.scope;
        VisageEnv<VisageAttrContext> localEnv =
                env.dup(tree, env.info.dup(localScope));
        localEnv.outer = env;
        localEnv.enclFunction = tree;
        if ((tree.mods.flags & STATIC) != 0) {
            localEnv.info.staticLevel++;
        }
        return localEnv;
    }

    public VisageEnv<VisageAttrContext> getMethodEnv(VisageFunctionDefinition tree, VisageEnv<VisageAttrContext> env) {
        VisageEnv<VisageAttrContext> mEnv = methodEnv(tree, env);
        mEnv.info.lint = mEnv.info.lint.augment(tree.sym.attributes_field, tree.sym.flags());
        for (List<VisageVar> l = tree.getParams(); l.nonEmpty(); l = l.tail) {
            mEnv.info.scope.enterIfAbsent(l.head.sym);
        }
        return mEnv;
    }

    /** Create a fresh environment for a variable's initializer.
     *  If the variable is a field, the owner of the environment's scope
     *  is be the variable itself, otherwise the owner is the method
     *  enclosing the variable definition.
     *
     *  @param tree     The variable definition.
     *  @param env      The environment current outside of the variable definition.
     */
    public VisageEnv<VisageAttrContext> initEnv(VisageVar tree, VisageEnv<VisageAttrContext> env) {
        VisageEnv<VisageAttrContext> localEnv = env.dupto(new VisageAttrContextEnv(tree, env.info.dup()));
        if (tree.sym.isMember()) {
            localEnv.info.scope = new Scope.DelegatedScope(env.info.scope);
            localEnv.info.scope.owner = tree.sym;
        }
        if ((tree.mods.flags & STATIC) != 0
                || (env.enclClass.sym.flags() & INTERFACE) != 0) {
            localEnv.info.staticLevel++;
        }
        return localEnv;
    }

    public VisageEnv<VisageAttrContext> getInitEnv(VisageVar tree, VisageEnv<VisageAttrContext> env) {
        VisageEnv<VisageAttrContext> iEnv = initEnv(tree, env);
        return iEnv;
    }

    @Override
    public void scan(VisageTree tree) {
        //do nothing!
    }

    private void addDefault(VisageAbstractVar tree) {
        Scope enclScope = VisageEnter.enterScope(env);
        if (enclScope.owner.kind == TYP && (tree.mods.flags & VisageFlags.DEFAULT) != 0) {
            VisageClassSymbol owner = (VisageClassSymbol) enclScope.owner;
            Name defaultVar = owner.getDefaultVar();
            if (defaultVar != null) {
                log.error(tree.pos, MsgSym.MESSAGE_VISAGE_MULTIPLE_DEFAULT_VARS, defaultVar, tree.name);
            }
            owner.setDefaultVar(tree.name);
        }
    }

    @Override
    public void visitOverrideClassVar(VisageOverrideClassVar tree) {
        addDefault(tree);
    }

    @Override
    public void visitVar(VisageVar tree) {
        VisageEnv<VisageAttrContext> localEnv = env;
        if ((tree.mods.flags & STATIC) != 0
                || (env.info.scope.owner.flags() & INTERFACE) != 0) {
            localEnv = env.dup(tree, env.info.dup());
            localEnv.info.staticLevel++;
        }

        Scope enclScope = VisageEnter.enterScope(env);
        VisageVarSymbol v = new VisageVarSymbol(types, names, 0, tree.name, null, enclScope.owner);
        if (enclScope.owner.kind == TYP) {
            ((VisageClassSymbol) enclScope.owner).addVar(v, (tree.mods.flags & STATIC) != 0);
        }
        addDefault(tree);
        attr.varSymToTree.put(v, tree);
        tree.sym = v;
        SymbolCompleter completer = new SymbolCompleter();
        completer.env = env;
        completer.tree = tree;
        completer.attr = attr;
        v.completer = completer;

        v.flags_field = chk.checkFlags(tree.pos(), tree.mods.flags, v, tree);
        if (tree.getInitializer() != null) {
            v.flags_field |= HASINIT;
        }
        if (tree.isBound()) {
            v.flags_field |= VisageFlags.VARUSE_BOUND_INIT;
        }

        if (chk.checkUnique(tree.pos(), v, env)) {
            chk.checkTransparentVar(tree.pos(), v, enclScope);
            enclScope.enter(v);
        }
        v.pos = tree.pos;
    }

    static class SymbolCompleter implements Completer {

        VisageEnv<VisageAttrContext> env;
        VisageTree tree;
        VisageAttr attr;

        public void complete(Symbol m) throws CompletionFailure {
            if (tree instanceof VisageVar) {
                attr.finishVar((VisageVar) tree, env);
            } else if (attr.pt != Type.noType) {
                // finishOperationDefinition makes use of the expected type pt.
                // This is useful when coming from visitFunctionValue - i.e.
                // attributing an anonymous function.  However, using the
                // expected type from a random call-site (which can happen if
                // we're called via complete) is a bit too flakey.
                // (ML can do it, because they unify across all the call-sites.)
                // This is a trick to run finishOperationDefinition, but in a
                // context where we're cleared the expected type attr.pt.
                m.completer = this;
                attr.attribExpr(tree, env);
            } else {
                attr.finishFunctionDefinition((VisageFunctionDefinition) tree, env);
            }
        }
    }

    @Override
    public void visitFunctionDefinition(VisageFunctionDefinition tree) {
        // If the function defintion is contained within an Erroneous
        // block, the enclosing scope may not be defined. In this case
        // we do not enter the function into any scope as it belongs to
        // the class and the tree is too erroneous to make any sense of
        // it. Doing nothing is the best course of action as at worst
        // other parst of the tree will complain that they don't know
        // anything about this function. So, just try the operation, but
        // forget about it if anything goes wrong...
        //
        // Note that it is only possible to get here with an erroneous tree
        // if the IDE is calling the attribution, trying to work out what it has.
        // We don't otherwise attribute erroneous trees, hence it is safe to
        // ignore any exception. Further, the parser recovers sensibly from
        // most class definition errors (an erroneous class containing a function
        // definition is the most likely case to throw this method out), so the
        // case is rare.
        //
        try {

            Scope enclScope = VisageEnter.enterScope(env);

            MethodSymbol m = new MethodSymbol(0, tree.name, null, enclScope.owner);
            m.flags_field = chk.checkFlags(tree.pos(), tree.mods.flags, m, tree);
            tree.sym = m;
            enclScope.enter(m);

            SymbolCompleter completer = new SymbolCompleter();
            completer.env = env;
            completer.tree = tree;
            completer.attr = attr;

            m.completer = completer;
            attr.methodSymToTree.put(m, tree);

        } catch (NullPointerException e) {
            // Looks like we could not enter the function into any symbol
            // table. Just ignore it.
        }
    }

//    @Override
//    public void visitClassDeclaration(VisageClassDeclaration that) {
//        for (VisageExpression superClass : that.getSupertypes()) {
//            attr.attribType(superClass, env);
//        }
//    }

    /* ********************************************************************
     * Type completion
     *********************************************************************/
    Type attribImportType(VisageTree tree, VisageEnv<VisageAttrContext> env) {
        assert completionEnabled;
        try {
            // To prevent deep recursion, suppress completion of some
            // types.
            completionEnabled = false;
            return attr.attribType(tree, env);
        } finally {
            completionEnabled = true;
        }
    }


    /* ********************************************************************
     * Source completer
     *********************************************************************/
    /** Complete entering a class.
     *  @param sym         The symbol of the class to be completed.
     */
    public void complete(Symbol sym) throws CompletionFailure {
        // Suppress some (recursive) MemberEnter invocations
        if (!completionEnabled) {
            // Re-install same completer for next time around and return.
            assert (sym.flags() & Flags.COMPOUND) == 0;
            sym.completer = this;
            return;
        }

        ClassSymbol c = (ClassSymbol) sym;
        ClassType ct = (ClassType) c.type;
        VisageEnv<VisageAttrContext> localEnv = enter.typeEnvs.get(c);
        VisageClassDeclaration tree = (VisageClassDeclaration) localEnv.tree;
        boolean wasFirst = isFirst;
        isFirst = false;

        JavaFileObject prev = log.useSource(localEnv.toplevel.sourcefile);
        try {
            // Save class environment for later member enter (2) processing.
            halfcompleted.append(localEnv);

            // Mark class as not yet attributed.
            c.flags_field |= UNATTRIBUTED;

            // If this is a toplevel script-class, make sure any preceding import
            // clauses have been seen.
            if (c.owner.kind == PCK) {
                memberEnter(localEnv.toplevel, localEnv.enclosing(VisageTag.TOPLEVEL));
                todo.append(localEnv);
            }

            if (c.owner.kind == TYP) {
                c.owner.complete();
            }

            boundAnalysis.analyzeBindContexts(localEnv);

            // create an environment for evaluating the base clauses
            VisageEnv<VisageAttrContext> baseEnv = baseEnv(tree, localEnv);
            Type supertype = null;
            ListBuffer<Type> interfaces = new ListBuffer<Type>();
            Set<Type> interfaceSet = new HashSet<Type>();
            Set<Type> mixinSet = new HashSet<Type>();
            {
                ListBuffer<VisageExpression> extending = ListBuffer.<VisageExpression>lb();
                ListBuffer<VisageExpression> implementing = ListBuffer.<VisageExpression>lb();
                ListBuffer<VisageExpression> mixing = ListBuffer.<VisageExpression>lb();
                for (VisageExpression stype : tree.getSupertypes()) {
                    Type st = attr.attribType(stype, localEnv);

                    if (st.isInterface()) {
                        implementing.append(stype);
                    } else {
                        long flags = st.tsym.flags_field;
                        boolean isMixin = (flags & VisageFlags.MIXIN) != 0;

                        if (isMixin) {
                            mixing.append(stype);
                            chk.checkNotRepeated(stype.pos(), types.erasure(st), mixinSet);
                        } else {
                            supertype = extending.isEmpty() ? st : null;
                            extending.append(stype);
                        }

                        interfaces.append(st);
                    }
                }

                if ((sym.flags() & VisageFlags.MIXIN) != 0) {
                    c.flags_field |= VisageFlags.MIXIN;
                }

                tree.setDifferentiatedExtendingImplementingMixing(extending.toList(), implementing.toList(), mixing.toList());
            }

            if (supertype == null) {
                if (c.fullname == names.java_lang_Object) {
                    supertype = Type.noType;
                } else {
                    supertype = syms.visage_BaseType;
                }
            }
            ct.supertype_field = supertype;

            // Determine interfaces.
            List<VisageExpression> interfaceTrees = tree.getImplementing();
            if ((tree.mods.flags & Flags.ENUM) != 0 && target.compilerBootstrap(c)) {
                // add interface Comparable<T>
                interfaceTrees =
                        interfaceTrees.prepend(visagemake.Type(new ClassType(syms.comparableType.getEnclosingType(),
                        List.of(c.type),
                        syms.comparableType.tsym)));
                // add interface Serializable
                interfaceTrees =
                        interfaceTrees.prepend(visagemake.Type(syms.serializableType));
            }
            for (VisageExpression iface : interfaceTrees) {
                Type i = attr.attribBase(iface, baseEnv, false, true, true);
                if (i.tag == CLASS) {
                    interfaces.append(i);
                    chk.checkNotRepeated(iface.pos(), types.erasure(i), interfaceSet);
                }
            }
            if ((c.flags_field & ANNOTATION) != 0) {
                ct.interfaces_field = List.of(syms.annotationType);
            } else {
                ct.interfaces_field = interfaces.toList();
            }

            if (c.fullname == names.java_lang_Object) {
                if (tree.getExtending().head != null) {
                    chk.checkNonCyclic(tree.getExtending().head.pos(),
                            supertype);
                    ct.supertype_field = Type.noType;
                } else if (tree.getImplementing().nonEmpty()) {
                    chk.checkNonCyclic(tree.getImplementing().head.pos(),
                            ct.interfaces_field.head);
                    ct.interfaces_field = List.nil();
                }
            }

            chk.checkNonCyclic(tree.pos(), c.type);

            // If this is a class, enter symbols for this and super into
            // current scope.
            if ((c.flags_field & INTERFACE) == 0) {
                VisageVarSymbol thisSym = visagemake.ThisSymbol(c.type);
                thisSym.pos = Position.FIRSTPOS;
                localEnv.info.scope.enter(thisSym);

                if (ct.supertype_field.tag == CLASS && supertype != null) {
                    VisageVarSymbol superSym = visagemake.SuperSymbol(supertype, c);
                    superSym.pos = Position.FIRSTPOS;
                    localEnv.info.scope.enter(superSym);
                }
            }

            // check that no package exists with same fully qualified name,
            // but admit classes in the unnamed package which have the same
            // name as a top-level package.
            if (checkClash
                    && c.owner.kind == PCK && c.owner != syms.unnamedPackage
                    && reader.packageExists(c.fullname)) {
                log.error(tree.pos, MsgSym.MESSAGE_CLASH_WITH_PKG_OF_SAME_NAME, c);
            }

        } catch (CompletionFailure ex) {
            chk.completionError(tree.pos(), ex);
        } finally {
            log.useSource(prev);
        }

        // Enter all member fields and methods of a set of half completed
        // classes in a second phase.
        if (wasFirst) {
            try {
                while (halfcompleted.nonEmpty()) {
                    finish(halfcompleted.next());
                }
            } finally {
                isFirst = true;
            }

            // commit pending annotations
            annotate.flush();
        }
    }

    private VisageEnv<VisageAttrContext> baseEnv(VisageClassDeclaration tree, VisageEnv<VisageAttrContext> env) {
        Scope typaramScope = new Scope(tree.sym);
        VisageEnv<VisageAttrContext> outer = env.outer; // the base clause can't see members of this class
        VisageEnv<VisageAttrContext> localEnv = outer.dup(tree, outer.info.dup(typaramScope));
        localEnv.baseClause = true;
        localEnv.outer = outer;
        localEnv.info.isSelfCall = false;
        return localEnv;
    }

    /** Enter member fields and methods of a class
     *  @param env        the environment current for the class block.
     */
    private void finish(VisageEnv<VisageAttrContext> env) {
        JavaFileObject prev = log.useSource(env.toplevel.sourcefile);
        try {
            VisageClassDeclaration tree = (VisageClassDeclaration) env.tree;
            memberEnter(tree.getMembers(), env);
        } finally {
            log.useSource(prev);
        }
    }
}
