package com.sun.javafx.ideaplugin.project;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.EnumSet;

/**
 * @author David Kaspar
 */
public final class FxSdkType extends SdkType implements ApplicationComponent {

    public static final Icon ICON = IconLoader.getIcon ("/com/sun/javafx/ideaplugin/resources/fx.png");
    public static final String JAVAFX_EXEC = SystemInfo.isWindows ? "javafx.bat" : "javafx";
    public static final String JAVA_SDK_PROPERTY_NAME = "java-sdk";

    public FxSdkType () {
        super ("JavaFX Script SDK");
    }

    public static FxSdkType getInstance() {
        return ApplicationManager.getApplication().getComponent(FxSdkType.class);
    }

    @Nullable
    public String suggestHomePath () {
        return "/spc/fxj1/javafx-sdk1.0"; // TODO
    }

    public boolean isValidSdkHome (String path) {
        File home = new File (path);
        File bin = new File (home, "bin");
        if (! bin.exists ())
            return false;
        File javafx = new File (bin, JAVAFX_EXEC);
        return javafx.exists ();
    }

    @Nullable
    public String getVersionString (String sdkHome) {
        return "1.0";
    }

    public String suggestSdkName (String currentSdkName, String sdkHome) {
        int i = sdkHome.lastIndexOf ('/');
        return i >= 0 ? sdkHome.substring (i + 1) : sdkHome;
    }

    public AdditionalDataConfigurable createAdditionalDataConfigurable (SdkModel sdkModel, SdkModificator sdkModificator) {
        return new FxSdkAdditionalDataConfigurable (sdkModel);
    }

    public void setupSdkPaths (Sdk sdk) {
        SdkModificator modificator = sdk.getSdkModificator ();
        modificator.removeAllRoots ();
        LocalFileSystem localFileSystem = LocalFileSystem.getInstance ();
        JarFileSystem jarFileSystem = JarFileSystem.getInstance ();

        VirtualFile lib = localFileSystem.findFileByPath (getLibraryPath (sdk).replace (File.separatorChar, '/'));
        if (lib != null) {
            for (VirtualFile libFile : lib.getChildren ()) {
                if (libFile.getName ().endsWith (".jar")) {
                    VirtualFile inJar = jarFileSystem.findFileByPath (libFile.getPath () + JarFileSystem.JAR_SEPARATOR);
                    if (inJar != null)
                        modificator.addRoot (inJar, ProjectRootType.CLASS);
                }
            }
        }

        VirtualFile doc = LocalFileSystem.getInstance ().findFileByPath (getJavaDocPath (sdk).replace (File.separatorChar, '/'));
        if (doc != null) {
            for (VirtualFile docFile : doc.getChildren ()) {
                if (docFile.isDirectory ()  &&  localFileSystem.findFileByPath (docFile.getPath () + "/index.html") != null) {
                    modificator.addRoot (docFile, ProjectRootType.JAVADOC);
                }
            }
        }

        FxSdkAdditionalData data = (FxSdkAdditionalData) sdk.getSdkAdditionalData ();
        if (data != null) {
            Sdk javaSdk = data.findSdk ();
            if (javaSdk != null) {
                SdkModificator javaModificator = javaSdk.getSdkModificator ();
                if (javaModificator != null) {
                    for (ProjectRootType type : EnumSet.of (ProjectRootType.SOURCE, ProjectRootType.CLASS, ProjectRootType.JAVADOC)) { // ProjectRootType.values()
                        VirtualFile[] roots = javaModificator.getRoots (type);
                        if (roots != null)
                            for (VirtualFile root : roots)
                                modificator.addRoot (root, type);
                    }
                }
            }
        }
        modificator.commitChanges ();
    }

    private static String getConvertedHomePath (Sdk sdk) {
        String home = sdk.getHomePath ().replace ('/', File.separatorChar);
        if (! home.endsWith (File.separator))
            home += File.separatorChar;
        return home;
    }

    @Nullable
    public String getBinPath (Sdk sdk) {
        return getConvertedHomePath (sdk) + "bin";
    }

    public static String getLibraryPath (Sdk sdk) {
        return getConvertedHomePath (sdk) + "lib";
    }

    public static String getJavaDocPath (Sdk sdk) {
        return getConvertedHomePath (sdk) + "docs";
    }

    @Nullable
    public Sdk getEncapsulatedSdk (Sdk sdk) {
        FxSdkAdditionalData data = (FxSdkAdditionalData) sdk.getSdkAdditionalData ();
        return data != null ? data.findSdk () : null;
    }

    @Nullable
    public String getToolsPath (Sdk sdk) {
        sdk = getEncapsulatedSdk (sdk);
        return sdk != null ? sdk.getSdkType ().getToolsPath (sdk) : null;
    }

    @Nullable
    public String getVMExecutablePath (Sdk sdk) {
        return getBinPath (sdk) + File.separator + JAVAFX_EXEC;
    }

    @Nullable
    public String getRtLibraryPath (Sdk sdk) {
        sdk = getEncapsulatedSdk (sdk);
        return sdk != null ? sdk.getSdkType ().getRtLibraryPath (sdk) : null;
    }

    @Nullable
    public SdkAdditionalData loadAdditionalData (Element additional) {
        String value = additional.getAttributeValue (JAVA_SDK_PROPERTY_NAME);
        return value != null ? new FxSdkAdditionalData (value, null) : null;
    }

    public void saveAdditionalData (SdkAdditionalData additionalData, Element additional) {
        if (additionalData instanceof FxSdkAdditionalData) {
            FxSdkAdditionalData data = (FxSdkAdditionalData) additionalData;
            String name = data.getJavaSdkName ();
            if (name != null)
                additional.setAttribute (JAVA_SDK_PROPERTY_NAME, name);
        }
    }

    public String getPresentableName () {
        return "JavaFX Script SDK";
    }

    public Icon getIcon () {
        return ICON;
    }

    public Icon getIconForAddAction () {
        return ICON;
    }

    @NonNls @NotNull
    public String getComponentName () {
        return "FxSdkType";
    }

    public void initComponent () {
    }

    public void disposeComponent () {
    }

}
