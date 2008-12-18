/**
@test
@run
**Functional Testcase for checking Type inference of vars and function return types based on their assignment expressions and return/last statements,verifcation done using Reflection Apis. Currently only basic functionalities like getvars(),getFunctions() ,getSuperClasses() are supported.
**/

import javafx.reflect.*;
import java.lang.*;
class TypeInference  {
   bound function func0(s:Integer) { s+2};
   function func1 (input:String) :String {input};
   function func2 (input:String) {func1(func1(input));};
   function func3(input:Integer[]){input [ n | n / 2 ==0]};
   function func4(par1:Integer,par2:Integer){1+par2;TypeInference{}}
   function func5(par1:Integer[],par2:Integer) { var seq = par1; insert par2 into seq; seq};
   function func6(){function(i:Integer){return i}}
   function func7(f:function():javafx.lang.Duration) {f()}; // Type of f() expression is javafx.lang.Duration hence result type of func7 is inferred to javafx.lang.Duration

   var fnAttr1= function(t:Integer,r:Integer) {r + t};  // Result type of function var inferred to Integer
   var fnAttr2 : function(:Number,:String)= function(v:Number,w:String){if(true){ new java.lang.Integer(5) } else if(false) {2}else {4}};
   var fnAttr3 : function(:Number,:String):Integer;
   var funAttr2: function():function(); //Bug JFXC-1425
   var fnAttr4 = function():function() {function(){}};
   
   var attr1 on replace {};
   var stringrAttr1 = func1("dummy1");
   var stringrAttr2 = {(((((func2("dummy2"))))));}
   var stringAttr3 = if(true){ new java.lang.String() } else if(false) {"String"}else {""};
   var numberAttr1 ;
   var numberAttr2 =if(true){(numberAttr1 +1)} else {numberAttr1 + numberAttr1};

   var intAttr1 = fnAttr1(1,2);
   var intAttr2 = if(true){ new java.lang.Integer(5) } else if(false) {2}else {-2};
   var intAttr3 = sizeof (for(i in [9..100] ) {i} );
   var intSeq1= func3([1..100]);
   var seq2 =[TypeInference{},TypeInference3{},new TypeInference3()]; //Type infered to TypeInference 
   var seq3 =[new String (),"ty1","ty2"];
   var seq4 =[1,0.2,new java.lang.Integer(2)];
   var objAttr1 = null;
   var objAttr2 = if(true){1}else {"av"};
   var booleanAttr1;
   var type1Attr1 = if(true){ TypeInference{} } else if (true) { null}else {new TypeInference};
   init{
	booleanAttr1 = type1Attr1 instanceof TypeInference;
   }	
};

abstract class  TypeInference2 extends ReflectionTest.TypeInference {
    var myAttr=func8(); //Type inferred to String
    abstract function func8():String;
}
class TypeInference3 extends TypeInference,TypeInference2 {
    override function func8(){ myAttr};
    override var intAttr1 = 20;
    override var myAttr = "t3";
    var newAttr = func3([2]);
    var anotherAttr = func8 ();
}
var context : FXLocal.Context = FXLocal.getContext();
var classRef = context.findClass("ReflectionTest.TypeInference3");
System.out.println("Reflecting class {classRef.getName()}");
System.out.println("Inherited SuperClasses of TypeInference3 => {classRef.getSuperClasses(true)}");
for (attr in classRef.getVariables(true)) {
  System.out.println(" Type of {attr.getName()} is inferred as {attr.getType()}") ;
}
for (mr in classRef.getFunctions(true)) {
    System.out.println(" Return Type of method {mr.getName()} inferred as {mr.getType().getReturnType()}");
}

