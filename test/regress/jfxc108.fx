import java.lang.System;

/*
 * @test
 * @run
 *
 * Copied from jfxc106.fx, but with different results, pending completion of jfxc108 (at which point, jfxc106 will
 * start failing and it should be amended.)
 */

class X {
    var nums: Number[];
    var xs: Number[] = bind nums
        on replace oldValue[begin..end]=newElements {
            System.out.println("replace {oldValue.toString()}[{begin}..{end}] = {newElements.toString()}");
        }
}

var x = X {
    nums: [1.0, 2.0, 3.0]
};

delete x.nums[0];
x.nums[0] = 20.0;
insert 99.0 into x.nums;
