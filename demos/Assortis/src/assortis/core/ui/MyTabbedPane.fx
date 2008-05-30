
package assortis.core.ui;


import javafx.gui.*;


import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SpringLayout;

//import javax.swing.*;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.BorderLayout;

import java.lang.System;

public class MyTabbedPane extends Component{
    
    private attribute tabbedPane: JTabbedPane;
    
    private attribute updateComponentFlag: Boolean = false;
    
    
    public attribute selectedIndex: Integer = -1 on replace{
        if( not updateComponentFlag){
            if((-1 < selectedIndex) and ( selectedIndex  < tabbedPane.getTabCount() )){
                tabbedPane.setSelectedIndex(selectedIndex);
            }
    }
};


    private function updateComponentField( index: Integer){
        updateComponentFlag = true;
        selectedIndex = index;
        updateComponentFlag = false;
    }


    public attribute tabs: MyTab[] on replace oldValue[lo..hi] = newVals{

        //System.out.println("[tabbed pane] set tabs");

        for (tab in oldValue[lo..hi]){
            //System.out.println("          remove tab: \"{tab.title}\"");
            tabbedPane.remove(tab.content.getJComponent().getParent());
        }
        for (tab in newVals){
            //System.out.println("          add tab: \"{tab.title}\"");
            var panel = new JPanel( new BorderLayout());
            panel.add( tab.content.getJComponent(), BorderLayout.CENTER);
            tabbedPane.addTab(tab.title,panel);
        }

    };


    protected function createJComponent(): JComponent {
        tabbedPane = new JTabbedPane();
        
        var panel = new JPanel(new BorderLayout());
        panel.add(tabbedPane, BorderLayout.CENTER);
        

        var listener = javax.swing.event.ChangeListener{
            public function stateChanged(e: javax.swing.event.ChangeEvent){
                //System.out.println("\n\n[tabbed pane] <state changed> index: {selectedIndex} -> {tabbedPane.getSelectedIndex()  - 1}");
                updateComponentField(tabbedPane.getSelectedIndex()); 
            } 
    }

        tabbedPane.addChangeListener(listener);


        //var scrollPane = new JScrollPane(panel);
        //return scrollPane;
        return panel;

    }

}