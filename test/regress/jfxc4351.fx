/* 
 * Regression: JFXC-4351 - javafxc execution failed with internal error when var declared with same name in two object literals.
 *
 * @test
 *
 */ 

class Stage {
    var scene;
}

class Scene {
}

Stage {
    var scene:Scene;
    scene: scene = Scene {
    }
}

Stage {
    var scene:Scene;
    scene: scene = Scene {
    }
}

Stage {
    var scene:Scene;
    scene: scene = Scene {
    }
}

Stage {
    var scene:Scene;
    scene: scene = Scene {
    }
}

Stage {
    var scene:Scene;
    scene: scene = Scene {
    }
}
