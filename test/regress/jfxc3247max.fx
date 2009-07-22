/**
 * JFXC-3247 : Slacker Binding: general case
 *
 * Set a high max for memory use of a linked list with bound select covered by JFXC-3247.
 * Fail if it goes over or if there is an apparent leak.
 *
 * Note: this differs from jfxc3317Max.fx by only an on-replace, which allows this to slacker
 *
 * @test
 * @run
 */

import java.lang.management.*;

def MAX_MEM : Long = 1500000;  // Mac OS X - July 13, 2009 : 1186496
var initialMem : Long;

class Links {
  public var next : Links;
  public var ool : Boolean;
  public def bb = bind next.ool;
}

function memUsed() : Long {
  java.lang.System.gc();
  ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
}

function checkMemory(maximum : Long, msg : String) : Void {
  def used = memUsed();
  if (used > maximum) {
    println("{msg}: {used}");
  }
}

initialMem = memUsed();
var top : Links = null;
for (m in [1..10]) {
  for (n in [1..1000]) {
    top = Links {next: top}
    0;
  }

  checkMemory(MAX_MEM, "A lot of memory used");
}

top = null;
// Allow 20% growth
checkMemory((initialMem * 1.2) as Long, "Possible leak");
