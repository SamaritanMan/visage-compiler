/*
 * Regression : JFXC-3964 - Overridden sequence var -- replaceSlice -- invalidate$: IllegalArgumentException: no such variable.
 *
 * @test
 * @run
 *
 */

class X { } 

class Y { 
    var a:X[]; 
} 

class Z extends Y { 
    var b:X[]; 
    override var a = [ 
         b = [ X { } ] 
    ] 
} 

Z { } 
