package assortis.sources.language.javafx.events;

import javafx.ext.swing.*;

var animals = ["cat", "dog", "mouse"];
var index = 0;

BorderPanel{
    top: Label{ text: bind "Selected animal: {animals[index]}"}
    center: List{
        items: for( animal in animals)[
            ListItem{ text: animal }
        ]
        selectedIndex: bind index with inverse
    }
}
