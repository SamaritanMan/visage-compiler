/**
 * JFXC-4196 :  Controlling flow in for loop with "continue"
 * 
 * @test
 */

var nodearray : String[];
var nodearraysize=sizeof nodearray;

var textarray:String[]=for(i in [1 .. nodearraysize-1]){
    if(nodearray[i] == "Text" ){
        nodearray[i]
    } else{
        // used to crash the compiler here.
        continue;
    }
}
