/*
 * Copyright (c) 2010-2011, Visage Project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name Visage nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Feature test #30 - angle literals
 *
 * @author Stephen Chin <steveonjava@gmail.com>
 * @test
 * @run
 */
import javafx.lang.Angle;
import javafx.lang.AngleUnit;

println("Zero = {Angle.ZERO}");
var a = Angle.valueOf(50.0, AngleUnit.DEGREE);
println("50deg = {a}");
a = 5turn;
println("5turn = {a}");
a = .5rad;
println(".5rad = {a} or {a.toDegreeAngle()} or {a.toTurnAngle()}");
a = 5deg + .1rad;
println("5deg + .1rad = {a}");

println("1deg + 1rad = {1deg + 1rad}");
println("1deg - 1rad = {1deg - 1rad}");
println("1deg * 2 = {1deg * 2}");
println("1deg / 2 = {1deg / 2}");
println("1deg < 1rad = {1deg < 1rad}");
println("1deg <= 1rad = {1deg <= 1rad}");
println("1deg > 1rad = {1deg > 1rad}");
println("1deg >= 1rad = {1deg >= 1rad}");
println("1deg == 1rad = {1deg == 1rad}");
println("1deg != 1rad = {1deg != 1rad}");
