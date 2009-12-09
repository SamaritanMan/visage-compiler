/**
 * JFXC-3786 : Compiled Bind: TV - Crash compiling tv/tv-javafx-ui-prism/src/com/sun/javafx/tk/prism/AlertControl.fx.
 * 
 * @test
 * @run
 */

class Language {
    var name : String;
    public override function toString() : String {
        return name;
    }
}

function func(x: Integer) {
    var languages : Language[];

    var l1 = Language { name: bind "Java" };
    var l2 = Language { name: bind "Javafx" };

    if (x == 0) languages = [l1, l2]
    else if (x == 1) languages = [l2, l1]
    else if (x == 2) languages = [l1]
    else throw new java.lang.IllegalArgumentException();
}

println(func(0));
println(func(1));
println(func(2));
