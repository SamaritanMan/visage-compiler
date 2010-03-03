/**
 * JFXC-3560 -  Compiling a JavaFX file having 2 classes crashes the compiler
 * 
 * @test/compile-error
 */

package com.cutlery;

class Cup {
protected var material: String;
protected var purpose: String;

init {
material = "Porcelain";
purpose = "tea";
}
}

class Saucer {
var material = bind Cup.material;
var purpose = bind Cup.purpose;
}

var s = Saucer {};

println(s.material);
println(s.purpose);

