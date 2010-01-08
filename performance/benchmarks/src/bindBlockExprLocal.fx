/*
 * bindBlockExprLocal.fx
 *
 * Created on Dec 23, 2009, 9:50:09 AM
 */


/**
 * @author ksrini
 */

class bindBlockExprLocal extends cbm {
    public-read var max:Number = 100;
    public-read var min:Number = 1;

    function getMagnitude(x: Number, y: Number) : Number {
        return java.lang.Math.sqrt(x*x + y*y);
    }

    function getAngle(x: Number, y:Number) : Number {
        return java.lang.Math.tan(y/x);
    }
    
    override public function test() {
        var i = min;
        var j = max;
        def polar = bind {
            def mag = getMagnitude(i, j);
            def angle = getAngle(i, j)*180/java.lang.Math.PI;
            "{mag}@{angle}";
        }
        while (i < max) {
            while (j > min) {
                debugOutln("i={i}, j={j}, polar={polar}");
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
    var t = new bindBlockExprLocal();
    cbm.runtest(args,t);
}