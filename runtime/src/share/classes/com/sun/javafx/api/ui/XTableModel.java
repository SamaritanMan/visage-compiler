/*
 *  $Id$
 * 
 *  Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 *  SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.javafx.api.ui;
import javax.swing.table.*;
import javax.swing.*;
import javax.swing.event.*;


public class XTableModel extends AbstractTableModel {

    boolean mLocked;
    boolean mDirty;
    int mRowCount;
    String[] mColumnNames;

    public void addRow(int row) {
        mRowCount++;
        fireTableRowsInserted(row, row);
    }

    public void setRowCount(int rowCount) {
        mRowCount = rowCount;
    }

    public void removeRow(int row) {
        mRowCount--;
        fireTableRowsDeleted(row, row);
    }

    public void updateRow(int row) {
        fireTableRowsUpdated(row, row);
    }

    public void setColumnNames(String[] names) {
        mColumnNames = names;
    }

    @Override
    public String getColumnName(int col) {
        return mColumnNames[col];
    }

    public Object getValueAt(int row, int column) {
        return null;
    }

    public int getColumnCount() {
        return mColumnNames == null ? 0 : mColumnNames.length;
    }

    public int getRowCount() {
        return mRowCount;
    }

    public void setLocked(boolean value) {
        mLocked = value;
        if (!value) {
            if (mDirty) {
                mDirty = false;
            }
            fireTableStructureChanged();
        }
    }

    public boolean isLocked() {
        return mLocked;
    }

    @Override
    public void fireTableDataChanged() {
        if (!mLocked) {
            super.fireTableDataChanged();
        } else {
            mDirty = true;
        }
    }

    @Override
    public void fireTableStructureChanged() {
        if (!mLocked) {
            super.fireTableStructureChanged();
        } else {
            mDirty = true;
        }
    }

    @Override
    public void fireTableRowsInserted(int firstRow, int lastRow) {
        if (!mLocked) {
            super.fireTableRowsInserted(firstRow, lastRow);
        } else {
            mDirty = true;
        }
    }

    @Override
    public void fireTableRowsUpdated(int firstRow, int lastRow) {
        if (!mLocked) {
            super.fireTableRowsUpdated(firstRow, lastRow);
        } else {
            mDirty = true;
        }
    }

    @Override
    public void fireTableRowsDeleted(int firstRow, int lastRow) {
        if (!mLocked) {
            super.fireTableRowsDeleted(firstRow, lastRow);
        } else {
            mDirty = true;
        }
       
    }
    @Override
    public void fireTableCellUpdated(int row, int column) {
        if (!mLocked) {
            super.fireTableCellUpdated(row, column);
        } else {
            mDirty = true;
        }
    }

    @Override
    public void fireTableChanged(TableModelEvent e) {
        if (!mLocked) {
            // Guaranteed to return a non-null array
            Object[] listeners = listenerList.getListenerList();
            // Process the listeners last to first, notifying
            // those that are interested in this event
            for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i]==TableModelListener.class) {
                    ((TableModelListener)listeners[i+1]).tableChanged(e);
                }
            }
        } else {
            mDirty = true;
        }
    }

}