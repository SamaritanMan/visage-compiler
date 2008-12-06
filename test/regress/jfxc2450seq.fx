/**
 * Regression test JFXC-2450 : Translation support for new runtime bound operator implementation
 *
 * @test
 * @run
 */

var seq = [ 1234 ];
var seq2 = [ 1234, 5 ];
var n = 1234;

def seq_seq2_eq = bind seq == seq2;
def n_seq_eq = bind n == seq;
println( seq_seq2_eq );
println( n_seq_eq );

def seq_seq2_ne = bind seq != seq2;
def n_seq_ne = bind n != seq;
println( seq_seq2_ne );
println( n_seq_ne );
