/**
 * JFXC-3971 : Compiler crashes when compiling fx source with bi-directional binds on sliders, checkboxes
 *
 * @compilefirst jfxc3971sub.fx
 * @test
 */

var x = bind jfxc3971sub.a with inverse;
