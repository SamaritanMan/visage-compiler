/*
 * Copyright 1999-2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package javafx.lang;

import java.lang.Object;
import java.lang.Comparable;
import java.lang.Long;
import java.lang.Math;

/**
 * A class that defines a duration of time.  Duration instances are defined in
 * milliseconds, but can be easily created using time literals; for
 * example, a two-and-a-half minute Duration instance can be defined in several
 * ways:
 * <code><pre>    Duration t = 2m + 30s;
    Duration t = 2.5m;
    Duration t = 2500ms;</pre></code>
 */
public class Duration extends com.sun.javafx.runtime.Duration {
    
    public static function valueOf(ms: Number): Duration {
        return com.sun.javafx.runtime.Duration.make(ms) as Duration;
    }

    /** Returns the number of milliseconds in this period. */
    public function toMillis():Number {
        return millis;
    }
    
    /** Returns the number of whole seconds in this period. */
    public function toSeconds():Number {
        return Math.floor(millis / 1000);
    }
    
    /** Returns the number of whole minutes in this period. */
    public function toMinutes(): Number {
        return Math.floor(millis / 60 / 1000);
    }
    
    /** Returns the number of whole hours in this period. */
    public function toHours(): Number {
        return Math.floor(millis / 60 / 60 / 1000);
    }

    /** Add this instance and another Duration instance to return a new Duration instance.
     *  This function does not change the value of called Duration instance. */
    public function add(other:Duration):Duration {
        return valueOf(millis + other.millis);
    }

    /** Subtract other Duration instance from this instance to return a new Duration instance.
     *  This function does not change the value of called Duration instance. */
    public function sub(other:Duration):Duration {
        return valueOf(millis - other.millis);
    }

    /** Multiply this instance with a number to return a new Duration instance.
     *  This function does not change the value of called Duration instance. */
    public function mul(n:Number):Duration {
        return valueOf(millis * n);
    }


    /** Divide this instance by a number to return a new Duration instance.
     *  This function does not change the value of called Duration instance. */
    public function div(n:Number):Duration {
        return valueOf(millis / n);
    }

    public function negate():Duration {
        return valueOf(-millis);
    }

    public function toString(): String {
        return "{millis}ms";
    }

    public function lt(other: Duration):Boolean {
        return compareTo(other) < 0;
    }

    public function le(other: Duration):Boolean {
        return compareTo(other) <= 0;
    }

    public function gt(other: Duration):Boolean {
        return compareTo(other) > 0;
    }

    public function ge(other: Duration):Boolean {
        return compareTo(other) >= 0;
    }

    public function toDate():java.util.Date {
        return new java.util.Date(millis.longValue());
    }
}
