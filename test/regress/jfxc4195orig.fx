/*
 * JFXC-4195 : EffectsPlaygroundMobile: Flip Vertical moves the image too high
 * aka: Diamond shaped invalidations
 *
 * Original non-GUI
 *
 * @test
 * @run
 */

class Parent {
    var scene:Scene;
    var children:Rectangle[] on replace oldNodes[a..b] = newNodes { }
}

class Root extends Parent {
    override var children = bind scene.content with inverse;
}

class Scene {
    var width:Number;
    var height:Number;
    def root = Root { scene:this }
    var content:Rectangle[];
}

class Stage {
    var width:Number on replace {};
    var height:Number on replace {};
    var scene:Scene on replace { scene.width = width; scene.height = height;}
}

class Rectangle {
   var transforms:Object[] on replace {};
   var width:Number;
   var height:Number;
}

class A {    
    public var scaleFactor = 1.0 on replace { println("factor is {scaleFactor}"); }
    public var photoWidth = bind 100*scaleFactor on invalidate {println("invalidating photoWidth");};
    public var photoHeight = bind 100*scaleFactor on invalidate {println("invalidating photoHeight");};

    var x =   Rectangle {
                    width:100
                    height:     bind 100 + photoHeight
                    transforms: bind f(0, photoWidth/2, photoHeight/2);
                };
}

function f(x:Number,y:Number,z:Number) {
   println("dump [angle={x}, width = {y}, height = {z}]");
   Object{}
}


//// START BLOCK
var width:Number = bind scene.width; //fwd ref here!
var height:Number = bind scene.height; //fwd ref here!
////END BLOCK --- move this block after 'scene' and the app will work

var scaleFactor: Number = bind if(width>height) height/320 else width/240 on replace {}
var a = A { scaleFactor: bind scaleFactor };
var scene:Scene = Scene {
    content: bind a.x
    width:100;
    height:100;
}

scene.content[0].transforms;
a.photoHeight; // <-------------------- remove this and it works just fine!!
Stage {
    scene: scene
    width: 240
    height: 360
};

scene.content[0].transforms;
