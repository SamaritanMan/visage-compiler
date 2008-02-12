/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.tools.xslhtml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import static java.util.logging.Level.*;

/**
 *
 * @author joshy
 */
public class XHTMLProcessingUtils {

    private static ResourceBundle messageRB = null;
    private static Logger logger = Logger.getLogger(XHTMLProcessingUtils.class.getName());;
    
    static {
        // set verbose for initial development
        logger.setLevel(ALL); //TODO: remove or set to INFO when finished
    }

    /**
     * Transform XMLDoclet output to XHTML using XSLT.
     * 
     * @param xmlInputPath the path of the XMLDoclet output to transform
     * @param xsltStream the XSLT to implement the transformation, as an input stream.
     * @throws java.lang.Exception
     */
    public static void process(String xmlInputPath, InputStream xsltStream,
            Map<String,String> parameters
            ) throws Exception {
        System.out.println(getString("transforming.to.html"));
        // TODO code application logic here
        
        //hack to get this to work on the mac
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
            "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
        System.setProperty("javax.xml.parsers.SAXParserFactory",
            "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
        
        if (xsltStream == null)
            xsltStream = XHTMLProcessingUtils.class.getResourceAsStream("resources/javadoc.xsl");
        
        File file = new File(xmlInputPath);
        p(INFO, MessageFormat.format(getString("reading.doc"), file.getAbsolutePath()));
        p(FINE, "exists: " + file.exists());
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new ErrorHandler() {

            public void warning(SAXParseException exception) throws SAXException {
                p(WARNING, "error: " + exception.getLineNumber());
            }

            public void error(SAXParseException exception) throws SAXException {
                p(SEVERE, "error: " + exception.getLineNumber());
            }

            public void fatalError(SAXParseException exception) throws SAXException {
                p(SEVERE, "error: " + exception.getLineNumber());
            }
        });
        Document doc = builder.parse(file);




        File docsdir = new File("fxdocs");
        if (!docsdir.exists()) {
            docsdir.mkdir();
        }

        p(INFO, getString("copying"));

        copy(XHTMLProcessingUtils.class.getResource("resources/index.html"), new File(docsdir, "index.html"));
        copy(XHTMLProcessingUtils.class.getResource("resources/empty.html"), new File(docsdir, "empty.html"));
        copy(XHTMLProcessingUtils.class.getResource("resources/master.css"), new File(docsdir, "master.css"));
        copy(XHTMLProcessingUtils.class.getResource("resources/demo.css"), new File(docsdir, "demo.css"));
        File images = new File(docsdir,"images");
        images.mkdir();
        copy(XHTMLProcessingUtils.class.getResource("resources/quote-background-1.gif"), new File(images, "quote-background-1.gif"));
        //copy(new File("demo.css"), new File(docsdir, "demo.css"));

        p(INFO, getString("transforming"));


        //File xsltFile = new File("javadoc.xsl");
        //p("reading xslt exists in: " + xsltFile.exists());
        Source xslt = new StreamSource(xsltStream);
        Transformer trans = TransformerFactory.newInstance().newTransformer(xslt);
        for(String key : parameters.keySet()) {
            System.out.println("using key: " + key + " " + parameters.get(key));
            trans.setParameter(key, parameters.get(key));
        }
        trans.setErrorListener(new MainErrorListener());

        XPath xpath = XPathFactory.newInstance().newXPath();

        // packages
        NodeList packages = (NodeList) xpath.evaluate("//package", doc, XPathConstants.NODESET); 
        MessageFormat form = new MessageFormat("The disk \"{1}\" contains {0}.");
        p(INFO, MessageFormat.format(getString("creating.packages"), packages.getLength()));
        
        
        Document packages_doc = builder.newDocument();
        Element package_list_elem = packages_doc.createElement("packageList");
        packages_doc.appendChild(package_list_elem);

        for (int i = 0; i < packages.getLength(); i++) {
            Element pkg = ((Element) packages.item(i));
            String name = pkg.getAttribute("name");
            Element package_elem = packages_doc.createElement("package");
            package_elem.setAttribute("name", name);
            package_list_elem.appendChild(package_elem);
            processPackage(name, pkg, xpath, docsdir, trans);
        }

        trans.transform(new DOMSource(packages_doc), new StreamResult(new File(docsdir,"overview-frame.html")));
        System.out.println(getString("finished"));
    }

    private static void processPackage(String packageName, Element pkg, XPath xpath, File docsdir, Transformer trans) throws TransformerException, XPathExpressionException, IOException, FileNotFoundException, ParserConfigurationException {
        File packageDir = new File(docsdir, packageName);
        packageDir.mkdir();
        
        //classes
        NodeList classesNodeList = (NodeList) xpath.evaluate(
                    "*[name() = 'class' or name() = 'abstractClass' or name() = 'interface']",
                    pkg, XPathConstants.NODESET);
        List<Element> classes = sort(classesNodeList);
        p(INFO, MessageFormat.format(getString("creating.classes"), classes.size()));
        
        Document classes_doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element class_list = classes_doc.createElement("classList");
        class_list.setAttribute("packageName", packageName);
        classes_doc.appendChild(class_list);
        
        for(Element clazz : classes) {
            processClass(clazz, class_list, trans, packageDir);
        }
        trans.transform(new DOMSource(classes_doc), new StreamResult(new File(packageDir,"package-frame.html")));
    }

    
    private static void processClass(Element clazz, Element class_list, Transformer trans, File packageDir) throws TransformerException, IOException {
        String qualifiedName = clazz.getAttribute("qualifiedName");
        String name = clazz.getAttribute("name");
        
        //add to class list
        Element class_elem = class_list.getOwnerDocument().createElement("class");
        class_list.appendChild(class_elem);
        class_elem.setAttribute("name", name);
        class_elem.setAttribute("qualifiedName", qualifiedName);
        
        File xhtmlFile = new File(packageDir, qualifiedName + ".html");
        Result xhtmlResult = new StreamResult(xhtmlFile);
        Source xmlSource = new DOMSource(clazz.getOwnerDocument());
        trans.setParameter("target-class", qualifiedName);
        trans.transform(xmlSource, xhtmlResult);
    }

    
    
    
    
    private static List<Element> sort(NodeList classesNodeList) {
        List<Element> nodes = new ArrayList<Element>();
        for(int i=0; i<classesNodeList.getLength(); i++) {
            nodes.add((Element)classesNodeList.item(i));
        }
        
        Collections.sort(nodes,new Comparator<Element>() {
            public int compare(Element o1, Element o2) {
                return o1.getAttribute("qualifiedName").compareTo(
                        o2.getAttribute("qualifiedName"));
            }
        }
        );

        return nodes;
    }
    
    
    private static void copy(URL url, File file) throws FileNotFoundException, IOException {
        p(FINE, "copying from: " + url);
        p(FINE, "copying to: " + file.getAbsolutePath());
        InputStream in = url.openStream();
        FileOutputStream out = new FileOutputStream(file);
        byte[] buf = new byte[1024];
        while (true) {
            int n = in.read(buf);
            if (n < 0) {
                break;
            }
            out.write(buf, 0, n);
        }
    }
    
    private static void copy(File infile, File outfile) throws FileNotFoundException, IOException {
        FileInputStream in = new FileInputStream(infile);
        FileOutputStream out = new FileOutputStream(outfile);
        byte[] buf = new byte[1024];
        while (true) {
            int n = in.read(buf);
            if (n < 0) {
                break;
            }
            out.write(buf, 0, n);
        }
    }
    
    static String getString(String key) {
        ResourceBundle msgRB = messageRB;
        if (msgRB == null) {
            try {
                messageRB = msgRB =
                    ResourceBundle.getBundle("com.sun.tools.xslhtml.resources.xslhtml");
            } catch (MissingResourceException e) {
                throw new Error("Fatal: Resource for javafxdoc is missing");
            }
        }
        return msgRB.getString(key);
    }

    private static void p(Level level, String string) {
        if (level.intValue() >= logger.getLevel().intValue())
            System.err.println(string);
    }

    private static void p(Level level, String string, Throwable t) {
        if (level.intValue() >= logger.getLevel().intValue()) {
            StringBuilder sb = new StringBuilder();
            sb.append(string);
            if (t != null) {
                sb.append(System.getProperty("line.separator"));
                try {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    t.printStackTrace(pw);
                    pw.close();
                    sb.append(sw.toString());
                } catch (Exception ex) {
                }
            }
            System.err.println(sb.toString());
        }
    }

    /**
     * Command-line/debugging entry
     */
    public static void main(String[] args) throws Exception {
        process("javadoc.xml", null, new HashMap<String, String>());
    }

    private static class MainErrorListener implements ErrorListener {

        public MainErrorListener() {
        }

        public void warning(TransformerException exception) throws TransformerException {
            p(WARNING, "warning: " + exception);
        }

        public void error(TransformerException exception) throws TransformerException {
            Throwable thr = exception;
            while (true) {
                p(SEVERE, "error: " + exception.getMessageAndLocation(), thr.getCause());
                if (thr.getCause() != null) {
                    thr = thr.getCause();
                } else {
                    break;
                }
            }
        }

        public void fatalError(TransformerException exception) throws TransformerException {
            p(SEVERE, "fatal error: " + exception.getMessageAndLocation(), exception);
        }
    }
}
