package studiomoto;
import javafx.ui.*;
import javafx.ui.canvas.*; 
import javafx.ui.filter.*;
import java.lang.System;
import studiomoto.MotoMenuButton;
import javafx.ui.animation.*;
import com.sun.javafx.runtime.PointerFactory;
import com.sun.javafx.runtime.Pointer;
import javafx.lang.Duration;


var frame:Frame;
var canvas:Canvas;

// Workaround for lack of local var trigger
class HomeModel {

    attribute pf: PointerFactory = PointerFactory{};

    attribute ys = [[0..-18 step -1],[-18..-12]];
    attribute homeY:Number = 0;
    attribute __homeY = bind pf.make(homeY);
    attribute _homeY = __homeY.unwrap();
    attribute interval = 300/sizeof ys;
    attribute homeSequence: Timeline = Timeline {
        keyFrames: for(s in reverse ys) {
            KeyFrame {
                keyTime: Duration {millis: interval}
                relative: true
                keyValues: NumberValue {
                    target: _homeY
                    value: s
                }
            }
        }
     };
    attribute homeSequenceR:Timeline = Timeline {
        keyFrames: for(s in  ys) {
            KeyFrame {
                keyTime: Duration {millis: interval}
                relative: true
                keyValues: NumberValue {
                    target: _homeY
                    value: s
                }
            }
        }
    };
    attribute homeButton: HomeButton;
    attribute selection: Integer on replace {
        if (selection > 0) {
            if(homeButton.hover) {
                homeSequence.start();
            } else {
                homeSequenceR.start();
            }
        }
    }
};


frame = Frame {

    /** various bugs encounted with this attempt - which is itself a workaround for lack of local variable trigger: having these varibles at the top level gets undefined symbol $receiver
var homeY:Number = 0;
var selection = 0;
var home: HomeButton;

var homeModel = HomeModel {
    homeY: bind homeY with inverse;
    selection: bind selection with inverse;
    homeButton: bind home with inverse
};
*/

    // final workaround: change references to local var x to homeModel.x

    var homeModel = HomeModel {}

    centerOnScreen: true
    onClose: function() {System.exit(0);}
    title: "JavaFX - Motorola Music"
    height: 700
    width: 1100
    visible: true 
    var splash = StudioMotoSplash {
        onDone: function() {homeModel.selection = 0;}
    }    
    private attribute tshowing = bind showing on replace {
            homeModel.selection = 0;
            splash.doSplash();
    };
    

    background: Color.BLACK
    content:
    canvas = Canvas {
        background: Color.BLACK
        var w = 1300
        var h = 700
        content:
        [Group {
            content:
            [HBox {
                content: 
                [ImageView {
                    image: Image{url: "{__DIR__}Image/1.jpg"}
                },
                ImageView {
                    image: Image{url: "{__DIR__}Image/1.jpg"}
                }]
                onMouseClicked: function(e) {splash.doSplash();}
            },
            Group {
                isSelectionRoot: true
                transform: Transform.translate(60, 70)
                content:
                [ImageView {
                    //982x527
                    //transform: Transform.translate(527/2, 40)
                    
                    //halign: HorizontalAlignment.CENTER
                    image: Image{url: "{__DIR__}Image/4.png"}
                },
                Group {
                    transform: Transform.translate(30, 20)
                    content:
                    [Title1 {
                        height: 50
                        width: 150
                        label1: "<html><span style='font-size:12;color:#dddddd'>welcome to</span></html>"
                        label2: "<html><div style='font-size:28;'><span style='color:white'>studio</span><span style='color:yellow;font-weight:bold;'>moto</span></div></html>"
                        label3: "<html><span style='font-size:12;color:white'>welcome to</span></html>"
                    } as Node],
                },
                Group {
                    //transform: bind Transform.translate(canvas.width/2, h/2)
                    transform: Transform.translate(17, 50)
                    //halign: HorizontalAlignment.CENTER
                    //valign: MIDDLE, halign: HorizontalAlignment.CENTER
                    content: //if true then null else
                    [Group {
                        //transform: Transform.translate(700, 0) // original 
                        transform: Transform.translate(410, 0) 
                        halign: HorizontalAlignment.CENTER
                        content: HBox {
                            clip: Clip{shape: Rect {y: -50, height: 100, width: w}}
                            var labels1 = ["about", "inside", "music", "moto", "site"]
                            var labels2 = ["studiomoto", "music", "playtime", "products", "support"]
                            var a = MotoMenuAnimation{}
                            var dummy = a.getNode()
                            content: 
                            [Group {
                                isSelectionRoot: true
                                content: 
                                [Rect {height: 30+68, width: 139, selectable: true, fill: Color.color(0, 0, 1, 0), visible: bind homeModel.selection > 0},
                                (homeModel.homeButton = HomeButton {
                                    // TODO moved var tmp up to Frame at the top. See note there.

                                    transform: bind Transform.translate(-5, -10  + (if (homeModel.selection == 0) 30 else homeModel.homeY))
                                    action: function() {homeModel.selection = 0;}
                                }) as Node ]
                            },
                            
                            for (i in [0..<sizeof labels1]) 
                            MotoMenuButton {
                                anim: a
                                action: function() {homeModel.selection = indexof i+1;}
                                label1: labels1[i]
                                label2: labels2[i]
                                transform: Transform.translate(5, 0)
                            } as Node]
                        }
                    },            
                    Group {
                        transform: Transform.translate(0, 28)
                        content:
                        [Group {
                            content:
                            [ImageView {
                                image: Image{url: "{__DIR__}Image/71.png"}
                            },
                            ImageView {
                                transform: Transform.translate(22, 17)
                                image: Image{url: "{__DIR__}Image/72.png"}
                            },
                            MotoCenterPanel {
                                transform: Transform.translate(957/2+15, 20)
                                halign: HorizontalAlignment.CENTER
                                height: 220
                                width: 400
                            } as Node]
                        }]                    
                    }]
                }]
            },
            Group {
                transform: Transform.translate(875, 100)
                content:
                [Title1 {
                    height: 50
                    width: 130
                    label1: "<html>powered by</html>"
                    label2: "<html>Motorola</html>"
                    label3: "<html>power</html>"
                } ,
                ImageView {
                    transform: Transform.translate(80, 0)
                    // 42x42
                    image: Image{url: "{__DIR__}Image/5.png"}
                }]
            },
            MotoBottomPane {
                transform: Transform.translate(100, 400)
                selection: bind homeModel.selection
                panels:
                [MotoBottomPanel {
                    panelHeight: 170
                    panelWidth: 240
                    panelMargin: 15
                },
                About {
                    height: 180
                    width: 880
                },
                InsideMusic {
                    height: 180
                    width: 300
                },
                MusicPlaytime {
                    height: 180
                    width: 880
                },
                MotoProducts {
                    height: 180
                    width: 880
                },
                SiteSupport {
                    height: 180,
                    width: 250
                }]
            }]
        },
        Group {
            visible: bind splash.backgroundAlpha > .01
            content: splash as Node
        }]
    }
}


