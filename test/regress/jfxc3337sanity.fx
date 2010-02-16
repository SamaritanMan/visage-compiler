/**
 * JFXC-3337 : memory leak in bound for -- found in pursuit of RT-5120.
 *
 * The fix involves clearing "new" gap in ArraySequence objects. We want
 * to make sure that sequence on replace, bind etc. are fine.
 *
 * @test
 * @run
 */

var limi : Integer; 

class Node {
  public var name : String;

  override public function toString() : String {
      return name;
  }
}

class Scene {
  public var content : Node[] on replace oldVal[first..last] = newElements {
      println("Scene.content on replace begin");
      println("old value {oldVal}");
      println("first {first}");
      println("last {last}"); 
      println("new elements {newElements}");
      println("Scene.content on replace end");
  }
} 

var s = Scene { 
   content: bind 
                for (i in [0..limi]) 
                    Node { 
                        name : " Node-{i} "  
                    } 
}; 

function run() { 
  for (n in [0..5]) { 
    limi = n; 
  } 
  limi = -1; 
} 
