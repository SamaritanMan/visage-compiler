/*
 *
 * @subtest
**/

public abstract class FxQueue{
public var myQueue:java.lang.Object[] ;
public abstract function poll();
public abstract function peek();
public function clear(){
	delete myQueue;
}
/*
 * 10/07/08
 * Renamed this print for Mobile testing compatiblity, but it
 * does not seem to affect MiTest.fx results???
 */
public function fqprint(){
	var indx:Integer =0;
	for(val in myQueue){	
	   java.lang.System.out.print(val);
	   if(++indx<sizeof myQueue){
		java.lang.System.out.print(",");
	   }	
	}
java.lang.System.out.println("");
}
public function  put(item) {
      insert item into myQueue;
}
}
