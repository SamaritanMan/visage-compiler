/*
 * @test/fxunit
 */
 
public class SequenceTest extends javafx.fxunit.FXTestCase {
    function testSimple() {
        assertEquals([1, 2], [1, 2]);
    }

    function testSimpleMessage() {
        assertEquals("message", [3, 4], [3, 4]);
    }
}
