/*
 * JFXC-3224:  StringIndexOutOfBoundsException thrown with LiveConnect using b08 respin2.
 * @test
 */
import javafx.reflect.*; 

var bad1 = true;
var bad2 = false;
var bad3 : Boolean = bind bad1;

var context = FXLocal.getContext(); 
var clazz = context.findClass("jfxc3224"); 
var variables_all = clazz.getVariables(true);