/**
 * Regression test JFXC-2942 : Lazy binding: range -- throws divide by zero exception in trigger
 *
 * Number ranges
 *
 * @test
 * @run
 */

import java.io.*;

def CALC = " >> ";
def sw = new StringWriter();
def pw = new PrintWriter( sw );

function m(x : Number[]) : Number[]  { pw.print(CALC); pw.print(x); x }
function n(x : Number) : Number  { pw.print(CALC); pw.print(x); x }

function check(upd : Number[], seeResult : Boolean, seq : Number[]) : Void {
  def swr = new StringWriter();
  def pwr = new PrintWriter( swr );
  pwr.print(seq);
  def res = swr.toString();
  var uc = "";
  for (u in upd) {
    uc = "{uc}{CALC}{u}";
  }
  if (seeResult) {
    uc = "{uc}{CALC}{res}";
  }
  def log = sw.toString();
  sw.getBuffer().setLength(0);
  if (log != uc) {
    println("ERROR");
    println("Expected: {uc}");
    println("Got: {log}");
  }
  println(seq);
}

function checkR(upd : Number[], chng : Boolean) : Void {
  check(upd, chng,  r1);
  check(upd, false, r2);
  check([],  chng,  r3);
  check([],  false, r4);
}

function checkS(upd : Number[]) : Void {
  check(upd, true,  s1);
  check(upd, false, s2);
  check([],  true,  s3);
  check([],  false, s4);
}

var a = 9.0;
var b = 10;
var c = 0.5;
def r1 = bind lazy m([n(a) .. n(b)]);
def r2 = bind lazy [n(a) .. n(b)];
def r3 = bind lazy m([a .. b]);
def r4 = bind lazy [a .. b];
def s1 = bind lazy m([n(a) .. n(b) step n(c)]);
def s2 = bind lazy [n(a) .. n(b) step n(c)];
def s3 = bind lazy m([a .. b step c]);
def s4 = bind lazy [a .. b step c];

check([], false, []); // make sure nothing seen yet

checkR([a, b], true);
checkS([a, b, c]);

b = 11.3;

checkR([b], true);
checkS([b]);

a = 9.75;

checkR([a], true);
checkS([a]);

c = 0.25;

checkR([], false);
checkS([c]);
