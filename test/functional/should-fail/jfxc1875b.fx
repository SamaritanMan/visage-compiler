import java.lang.System;

/*
 * JFXC-1875 public-init variable is boundable from outside the script
 *
 * @compilefirst ./jfxc1875a.fx
 * @test/compile-error
 *
 */


var str:String[]=["JAN","AUG","MAR","APR"];
var bz1=jfxc1875a{
    months:bind str; // binding a public-init variable
}

System.out.println(bz1.months);
str=["Mon","Tue","Wed","Fri"]; // variable str is being re-assgined
System.out.println(bz1.months); 

var bz2=jfxc1875a{
months:["febbraio","marzo","aprile"]; }
System.out.println(bz2.months);

