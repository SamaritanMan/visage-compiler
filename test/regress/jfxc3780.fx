/**
 * JFXC-3780 : Compiled bind: bound function invocation selected on a immutable local variable of non-FX type gives symbol non found on attempt to make it a getter
 *
 * @test
 * @run
 */

import java.net.URI;

public class FBR {
    var location : String
}

package class jfxc3780 {
    package function getFriendsDetails() : String {
       println(" Get Friends details ....."); 
       var uri = new URI("foo");
       var request = FBR {
           location: bind uri.toString();
       };
       request.location
    }
}

function run() {
    println(jfxc3780{}.getFriendsDetails());
}
