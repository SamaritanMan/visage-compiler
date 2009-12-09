/*
 * Regression test JFXC-3704 : Compiled bind: Mixin statics missing accessors.
 *
 * @subtest
 */

public def jj: String[] = [ 
    "text","hpos","vpos","textAlignment","textOverrun","textWrap","font", 
    "graphic","graphicHPos","graphicVPos","graphicTextGap" 
]; 

public mixin class jfx3704m { 
} 
