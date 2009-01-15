/**
 * JFXC-2668 : Regression: crash: no bound conversion of instance to sequence of superclass 
 *
 * @test
 */

class Transform {
}

class Scale extends Transform {
}

function scale() {
    return Scale{}
}

class Rectangle {
   var transforms: Transform[];
}

Rectangle {
   transforms: bind scale();
} 
