/*
 * do, protected, and, or
 *
 * @test
 * @compilefirst ../TestUtils.fx
 * @run
 */

/**
 * protected	- does not function as it should yet due to incomplete implementation
 * do				- not implemented
 */

var TU = new TestUtils;
var bt1 = true;
var bt2 = true;
var bf1 = false;
var bf2 = false;
var b2 = false;

function t():Boolean { return true; }
function f():Boolean { return false;}
TU.checkB( bt1 and bt2,"check boolean true and boolean true");
TU.checkB( not bf1 and bt1,"check not boolean false and boolean true");
TU.checkB( true and true,"check true and true");
TU.checkB( not false and not false, "check not false and not false");
TU.checkB( t() and true,"t() and true" );
TU.checkB( t() and t(), "t() and t()" );
TU.checkB( t() and not f(),"t() and not f()");
TU.checkB( t() and t() and not f() and not f() and t() and true and true and not false,"strings of ands");
TU.checkB( true or false, "true or false");
TU.checkB( t() or f(),"t() or f()");
TU.checkB( (t() or f())  and  (t() and not f()),  "more and or combos");
TU.checkB( f() or false or ( f() and false) or (true and f()) or (t() or f()),"even more and/or combos");

TU.report();
