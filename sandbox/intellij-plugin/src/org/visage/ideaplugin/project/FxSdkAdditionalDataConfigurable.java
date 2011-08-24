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

package org.visage.ideaplugin.project;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.util.Comparing;

import javax.swing.*;
import java.awt.*;

/**
 * @author David Kaspar
 */
public final class FxSdkAdditionalDataConfigurable implements AdditionalDataConfigurable {

    private final SdkModel sdkModel;
    private final SdkModel.Listener listener;
    private final DefaultComboBoxModel model;
    private final FxSdkAdditionalDataPanel panel;
    private Sdk visageSdk;

    public FxSdkAdditionalDataConfigurable (SdkModel sdkModel) {
        this.sdkModel = sdkModel;

        model = new DefaultComboBoxModel ();

        panel = new FxSdkAdditionalDataPanel ();
        panel.javaSdkCombo.setModel (model);
        panel.javaSdkCombo.setRenderer (new DefaultListCellRenderer() {
            public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                return super.getListCellRendererComponent (list, value instanceof ProjectJdk ? ((ProjectJdk) value).getName (): value, index, isSelected, cellHasFocus);
            }
        });

        listener = new SdkModel.Listener() {
            public void sdkAdded (Sdk sdk) {
                reloadModel ();
            }

            public void beforeSdkRemove (Sdk sdk) {
                reloadModel ();
            }

            public void sdkChanged (Sdk sdk, String previousName) {
                reloadModel ();
            }

            public void sdkHomeSelected (Sdk sdk, String newSdkHome) {
                reloadModel ();
            }
        };
    }

    private void reloadModel () {
        model.removeAllElements ();
        Object previouslySelected = model.getSelectedItem ();
        for (Sdk sdk : sdkModel.getSdks ()) {
            SdkType sdkType = sdk.getSdkType ();
            if (Comparing.equal (sdkType, JavaSdk.getInstance ()) || "IDEA JDK".equals (sdkType.getName ()))
                model.addElement (sdk);
        }
        model.setSelectedItem (previouslySelected);
    }

    public void setSdk (Sdk sdk) {
        visageSdk = sdk;
    }

    public JComponent createComponent () {
        sdkModel.addListener (listener);
        reloadModel ();
	reset ();
        apply ();
        return panel.panel;
    }

    public boolean isModified () {
        FxSdkAdditionalData additionalData = (FxSdkAdditionalData) visageSdk.getSdkAdditionalData();
        Sdk selectedSdk = (Sdk) model.getSelectedItem ();
        return additionalData == null  ||  ! Comparing.equal(selectedSdk != null ? selectedSdk.getName () : null, additionalData.getJavaSdkName());
    }

    public void apply () {
        final SdkModificator modificator = visageSdk.getSdkModificator ();
        Sdk selectedSdk = (Sdk) model.getSelectedItem ();
        modificator.setSdkAdditionalData (new FxSdkAdditionalData (selectedSdk != null ? selectedSdk.getName () : null, sdkModel));
        ApplicationManager.getApplication ().runWriteAction (new Runnable() {
            public void run () {
                modificator.commitChanges ();
            }
        });
        visageSdk.getSdkType ().setupSdkPaths (visageSdk);
    }

    public void reset () {
        FxSdkAdditionalData data = (FxSdkAdditionalData) visageSdk.getSdkAdditionalData ();
        if (data != null) {
            Sdk sdk = data.findSdk ();
            if (sdk != null)
                model.setSelectedItem (sdk);
        }
    }

    public void disposeUIResources () {
        sdkModel.removeListener (listener);
    }

}

