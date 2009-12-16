/*
 * Regression Test : JFXC-3811 - Base class's method called twice when called once
 *
 * @test
 * @run
 *
 */
class jjStage { 
    var seq1: Integer[]; 
} 

public class BaseTest { 
    public function getStage() { 
        println("jjj"); 
        return jjStage {} 
    } 
} 

class ImageTestx extends BaseTest { 
    init { 
        getStage().seq1 = [0..5]; 
    } 
} 

public function run() { 
    ImageTestx{}; 
} 
