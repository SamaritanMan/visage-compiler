/*
 * boundMethodGlobal.fx
 *
 * Created on Dec 22, 2009, 9:18:12 AM
 */


/**
 * @author ksrini
 */

class boundMethodGlobal extends cbm {
    public-read var max:Number = 1000;
    public-read var min:Number = 0;
    function getSum(x: Number, y: Number) : Number {
        return x + y;
    }
    var i = min;
    var j = max;
    def sum = bind getSum(i, j);
    
    override public function test() {
        while (i < max) {
            while (j > min) {
                //debugOutln("i={i}, j={j}, mag={sum}");
                j--;
            }
            j=max;
            i++;
        }
        return 0;
    }//test

};//class

/**
 * define a run method to call runtests passing args and instance of this class
 */
public function run(args:String[]) {
    var t = new boundMethodGlobal();
    cbm.runtest(args,t);
}