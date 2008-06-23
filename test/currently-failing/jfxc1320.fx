/**
 * regression test: JFXC-1320 AIOOBE thrown when sequence-index iou--bouns
 * @test
 * @run/fail
 */

import javafx.lang.Duration;

import java.lang.System;

var ints:Integer[] = [1,2,3];
System.out.println("Integer[-1]: {ints[-1]}");
System.out.println("Integer[n]: {ints[3]}");

var nums:Number[] = [1.0, 2.0, 3.0];
System.out.println("\nNumber[-1]: {nums[-1]}");
System.out.println("Number[n]: {nums[3]}");

var bools:Boolean[] = [true];
System.out.println("\nBoolean[-1]: {bools[-1]}");
System.out.println("Boolean[n]: {bools[1]}");

var objs:java.lang.Object[] = [];
System.out.println("\nObject[-1]: {objs[-1]}");
System.out.println("Object[n]: {objs[0]}");

var strngs:String[] = ["Hello", "World"];
System.out.println("\nString[-1]: {strngs[-1]}");
System.out.println("String[n]: {strngs[2]}");

var durs:Duration[] = [1s, 2s, 3s];
System.out.println("\nDuration[-1]: {durs[-1]}");
System.out.println("Duration[n]: {durs[3]}");
 