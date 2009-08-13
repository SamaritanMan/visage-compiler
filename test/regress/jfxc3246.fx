/**
 * Regress test for JFXC-3246 : Mixins: ambiguities that should not be.
 *
 * @test
 * @run
 */

mixin class Mixin1 {
    var name = "Mixin1:name";
	function nameFun() { "Mixin1:nameFun" }
}

mixin class Mixin2 {
    var name = "Mixin2:name";
	function nameFun() { "Mixin2:nameFun" }
}

class Mixee1 extends Mixin1, Mixin2 {}

class Mixee2 extends Mixin2, Mixin1 {}

class Mixee3 extends Mixin1, Mixin2 {
    override var name = "Mixee:name";
	override function nameFun() { "Mixee:nameFun" }
}

var m1 = Mixee1 {};
println("receiver is Mixee1");
println("name: {m1.name}");
println("nameFun: {m1.nameFun()}");

var m2 = Mixee2 {};
println("receiver is Mixee2");
println("name: {m2.name}");
println("nameFun: {m2.nameFun()}");

var m3 = Mixee3 {};
println("receiver is Mixee3");
println("name: {m3.name}");
println("nameFun: {m3.nameFun()}");
