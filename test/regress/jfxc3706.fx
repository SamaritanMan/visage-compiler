/**
 * JFXC-3706 :  Compiler crashes when using ClassName.class.getResourceAsStream(...)
 *
 * @test
 */

// this used to crash in compiler back-end 
var input = jfxc3706.class.getResourceAsStream("/invalidresource.xml");
