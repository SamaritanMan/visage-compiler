/* Feature test #20 -- bound function
 *
 * Make sure that bound function is evaluated for argument changes,
 * receiver change and change in instance fields of receiver.
 *
 * @author A. Sundararajan
 * @test
 * @run
 */


class BoundPerson {
   var name : String;
   bound function greet(g: String) {
      return "{g}, my name is {name}";
   }
}

var sundar = BoundPerson { name: "Sundar" }
var kannan = BoundPerson { name: "Kannan" }

var person = sundar;
var arg = "JavaFX";
var k = bind person.greet(arg);
println(k);

// change instance field of receiver
person.name = "Sundararajan";
println(k);

// argument change
arg = "JavaFX Script";
println(k);

// change the receiver
person = kannan;
println(k);
