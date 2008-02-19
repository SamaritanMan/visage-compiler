package studiomoto;
import javafx.ui.*;
import javafx.ui.canvas.*;

public class InsideMusicPane extends MotoPanel {
    
    init {
        //TODO Override
    title = Group {
        content:
        [View {
            content: Label {
                text: "<html><div style='font-face:Arial;font-size:14pt'><span style='color:white;'>Inside</span><span style='color:yellow;'>Music</span></div></html>"
            }
        },
        Text {content: "COURTESY OF ROLLING STONE", fill: Color.WHITE,
            font: Font.Font("Arial", ["PLAIN"], 8),
            halign: HorizontalAlignment.TRAILING
            transform: bind Transform.translate(width-30, 8)
        }]
    };
    content = Group {
        content:
        [Group {
        // items
        },
        ImageView {
            transform: bind Transform.translate(0, 120)
            image: Image {url: "{base}/Image/95.png"}
        },
        ImageView {
            halign: HorizontalAlignment.TRAILING
            valign: VerticalAlignment.BOTTOM
            transform: bind Transform.translate(width -30, height -35)
            image: Image {url: "{base}/Image/88.png"}
        }]
    };
    
    }
}



Canvas {
    background: Color.BLACK
    content:
    InsideMusic {height: 180, width: 250}
}

