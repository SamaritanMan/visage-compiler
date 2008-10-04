/*
 * Copyright 2008 Sun Microsystems, Inc.  All Rights Reserved.
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

package javafx.util;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.sun.javafx.api.JavaFXScriptEngine;

// factored out to avoid linkage error for javax.script.* on Java 1.5
class Evaluator {
    static Object eval(String script) throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine scrEng = manager.getEngineByExtension("javafx");
        JavaFXScriptEngine engine = (JavaFXScriptEngine)scrEng;
        if (engine == null)
            throw new ScriptException("no scripting engine available");
        return engine.eval(script);
    }
}

/**
 *
 * @author Saul Wold
 */
public class FXEvaluator {

    /**
     * Evaluates a JavaFX Script source string and returns its result, if any.
     * For example, 
     * <br/>
     * This method depends upon the JavaFX Script compiler API being accessible
     * by the application, such as including the <code>javafxc.jar</code> file
     * in the application's classpath.
     * <br/>
     * Note:  this method provides only the simplest scripting functionality;
     * the script is evaluated without any specified context state, nor can 
     * any state it creates during evaluation be reused by other scripts.  For
     * sophisticated scripting applications, use the Java Scripting API
     * (<code>javax.scripting</code>).
     * 
     * @param script the JavaFX Script source to evaluate
     * @return the results from evaluating the script, or null if no results
     *         are returned by the script.
     * @throws javax.script.ScriptException
     */
    public static Object eval(String script) {
        try {
            return Evaluator.eval(script);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
