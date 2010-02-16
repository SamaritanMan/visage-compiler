/*
 * Regression :  JFXC-3774 - names of public generated vars/funcs change in multiple file compile vs. single file compile.
 *
 * @subtest
 */
public class jfxc3774A { 
  protected var children:Integer[] on replace oldNodes[a..b] = newNodes { 
     println("newNodes = {newNodes}"); 
  } 
} 
