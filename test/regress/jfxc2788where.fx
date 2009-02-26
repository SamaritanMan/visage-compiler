/**
 * JFXC-2788 : Collapse bound conditional accesses into the single per-scipt BindingExpression
 *
 * where clauses
 *
 * @test
 * @run
 */

function eval(str : String) : String { println(str); str }

var seq = ["rats", "mice", "elephants", "pigs"];

def bf = bind for (val in seq where val.length() <= 5) eval(val);
println(bf);
insert "moles" into seq;
insert "kangaroos" into seq;
println(bf);

var SEQ = ["RATS", "MICE", "ELEPHANTS", "PIGS"];

def bfs = bind for (val in SEQ where val.length() <= 5) [eval(val)];
println(bfs);
insert "MOLES" into SEQ;
insert "KANGAROOS" into SEQ;
println(bfs);
