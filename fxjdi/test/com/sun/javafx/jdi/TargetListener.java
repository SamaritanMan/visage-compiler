/*
 * Copyright 2001-2005 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.javafx.jdi;

import com.sun.jdi.event.*;

/**
 * Event listener framework
 */
public interface TargetListener {
    boolean shouldRemoveListener();

    void eventSetReceived(EventSet set);
    void eventSetComplete(EventSet set);
    void eventReceived(Event event);
    void breakpointReached(BreakpointEvent event);
    void exceptionThrown(ExceptionEvent event);
    void stepCompleted(StepEvent event);
    void classPrepared(ClassPrepareEvent event);
    void classUnloaded(ClassUnloadEvent event);
    void methodEntered(MethodEntryEvent event);
    void methodExited(MethodExitEvent event);
    void monitorContendedEnter(MonitorContendedEnterEvent event);
    void monitorContendedEntered(MonitorContendedEnteredEvent event);
    void monitorWait(MonitorWaitEvent event);
    void monitorWaited(MonitorWaitedEvent event);
    void fieldAccessed(AccessWatchpointEvent event);
    void fieldModified(ModificationWatchpointEvent event);
    void threadStarted(ThreadStartEvent event);
    void threadDied(ThreadDeathEvent event);
    void vmStarted(VMStartEvent event);
    void vmDied(VMDeathEvent event);
    void vmDisconnected(VMDisconnectEvent event);
}
