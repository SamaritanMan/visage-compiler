/*
 * Copyright 2010 Sun Microsystems, Inc.  All Rights Reserved.
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

package org.visage.jdi;

import org.visage.jdi.event.VisageEventQueue;
import org.visage.jdi.request.VisageEventRequestManager;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.BooleanType;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.ByteType;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharType;
import com.sun.jdi.CharValue;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.DoubleType;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.Field;
import com.sun.jdi.FloatType;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IntegerType;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.LongType;
import com.sun.jdi.LongValue;
import com.sun.jdi.Method;
import com.sun.jdi.MonitorInfo;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveType;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ShortType;
import com.sun.jdi.ShortValue;
import com.sun.jdi.StackFrame;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VoidType;
import com.sun.jdi.VoidValue;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.request.EventRequestManager;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sundar
 */
public class VisageWrapper {
    public static VisageVirtualMachine wrap(VirtualMachine vm) {
        return (vm == null)? null : new VisageVirtualMachine(vm);
    }

    public static List<VirtualMachine> wrapVirtualMachines(List<VirtualMachine> vms) {
        List<VirtualMachine> res = new ArrayList<VirtualMachine>(vms.size());
        for (VirtualMachine vm : vms) {
            res.add(wrap(vm));
        }
        return res;
    }

    public static VisageType wrap(VisageVirtualMachine visagevm, Type type) {
        if (type == null) {
            return null;
        }
        
        if (type instanceof VoidType) {
            return visagevm.voidType((VoidType)type);
        } else if (type instanceof PrimitiveType) {
            if (type instanceof BooleanType) {
                return visagevm.booleanType((BooleanType)type);
            } else if (type instanceof CharType) {
                return visagevm.charType((CharType)type);
            } else if (type instanceof ByteType) {
                return visagevm.byteType((ByteType)type);
            } else if (type instanceof ShortType) {
                return visagevm.shortType((ShortType)type);
            } else if (type instanceof IntegerType) {
                return visagevm.integerType((IntegerType)type);
            } else if (type instanceof LongType) {
                return visagevm.longType((LongType)type);
            } else if (type instanceof FloatType) {
                return visagevm.floatType((FloatType)type);
            } else if (type instanceof DoubleType) {
                return visagevm.doubleType((DoubleType)type);
            } else {
                throw new IllegalArgumentException("illegal primitive type : " + type);
            }
        } else if (type instanceof ReferenceType) {
            return wrap(visagevm, (ReferenceType)type);
        } else {
            throw new IllegalArgumentException("illegal type: " + type);
        }
    }

    public static List<Type> wrapTypes(VisageVirtualMachine visagevm, List<Type> types) {
        if (types == null) {
            return null;
        }
        List<Type> result = new ArrayList<Type>(types.size());
        for (Type type : types) {
            result.add(wrap(visagevm, type));
        }
        return result;
    }

    public static VisageReferenceType wrap(VisageVirtualMachine visagevm, ReferenceType rt) {
        if (rt == null) {
            return null;
        } else if (rt instanceof ClassType) {
            return visagevm.classType((ClassType)rt);
        } else if (rt instanceof InterfaceType) {
            return visagevm.interfaceType((InterfaceType)rt);
        } else if (rt instanceof ArrayType) {
            return visagevm.arrayType((ArrayType)rt);
        } else {
            return visagevm.referenceType(rt);
        }
    }

    public static VisageClassType wrap(VisageVirtualMachine visagevm, ClassType ct) {
        return (ct == null)? null : visagevm.classType(ct);
    }

    public static VisageInterfaceType wrap(VisageVirtualMachine visagevm, InterfaceType it) {
        return (it == null)? null : visagevm.interfaceType(it);
    }

    public static VisageArrayType wrap(VisageVirtualMachine visagevm, ArrayType at) {
        return (at == null)? null : visagevm.arrayType(at);
    }

    public static List<ReferenceType> wrapReferenceTypes(VisageVirtualMachine visagevm, List<ReferenceType> refTypes) {
        // Note that VirtualMachineImpl caches the list, and returns an unmodifiable wrapped list.
        // Classes that get loaded in the future are added to its list by an EventListener on ClassPrepared 
        // events.  If we cache our wrapped list, they we would have to do the same thing, or be able
        // to update our cached list when this method is called again.  So for the time being,
        // we won't cache and thus don't have to return an unmodifiable list.
        if (refTypes == null) {
            return null;
        }
        List<ReferenceType> result = new ArrayList<ReferenceType>(refTypes.size());
        for (ReferenceType rt : refTypes) {
            String className = rt.name();
            // visage generated clases contain $[1-9]Local$ or $ObjLit$
            if (className.indexOf('$') != -1) {
                if (className.indexOf("$ObjLit$") != -1) {
                    continue;
                }
                if (className.matches(".*\\$[0-9]+Local\\$.*")) {
                    continue;
                }
            }
            result.add(VisageWrapper.wrap(visagevm, rt));
        }
        return result;
    }

    public static List<ClassType> wrapClassTypes(VisageVirtualMachine visagevm, List<ClassType> classes) {
        if (classes == null) {
            return null;
        }
        List<ClassType> result = new ArrayList<ClassType>(classes.size());
        for (ClassType ct : classes) {
            result.add(VisageWrapper.wrap(visagevm, ct));
        }
        return result;
    }

    public static List<InterfaceType> wrapInterfaceTypes(VisageVirtualMachine visagevm, List<InterfaceType> interfaces) {
        if (interfaces == null) {
            return null;
        }
        List<InterfaceType> result = new ArrayList<InterfaceType>(interfaces.size());
        for (InterfaceType it : interfaces) {
            result.add(VisageWrapper.wrap(visagevm, it));
        }
        return result;
    }

    public static VisageLocation wrap(VisageVirtualMachine visagevm, Location loc) {
        return (loc == null)? null : visagevm.location(loc);
    }

    public static List<Location> wrapLocations(VisageVirtualMachine visagevm, List<Location> locations) {
        if (locations == null) {
            return null;
        }
        List<Location> result = new ArrayList<Location>(locations.size());
        for (Location loc: locations) {
            result.add(wrap(visagevm, loc));
        }
        return result;
    }

    public static VisageField wrap(VisageVirtualMachine visagevm, Field field) {
        return (field == null)? null : visagevm.field(field);
    }

    /*
     * The fields are JDI Fields.
     * Each field can be a user field of an Visage class, an internal field of an Visage class,
     * or a field of a Java class.
     */
    public static List<Field> wrapFields(VisageVirtualMachine visagevm, List<Field> fields) {
        // Create VisageField wrappers for each field that is a valid Visage field.
        if (fields == null) {
            return null;
        }
        // We will have far fewer fields than fields.size() due to all the VFLGS etc
        // fields we will discard , so start with some small random amount
        List<Field> result = new ArrayList<Field>(20);

        for (Field fld : fields) {
            String fldName = fld.name();
            int firstDollar = fldName.indexOf('$');
            // java names do not start with $.
            // Visage user names start with a $ but so do various internal names
            // mixin vars are mangled with the mixin classname, et,   $MixinClassName$fieldName
            if (firstDollar != -1) {
                if ((fldName.indexOf("_$",1)    != -1) ||
                    (fldName.indexOf("$$")      != -1) ||
                    (fldName.indexOf("$helper$") == 0) ||
                    (fldName.indexOf("$script$") == 0) ||
                    (fldName.indexOf("$ol$")    != -1)) {
                    // $ol$ means it is a shredded name from a bound obj lit (see VisageLower.java)
                    // _$ means it is a synth var (see VisagePreTranslationSupport.java)
                    // $helper$ is in VisageDefs.java
                    continue;
                }
            }

            if (fldName.equals("$assertionsDisabled") && fld.declaringType().name().equals("org.visage.runtime.VisageBase")) {
                continue;
            }
            /*
              - mixin fields are named $MixinClassName$fieldName
              - a private script field is java private, and is named with its normal name 
              UNLESS it is referenced in a subclass. In this case it is java public and
              its name is $ClassName$fieldName.  
              This mangling in of the classname is not yet handled.
            */
            if (firstDollar <= 0) {
                result.add(visagevm.field(fld));
            }
        }
        return result;
    }

    public static VisageMethod wrap(VisageVirtualMachine visagevm, Method method) {
        return (method == null)? null : visagevm.method(method);
    }

    public static List<Method> wrapMethods(VisageVirtualMachine visagevm, List<Method> methods) {
        if (methods == null) {
            return null;
        }
        List<Method> result = new ArrayList<Method>(20);
        for (Method mth : methods) {
            VisageMethod visagem = visagevm.method(mth);
            if (!visagem.isVisageInternalMethod()) {
                result.add(visagem);
            }
        }
        return result;
    }

    public static VisageMonitorInfo wrap(VisageVirtualMachine visagevm, MonitorInfo monitorInfo) {
        return (monitorInfo == null)? null : visagevm.monitorInfo(monitorInfo);
    }

    public static List<MonitorInfo> wrapMonitorInfos(VisageVirtualMachine visagevm, List<MonitorInfo> monInfos) {
        if (monInfos == null) {
            return null;
        }
        List<MonitorInfo> result = new ArrayList<MonitorInfo>(monInfos.size());
        for (MonitorInfo mi : monInfos) {
            result.add(wrap(visagevm, mi));
        }
        return result;
    }

    public static VisageStackFrame wrap(VisageVirtualMachine visagevm, StackFrame frame) {
        return (frame == null)? null : visagevm.stackFrame(frame);
    }

    public static List<StackFrame> wrapFrames(VisageVirtualMachine visagevm, List<StackFrame> frames) {
        if (frames == null) {
            return null;
        }
        List<StackFrame> result = new ArrayList<StackFrame>(frames.size());
        for (StackFrame fr : frames) {
            result.add(wrap(visagevm, fr));
        }
        return result;
    }

    public static VisageLocalVariable wrap(VisageVirtualMachine visagevm, LocalVariable var) {
        return (var == null)? null : visagevm.localVariable(var);
    }

    public static List<LocalVariable> wrapLocalVariables(VisageVirtualMachine visagevm, List<LocalVariable> locals) {
        if (locals == null) {
            return null;
        }
        List<LocalVariable> result = new ArrayList<LocalVariable>(locals.size());
        for (LocalVariable var: locals) {
            result.add(wrap(visagevm, var));
        }
        return result;
    }

    public static VisageValue wrap(VisageVirtualMachine visagevm, Value value) {
        if (value == null) {
            return null;
        }

        if (value instanceof PrimitiveValue) {
            if (value instanceof BooleanValue) {
                return visagevm.booleanValue((BooleanValue)value);
            } else if (value instanceof CharValue) {
                return visagevm.charValue((CharValue)value);
            } else if (value instanceof ByteValue) {
                return visagevm.byteValue((ByteValue)value);
            } else if (value instanceof ShortValue) {
                return visagevm.shortValue((ShortValue)value);
            } else if (value instanceof IntegerValue) {
                return visagevm.integerValue((IntegerValue)value);
            } else if (value instanceof LongValue) {
                return visagevm.longValue((LongValue)value);
            } else if (value instanceof FloatValue) {
                return visagevm.floatValue((FloatValue)value);
            } else if (value instanceof DoubleValue) {
                return visagevm.doubleValue((DoubleValue)value);
            } else {
                throw new IllegalArgumentException("illegal primitive value : " + value);
            }
        } else if (value instanceof VoidValue) {
            return visagevm.voidValue();
        } else if (value instanceof ObjectReference) {
            return  wrap(visagevm, (ObjectReference)value);
        } else {
            throw new IllegalArgumentException("illegal value: " + value);
        }
    }

    public static List<ObjectReference> wrapObjectReferences(VisageVirtualMachine visagevm, List<ObjectReference> refs) {
        if (refs == null) {
            return null;
        }
        List<ObjectReference> result = new ArrayList<ObjectReference>(refs.size());
        for (ObjectReference ref : refs) {
            result.add(wrap(visagevm, ref));
        }
        return result;
    }


    public static VisageObjectReference wrap(VisageVirtualMachine visagevm, ObjectReference ref) {
        if (ref == null) {
            return null;
        } else if (ref instanceof ArrayReference) {
            return visagevm.arrayReference((ArrayReference)ref);
        } else if (ref instanceof StringReference) {
            return visagevm.stringReference((StringReference)ref);
        } else if (ref instanceof ThreadReference) {
            return visagevm.threadReference((ThreadReference)ref);
        } else if (ref instanceof ThreadGroupReference) {
            return visagevm.threadGroupReference((ThreadGroupReference)ref);
        } else if (ref instanceof ClassLoaderReference) {
            return visagevm.classLoaderReference((ClassLoaderReference)ref);
        } else if (ref instanceof ClassObjectReference) {
            return visagevm.classObjectReference((ClassObjectReference)ref);
        } else {
            return visagevm.objectReference(ref);
        }
    }

    public static VisageArrayReference wrap(VisageVirtualMachine visagevm, ArrayReference ref) {
        return (ref == null)? null : visagevm.arrayReference(ref);
    }

    public static VisageThreadReference wrap(VisageVirtualMachine visagevm, ThreadReference ref) {
        return (ref == null)? null : visagevm.threadReference(ref);
    }


    public static VisageThreadGroupReference wrap(VisageVirtualMachine visagevm, ThreadGroupReference ref) {
        return (ref == null)? null : visagevm.threadGroupReference(ref);
    }

    public static List<ThreadReference> wrapThreads(VisageVirtualMachine visagevm, List<ThreadReference> threads) {
        if (threads == null) {
            return null;
        }
        List<ThreadReference> result = new ArrayList<ThreadReference>(threads.size());
        for (ThreadReference tref : threads) {
            result.add(wrap(visagevm, tref));
        }
        return result;
    }

    public static List<ThreadGroupReference> wrapThreadGroups(VisageVirtualMachine visagevm, List<ThreadGroupReference> threadGroups) {
        if (threadGroups == null) {
            return null;
        }
        List<ThreadGroupReference> result = new ArrayList<ThreadGroupReference>(threadGroups.size());
        for (ThreadGroupReference tref : threadGroups) {
            result.add(wrap(visagevm, tref));
        }
        return result;
    }

    public static VisageClassLoaderReference wrap(VisageVirtualMachine visagevm, ClassLoaderReference ref) {
        return (ref == null)? null : visagevm.classLoaderReference(ref);
    }

    public static VisageClassObjectReference wrap(VisageVirtualMachine visagevm, ClassObjectReference ref) {
        return (ref == null)? null : visagevm.classObjectReference(ref);
    }

    public static List<Value> wrapValues(VisageVirtualMachine visagevm, List<Value> values) {
        if (values == null) {
            return null;
        }
        List<Value> result = new ArrayList<Value>(values.size());
        for (Value v : values) {
            result.add(wrap(visagevm, v));
        }
        return result;
    }

    public static Location unwrap(Location loc) {
        return (loc instanceof VisageLocation)? ((VisageLocation)loc).underlying() : loc;
    }

    public static StackFrame unwrap(StackFrame frame) {
        return (frame instanceof VisageStackFrame)? ((VisageStackFrame)frame).underlying() : frame;
    }

    public static LocalVariable unwrap(LocalVariable var) {
        return (var instanceof VisageLocalVariable)? ((VisageLocalVariable)var).underlying() : var;
    }
    
    public static Value unwrap(Value value) {
        return (value instanceof VisageValue)? ((VisageValue)value).underlying() : value;
    }

    public static List<? extends Value> unwrapValues(List<? extends Value> values) {
        if (values == null) {
            return null;
        }
        List<Value> result = new ArrayList<Value>(values.size());
        for (Value v : values) {
            result.add(unwrap(v));
        }
        return result;
    }

    public static Field unwrap(Field field) {
        return (field instanceof VisageField)? ((VisageField)field).underlying() : field;
    }

    public static Method unwrap(Method method) {
        return (method instanceof VisageMethod)? ((VisageMethod)method).underlying() : method;
    }

    public static ObjectReference unwrap(ObjectReference ref) {
        return (ref instanceof VisageObjectReference)? ((VisageObjectReference)ref).underlying() : ref;
    }

    public static ThreadReference unwrap(ThreadReference ref) {
        return (ref instanceof VisageThreadReference)? ((VisageThreadReference)ref).underlying() : ref;
    }

    public static ReferenceType unwrap(ReferenceType rt) {
        return (rt instanceof VisageReferenceType)? ((VisageReferenceType)rt).underlying() : rt;
    }

    public static List<? extends ReferenceType> unwrapReferenceTypes(List<? extends ReferenceType> refTypes) {
        if (refTypes == null) {
            return null;
        }
        List<ReferenceType> result = new ArrayList<ReferenceType>(refTypes.size());
        for (ReferenceType rt : refTypes) {
            result.add(unwrap(rt));
        }
        return result;
    }

    // event requests
    public static VisageEventRequestManager wrap(VisageVirtualMachine visagevm, EventRequestManager man) {
        return (man == null)? null : new VisageEventRequestManager(visagevm, man);
    }

    // event queue
    public static VisageEventQueue wrap(VisageVirtualMachine visagevm, EventQueue evtQueue) {
        return (evtQueue == null)? null : new VisageEventQueue(visagevm, evtQueue);
    }
}
