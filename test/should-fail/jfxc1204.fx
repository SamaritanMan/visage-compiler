/*
 * JFXC-12-4: NPE thrown by Compiler trying to access a sequence.
 *
 * @test/compile-error
 */
var f = [if(true){"Always"}] ;
//var f = [if(true){"Always"} else "Never" ] ; //On commenting above and uncommenting this line no error is thrown
java.lang.System.out.println("{f[0]}");
