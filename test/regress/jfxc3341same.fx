/**
 * JFXC-3341 : Play button doesn't change to Stop in PathAnimation 
 *
 * @test
 * @run
 */

class Scenario {
    def vb1 = bind anim.animated on replace {};
    var anim = Anim{animated: true};
}

class Anim {
    var animated : Boolean;
}

function run() {
    var jj = Scenario { };
    if (not jj.vb1) {
        println("--Fails: jj.vb1 is false");
    }
}
