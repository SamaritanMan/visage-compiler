/**
 * Return statement inconsistency
 *
 * @test/fail
 */

var initialized = false;

function initialize1 () {
    if (initialized) {
        return 100;
    }
	else { return ; }
}

function initialize2 () {
    if (initialized) {
        return;
    }
	else { return 100; }
}