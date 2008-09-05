/*
 * jfxc-1945: check for malformed main
 * Incorrect return value.
 * @test
 *
 * Test currently compiles with incorrect return value.
 * Actually, I can put a comletely bogus return value and it appears it is
 * not checked at all
 * eg., run():java.lang.whatisthisreturnvalue {...}
 */
import java.lang.*;

public var varcount = 0;

function run(__ARGS__ : String[]):Integer {
 varcount = sizeof __ARGS__;
 System.out.println("var count: {varcount}");
 return varcount;
}
