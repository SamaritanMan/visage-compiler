/*
 *
 * JFXC-1779: Result of a bound function is not re-evaluated when a function dependency changes
 * @test
 * @run/fail
 *
 *
 */

import java.lang.System;
import java.lang.Math;

var fn:function(arg:Number):Number;

bound function applyFunction(args:Number[]):Number[] {
    var results = for (arg in args) fn(arg);
    return results[n | n > 0];
}

fn = function(arg:Number) {
         var radians = Math.toRadians(arg);
         return Math.sin(radians);
     };
 
var values:Number[] = [0, 45, 90, 135, 180, 225, 270, 315, 360];
var results = bind applyFunction(values);
System.out.println(results.toString());

fn = function(arg:Number) {
         var radians = Math.toRadians(arg);
         return Math.cos(radians);
     };
System.out.println(results.toString());
 