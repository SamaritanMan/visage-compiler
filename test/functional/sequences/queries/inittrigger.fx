/*
 * extended version of testcase for jfxc635
 *
 * @test
 * @run
 */

import java.lang.System;
class InitTest {
    public attribute foo:Boolean = true on replace old {
        System.out.println("1. Replace Boolean old = {old}; new = {foo}");
    }
    public attribute ifoo:Integer = 10 on replace old {
        System.out.println("2. Replace Integer old = {old}; new = {ifoo}");
    }
    public attribute dfoo:Number = 20 on replace old {
        System.out.println("3. Replace Number old = {old}; new = {dfoo}");
    }
    public attribute sfoo:String = "Hello, World!" on replace old {
        System.out.println("4. Replace String old = {old}; new = {sfoo}");
    }

	 public attribute tobject:test_object = test_object{name:"declared default test_object"} on replace old {
		 System.out.println("5. Replace test_object old = {old.name}; new = {tobject.name}");
	 }

    public attribute seqbfoo:Boolean[] = [false,false,false] on replace old {
        System.out.println("6. Replace Boolean[] old = {old}; new = {seqbfoo}");
    }

    public attribute seqifoo:Integer[] = [1,2,3,4,5] on replace old {
        System.out.println("7. Replace Integer [] old = {old}; new = {seqifoo}");
    }

    public attribute seqnfoo:Number[] = [20,30,40] on replace old {
        System.out.println("8. Replace Number[] old = {old}; new = {seqnfoo}");
    }

    public attribute seqsfoo:String[] = ["Hello",","," World!"] on replace old {
        System.out.println("9. Replace String[] old = {old}; new = {seqsfoo}");
    }
    public attribute seqTOfoo:test_object[] = [  test_object{name:"test_object1"}, test_object{name:"test_object2"},test_object{name:"test_object3"}  ] on replace old {
		 System.out.print("10. Replace test_object[] old = ");
			 for (i in old) {System.out.print(" {i.name}"); }		 System.out.println();
		 System.out.print("      ...with new = ");
			 for (j in seqTOfoo) {System.out.print(" {j.name}"); }		 System.out.println();
	 }


}
System.out.println("Test i1: default init...");
var i1 = new InitTest;
System.out.println();

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
public attribute name:String = "default-test-object";
};
