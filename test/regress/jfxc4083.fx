/**
 * JFXC-4083 : On-replace handler is not called for a bound-with-inverse sequence property
 *
 * @test
 * @run
 */

var ok = false; // just tracks whether the on-replace handler is called

class A {
    public var p: String[];

    init {
        B {
            p: bind p with inverse
        }
    }
}

class B {
    public var p:String[] on replace {
        ok = true;
    }
}

A {
    p: ["bar"]
};

println(if (ok) "OK" else "FAIL!"); 
