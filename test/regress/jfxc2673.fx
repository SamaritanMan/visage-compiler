/**
 * JFXC-2673 : Regression: crash in forest on BoundComprehension
 *
 * @test
 */

class A {
   var selected:Boolean;
}

var buttons:A[];

bound function getSelection() { 
   var selection = for (button in buttons where button.selected) button;
};
