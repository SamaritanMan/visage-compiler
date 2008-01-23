package com.sun.javafx.runtime;

import com.sun.javafx.runtime.sequence.Sequence;

/**
 * ErrorHandler
 *
 * @author Brian Goetz
 */
public class ErrorHandler {
    private final static boolean debug = Boolean.getBoolean("javafx.debug");
    private final static boolean strict = Boolean.getBoolean("javafx.strict");

    public static boolean isStrict() {
        return strict;
    }

    public static boolean isDebug() {
        return debug;
    }

    /** Called when attempting to insert an element into a sequence at an out-of-bounds location */
    public static<T> void outOfBoundsInsert(Sequence<T> seq, int index, T value) {

    }

    /** Called when attempting to replace an element of a sequence at an out-of-bounds location */
    public static<T> void outOfBoundsReplace(Sequence<T> seq, int index, T value) {

    }

    /** Called when attempting to delete an element of a sequence at an out-of-bounds location */
    public static<T> void outOfBoundsDelete(Sequence<T> seq, int index) {

    }

    /** Called when attempting to read an element of a sequence at an out-of-bounds location */
    public static<T> void outOfBoundsRead(Sequence<T> seq, int index) {

    }

    /** Called when attempting to dereference a null value */
    public static void nullDereference() {

    }

    /** Called when attempting to coerce a null numeric or boolean value to a primitive */
    public static void nullToPrimitiveCoercion(String type) {
        if (isStrict())
            throw new NullPointerException("strict: cannot assign null to " + type);
        else if (isDebug())
            System.err.println("Coercing " + type + " to null");

    }

    /** Called when attempting to write a null value to a non-nullable variable */
    public static void invalidNullWrite() {

    }
}
