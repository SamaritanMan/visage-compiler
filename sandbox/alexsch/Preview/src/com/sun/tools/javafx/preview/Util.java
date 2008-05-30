package com.sun.tools.javafx.preview;

import com.sun.javafx.api.JavafxcTask;
import com.sun.javafx.runtime.Entry;
import com.sun.javafx.runtime.sequence.Sequence;
import com.sun.javafx.runtime.sequence.Sequences;

import com.sun.tools.javafx.api.JavafxcTool;
import com.sun.tools.javafx.script.MemoryFileManager;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;



public class Util {

    private static final String[] commandLineArgs = new String[]{};

    public static String getFileName(String className) {
        return className.substring(className.lastIndexOf('.') + 1) + ".fx";
    }
    
    public static String getFilePath(String className) {
        return className.replace('.', '/') + ".fx";
    }
    
    public static String readResource(String className) throws Exception {
        return readResource(className, getFileName(className));
    }

    public static String readResource(String className, String resource) throws Exception {
        Class cls = Class.forName(className);
        return readResource(cls.getResourceAsStream(resource));
    }

    public static String readResource(InputStream is) throws Exception {
        StringBuffer contents = new StringBuffer();
        if (is != null) {
            //LATER: specify encoding properly!
            BufferedReader input = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line = null;
            while ((line = input.readLine()) != null) {
                contents.append(line);
                contents.append(System.getProperty("line.separator"));
            }
        }
        return contents.toString();
    }

    public static Object runFXFile(String name) throws Exception {
        return runFXFile(name, Thread.currentThread().getContextClassLoader());
    }

    public static Object runFXFile(String name, ClassLoader classLoader) throws Exception {
        try {
            Class cls = classLoader.loadClass(name);
            //System.out.println("class: \"" + cls + "\"");
            Method run = cls.getDeclaredMethod(Entry.entryMethodName(), Sequence.class);
            Object args = Sequences.make(String.class, commandLineArgs);
            return run.invoke(null, args);
        } catch (Throwable e) {
            throw new Exception("FX file: \"" + name + "\" was not compiled!!", e);
        }
    }

    public static Object executeFXCode(String code) throws Exception {
        return CodeManager.execute("Preview", code, Locale.getDefault().getDisplayName(), "");
    }

    public static Object executeFXCode(String className, String code, String localeName, String propertyText) throws Exception {
        return CodeManager.execute(className, code, localeName, propertyText);
    }
}



class FXFileObject extends SimpleJavaFileObject {

    String code;
    String className;

    public FXFileObject(String className, String code) {
        super(toURI(className), Kind.SOURCE);
        this.code = code;
        this.className = className;
    }

    public boolean isNameCompatible(String simpleName, Kind kind) {
        return true;
    }

    public URI toUri() {
        return toURI(className);
    }

    public String getName() {
        return Util.getFileName(className);
    }

    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return code;
    }

    private static URI toURI(String className) {
        return URI.create("./" + Util.getFilePath(className));
    }

    private void print(String text) {
        System.out.println("[file object] " + text);
    }
}





