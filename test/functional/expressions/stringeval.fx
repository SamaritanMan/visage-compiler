/*
 * @test
 * @run
 *
 * Example of one of the differnt behaviours of fx evaluation.
 * With '}' not semicolon is required, so the block expression below
 * amounts to 7 expressions, the last of which evaluates to 'x' and that
 * is the value of the expression that is assigned to st.
 */


function foo() {println("hello")}
function y() { 5 }

var st:String = {
    {"a";"b"}
    {"c";"d"}
    {"e";"f"}
    { foo() }
    { y()   }
   "x"
};
println( st )
