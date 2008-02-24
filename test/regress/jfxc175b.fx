/**
 * regression test: fix for one of the bugs in JFXC-175. The isssue is that the synthetic
 * class of a local ObjLit was generated at the top of the enclosing class and there was 
 * no access to any subsequent locall var declarations. This is for the case when the 
 * ObjLit is in onInsert, onReplace, etc methods of the Sequence modifications.
 * @test
 * @run
 */
class BarTest {
	attribute a : Integer = 0;
	function action() : Void {};
}

class Bar {
    public attribute barTest : BarTest[]
	on replace oldValue[a..b] = newElements {
	    for (newValue in newElements) {
                var k = newValue.a;
	        java.lang.Object {
                    public function isEnabled():Integer {
                        return newValue.a;
                    }
                    public function actionPerformed(e:java.awt.event.ActionEvent):Void {
                        newValue.action();
                    }
                }
	    }
        };
}
