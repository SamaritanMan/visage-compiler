/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.visage.tools.api;

import org.visage.api.JavafxcTask;
import org.visage.api.tree.ClassDeclarationTree;
import org.visage.api.tree.VisageTreePathScanner;
import org.visage.api.tree.SequenceIndexedTree;
import org.visage.api.tree.UnitTree;
import org.visage.api.tree.SourcePositions;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import static javax.tools.StandardLocation.*;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Verifies correct start and end position for indexed sequence expression.
 * 
 * @author tball
 */
public class JFXC1330Test {
    @Test
    public void sequenceExpressionPosTest() throws Exception {
        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        try {            
            Thread.currentThread().setContextClassLoader(JavafxcTool.class.getClassLoader());
            JavafxcTool tool = JavafxcTool.create();
            MockDiagnosticListener<? super FileObject> dl = new MockDiagnosticListener<FileObject>();
            
            StandardJavaFileManager fileManager = tool.getStandardFileManager(dl, null, null);
            List<File> dirs = new ArrayList<File>();
            dirs.add(getTmpDir());
            fileManager.setLocation(CLASS_OUTPUT, dirs);
            
            File file = new File("test/src/org/visage/tools/api/Boids.visage");
            Iterable<? extends JavaFileObject> fileObjects = fileManager.getJavaFileObjects(file); 
            JavafxcTask visageTask = tool.getTask(null, fileManager, dl, null, fileObjects);
            Iterable<? extends UnitTree> treeList = visageTask.parse();
            assertTrue("no parse tree(s) returned", treeList.iterator().hasNext());
            
            final JavafxcTrees trees = JavafxcTrees.instance(visageTask);
            final SourcePositions sp = trees.getSourcePositions();
            for (final UnitTree unit : treeList) {
                VisageTreePathScanner scanner = new VisageTreePathScanner<Object,Void>() {
                    @Override
                    public Object visitSequenceIndexed(SequenceIndexedTree node, Void p) {
                        assertEquals(37, sp.getStartPosition(unit, node));
                        assertEquals(45, sp.getEndPosition(unit, node));
                        return super.visitSequenceIndexed(node, p);
                    }
                };
                scanner.scan(unit, null);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(orig);
        }
    }
    
    private static File getTmpDir() {
        try {
            File f = File.createTempFile("dummy", "file");
            f.deleteOnExit();
            File tmpdir = f.getParentFile();
            if (tmpdir != null)
                return tmpdir;
        } catch (IOException ex) {
        }
        File f = new File("test-output");
        f.mkdir();
        return f;        
    }
}