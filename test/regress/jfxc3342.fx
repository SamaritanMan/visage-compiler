/**
 * JFXC-3342 : variable not updated properly when it is initialized as bound to a variable defined later in the class.
 *
 * @test
 * @run
 */

class A { 
    public-read var disabled:Boolean = bind disable; 
    public var disable:Boolean = false; 
} 

class B { 
    // variable order is reversed 
    public var disable:Boolean = false; 
    public-read var disabled:Boolean = bind disable; 
} 

class C { 
    var a:A; 
    var b:B; 
    public var aDisabled:Boolean = bind a.disabled; 
    public var bDisabled:Boolean = bind b.disabled; 
} 

var c1 = C { 
    a: A { disable: true } 
    b: B { disable: true } 
} 
println("c1.a.disabled={c1.a.disabled} c1.b.disabled={c1.b.disabled}"); 
println("c1.aDisabled={c1.aDisabled} c1.bDisabled={c1.bDisabled}"); 

var d = true; 
var c2 = C { 
    a: A { disable: bind d } 
    b: B { disable: bind d } 
} 
println("c2.a.disabled={c2.a.disabled} c2.b.disabled={c2.b.disabled}"); 
println("c2.aDisabled={c2.aDisabled} c2.bDisabled={c2.bDisabled}"); 

