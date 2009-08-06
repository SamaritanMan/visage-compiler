/*
 * Timeline_TS020_01.fx
 * @test
 * was: 'at' run
 */

/**
 * @author Leonid Popov
 */

import javafx.animation.*;
import javafx.lang.Duration;
import java.lang.System;
import java.lang.Thread;
import java.lang.Runnable;
import java.lang.AssertionError;
import java.lang.Throwable;
import java.lang.Math;

// factor of all time values in reference test values
def SCALE = 100;

function p(msg) {
//    System.out.println("{msg}");
}
function printSeq(seq: Number[]) {
//    System.out.print("[");
//    for (i in seq) {
//        System.out.print("{i} ");
//    }
//    System.out.println("]");
}


class TestKF extends KeyFrame {
    def NO_TO = Number.NEGATIVE_INFINITY;

    var t = 0;
    var to = NO_TO;
    var rev = false;
    var test: KFTest;

    postinit {
        time = Duration.valueOf(t * SCALE);
        p("t={t} time={time} to={to}");
        action = function() {
            test.checkFrame(time.toMillis());
            if (to != NO_TO)    test.timeline.time = Duration.valueOf(to * SCALE);
            if (rev)            test.timeline.rate = -test.timeline.rate;
        }
    }
}

class KFTest {
    def MAX_DIFF_MS = 90;

    var name = "";
    var startTime: Integer;
    var refIndex = -1;
    var refList: Number[];
    var refDur = 0.0;

    var timeline: Timeline;
    var time = 0.0;
    var repeatCount = 1;
    var autoReverse = false;
    var rate = 1.0;
    var keyFrames: TestKF[];

    var failed = false;
    var doneCallback: function();
    var isDone = false;

    postinit {
        for (i in [refList.size() - 1 .. 0 step -1]) {
            if (refList[i] != AR) {
                refList[i] *= SCALE;
                if (i mod 2 == 0) {
                    refList[i] /= Math.abs(rate);
                }
                p("refList[{i}]={refList[i]}");
            }
        }
        if (refList.size() >= 2) {
            refDur = refList[refList.size() - 2];
        }
        p("refDur={refDur}");
        time *= SCALE;
        p("time={time}");
        var sep = if (name == "") "" else " ";
        name = "{name}{sep}repeatCount={repeatCount} rate={rate} time={time}";
        if (autoReverse) name = "{name} autoReverse={autoReverse}";
        timeline = Timeline {
            rate: this.rate;
            time: Duration.valueOf(this.time);
            repeatCount: this.repeatCount;
            autoReverse: this.autoReverse;
            keyFrames: this.keyFrames;
        }
    }

    var running = bind timeline.running on replace wasRunning = isRunning {
        if (wasRunning and not isRunning) {
            if (not isDone) {
                var tick: Integer = System.currentTimeMillis() - startTime;
                // check if timeline stopped at correct time
                check(closeEnough(tick, refDur), "stopped at total time={tick} instead of {refDur}");
                // check if timeline stopped at correct key frame
                var maxIndex = refList.size() / 2 - 1;
                check(refIndex == maxIndex, "stopped at refIndex={refIndex} instead of {maxIndex}");
                done();
            }
        }
    }

    function done() {
        if (not isDone) {
            isDone = true;
            if (not failed) System.out.println("OK");
            if (doneCallback != null) doneCallback();
        }
    }

    function closeEnough(t1: Number, t2: Number) {
        return Math.abs(t1 - t2) <= MAX_DIFF_MS;
    }

    function checkFrame(kfTime: Number): Void {
        refIndex ++;
        var tick: Integer = System.currentTimeMillis() - startTime;
        p("tick={tick} kfTime={kfTime} timeline.time={timeline.time}");

        var refI = refIndex * 2;
        // check if timeline hasn't stopped in time
        check(refI < refList.size(), "excess frame: refIndex={refIndex}, kfTime={kfTime}, tick={tick}");

        var refTick = refList[refI];
        var refElapsed = refList[refI + 1];

        // check if frame vizited in correct time
        check(closeEnough(tick, refTick), "refIndex={refIndex} expected time {refTick}, visited at {tick}");
        // check if correct frame vizited
        check(refElapsed == kfTime, "refIndex={refIndex} expected frame {refElapsed}, visited {kfTime}");

        if (failed or closeEnough(tick, refDur)) {
            // if tests faled or testing time is over
            timeline.stop();
        }
    }
    
    function test() {
        System.out.print("{name}... ");
        for (kf in keyFrames) {
            kf.test = this;
        }
        startTime = System.currentTimeMillis();
        timeline.play();
        startTime = System.currentTimeMillis();
    }

    function check (condition: Boolean, msg: String){
        if (not condition) {
            if (not failed) System.out.println();
            System.out.println("        FAILED: {msg}");
            failed = true;
        } else {
            p("        OK: {msg}");
        }
    }
}

class KFTestSuite {
    var tests: KFTest[];
    var curTestIndex = 0;
    var curTest: KFTest;
    var numPassed = 0;
    var numFailed = 0;
    function test() {
        for (t in tests) {
            t.doneCallback = testNext;
        }
        testNext();
    }
    function testNext(): Void {
        if (curTest != null) {
            if (curTest.failed) {
                numFailed ++;
            } else {
                numPassed ++;
            }
        }
        if (curTestIndex == tests.size()) {
            var msg = "TESTED: {curTestIndex}, PASSED: {numPassed}, FAILED: {numFailed}";
            System.out.println(msg);
            if (numFailed > 0) {
                throw new AssertionError(msg);
            }
            return;
        }
        curTest = tests[curTestIndex++];
        printSeq(curTest.refList);
        curTest.test();
    }
}

// ==================== GUIDELINES FOR WRITING TESTS ===========================
// TBD


// AR (abbreviation for autoReverse) is a placeholder for terminal frames
// suppressed if autoReverse=true. BOTH values in a pair should be set to AR.
// On generation of reference sequence, such pairs are deleted from the sequence.
// Usage of AR simplifies generating ref lists for different repeatCount values
def AR = Number.NEGATIVE_INFINITY;

// Helper class for automated generation of test sequences for
// regular timelines, starting from the beginning and finishing at the end,
// i.e. without changing its parameters during run
class TestSet {
    var name = "";
    var frames: TestKF[]; // keyFrames to be tested
    var dur = 0;
    // frame visiting reference data for...
    var refF: Number[]; // forward (rate >= 0)
    var refB: Number[]; // backward (rate < 0)
    var refFR: Number[]; // forward with autoReverse
    var refBR: Number[]; // backward with autoReverse

    function getRef(repeatCount: Number, rate: Number, autoReverse: Boolean, endOfCycle: Boolean): Number[] {
        var usedRef =
            if (rate >= 0) {
                if (autoReverse) refFR else refF;
            } else {
                if (autoReverse) refBR else refB;
            };
        p("usedRef repeatCount={repeatCount} rate={rate} autoReverse={autoReverse} endOfCycle={endOfCycle}");
        printSeq(usedRef);
        var n = 2 * frames.size();
        // if initial time set to cycle end (for forward) or 0s (otherwise)
        // then timeline should skip the 1st cycle.
        var fst = if (endOfCycle) n else 0;
        var lst = repeatCount * n - 1 as Integer;
        p("fst={fst} lst={lst}");
        var ref = [
                for (i in [fst .. lst]) {
                    usedRef[i]
                }
            ];
        p("ref");
        printSeq(ref);
        if (endOfCycle) {
            if (ref.size() > 1 and ref[0 .. 1] == [AR, AR]) {
                // if 1st cycle removed, and autoReverse is on, replace AR
                // placeholders on start point with values of the end of 1st cycle
                ref[0 .. 1] = usedRef[n - 2 .. n - 1];
                p("endOfCycle - after init");
                printSeq(ref);
            }
            // since the 1st cycle skipped, reduce the reference time accordingly
            for (i in [0 .. ref.size() - 2 step 2]) {
                if (ref[i] != AR) {
                    ref[i] -= dur;
                };
            };
            p("endOfCycle - after substraction");
            printSeq(ref);
        }
        for (i in [ref.size() - 1 .. 0 step -1]) {
            if (ref[i] == AR) {
                p("deleting i={i} refList[i]={ref[i]}");
                delete ref[i];
            }
        }
        p("finally");
        printSeq(ref);
        return ref;
    }


    postinit {
        dur = frames[frames.size() - 1].t;
    }

    function generateKFTests(): KFTest[] {
        def N = 3; // max repeatCount
        for (rc in [-1..N]) {
            //repeatCount
            var n = if (rc < 0) N else rc; // INDEFINITE timelines use full refList
            for (r in [[-1.5 .. -0.5 step 0.5], [0.5 .. 1.5 step 0.5]]) {
                // rate
                for (ar in [0 .. 1]) {
                    // autoReverse
                    var aRev = (ar == 1);
                    for (st in [-2 .. 1] ) {
                        // st: timeline's start (initial) time is set to:
                        // -2: -1
                        // -1: 0
                        //  0: cycle duration
                        //  1: cycle duration +1
                        var startTime = if (st < 0) st + 1 else dur + st;
                        // 1st cycle should be skipped if time set to the end of cycle
                        var endOfCycle = (r >= 0) == (st >= 0);
                        p("rc={rc} n={n} r={r} ar={ar} aRev={aRev} st={st} endOfCycle={endOfCycle} startTime={startTime}");
                        var ref = getRef(n, r, aRev, endOfCycle);
                        printSeq(ref);
                        [
                            KFTest {
                                name: name
                                refList: ref
                                rate: r;
                                time: startTime
                                repeatCount: rc
                                autoReverse: aRev
                                keyFrames: frames
                            },
                        ]
                    }
                }
            }
        }
    }
}

var test3 = TestSet {
    name: "SINGLE FRAME"
    frames: [
        TestKF {t: 3},
    ]
    refF: [
        [ 3, 3],
        [ 6, 3],
        [ 9, 3],
    ]
    refB: [
        [ 0, 3],
        [ 3, 3],
        [ 6, 3],
    ]
    refFR: [
        [ 3, 3],
        [AR,AR],
        [ 9, 3],
    ]
    refBR: [
        [ 0, 3],
        [ 6, 3],
        [AR,AR],
    ]
};

var test25810 = TestSet {
    name: "NO 0s FRAME"
    frames: [
        TestKF {t: 2},
        TestKF {t: 5},
        TestKF {t: 8},
        TestKF {t: 10},
    ]
    refF: [
        [ 2, 2], [ 5, 5], [ 8, 8], [10, 10],
        [12, 2], [15, 5], [18, 8], [20, 10],
        [22, 2], [25, 5], [28, 8], [30, 10],
    ]
    refB: [
        [ 0, 10], [ 2, 8], [ 5, 5], [ 8, 2],
        [10, 10], [12, 8], [15, 5], [18, 2],
        [20, 10], [22, 8], [25, 5], [28, 2],
    ]
    refFR: [
        [ 2,  2], [ 5, 5], [ 8, 8], [10, 10],
        [AR, AR], [12, 8], [15, 5], [18, 2],
        [22,  2], [25, 5], [28, 8], [30, 10],
    ]
    refBR: [
        [ 0, 10], [ 2, 8], [ 5, 5], [ 8, 2],
        [12,  2], [15, 5], [18, 8], [20, 10],
        [AR, AR], [22, 8], [25, 5], [28, 2],
    ]
};

var test03810 = TestSet {
    name: "WITH 0s FRAME"
    frames: [
        TestKF {t: 0},
        TestKF {t: 3},
        TestKF {t: 8},
        TestKF {t: 10},
    ]
    refF: [
        [0,  0], [ 3, 3], [ 8, 8], [10, 10],
        [10, 0], [13, 3], [18, 8], [20, 10],
        [20, 0], [23, 3], [28, 8], [30, 10],
    ]
    refB: [
        [ 0, 10], [ 2, 8], [ 7, 3], [10, 0],
        [10, 10], [12, 8], [17, 3], [20, 0],
        [20, 10], [22, 8], [27, 3], [30, 0],
    ]
    refFR: [
        [ 0,  0], [ 3, 3], [ 8, 8], [10, 10],
        [AR, AR], [12, 8], [17, 3], [20,  0],
        [AR, AR], [23, 3], [28, 8], [30, 10],
    ]
    refBR: [
        [ 0, 10], [ 2, 8], [ 7, 3], [10,  0],
        [AR, AR], [13, 3], [18, 8], [20, 10],
        [AR, AR], [22, 8], [27, 3], [30,  0],
    ]
}


KFTestSuite {
    tests: [

        // test common cases
        test3.generateKFTests(),
        test25810.generateKFTests(),
        test03810.generateKFTests(),

        // test special cases
        KFTest {
            name: "SPECIAL: jump 3 to 10"
            refList: [
                [0, 0], [3, 3], [3, 8], [3, 10],
                [3, 0], [6, 3]
            ]
            repeatCount: 2
            keyFrames: [
                TestKF {t: 0},
                TestKF {t: 3 to: 10},
                TestKF {t: 8},
                TestKF {t: 10},
            ]
        },
        KFTest {
            name: "SPECIAL: jump 3 to 10 skipping 8"
            refList: [
                [0, 0], [3, 3], [3, 10],
                [3, 0], [6, 3]
            ]
            repeatCount: 2
            keyFrames: [
                TestKF {t: 0},
                TestKF {t: 3 to: 10},
                TestKF {t: 8 canSkip: true},
                TestKF {t: 10},
            ]
        },
        KFTest {
            name: "SPECIAL: jump 3 to 15"
            refList: [
                [0, 0], [3, 3], [3, 8], [3, 10],
                [3, 0], [6, 3]
            ]
            repeatCount: 2
            keyFrames: [
                TestKF {t: 0},
                TestKF {t: 3 to: 15},
                TestKF {t: 8},
                TestKF {t: 10},
            ]
        },
        KFTest {
            name: "SPECIAL: jump 3 to 13 skipping 8"
            refList: [
                [0, 0], [3, 3], [3, 10],
                [3, 0], [6, 3]
            ]
            repeatCount: 2
            keyFrames: [
                TestKF {t: 0},
                TestKF {t: 3 to: 13},
                TestKF {t: 8 canSkip: true},
                TestKF {t: 10},
            ]
        },
        KFTest {
            name: "SPECIAL: jump 3 to 7 (between frames)"
            refList: [
                [0, 0], [3, 3], [3, 6], [6, 10],
                [6, 0], [9, 3]
            ]
            repeatCount: 2
            keyFrames: [
                TestKF {t: 0},
                TestKF {t: 3 to: 7},
                TestKF {t: 6},
                TestKF {t: 10},
            ]
        },
        KFTest {
            name: "SPECIAL: jump 3 to 7 (between frames) skipping 6"
            refList: [
                [0, 0], [3, 3], [6, 10],
                [6, 0], [9, 3]
            ]
            repeatCount: 2
            keyFrames: [
                TestKF {t: 0},
                TestKF {t: 3 to: 7},
                TestKF {t: 6 canSkip: true},
                TestKF {t: 10},
            ]
        },
        KFTest {
            name: "SPECIAL: jump 8 to 0"
            refList: [
                [0, 0], [3, 3], [8, 8], [8, 3], [8, 0], [11, 3], [16, 8]
            ]
            keyFrames: [
                TestKF {t: 0},
                TestKF {t: 3},
                TestKF {t: 8 to: 0},
                TestKF {t: 10},
            ]
        },
        KFTest {
            name: "SPECIAL: jump 8 to 0 skipping 3"
            refList: [
                [0, 0], [3, 3], [8, 8], [8, 0], [11, 3], [16, 8]
            ]
            keyFrames: [
                TestKF {t: 0},
                TestKF {t: 3 canSkip: true},
                TestKF {t: 8 to: 0},
                TestKF {t: 10},
            ]
        },
        KFTest {
            name: "SPECIAL: jump 8 to -2"
            refList: [
                [0, 0], [3, 3], [8, 8], [8, 3], [8, 0], [11, 3], [16, 8]
            ]
            keyFrames: [
                TestKF {t: 0},
                TestKF {t: 3},
                TestKF {t: 8 to: -2},
                TestKF {t: 10},
            ]
        },
        KFTest {
            name: "SPECIAL: jump 8 to -1 skipping 3"
            refList: [
                [0, 0], [3, 3], [8, 8], [8, 0], [11, 3], [16, 8]
            ]
            keyFrames: [
                TestKF {t: 0},
                TestKF {t: 3 canSkip: true},
                TestKF {t: 8 to: -1},
                TestKF {t: 10},
            ]
        },
        KFTest {
            name: "SPECIAL: toggle at 8"
            refList: [
                [0, 0], [3, 3], [8, 8], [13, 3], [16, 0]
            ]
            keyFrames: [
                TestKF {t: 0},
                TestKF {t: 3},
                TestKF {t: 8 rev: true},
                TestKF {t: 10},
            ]
        },

        KFTest {
            name: "SPECIAL: backward, jump 8 to 0"
            refList: [
                [0, 10], [2, 8], [2, 3], [2, 0],
                [2, 10], [4, 8]
            ]
            repeatCount: 2
            rate: -1
            time: 10
            keyFrames: [
                TestKF {t: 0},
                TestKF {t: 3},
                TestKF {t: 8 to: 0},
                TestKF {t: 10},
            ]
        },
        KFTest {
            name: "SPECIAL: backward, jump 8 to 0 skipping 3"
            refList: [
                [0, 10], [2, 8], [2, 0],
                [2, 10], [4, 8]
            ]
            repeatCount: 2
            rate: -1
            time: 10
            keyFrames: [
                TestKF {t: 0},
                TestKF {t: 3 canSkip: true},
                TestKF {t: 8 to: 0},
                TestKF {t: 10},
            ]
        },
        KFTest {
            name: "SPECIAL: backward, jump 8 to -3"
            refList: [
                [0, 10], [2, 8], [2, 3], [2, 0],
                [2, 10], [4, 8]
            ]
            repeatCount: 2
            rate: -1
            time: 10
            keyFrames: [
                TestKF {t: 0},
                TestKF {t: 3},
                TestKF {t: 8 to: -3},
                TestKF {t: 10},
            ]
        },
        KFTest {
            name: "SPECIAL: backward, jump 8 to -3 skipping 3"
            refList: [
                [0, 10], [2, 8], [2, 0],
                [2, 10], [4, 8]
            ]
            repeatCount: 2
            rate: -1
            time: 10
            keyFrames: [
                TestKF {t: 0},
                TestKF {t: 3 canSkip: true},
                TestKF {t: 8 to: -3},
                TestKF {t: 10},
            ]
        },
        KFTest {
            name: "SPECIAL: backward, jump 8 to 3 (between frames)"
            refList: [
                [0, 10], [2, 8], [2, 5], [5, 0],
                [5, 10], [7, 8]
            ]
            repeatCount: 2
            rate: -1
            time: 10
            keyFrames: [
                TestKF {t: 0},
                TestKF {t: 5},
                TestKF {t: 8 to: 3},
                TestKF {t: 10},
            ]
        },
        KFTest {
            name: "SPECIAL: backward, jump 8 to 3 (between frames) skipping 5"
            refList: [
                [0, 10], [2, 8], [5, 0],
                [5, 10], [7, 8]
            ]
            repeatCount: 2
            rate: -1
            time: 10
            keyFrames: [
                TestKF {t: 0},
                TestKF {t: 5 canSkip: true},
                TestKF {t: 8 to: 3},
                TestKF {t: 10},
            ]
        },
        KFTest {
            name: "SPECIAL: backward, jump 3 to 10"
            refList: [
                [0, 10], [2, 8], [7, 3], [7, 8], [7, 10], [9, 8], [14, 3]
            ]
            rate: -1
            time: 10
            keyFrames: [
                TestKF {t: 0},
                TestKF {t: 3 to: 10},
                TestKF {t: 8},
                TestKF {t: 10},
            ]
        },
        KFTest {
            name: "SPECIAL: backward, jump 3 to 12 skipping 8"
            refList: [
                [0, 10], [2, 8], [7, 3], [7, 10], [9, 8], [14, 3]
            ]
            rate: -1
            time: 10
            keyFrames: [
                TestKF {t: 0},
                TestKF {t: 3 to: 10},
                TestKF {t: 8 canSkip: true},
                TestKF {t: 10},
            ]
        },
        KFTest {
            name: "SPECIAL: backward, toggle at 3"
            refList: [
                [0, 10], [2, 8], [7, 3], [12, 8], [14, 10]
            ]
            rate: -1
            time: 10
            keyFrames: [
                TestKF {t: 0},
                TestKF {t: 3 rev: true},
                TestKF {t: 8},
                TestKF {t: 10},
            ]
        },

        KFTest {
            name: "SPECIAL: complex"
            refList: generateRefs([
                [0, 0], [2, 2], [4, 13], [8, 9], [10, 13], [14, 17], [14, 7], [16, 5], [16, 24], [18, 26], [20, 21], [20, 28], [22, 30]
            ], 3)
            repeatCount: 3
            keyFrames: [
                TestKF {t: 0},
                TestKF {t: 2 to: 11},
                TestKF {t: 5 to: 24  rev: true canSkip: true},
                TestKF {t: 7                   canSkip: true},
                TestKF {t: 9 to: 15            canSkip: true},
                TestKF {t: 13        rev: true canSkip: true},
                TestKF {t: 17 to: 7  rev: true canSkip: true},
                TestKF {t: 21 to: 28           canSkip: true},
                TestKF {t: 24                  canSkip: true},
                TestKF {t: 26 to: 19           canSkip: true},
                TestKF {t: 28},
                TestKF {t: 30},
            ]
        },
    ]
}.test();

function generateRefs(ref: Integer[], n: Integer): Number[] {
    var dur = ref[ref.size() - 2];
    for (i in [0..<n]) {
        for (r in [0..ref.size() - 2 step 2]) {
            [
                ref[r] + i * dur, ref[r + 1]
            ]
        }
    }
}