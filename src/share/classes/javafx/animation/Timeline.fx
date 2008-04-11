/*
 * Copyright 2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
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

package javafx.animation;

import com.sun.javafx.runtime.Pointer;
import com.sun.scenario.animation.Clip;
import com.sun.scenario.animation.Interpolators;
import com.sun.scenario.animation.TimingTarget;
import com.sun.scenario.animation.TimingTargetAdapter;
import javafx.lang.Duration;
import javafx.lang.Sequences;
import java.lang.Object;
import java.lang.System;
import java.util.ArrayList;
import java.lang.System;

public class Timeline {

    public static attribute INDEFINITE = -1;

    public attribute repeatCount: Number = 1.0;
    public attribute autoReverse: Boolean = false;
    public attribute toggle: Boolean = false on replace {
        isReverse = true;
    };
    public attribute keyFrames: KeyFrame[] on replace {
        invalidate();
    };

    // if false, indicates that the internal (optimized) data structure
    // needs to be rebuilt
    private attribute valid = false;
    function invalidate() {
        valid = false;
    }

    // duration is inferred from time of last key frame and durations
    // of any sub-timelines in rebuildTargets()
    private attribute duration: Number = -1;

    function getTotalDur():Number {
        if (not valid) {
            rebuildTargets();
        }
        if (duration < 0 or repeatCount < 0) {
            return -1;
        }
        return duration * repeatCount;
    }

    public function start() {
        if (toggle) {
            // change direction in place
            if (clip == null) {
                buildClip();
            }
            isReverse = not isReverse;
            offsetValid = false;
            frameIndex = keyFrames.size() - frameIndex;
            if (not clip.isRunning()) {
                clip.start();
            }
        } else {
            // stop current clip and restart from beginning
            if (clip <> null) {
                clip.stop();
            }
            buildClip();
            clip.start();
        }
    }

    public function stop() {
        clip.stop();
    }

    public function pause() {
        clip.pause();
    }

    public function resume() {
        clip.resume();
    }

    public function isRunning():Boolean {
        clip.isRunning();
    }

    private function buildClip() {
        if (clip <> null and clip.isRunning()) {
            clip.cancel();
        }
        clip = Clip.create(Clip.INDEFINITE, adapter);
        clip.setInterpolator(Interpolators.getLinearInstance());
    }

    private attribute clip: Clip;
    private attribute sortedFrames: KeyFrame[];
    private attribute targets: ArrayList = new ArrayList();
    private attribute subtimelines: ArrayList = new ArrayList();
    private attribute adapter: TimingTarget = createAdapter();

    private attribute cycleIndex: Integer = 0;
    private attribute frameIndex: Integer = 0;

    private attribute isReverse: Boolean = true;
    private attribute offsetT: Number = 0;
    private attribute lastElapsed: Number = 0;
    private attribute offsetValid: Boolean = false;

    //
    // Need to revalidate everything (call rebuildTargets() again) if
    // any of the following change after construction:
    //   - Timeline.keyFrames (insert, delete, or replace)
    //   - KeyFrame.time (any)
    //   - KeyValue.target (any)
    //
    // The following should be safe to change at any time:
    //   - Timeline.repeatCount
    //   - Timeline.autoReverse
    //   - Timeline.isReverse
    //   - KeyValue.value
    //   - KeyValue.interpolate
    //
    private function rebuildTargets() {
        targets.clear();
        subtimelines.clear();
        duration = 0;

        sortedFrames = Sequences.sort(keyFrames) as KeyFrame[];

        var zeroFrame:KeyFrame;
        if (sortedFrames[0].time == 0s) {
            zeroFrame = sortedFrames[0];
        } else {
            zeroFrame = KeyFrame { time: 0s };
        }

        for (keyFrame in keyFrames) {
            if (duration >= 0) {
                duration = java.lang.Math.max(duration, keyFrame.time.millis);
            }

            if (keyFrame.timelines <> null) {
                for (timeline in keyFrame.timelines) {
                    var subDur = timeline.getTotalDur();
                    if (duration >= 0 and subDur >= 0) {
                        duration = java.lang.Math.max(duration, keyFrame.time.millis + subDur);
                    } else {
                        duration = -1;
                    }
                    var sub = SubTimeline {
                        startTime: keyFrame.time
                        timeline: timeline
                    }
                    subtimelines.add(sub);
                }
            }

            for (keyValue in keyFrame.values) {
                // TODO: targets should really be Map<Pointer,List<KFPair>>
                var pairlist: KFPairList;
                for (i in [0..<targets.size()]) {
                    var pl = targets.get(i) as KFPairList;
                    if (pl.target == keyValue.target) {
                        // already have a KFPairList for this target
                        pairlist = pl;
                        break;
                    }
                }
                if (pairlist == null) {
                    pairlist = KFPairList { 
                        target: keyValue.target 
                    }
                    if (keyFrame.time <> 0s) {
                        // get current value and attach it to zero frame
                        var kv = KeyValue {
                            target: keyValue.target;
                            value: keyValue.target.get();
                        }
                        var kfp = KFPair {
                            value: kv
                            frame: zeroFrame
                        }
                        pairlist.add(kfp);
                    }
                    targets.add(pairlist);
                }
                var kfpair = KFPair {
                    frame: keyFrame
                    value: keyValue
                }
                pairlist.add(kfpair);
            }
        }

        valid = true;
    }

    function process(totalElapsed:Number):Void {
        // 1. calculate totalDur
        // 2. modify totalElapsed depending on direction
        // 3. clamp totalElapsed and set needsStop if necessary
        // 4. calculate curT and cycle based on totalElapsed
        // 5. decide whether to increment or decrement cycle/frame index, depending on direction
        // 6. visit key frames
        // 7. do interpolation between active key frames
        // 8. visit subtimelines
        // 9. stop clip if needsStop

        var needsStop = false;
        var totalDur = getTotalDur();

        if (totalDur >= 0) {
            if (toggle) {
                if (not offsetValid) {
                    if (isReverse) {
                        offsetT = totalElapsed + lastElapsed;
                    } else {
                        offsetT = totalElapsed - lastElapsed;
                    }
                    offsetValid = true;
                }

                // adjust totalElapsed to account for direction (the
                // incoming totalElapsed value will continue to increase
                // monotonically regardless of how many times the direction
                // has been reversed, so here we just massage it back into
                // the range [0,totalDur] so that other calculations below
                // will work as usual)
                if (isReverse) {
                    totalElapsed = offsetT - totalElapsed;
                } else {
                    totalElapsed = totalElapsed - offsetT;
                }
            }

            // process one last pulse to ensure targets reach their end values
            if (toggle and isReverse) {
                if (totalElapsed <= 0) {
                    totalElapsed = 0;
                    needsStop = true;
                }
            } else {
                if (totalElapsed >= totalDur) {
                    totalElapsed = totalDur;
                    needsStop = true;
                }
            }

            // capture last adjusted totalElapsed value (used in toggle case)
            lastElapsed = totalElapsed;
        }

        var curT:Number;
        var cycle:Integer;
        var backward = false;
        if (duration < 0) {
            // indefinite duration (e.g. will occur when a sub-timeline
            // has indefinite repeatCount); always stay on zero cycle
            curT = totalElapsed;
            cycle = 0;
        } else {
            curT = totalElapsed % duration;
            cycle = totalElapsed / duration as Integer;
            if (curT == 0 and totalElapsed <> 0) {
                // we're at the end, or exactly on a cycle boundary;
                // treat this as the "1.0" case of the previous cycle
                // instead of the "0.0" case of the current cycle
                // TODO: there's probably a better way to deal with this...
                curT = duration;
                cycle -= 1;
            }
            if (autoReverse) {
                if (cycle % 2 == 1) {
                    curT = duration - curT;
                    backward = true;
                }
            }
        }

        // look through each KeyFrame and see if we need to visit its
        // key values and its action function
        if (toggle and isReverse) {
            backward = not backward;
            while (cycleIndex > cycle) {
                // we're on a new cycle; visit any key frames that we may
                // have missed along the way
                visitCycle(cycleIndex, cycleIndex > cycle+1);
                cycleIndex--;
            }
        } else {
            while (cycleIndex < cycle) {
                // we're on a new cycle; visit any key frames that we may
                // have missed along the way
                visitCycle(cycleIndex, cycleIndex < cycle-1);
                cycleIndex++;
            }
        }
        visitFrames(curT, backward, false);

        // now handle the active interval for each target
        for (i in [0..<targets.size()]) {
            var pairlist = targets.get(i) as KFPairList;
            var kfpair1 = pairlist.get(0);
            var leftT = kfpair1.frame.time.millis;

            if (curT < leftT) {
                // haven't yet reached the first key frame
                // for this target
                continue;
            }

            var v1:KeyValue;
            var v2:KeyValue;
            var segT = 0.0;

            for (j in [1..<pairlist.size()]) {
                // find keyframes on either side of the curT value
                var kfpair2 = pairlist.get(j);
                var rightT = kfpair2.frame.time.millis;
                if (curT <= rightT) {
                    v1 = kfpair1.value;
                    v2 = kfpair2.value;
                    segT = (curT - leftT) / (rightT - leftT);
                    break;
                }

                kfpair1 = kfpair2;
                leftT = kfpair1.frame.time.millis;
            }

            if (v1 <> null and v2 <> null) {
                pairlist.target.set(v2.interpolate.interpolate(v1.value, v2.value, segT));
            } 
        }

        // look through all sub-timelines and recursively call process()
        // on any active SubTimeline objects
        for (i in [0..<subtimelines.size()]) {
            var sub = subtimelines.get(i) as SubTimeline;
            if (curT >= sub.startTime.millis) {
                var subDur = sub.timeline.getTotalDur();
                if (subDur < 0 or curT <= sub.startTime.millis + subDur) {
                    sub.timeline.process(curT - sub.startTime.millis);
                }
            }
        }

        if (needsStop and clip <> null) {
            clip.stop();
        }
    }

    private function visitCycle(cycle:Integer, catchingUp:Boolean) {
        var cycleBackward = false;
        if (autoReverse) {
            if (cycle % 2 == 1) {
                cycleBackward = true;
            }
        }
        if (toggle and isReverse) {
            cycleBackward = not cycleBackward;
        }
        var cycleT = if (cycleBackward) 0 else duration;
        visitFrames(cycleT, cycleBackward, catchingUp);
        // avoid repeated visits to terminals in autoReverse case
        frameIndex = if (autoReverse) 1 else 0;
    }

    private function visitFrames(curT:Number, backward:Boolean, catchingUp:Boolean) {
        if (backward) {
            var i1 = sortedFrames.size()-1-frameIndex;
            var i2 = 0;
            for (fi in [i1..i2 step -1]) {
                var kf = sortedFrames[fi];
                if (curT <= kf.time.millis) {
                    if (not (catchingUp and kf.canSkip)) {
                        kf.visit();
                    }
                    frameIndex++;
                } else {
                    break;
                }
            }
        } else {
            var i1 = frameIndex;
            var i2 = sortedFrames.size()-1;
            for (fi in [i1..i2]) {
                var kf = sortedFrames[fi];
                if (curT >= kf.time.millis) {
                    if (not (catchingUp and kf.canSkip)) {
                        kf.visit();
                    }
                    frameIndex++;
                } else {
                    break;
                }
            }
        }
    }

    private function createAdapter():TimingTarget {
        TimingTargetAdapter {
            public function begin() : Void {
                if (toggle and isReverse) {
                    cycleIndex = (repeatCount-1) as Integer;
                    lastElapsed = getTotalDur();
                } else {
                    cycleIndex = 0;
                    lastElapsed = 0;
                }
                frameIndex = 0;
                offsetT = 0;
                offsetValid = false;
            }
            
            public function timingEvent(fraction, totalElapsed) : Void {
                process(totalElapsed as Number);
            }
        }
    }
}

class KFPair {
    attribute frame:KeyFrame;
    attribute value:KeyValue;
}

class KFPairList {
    attribute target:Pointer;
    private attribute pairs:ArrayList = new ArrayList();

    function size(): Integer {
        return pairs.size();
    }

    function add(pair:KFPair): Void {
        // keep list sorted chronologically
        for (i in [0..<pairs.size()]) {
            var listval = get(i);
            if (pair.frame.time < listval.frame.time) {
                pairs.add(i, pair);
                return;
            }
        }
        pairs.add(pair);
    }

    function get(i:Integer): KFPair {
        return pairs.get(i) as KFPair;
    }
}

class SubTimeline {
    attribute startTime:Duration;
    attribute timeline:Timeline;
}
