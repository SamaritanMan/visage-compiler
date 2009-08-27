/**
 * JFXC-3404 : regression: infinite loop in Pointer.set().
 *
 * @test
 * @run
 */

class Color {
    var value:Integer;
    override public function toString(): String {
        return "Color \{ value: {value} \}";
    }
}

var color:Color;
var fill:Color = bind if (color.value < 5) 
     then color 
     else Color { value: 1 };

println("script start");

// The following assignments used to result with an infinite loop.
color = Color { value: 10 };
println("color = {color}, fill = {fill}");
color = Color { value: 1 };
println("color = {color}, fill = {fill}");
color = Color { value: 2 };
println("color = {color}, fill = {fill}");

println("script end"); 
