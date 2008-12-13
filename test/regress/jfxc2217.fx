/**
 * jfx2217: On assignment, object literal init, or initial value, convert (non-literal) nullable value
 *
 * @test
 * @run
 */

sun.io.ByteToCharEUC_TW.unicodeCNS2 = null;

// initial values

var str2 : String = sun.io.ByteToCharEUC_TW.unicodeCNS2;
println("init: str2 should be empty: /{str2}/");

var str4 : String = java.lang.System.getProperty("abc");
println("init: str4 should be empty: /{str4}/");

var str5: java.lang.String = null;
println("init: str5 should be empty: /{str5}/");


var obj1: java.lang.Object = null;
var dur1: Duration = obj1 as Duration;
println("init: dur1 should be empty: /{dur1}/");

var dur2 = obj1 as Duration;
println("init: dur2 should be empty: /{dur2}/");

// object literal init
class class1 {
    var strField: String;
};

var class1Var = class1{strField: sun.io.ByteToCharEUC_TW.unicodeCNS2};
println("object literal: strField should be empty: /{class1Var.strField}/");

var class2Var = class1{strField: java.lang.System.getProperty("abc")};
println("object literal: strField should be empty: /{class2Var.strField}/");


// assignment
function func1() {
    var local1: String;

   local1 = sun.io.ByteToCharEUC_TW.unicodeCNS2;
   println("assing: local1 should be empty: /{local1}/");

   local1 = java.lang.System.getProperty("abc");
   println("assign: local1 should be empty: /{local1}/");
}

func1();

// negative tests
def val1 = "hi";
function func2(): String {
    return "89";
}

var neg1 = val1;
var neg2 = func2();
var neg3 = class1{strField: val1};
var neg4 = class1{strField: func2()};

