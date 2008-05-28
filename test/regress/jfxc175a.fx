/**
 * regression test: fix for one of the bugs in JFXC-175. The isssue is that the synthetic
 * class of a local ObjLit was generated at the top of the enclosing method and there was 
 * no access to any subsequent locall var declarations.
 * This tests is intended to verify that access from object literal to enclosing method
 * local variables is resolved correctly.
 * @test
 * @run
 */
class Bar {
	attribute a : Integer;
	function getA() : Integer { return a; }
}

class FooBar {
	function x() : Void {
		var methodFooBar : Integer = 0;
		var bar1 : Bar = new Bar;
		var barFoo : FooBar = new FooBar;
		Bar {
			function fooBar() : Void {
				methodFooBar = methodFooBar + 1;
				bar1.a = bar1.a + 1;
				bar1.getA();
			}
		};
		var aaa = 0;

		if (barFoo == this) {
			aaa = aaa + 1;
		}

		aaa = aaa + 1;
	}
}
