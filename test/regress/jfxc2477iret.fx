/**
 * JFXC-2477 : Correct conversion problems that show-up when Number=Float
 *
 * @test
 * @run
 */

class A {
	def bv : Byte = 16;
	def sv : Short = 0x202;
	def iv : Integer = 0x04040404;
	def lv : Long = 0x08080808; //TODO: constants, separate issue 08;
	def fv : Float = 2.5;
	def dv : Double = 3.14159265358979323;
}

class B extends A {
	function bl() : Byte { if (true) { return bv }; 1 }
	function sl() : Short { if (true) { return bv }; 1 }
	function il() : Integer { if (true) { return bv }; 1 }
	function ll() : Long { if (true) { return bv }; 1 }
	function fl() : Float { if (true) { return bv }; 1 }
	function dl() : Double { if (true) { return bv }; 1 }
	function p() {
		println( bl() );
		println( sl() );
		println( il() );
		println( ll() );
		println( fl() );
		println( dl() );
	}
}
(new B).p();

class S extends A {
	function bl() : Byte { if (true) { return sv }; 1 }
	function sl() : Short { if (true) { return sv }; 1 }
	function il() : Integer { if (true) { return sv }; 1 }
	function ll() : Long { if (true) { return sv }; 1 }
	function fl() : Float { if (true) { return sv }; 1 }
	function dl() : Double { if (true) { return sv }; 1 }
	function p() {
		println( bl() );
		println( sl() );
		println( il() );
		println( ll() );
		println( fl() );
		println( dl() );
	}
}
(new S).p();

class I extends A {
	function bl() : Byte { if (true) { return iv }; 1 }
	function sl() : Short { if (true) { return iv }; 1 }
	function il() : Integer { if (true) { return iv }; 1 }
	function ll() : Long { if (true) { return iv }; 1 }
	function fl() : Float { if (true) { return iv }; 1 }
	function dl() : Double { if (true) { return iv }; 1 }
	function p() {
		println( bl() );
		println( sl() );
		println( il() );
		println( ll() );
		println( fl() );
		println( dl() );
	}
}
(new I).p();

class L extends A {
	function bl() : Byte { if (true) { return lv }; 1 }
	function sl() : Short { if (true) { return lv }; 1 }
	function il() : Integer { if (true) { return lv }; 1 }
	function ll() : Long { if (true) { return lv }; 1 }
	function fl() : Float { if (true) { return lv }; 1 }
	function dl() : Double { if (true) { return lv }; 1 }
	function p() {
		println( bl() );
		println( sl() );
		println( il() );
		println( ll() );
		println( fl() );
		println( dl() );
	}
}
(new L).p();

class F extends A {
	function bl() : Byte { if (true) { return fv }; 1 }
	function sl() : Short { if (true) { return fv }; 1 }
	function il() : Integer { if (true) { return fv }; 1 }
	function ll() : Long { if (true) { return fv }; 1 }
	function fl() : Float { if (true) { return fv }; 1 }
	function dl() : Double { if (true) { return fv }; 1 }
	function p() {
		println( bl() );
		println( sl() );
		println( il() );
		println( ll() );
		println( fl() );
		println( dl() );
	}
}
(new F).p();

class D extends A {
	function bl() : Byte { if (true) { return dv }; 1 }
	function sl() : Short { if (true) { return dv }; 1 }
	function il() : Integer { if (true) { return dv }; 1 }
	function ll() : Long { if (true) { return dv }; 1 }
	function fl() : Float { if (true) { return dv }; 1 }
	function dl() : Double { if (true) { return dv }; 1 }
	function p() {
		println( bl() );
		println( sl() );
		println( il() );
		println( ll() );
		println( fl() );
		println( dl() );
	}
}
(new D).p();
