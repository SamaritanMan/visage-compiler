/**
 * Regress test for JFXC-3076 - Can no longer use same variable name as member var and as local var in run() function
 *
 * @test/fail
 */

public class jfxc3076script {
    var name = 0;
}

public function run(args:String[]):Void {
    var name = 0;
    println("ran!");
}

