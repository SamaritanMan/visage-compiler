/*
 * init triggers with defaults
 *
 * @test
 * @compilefirst ../../TestUtils.fx
 * @run
 */

import java.lang.System;

var TU = new TestUtils;

class InitTest {
    var tf : Integer =0;
	 var firedtriggers:String[];
	 function triggers(msg:String) { System.out.print( "{msg}: triggers fired: {tf}: ");System.out.println({firedtriggers} ); }
    public var foo      : Boolean		 on replace old        {tf++; insert "foo" into firedtriggers; TU.addGFT(1);System.out.println("1. Replace Boolean old = {old}; new = {foo}");    }
    public var ifoo     : Integer		 on replace old        {tf++;  insert "ifoo" into firedtriggers;TU.addGFT(1);System.out.println("2. Replace Integer old = {old}; new = {ifoo}");    }
    public var dfoo     : Number			 on replace old{tf++;  insert "dfoo" into firedtriggers;TU.addGFT(1);System.out.println("3. Replace Number old = {old}; new = {dfoo}");    }
    public var sfoo     : String			 on replace old{tf++;  insert "sfoo" into firedtriggers;TU.addGFT(1);System.out.println("4. Replace String old = {old}; new = {sfoo}");    }
    public var tobject  : test_object	 on replace old{tf++;  insert "tobject" into firedtriggers;TU.addGFT(1);System.out.println("5. Replace test_object old = {old.name}; new = {tobject.name}");	 }
    public var seqbfoo  : Boolean[]		 on replace old{tf++;  insert "seqbfoo" into firedtriggers;TU.addGFT(1);if(seqbfoo!=null)System.out.println("6. Replace Boolean[] old = {old}; new = {seqbfoo}");    }
    public var seqifoo  : Integer[]		 on replace old{tf++; insert "seqifoo" into firedtriggers;TU.addGFT(1);if(seqifoo!=null)System.out.println("7. Replace Integer [] old = {old}; new = {seqifoo}");    }
    public var seqnfoo  : Number[]		 on replace old{tf++; insert "seqnfoo" into firedtriggers;TU.addGFT(1);if(seqnfoo!=null)System.out.println("8. Replace Number[] old = {old}; new = {seqnfoo}");    }
    public var seqsfoo  : String[]		 on replace old{tf++; insert "seqsfoo" into firedtriggers;TU.addGFT(1);if(seqsfoo!=null)System.out.println("9. Replace String[] old = {old}; new = {seqsfoo}");    }
    public var seqTOfoo : test_object[] on replace old{tf++;insert "seqTOfoo" into firedtriggers;TU.addGFT(1);
		 if(seqTOfoo!=null) {
		 System.out.print("10. Replace test_object[] old = ");
			 for (i in old) {System.out.print(" {i.name}"); }
		 System.out.print(" ... with new = ");
			 for (j in seqTOfoo) {System.out.print(" {j.name}"); }		 System.out.println();
		 }
	 }
}
/** default init */
System.out.println("Test i0: default init...");
var i0 = new InitTest;
i0.triggers("default init");
System.out.println();
/** defaults re-init */
System.out.println("Test i1: defaults re-init...");
var i1 = InitTest {
    foo:false
    ifoo: 0
    dfoo: 0.0
    sfoo: ""
    tobject: null
    seqbfoo: []
    seqifoo: []
    seqnfoo: []
    seqsfoo: []
	 seqTOfoo: []
};
i1.triggers("default re-init");
System.out.println("i1 After Create");
System.out.println("1. i1.foo = {i1.foo}");
System.out.println("2. i1.ifoo = {i1.ifoo}");
System.out.println("3. i1.dfoo = {i1.dfoo}");
if(i1.sfoo!=null)   System.out.println("4. i1.sfoo = {i1.sfoo}");
if(i1.tobject!=null)System.out.println("5. i1.tobject = {i1.tobject.name}");
if(i1.seqbfoo!=null)System.out.println("6. i1.seqbfoo = {i1.seqbfoo}");
if(i1.seqifoo!=null)System.out.println("7. i1.seqifoo = {i1.seqifoo}");
if(i1.seqnfoo!=null)System.out.println("8. i1.seqnfoo = {i1.seqnfoo}");
if(i1.seqsfoo!=null)System.out.println("9. i1.seqsfoo = {i1.seqsfoo}");
if(i1.seqTOfoo!=null)System.out.print("10. i1.seqTOfoo = {i1.seqTOfoo}");
System.out.println();

/** non-defaults init */
System.out.println("Test i2: non-defaults init...");
var i2 = InitTest {
    foo:true
    ifoo: 5
    dfoo: 3.14
    sfoo: "Hello, Dollie!"
    tobject: test_object { name: " non-default test_object" }
    seqbfoo: [true,true,true]
    seqifoo: [ 6,7,8,9,10]
    seqnfoo: [ 100,200,300]
    seqsfoo: ["Mary", " had", " a", " little"," lamb."]
	 seqTOfoo: [  test_object{name:"Peter"}, test_object{name:"Michael"},test_object{name:"Jan"}  ]
};
i2.triggers("non-default init");
System.out.println("i2 After Create");
System.out.println("1. i2.foo = {i2.foo}");
System.out.println("2. i2.ifoo = {i2.ifoo}");
System.out.println("3. i2.dfoo = {i2.dfoo}");
System.out.println("4. i2.sfoo = {i2.sfoo}");
System.out.println("5. i2.tobject = {i2.tobject.name}");
System.out.println("6. i2.seqbfoo = {i2.seqbfoo}");
System.out.println("7. i2.seqifoo = {i2.seqifoo}");
System.out.println("8. i2.seqnfoo = {i2.seqnfoo}");
System.out.println("9. i2.seqsfoo = {i2.seqsfoo}");
System.out.print("10. i2.seqTOfoo = ");  for (j in i2.seqTOfoo) {System.out.print(" {j.name}"); }		 System.out.println();
System.out.println();

/** non-defaults init mixed order*/
System.out.println("Test i3: non-defaults init, mixed order...");
var i3 = InitTest {
    seqTOfoo: [  test_object{name:"Peter"}, test_object{name:"Michael"},test_object{name:"Jan"}  ]
    seqnfoo: [ 100,200,300]
    dfoo: 3.14
    sfoo: "Hello, Dollie!"
    tobject: test_object { name: " non-default test_object" }
    foo:true
    seqbfoo: [true,true,true]
    seqifoo: [ 6,7,8,9,10]
    seqsfoo: ["Mary", " had", " a", " little"," lamb."]
    ifoo: 5
};
i3.triggers("mixed order init");
System.out.println();
System.out.println("i3 After Create");
System.out.println("1. i3.foo =  {i3.foo}");
System.out.println("2. i3.ifoo = {i3.ifoo}");
System.out.println("3. i3.dfoo = {i3.dfoo}");
System.out.println("4. i3.sfoo = {i3.sfoo}");
System.out.println("5. i3.tobject = {i3.tobject.name}");
System.out.println("6. i3.seqbfoo = {i3.seqbfoo}");
System.out.println("7. i3.seqifoo = {i3.seqifoo}");
System.out.println("8. i3.seqnfoo = {i3.seqnfoo}");
System.out.println("9. i3.seqsfoo = {i3.seqsfoo}");
System.out.print("10. i3.seqTOfoo = ");  for (j in i3.seqTOfoo) {System.out.print(" {j.name}"); }		 System.out.println();

class test_object {
   public var name:String = "default-test-object";
};
TU.report();
