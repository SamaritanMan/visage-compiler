import java.lang.System;
/*
 * The first sequence results in the sequence [1,2,3,..,10] as expected and
 * sizeof seq1 == 10, also as expected.
 * The second sequence results in an EMPTY sequence [], not as expected and
 * sizeof seq2 == 0.
 * To get a descending sequence one must declare the downward step
 * seq2b = [10..1 step -1]
 * to get a sequence of 10 elements 10,9,8,7..,1.
 * seq2c show that seq2 = [10..1] is equivalent to [10..1 step 1], which apparently
 * mean go up from 10 by 1's, and there is none up from 10 in the sequence.
 */

var seq1 = [1..10]; //implied 'step 1'
var seq2 = [10..1]; //does not imply 'step -1'
System.out.println("Contents of seq1[1..10]: {seq1}, and size of seq1[1..10]: {sizeof seq1}");
System.out.println("Contents of seq2[10..1]: {seq2}, but size of seq2[10..1]: {sizeof seq2}");


var seq2b = [10..1 step -1];
System.out.println("Contents of seq2b[10..1 step -1]: {seq2b}; size of seq2b: {sizeof seq2b}");

var seq2c = [10..1 step 1];
System.out.println("Contents of seq2c[10..1 step 1]: {seq2c}; size of seq2c: {sizeof seq2c}");

var bEmptySequenceDeclaration: Boolean = [10..1] == [10..1 step 1];
var emptysequence:Integer[] = [];
System.out.println("[10..1] == [10..1 step 1] ==  {[10..1] == [10..1 step 1]}");
System.out.println("sizeof[10..1] == sizeof[10..1 step 1] and sizeof[10..1]==(sizeof emptysequence) ==  {sizeof[10..1] == sizeof[10..1 step 1] and sizeof[10..1]==(sizeof emptysequence)}");
System.out.println("sizeof [10..1] = {sizeof [10..1]}");
System.out.println("sizeof [10..1 step 1] = {sizeof [10..1 step 1]}");
System.out.println("sizeof emptysequence = {sizeof emptysequence}");
