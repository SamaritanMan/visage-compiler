
package jfx.assortie.lang.api.widgets;


import jfx.assortie.system.Module;
import jfx.assortie.system.Sample;

Module {
    
    name: "Widgets"
    
    samples: [
    Sample{
        name: "Label"
        className: "jfx.assortie.lang.api.widgets.label.FXLabel"
    },Sample{
        name: "Button"
        className: "jfx.assortie.lang.api.widgets.button.FXButton"
        //visible: true
    },Sample{
        name: "CheckBox"
        className: "jfx.assortie.lang.api.widgets.button.FXCheckBox"
    },Sample{
        name: "RadioButton"
        className: "jfx.assortie.lang.api.widgets.button.FXRadioButton"
    },Sample{
        name: "List"
        className: "jfx.assortie.lang.api.widgets.list.FXList",
        //visible: true
    },Sample{
        name: "ComboBox"
        className: "jfx.assortie.lang.api.widgets.combobox.FXComboBox",
       //visible: true
    },Sample{
        name: "Table"
        className: "jfx.assortie.lang.api.widgets.table.FXTable",
    },Sample{
        name: "Tree"
        className: "jfx.assortie.lang.api.widgets.tree.FXTree",
    },Sample{
        name: "Box"
        className: "jfx.assortie.lang.api.widgets.box.FXBox",
    },Sample{
        name: "Frame"
        className: "jfx.assortie.lang.api.widgets.frame.FXFrame",
    }
    ]
}
