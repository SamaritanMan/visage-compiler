/**
 * Functional test:  To test different operators( Small stress test too).
 * @test
 * @run
 */
import java.lang.System;
import java.lang.Math;

function isPrime(n:Integer):Boolean {
    	return (sizeof (for(i in [2 .. Math.sqrt(n)] where n mod i == 0) i) == 0);
}
function pow(b:Number, P:Integer):Number {
    var inv = false;
    var p = P;
    if( p ==0){ return 1;}
    if( p < 0){ inv = true; p=p*(-1); }
    var b2 = b;
    for ( i in [ 1..p-1]) {       b2 = b2*b;    }
    if(inv){return 1.0/b2; }
    return b2;
}

function roundMe(src:Number, digits:Integer) {
	var bigSrc = src * pow(10,digits);
	return Math.round(bigSrc)/pow(10,digits);
}
function factors(n:Integer) {
    return for(i in [1 .. n/2] where n  mod  i == 0) i;
}
function harmonics(n:Integer):Number {
	var flag:Number = 0;
	return roundMe(1 + ({for(i in [2..n] where n >= 2){ flag += roundMe((1.0/i),3);} flag;}),2);
}
function isArmstrong(num:Integer) {
	var flag:Integer=0;
	var n = num;
	while(n > 0) {
		flag += pow(n mod 10,3);
		n /= 10;
	}
	return (flag==num);
} 
function doit(n:Integer) {
	var primes:Integer[];
	var armstrongs:Integer[];
	var harmonicSeries:Number[];
	for(i in [2..n]) {
		if(isPrime(i)){ insert i into primes; }
		 else{ System.out.println("Factorts for {i} : {factors(i).toString()}"); }
		if(isArmstrong(i)){ insert i into armstrongs; }
		insert harmonics(i) into  harmonicSeries;
	}
	System.out.println("Pime numbers : {primes.toString()}");	
	System.out.println("Armstrongs numbers : {armstrongs.toString()}");
	System.out.println("Harmonic Serious : {harmonicSeries.toString()}");
}
doit(1000);
