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

package org.visage.ideaplugin;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * VisageFileType
 *
 * @author Brian Goetz
 */
public class VisageFileType extends LanguageFileType {

    public VisageFileType() {
        super(VisagePlugin.VISAGE_LANGUAGE);
    }

    @NotNull
    @NonNls
    public String getName() {
        return VisagePlugin.VISAGE_LANGUAGE_NAME;
    }

    @NotNull
    public String getDescription() {
        return VisagePlugin.VISAGE_LANGUAGE_NAME;
    }

    @NotNull
    @NonNls
    public String getDefaultExtension() {
        return VisagePlugin.VISAGE_FILE_EXTENSION;
    }

    @Nullable
    public Icon getIcon() {
        return VisagePlugin.VISAGE_ICON;
    }

    public boolean isJVMDebuggingSupported () {
        return true;
    }
}
