 /*
  * JFXC-1859 : public-read variable can be written from outside the script using bind with inverse
  *
  * @subtest
  *
  *  
  */
  
 import java.lang.System;
  
  public class jfxc1859{
	  public-read var s:String="Hello Everyone"; // script-only write access
  }