/**
 * Regression test JFXC-1106 : Assigning to parameter promotes param to IntLocation causing back-end error
 * Assigning to parameter not allowed
 *
 * @test/compile-error
 */

function decrementer(initial:Integer):function(amount:Integer):Integer {
    return function(amount:Integer):Integer {
        initial = initial - amount;
        return initial;
    }
}

var myDecrementer = decrementer(10);
