/*
 * Regression: JFXC-2081 - Sequence Number range index is not allowed.
 *
 * @test
 *
 */

var str1:String[] = ["J","a","v","a","F","X"][1.1 ..4.1];

var str2:String[] = bind str1[5.1 ..7.1];
