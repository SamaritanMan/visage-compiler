/**
 * JFXC-2706 : AbstractBoundComprehension: NPE during execution
 *
 * Number ranges
 *
 * @test
 * @run
 */

var x = 5;
var y = 10;
var z = 1;
 
var range = bind for (i in [x..y step z]) "{i},";
println("1st range = [{range}]");
z=5;
println("2nd range = [{range}]");
x = -2;
y = 0;
println("3rd range = [{range}]"); 
