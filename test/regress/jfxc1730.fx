/**
 * Regression test JFXC-1730 :  Compilation error in doing Duration += n
 *
 * @test
 */

var durs : Duration[] = [1s, 2s, 3s];
var dur: Duration = 0s;
dur += 1s;
durs[0] += 1s;
