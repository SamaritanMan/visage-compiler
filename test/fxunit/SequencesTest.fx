/*
 * @test/fxunit
 * @run
 */

// TODO JFXC-833
// The tests for using a single element as input are deactivated until JFXC-833
// is resolved.
 
import javafx.lang.Sequences;

import com.sun.javafx.runtime.JavaFXTestCase;
import java.util.Comparator;
import java.lang.Object;
import java.lang.System;

import java.lang.Exception;
import java.lang.ClassCastException;
import java.lang.NullPointerException;
import java.lang.IllegalArgumentException;


public class DummyElement {
    public attribute id: Integer;

    public function equals(o: java.lang.Object): Boolean {
        if (o instanceof DummyElement and id == (o as DummyElement).id) {
            return true;
        }
        return false;
    }
    public function hashCode(): Integer {
        return id;
    }
}
public class DummyComparator extends Comparator<DummyElement> {
    public function compare(o1: DummyElement, o2: DummyElement): Integer {
        return o1.id - o2.id;
    };
    public function equals(o: Object): Boolean {
        return o instanceof DummyComparator;
    }
}

public class SequencesTest extends javafx.fxunit.FXTestCase {
    
    // TODO: JFXC-833 Simplify singleInteger, singleElement, and assertEquals
    // TODO: JFXC-869 Simplify try-catch blocks below after resolve

    attribute emptyInteger:     Integer[];
    attribute singleInteger:    Integer[];
    attribute sortedInteger:    Integer[];
    attribute unsortedInteger:  Integer[];
    attribute emptyElements:    DummyElement[];
    attribute singleElements:   DummyElement[];
    attribute sortedElements:   DummyElement[];
    attribute unsortedElements: DummyElement[];
    attribute longSequence:     DummyElement[];
    
    attribute element: DummyElement[];
    attribute comparator: DummyComparator;
    
    protected function setUp(): Void {
        // Integer-sequences
        emptyInteger    = [];
        singleInteger   = [0];
        sortedInteger   = [1, 2, 3];
        unsortedInteger = [3, 1, 2];
        
        // 7 Dummyelements
        element = for (i in [0..4]) DummyElement{id:i};
        
        // DummyElement-sequences
        emptyElements    = [];
        singleElements   = [element[0]];
        sortedElements   = [element[1], element[2], element[3]];
        unsortedElements = [element[3], element[1], element[2]];
        longSequence     = [element[0], element[1], element[2], element[1], element[3]];

        // Comparator
        comparator = DummyComparator {};
    }
    
    /** 
     * function binarySearch(seq: Comparable[], key: Comparable): Integer 
     * This method uses Arrays.binarySearch for sorting, which we can asume to
     * work. Only tests for the mapping are needed.
     */
    function testBinarySearchComparable() {
        var result: Integer;
        
        // search in empty sequence
        result = Sequences.binarySearch(emptyInteger, 1);
        assertEquals([], emptyInteger);
        assertEquals(-1, result);
        
        // single element sequence
        // successful search
        result = Sequences.binarySearch(singleInteger, 0);
        assertEquals([0], singleInteger);
        assertEquals(0, result);
        
        // unsuccessful search
        result = Sequences.binarySearch(singleInteger, 1);
        assertEquals([0], singleInteger);
        assertEquals(-2, result);
        
        // three elements sequence
        // successful search
        result = Sequences.binarySearch(sortedInteger, 2);
        assertEquals([1, 2, 3], sortedInteger);
        assertEquals(1, result);
        
        // unsuccessful search
        result = Sequences.binarySearch(sortedInteger, 0);
        assertEquals([1, 2, 3], sortedInteger);
        assertEquals(-1, result);
        
        // exception when sequence is null
        try {
            Sequences.binarySearch(null, 1);
            fail("No exception thrown.");
        }
        catch (ex1: NullPointerException) {
        }
        catch (ex2: Exception) {
            fail ("Unexpected exception thrown: {ex2}");
        }
        
    }

    /** 
     * function binarySearch(seq: Object[], key: Object, c: Comparator): Integer
     * This method uses Arrays.binarySearch for sorting, which we can asume to
     * work. Only tests for the mapping are needed.
     */
    function testBinarySearchComparator() {
        var result: Integer;
        
        // search in empty sequence
        result = Sequences.binarySearch(emptyElements, element[1], comparator);
        assertEquals([], emptyElements);
        assertEquals(-1, result);
        
        // single element sequence
        // successful search
        result = Sequences.binarySearch(singleElements, element[0], comparator);
        assertEquals([element[0]], singleElements);
        assertEquals(0, result);
        
        // unsuccessful search
        result = Sequences.binarySearch(singleElements, element[1], comparator);
        assertEquals([element[0]], singleElements);
        assertEquals(-2, result);
        
        // three elements sequence
        // successful search
        result = Sequences.binarySearch(sortedElements, element[2], comparator);
        assertEquals([element[1], element[2], element[3]], sortedElements);
        assertEquals(1, result);
        
        // unsuccessful search
        result = Sequences.binarySearch(sortedElements, element[0], comparator);
        assertEquals([element[1], element[2], element[3]], sortedElements);
        assertEquals(-1, result);

        // search using null-comparator
        var resultInt: Integer = Sequences.binarySearch(sortedInteger, 2, null);
        assertEquals([1, 2, 3], sortedInteger);
        assertEquals(1, resultInt);
        
        // exception if using null-operator with non-comparable elements
        try {
            Sequences.binarySearch(sortedElements, element[2], null);
            fail("No exception thrown.");
        }
        catch (ex1: ClassCastException) {
            assertEquals([element[1], element[2], element[3]], sortedElements);
        }
        catch (ex2: Exception) {
            fail("Unexpected exception thrown: " + ex2.getMessage());
        }

        // exception when sequence is null
        try {
            Sequences.binarySearch(null, element, null);
            fail("No exception thrown.");
        }
        catch (ex3: NullPointerException) {
        }
        catch (ex4: Exception) {
            fail ("Unexpected exception thrown: {ex4}");
        }
        
    }
    
    /**
     * function indexByIdentity(seq: Object[], key: Object): Integer
     */
    function testIndexByIdentity() {
        var result: Integer;
        
        // search in empty sequence
        result = Sequences.indexByIdentity(emptyElements, element[1]);
        assertEquals([], emptyElements);
        assertEquals(-1, result);
        
        // single element sequence
        // successful search
        result = Sequences.indexByIdentity(singleElements, element[0]);
        assertEquals([element[0]], singleElements);
        assertEquals(0, result);
        
        // unsuccessful search
        result = Sequences.indexByIdentity(singleElements, element[1]);
        assertEquals([element[0]], singleElements);
        assertEquals(-1, result);
        
        // three elements sequence
        // successful search for first element
        result = Sequences.indexByIdentity(unsortedElements, element[3]);
        assertEquals([element[3], element[1], element[2]], unsortedElements);
        assertEquals(0, result);
        
        // successful search for middle element
        result = Sequences.indexByIdentity(unsortedElements, element[1]);
        assertEquals([element[3], element[1], element[2]], unsortedElements);
        assertEquals(1, result);
        
        // successful search for last element
        result = Sequences.indexByIdentity(unsortedElements, element[2]);
        assertEquals([element[3], element[1], element[2]], unsortedElements);
        assertEquals(2, result);
        
        // make sure first element is returned
        result = Sequences.indexByIdentity(longSequence, element[1]);
        assertEquals([element[0], element[1], element[2], element[1], element[3]], longSequence);
        assertEquals(1, result);
        
        // unsuccessful search
        result = Sequences.indexByIdentity(unsortedElements, element[0]);
        assertEquals([element[3], element[1], element[2]], unsortedElements);
        assertEquals(-1, result);

        // make sure search is by identity
        var localElement: DummyElement = DummyElement {id: 1};
        assertNotSame(element[1], localElement);
        assertEquals(element[1], localElement);
        result = Sequences.indexByIdentity(unsortedElements, localElement);
        assertEquals([element[3], element[1], element[2]], unsortedElements);
        assertEquals(-1, result);

        // exception when sequence is null
        try {
            Sequences.indexByIdentity(null, 1);
            fail("No exception thrown.");
        }
        catch (ex1: NullPointerException) {
        }
        catch (ex2: Exception) {
            fail ("Unexpected exception thrown: " + ex2.getMessage());
        }

        // exception when sequence is null
        try {
            Sequences.indexByIdentity(unsortedElements, null);
            fail("No exception thrown.");
        }
        catch (ex3: NullPointerException) {
        }
        catch (ex4: Exception) {
            fail ("Unexpected exception thrown: " + ex4.getMessage());
        }
    }
    
    /**
     * function indexOf(seq: Object[], key: Object): Integer
     */
    function testIndexOf() {
        var result: Integer;
        
        // search in empty sequence
        result = Sequences.indexOf(emptyElements, element[1]);
        assertEquals([], emptyElements);
        assertEquals(-1, result);
        
        // single element sequence
        // successful search
        result = Sequences.indexOf(singleElements, element[0]);
        assertEquals([element[0]], singleElements);
        assertEquals(0, result);
        
        // unsuccessful search
        result = Sequences.indexOf(singleElements, element[1]);
        assertEquals([element[0]], singleElements);
        assertEquals(-1, result);
        
        // three elements sequence
        // successful search for first element
        result = Sequences.indexOf(unsortedElements, element[3]);
        assertEquals([element[3], element[1], element[2]], unsortedElements);
        assertEquals(0, result);
        
        // successful search for middle element
        result = Sequences.indexOf(unsortedElements, element[1]);
        assertEquals([element[3], element[1], element[2]], unsortedElements);
        assertEquals(1, result);
        
        // successful search for last element
        result = Sequences.indexOf(unsortedElements, element[2]);
        assertEquals([element[3], element[1], element[2]], unsortedElements);
        assertEquals(2, result);
        
        // make sure first element is returned
        result = Sequences.indexOf(longSequence, element[1]);
        assertEquals([element[0], element[1], element[2], element[1], element[3]], longSequence);
        assertEquals(1, result);
        
        // unsuccessful search
        result = Sequences.indexOf(unsortedElements, element[0]);
        assertEquals([element[3], element[1], element[2]], unsortedElements);
        assertEquals(-1, result);

        // exception when sequence is null
        try {
            Sequences.indexOf(null, 1);
            fail("No exception thrown.");
        }
        catch (ex1: NullPointerException) {
        }
        catch (ex2: Exception) {
            fail ("Unexpected exception thrown: " + ex2.getMessage());
        }

        // exception when sequence is null
        try {
            Sequences.indexOf(unsortedElements, null);
            fail("No exception thrown.");
        }
        catch (ex3: NullPointerException) {
        }
        catch (ex4: Exception) {
            fail ("Unexpected exception thrown: " + ex4.getMessage());
        }
    }
    
    /**
     * function max(seq: Comparable[]): Comparable
     */
    function testMaxComparable() {
        var result: Integer;
        
        // get maximum in single element sequence
        result = Sequences.max(singleInteger) as Integer;
        assertEquals([0], singleInteger);
        assertEquals(0, result);
        
        // get first element
        result = Sequences.max(unsortedInteger) as Integer;
        assertEquals([3, 1, 2], unsortedInteger);
        assertEquals(3, result);
        
        // get middle element
        var fixture: Integer[] = [11, 13, 12];
        result = Sequences.max(fixture) as Integer;
        assertEquals([11, 13, 12], fixture);
        assertEquals(13, result);
        
        // get last element
        result = Sequences.max(sortedInteger) as Integer;
        assertEquals([1, 2, 3], sortedInteger);
        assertEquals(3, result);
        
        // exception when sequence is null
        try {
            Sequences.max(null);
            fail("No exception thrown.");
        }
        catch (ex1: NullPointerException) {
        }
        catch (ex2: Exception) {
            fail ("Unexpected exception thrown: " + ex2.getMessage());
        }
        
        // exception when sequence is empty
        try {
            Sequences.max(emptyInteger);
            fail("No exception thrown.");
        }
        catch (ex3: IllegalArgumentException) {
        }
        catch (ex4: Exception) {
            fail ("Unexpected exception thrown: " + ex4.getMessage());
        }
        
    }
    
    /**
     * function max(seq: Object[], c: Comparator): Object
     */
    function testMaxComparator() {
        var result: DummyElement;
        
        // get maximum in single element sequence
        result = Sequences.max(singleElements, comparator) as DummyElement;
        assertEquals([element[0]], singleElements);
        assertEquals(element[0], result);
        
        // get first element
        result = Sequences.max(unsortedElements, comparator) as DummyElement;
        assertEquals([element[3], element[1], element[2]], unsortedElements);
        assertEquals(element[3], result);
        
        // get middle element
        var fixture: DummyElement[] = [element[0], element[3], element[2]];
        result = Sequences.max(fixture, comparator) as DummyElement;
        assertEquals([element[0], element[3], element[2]], fixture);
        assertEquals(element[3], result);
        
        // get last element
        result = Sequences.max(sortedElements, comparator) as DummyElement;
        assertEquals([element[1], element[2], element[3]], sortedElements);
        assertEquals(element[3], result);
        
        // max using natural order
        var resultInt: Integer = Sequences.max(unsortedInteger, null) as Integer;
        assertEquals([3, 1, 2], unsortedInteger);
        assertEquals(3, resultInt);
        
        // exception when sequence is null
        try {
            Sequences.max(null, null);
            fail("No exception thrown.");
        }
        catch (ex1: NullPointerException) {
        }
        catch (ex2: Exception) {
            fail ("Unexpected exception thrown: " + ex2.getMessage());
        }
        
        // exception when sequence is empty
        try {
            Sequences.max(emptyElements, comparator);
            fail("No exception thrown.");
        }
        catch (ex3: IllegalArgumentException) {
        }
        catch (ex4: Exception) {
            fail ("Unexpected exception thrown: " + ex4.getMessage());
        }
        
    }
    
    /**
     * function min(seq: Comparable[]): Comparable
     */
    function testMinComparable() {
        var result: Integer;
        
        // get maximum in single element sequence
        result = Sequences.min(singleInteger) as Integer;
        assertEquals([0], singleInteger);
        assertEquals(0, result);
        
        // get first element
        result = Sequences.min(sortedInteger) as Integer;
        assertEquals([1, 2, 3], sortedInteger);
        assertEquals(1, result);
        
        // get middle element
        result = Sequences.min(unsortedInteger) as Integer;
        assertEquals([3, 1, 2], unsortedInteger);
        assertEquals(1, result);
        
        // get last element
        var fixture: Integer[] = [12, 13, 11];
        result = Sequences.min(fixture) as Integer;
        assertEquals([12, 13, 11], fixture);
        assertEquals(11, result);
        
        // exception when sequence is null
        try {
            Sequences.min(null);
            fail("No exception thrown.");
        }
        catch (ex1: NullPointerException) {
        }
        catch (ex2: Exception) {
            fail ("Unexpected exception thrown: " + ex2.getMessage());
        }
        
        // exception when sequence is empty
        try {
            Sequences.min(emptyInteger);
            fail("No exception thrown.");
        }
        catch (ex3: IllegalArgumentException) {
        }
        catch (ex4: Exception) {
            fail ("Unexpected exception thrown: " + ex4.getMessage());
        }
        
    }
    
    /**
     * function min(seq: Object[], c: Comparator): Object
     */
    function testMinComparator() {
        var result: DummyElement;
        
        // get minimum in single element sequence
        result = Sequences.min(singleElements, comparator) as DummyElement;
        assertEquals([element[0]], singleElements);
        assertEquals(element[0], result);
        
        // get first element
        result = Sequences.min(sortedElements, comparator) as DummyElement;
        assertEquals([element[1], element[2], element[3]], sortedElements);
        assertEquals(element[1], result);
        
        // get middle element
        result = Sequences.min(unsortedElements, comparator) as DummyElement;
        assertEquals([element[3], element[1], element[2]], unsortedElements);
        assertEquals(element[1], result);
        
        // get last element
        var fixture: DummyElement[] = [element[0], element[3], element[2]];
        result = Sequences.min(fixture, comparator) as DummyElement;
        assertEquals([element[0], element[3], element[2]], fixture);
        assertEquals(element[0], result);
        
        // min using natural order
        var resultInt: Integer = Sequences.min(unsortedInteger, null) as Integer;
        assertEquals([3, 1, 2], unsortedInteger);
        assertEquals(1, resultInt);
        
        // exception when sequence is null
        try {
            Sequences.min(null, null);
            fail("No exception thrown.");
        }
        catch (ex1: NullPointerException) {
        }
        catch (ex2: Exception) {
            fail ("Unexpected exception thrown: " + ex2.getMessage());
        }
        
        // exception when sequence is empty
        try {
            Sequences.min(emptyElements, comparator);
            fail("No exception thrown.");
        }
        catch (ex3: IllegalArgumentException) {
        }
        catch (ex4: Exception) {
            fail ("Unexpected exception thrown: " + ex4.getMessage());
        }
        
    }
    
    /**
     * function nextIndexByIdentity(seq: Object[], key: Object, pos: Integer): Integer
     * The basic functionality is tested by testIndexByIdentity. Only tests for 
     * pos>0 are needed here.
     */
    function testNextIndexByIdentity() {
        var result: Integer;
        
        // search in empty sequence
        result = Sequences.nextIndexByIdentity(emptyElements, element[1], 1);
        assertEquals([], emptyElements);
        assertEquals(-1, result);
        
        // single element sequence
        result = Sequences.nextIndexByIdentity(singleElements, element[0], 1);
        assertEquals([element[0]], singleElements);
        assertEquals(-1, result);
        
        // search with pos = result
        result = Sequences.nextIndexByIdentity(longSequence, element[1], 1);
        assertEquals([element[0], element[1], element[2], element[1], element[3]], longSequence);
        assertEquals(1, result);
        
        // search with pos < result
        result = Sequences.nextIndexByIdentity(longSequence, element[1], 2);
        assertEquals([element[0], element[1], element[2], element[1], element[3]], longSequence);
        assertEquals(3, result);
        
        // unsuccessful search
        result = Sequences.nextIndexByIdentity(longSequence, element[1], 4);
        assertEquals([element[0], element[1], element[2], element[1], element[3]], longSequence);
        assertEquals(-1, result);
        
        // search with pos > sequence-size
        result = Sequences.nextIndexByIdentity(longSequence, element[1], 5);
        assertEquals([element[0], element[1], element[2], element[1], element[3]], longSequence);
        assertEquals(-1, result);

        // make sure search is by identity
        var localElement: DummyElement = DummyElement {id: 1};
        assertNotSame(element[1], localElement);
        assertEquals(element[1], localElement);
        result = Sequences.nextIndexByIdentity(longSequence, localElement, 1);
        assertEquals([element[0], element[1], element[2], element[1], element[3]], longSequence);
        assertEquals(-1, result);
    }
    
    /**
     * function nextIndexOf(seq: Object[], key: Object, pos: Integer): Integer
     * The basic functionality is tested by testIndexOf. Only tests for 
     * pos>0 are needed here.
     */
    function testNextIndexOf() {
        var result: Integer;
        
        // search in empty sequence
        result = Sequences.nextIndexOf(emptyElements, element[1], 1);
        assertEquals([], emptyElements);
        assertEquals(-1, result);
        
        // single element sequence
        result = Sequences.nextIndexOf(singleElements, element[0], 1);
        assertEquals([element[0]], singleElements);
        assertEquals(-1, result);
        
        // search with pos = result
        result = Sequences.nextIndexOf(longSequence, element[1], 1);
        assertEquals([element[0], element[1], element[2], element[1], element[3]], longSequence);
        assertEquals(1, result);
        
        // search with pos < result
        result = Sequences.nextIndexOf(longSequence, element[1], 2);
        assertEquals([element[0], element[1], element[2], element[1], element[3]], longSequence);
        assertEquals(3, result);
        
        // unsuccessful search
        result = Sequences.nextIndexOf(longSequence, element[1], 4);
        assertEquals([element[0], element[1], element[2], element[1], element[3]], longSequence);
        assertEquals(-1, result);
        
        // search with pos > sequence-size
        result = Sequences.nextIndexOf(longSequence, element[1], 5);
        assertEquals([element[0], element[1], element[2], element[1], element[3]], longSequence);
        assertEquals(-1, result);
    }
    
    /**
     * function <<reverse>> (seq:Object[]): Object[]
     */
    function testReverse() {
        var result: Integer[];
        
        // reverse empty sequence
        result = Sequences.<<reverse>>(emptyInteger) as Integer[];
        assertEquals([], emptyInteger);
        assertEquals([], result);
        
        // reverse single element sequence
        result = Sequences.<<reverse>>(singleInteger) as Integer[];
        assertEquals([0], singleInteger);
        assertEquals([0], result);
        
        // reverse three element sequence
        result = Sequences.<<reverse>>(unsortedInteger) as Integer[];
        assertEquals([3, 1, 2], unsortedInteger);
        assertEquals([2, 1, 3], result);
        
        // exception when sequence is null
        try {
            Sequences.sort(null);
            fail("No exception thrown.");
        }
        catch (ex1: NullPointerException) {
        }
        catch (ex2: Exception) {
            fail ("Unexpected exception thrown: " + ex2.getMessage());
        }
    }
    
    /**
     * function sort(seq: Comparable[]): Comparable[]
     * This method uses Arrays.sort for sorting, which we can asume to work.
     * Only tests for the mapping are needed.
     */
    function testSortComparable() {
        var result: Integer[];
        
        // sort empty sequence
        result = Sequences.sort(emptyInteger) as Integer[];
        assertEquals([], emptyInteger);
        assertEquals([], result);
        
        // sort single element
        result = Sequences.sort(singleInteger) as Integer[];
        assertEquals([0], singleInteger);
        assertEquals([0], result);
        
        // sort sequence
        result = Sequences.sort(unsortedInteger) as Integer[];
        assertEquals([3, 1, 2], unsortedInteger);
        assertEquals([1, 2, 3], result);

        // exception when sequence is null
        try {
            Sequences.sort(null);
            fail("No exception thrown.");
        }
        catch (ex1: NullPointerException) {
        }
        catch (ex2: Exception) {
            fail ("Unexpected exception thrown: {ex2}");
        }
        
    }

    /**
     * function sort(seq: Object[], c: Comparator): Object[]
     * This method uses Arrays.sort for sorting, which we can asume to work.
     * Only tests for the mapping are needed.
     */
    function testSortComparator() {
        var result: DummyElement[];
        
        // sort empty sequence
        result = Sequences.sort(emptyElements, comparator) as DummyElement[];
        assertEquals([], emptyElements);
        assertEquals([], result);
        
        // sort single element
        result = Sequences.sort(singleElements, comparator) as DummyElement[];
        assertEquals([element[0]], singleElements);
        assertEquals([element[0]], result);
        
        // sort sequence
        result = Sequences.sort(unsortedElements, comparator) as DummyElement[];
        assertEquals([element[3], element[1], element[2]], unsortedElements);
        assertEquals([element[1], element[2], element[3]], result);
        
        // sort using null-comparator
        var resultInt: Integer[] = Sequences.sort(unsortedInteger, null) as Integer[];
        assertEquals([3, 1, 2], unsortedInteger);
        assertEquals([1, 2, 3], resultInt);

        // exception if using null-operator with non-comparable elements
        try {
            Sequences.sort(unsortedElements, null);
            fail("No exception thrown.");
        }
        catch (ex1: ClassCastException) {
            assertEquals([element[3], element[1], element[2]], unsortedElements);
        }
        catch (ex2: Exception) {
            fail("Unexpected exception thrown: " + ex2.getMessage());
        }

        // exception when sequence is null
        try {
            Sequences.sort(null, null);
            fail("No exception thrown.");
        }
        catch (ex3: NullPointerException) {
        }
        catch (ex4: Exception) {
            fail ("Unexpected exception thrown: {ex4}");
        }
        
    }
}
