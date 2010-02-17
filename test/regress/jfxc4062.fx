/*
 * Regression: JFXC-4062 - Initialization of another object using a public-init field doesn't work in some scenarios
 *
 * @test
 * @run
 *
 */

class P { 
    public var d: Object[]; 
} 

class B { 
    public var c: String; 
} 

class A extends P { 
    public-init var t: String; 

    def l = B { 
        // Bind shoudln't be necessary! 
        //c: bind t 
        c: t 
    } 
     
    override var d = bind [ l ]; 

    postinit { 
        if (l.c == null) { 
            println('FAIL!!! t="{t}" but l.c="{l.c}", and they should be equal'); 
        } else { 
            println("Ok."); 
        } 
    } 
} 

A { 
    t: "-" 
} 

