/*
 * Feature test #22 - time literals
 *
 * @test
 * @run
 */

import java.lang.System;
import javafx.lang.Time;

var t = Time {millis: 50};
System.out.println("Time object literal, millis=50 = {t.toString()}");
t = 500ms;
System.out.println("500ms = {t}");
t = 5s;
System.out.println("5s = {t} or {%tS t} seconds");
t = 1.5s;
System.out.println("1.5s = {t} or {%tS t}.{%tL t} seconds");
t = 2m;
System.out.println("2m = {t} or {%tM t} minutes");
t = 5s + 750ms;
System.out.println("5s + 750ms = {t} or {%tS t}.{%tL t} seconds");
t = 1h + 25m + 15s;
System.out.println("1h + 25m + 15s = {t}");
t = 120s;
System.out.println("t=120s t.toMinutes() = {t.toMinutes()}");
System.out.println("2m.toSeconds() = {2m.toSeconds()}");

System.out.println("1m + 1s = { 1m + 1s}");
System.out.println("1m - 1s = { 1m - 1s}");
System.out.println("50ms * 2 = {50ms * 2}");
System.out.println("50ms / 2 = {50ms / 2}")
