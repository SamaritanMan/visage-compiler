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
 * Exercise various operators on length.
 *
 * @author Stephen Chin <steveonjava@gmail.com>
 * @test
 * @run
 */

// length addition
var r = 3mm + 2mm;
println("3mm + 2mm = {%#s r}");

// length subtraction
r = 5mm - 2mm;
println("5mm - 2mm = {%#s r}");

// multiply by integer
r = 3mm * 2;
println("3mm * 2 = {%#s r}");
// multiply by integer
r = 2 * 3mm;
println("2 * 3mm = {%#s r}");

// multiply by number
r = 2cm * 2.5;
println("2cm * 2.5 = {%#s r}");
// multiply by number
r = 2.5 * 2cm;
println("2.5 * 2cm = {%#s r}");

// divide by integer
r = 3mm / 2;
println("3mm / 2 = {%#s r}");

// divide by number
r = 2.5cm / 2.5;
println("2.5cm / 2.5 = {%#s r}");

// divide by another length
var ratio = 2.5mm/5.0mm;
println("2.5mm / 5.0mm = {ratio}");
