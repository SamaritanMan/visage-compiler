/*
 * Regression: JFXC-3960 - Compiled bind optimization: Enforce access protection and remove qualification of private names
 *
 * @compilefirst jfxc3960staticA.fx
 * @test
 *
 */ 

def CSS:String[] = jfxc3960staticA.CSS;

public class jfxc3960staticB extends jfxc3960staticA {
}