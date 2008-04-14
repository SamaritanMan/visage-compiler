package framework;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;

import com.sun.javafx.runtime.location.*;
import com.sun.javafx.runtime.FXObject;
import com.sun.javafx.runtime.InitHelper;

/**
 * SimulatedFXObject
 *
 * @author Brian Goetz
 */
public abstract class FXObjectFactory<T extends FXObject> {
    private final String[] attributes;
    private final Class<T> intf;

    protected FXObjectFactory(Class<T> intf, String[] attributes) {
        this.attributes = attributes;
        this.intf = intf;
    }

    public T make() {
        final Map<String, AbstractVariable> locs = new HashMap<String, AbstractVariable>();
        Method[] methods = intf.getMethods();
        for (Method m : methods) {
            if (!m.getName().startsWith("get$"))
                continue;
            String name = m.getName().substring("get$".length());
            String fqReturnType = m.getReturnType().getName();
            String returnType = fqReturnType.substring(fqReturnType.lastIndexOf(".") + 1);
            if (returnType.equals("IntLocation"))
                locs.put(name, IntVariable.make());
            else if (returnType.equals("DoubleLocation"))
                locs.put(name, DoubleVariable.make());
            else if (returnType.equals("BooleanLocation"))
                locs.put(name, BooleanVariable.make());
            else if (returnType.equals("ObjectLocation"))
                locs.put(name, ObjectVariable.make());
            else if (returnType.equals("SequenceLocation"))
                locs.put(name, SequenceVariable.make(Object.class));
            else
                throw new IllegalStateException("Unknown location type " + fqReturnType);
        }

        @SuppressWarnings("unchecked")
        T result = (T) Proxy.newProxyInstance(intf.getClassLoader(),
                                              new Class[]{intf},
                                              new InvocationHandler() {
                                                  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                                      if (method.getName().equals("initialize$")) {
                                                          List<AbstractVariable> vars = new ArrayList<AbstractVariable>();
                                                          for (String name : attributes) {
                                                              if (locs.get(name).needDefault())
                                                                  applyDefault((T) proxy, name, locs.get(name));
                                                              vars.add((AbstractVariable) locs.get(name));
                                                          }
                                                          init((T) proxy);
                                                          postInit((T) proxy);
                                                          InitHelper.finish(vars.toArray(new AbstractVariable[vars.size()]));
                                                      }
                                                      else if (method.getName().startsWith("get$")) {
                                                          return locs.get(method.getName().substring("get$".length()));
                                                      }
                                                      return null;
                                                  }
                                              });
        return result;
    }

    public void addTriggers(T receiver) { }
    public void applyDefault(T receiver, String attrName, AbstractVariable attrLocation) { }
    public void init(T receiver) { }
    public void postInit(T receiver) { }
}
