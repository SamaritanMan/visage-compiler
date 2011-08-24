/*
 * Copyright 2007-2009 Sun Microsystems, Inc.  All Rights Reserved.
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

package org.visage.tools.api;

import org.visage.api.VisagecTask;
import org.visage.tools.api.VisagecTool;
import com.sun.tools.mjavac.util.JavacFileManager;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit test for VisageTool.
 */
public class VisagecToolTest {
    private static File tmpDirectory;

    @Test
    public void create() {
        Object result = VisagecTool.create();
        assertTrue(result instanceof VisagecTool);
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        File f = File.createTempFile("foo", null);
        tmpDirectory = f.getParentFile();
        f.delete();
    }

    @Test
    public void run() {
        InputStream in = null;   // use System.in
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        VisagecTool instance = VisagecTool.create();
        int errors = instance.run(in, out, err, "-d", tmpDirectory.getPath(), 
                                  "test/src/org/visage/tools/api/Hello.visage");
        if (errors > 0) {
            System.err.println("VisageToolTest.run() out:");
            System.err.println(new String(out.toByteArray()));
            System.err.println("VisageToolTest.run() err:");
            System.err.println(new String(err.toByteArray()));
            System.err.println(Integer.toString(errors) + " error" + (errors == 1 ? "" : "s"));
        }
        assertEquals(errors, 0);
        assertTrue(out.size() == 0);
        assertTrue(err.size() == 0);
    }

    @Test
    public void getSourceVersions() {
        VisagecTool instance = new VisagecTool();
        Set<SourceVersion> expResult = EnumSet.range(SourceVersion.RELEASE_3,
                                                     SourceVersion.latest());
        Set<SourceVersion> result = instance.getSourceVersions();
        assertTrue(result.size() == expResult.size() && result.containsAll(expResult));
    }

    @Test
    public void getStandardFileManager() {
        VisagecTool instance = new VisagecTool();
        Object result = instance.getStandardFileManager(null, null, null);
        assertTrue(result instanceof JavacFileManager);
    }

    @Test
    public void getStandardFileManagerWithParams() {
        DiagnosticListener<? super JavaFileObject> diagnosticListener = 
                new DiagnosticListener<JavaFileObject>() {
            public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
                System.err.println(diagnostic.toString());
            }
        };
        Locale locale = Locale.getDefault();
        Charset charset = Charset.defaultCharset();
        VisagecTool instance = new VisagecTool();
        Object result = instance.getStandardFileManager(diagnosticListener, locale, charset);
        assertTrue(result instanceof JavacFileManager);
    }

    @Test
    public void getTask() {
        Writer out = null;
        JavaFileManager fileManager = null;
        DiagnosticListener<? super JavaFileObject> diagnosticListener = null;
        Iterable<String> options = null;
        Iterable<String> classes = null;
        Iterable<? extends JavaFileObject> compilationUnits = null;
        VisagecTool instance = new VisagecTool();
        Object result = instance.getTask(out, fileManager, diagnosticListener, options, compilationUnits);
        assertTrue(result instanceof VisagecTask);
    }

    @Test
    public void isSupportedOption() {
        VisagecTool instance = new VisagecTool();
        int result = instance.isSupportedOption("");
        assertTrue(result == -1);
        result = instance.isSupportedOption("-invalidOption");
        assertTrue(result == -1);
        result = instance.isSupportedOption("-nowarn");
        assertTrue(result == 0);
        result = instance.isSupportedOption("-source");
        assertTrue(result == 1);
    }
    
}
