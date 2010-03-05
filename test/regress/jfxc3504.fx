/**
 * JFXC-3504 :  Using a break within a for-Expression crashes the compiler.
 *
 * @test
 * @run
 */

var words = for (length in [3..6]) {
    for (word in ['moose', 'wolf', 'turkey', 'bee']) {
        if (word.length() >= length) {
            word
        } else {
            break;
        }
    }
};

println(words);

