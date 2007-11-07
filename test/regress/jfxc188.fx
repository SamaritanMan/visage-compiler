/*
 * NullPointerException is thrown if compiled, but it compiles successfully
 * if "hello():Object" is substituted.
 */

import java.lang.*;

function hello() { return "hello, world"; }

System.out.println(hello().toString());
