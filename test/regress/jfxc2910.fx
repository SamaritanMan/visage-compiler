/*
 * @test
 * @run
 */

import javafx.util.Sequences;

class State {
    var active : Boolean;
    var value : Integer;

    override function toString() : String {
        "active={active}, value={value}"
    }
}

var states : State[];
var STATE_COMPARATOR : java.util.Comparator = java.util.Comparator {
    override public function compare (o1 : Object, o2 : Object) : Integer {
        var s1 : State = o1 as State;
        var s2 : State = o2 as State;

java.lang.System.out.println("comparing {s1.value} with {s2.value}");
        if (s1.value == s2.value) return 0;
        if (s1.value < s2.value) return -1;
        return 1;
    }

    override public function equals (o : Object) : Boolean {
        // I'm forced to override this and I can't use
        // return super.equals(o) for some reason.
        return false;
    }
};


// submitter's original code snippet starts here:
//
public-read var currentState:State = bind {
    var activeStates = states[s | s.active];
    if (sizeof activeStates == 0) {
        null
    } else if (sizeof activeStates == 1) {
        activeStates[0]
    } else {
        // sort the active states and pluck off the first state
        var sortedActiveStates = Sequences.sort(activeStates, STATE_COMPARATOR);
        sortedActiveStates[0] as State;
    }
} 
//
// submitter's original code snippet ends here.


// The submitter's original code with hacks to make it work.
//
public-read var currentState2:State = bind {
    var activeStates = states[s | s.active];
    if (sizeof activeStates == 0) {
        null
    } else if (sizeof activeStates == 1) {
        activeStates[0]
    } else {
        // sort the active states and pluck off the first state
        var sortedActiveStates = Sequences.sort(activeStates, STATE_COMPARATOR);

// The submitter's original code always returns a state with a value
// of 69. If the expression is changed to sortedActiveStates[1], then
// null is always returned which shows that sortedActiveStates always
// has just the one element.
//
// This next line causes sortedActiveStates to be updated each time.
//
var junk1 = sizeof sortedActiveStates;

        sortedActiveStates[0] as State;
    }
} 


function expect(val : State, exp : State) {
    if (val == exp) {
        println("PASS: expect={exp}, value={val}");
    } else {
        println("FAIL: expect={exp}, value={val}");
    }
}


function expect(val : State, exp : Integer) {
    if (val == null) {
        println("FAIL: expected non-null state with value={exp}");
    } else {
        if (val.value == exp) {
            println("PASS: expect={exp}, value={val.value}");
        } else {
            println("FAIL: expect={exp}, value={val.value}");
        }
    }
}


function run() {
    println("test case: states is empty");
    println("currentState={currentState}");
    expect(currentState, null);
    println("currentState2={currentState2}");
    expect(currentState2, null);

    println("");
    println("test case: states contains 1 inactive state value");
    var notActive = State {active : false, value : 42};
    insert notActive into states;
    // output should still be "null"
    println("currentState={currentState}");
    expect(currentState, null);
    println("currentState2={currentState2}");
    expect(currentState2, null);

    println("");
    println("test case: added 1 active state value");
    var isActive = State {active : true, value : 69};
    insert isActive into states;
    // output should be active==true, value==69
    println("currentState={currentState}");
    expect(currentState, 69);
    println("currentState2={currentState2}");
    expect(currentState2, 69);

    println("");
    println("test case: added a higher active state value");
    var higherActive = State {active : true, value : 72};
    insert higherActive into states;
    // output should still be active==true, value==69
    println("currentState={currentState}");
    expect(currentState, 69);
    println("currentState2={currentState2}");
    expect(currentState2, 69);

    println("");
    println("test case: added a lower active state value");
    var lowerActive = State {active : true, value : 67};
    insert lowerActive into states;
    // output should still be active==true, value==67
    println("currentState={currentState}");
    expect(currentState, 67);
    println("currentState2={currentState2}");
    expect(currentState2, 67);
}
