/**
 * JFXC-3755 : Compiled bind: cannot find symbol: isNull(java.lang.Object)
 *
 * @test
 */

public var cascadingsheet : String[] on replace = newsheet {
    (null == cascadingsheet[0]) and (false == cascadingsheet[0].endsWith(".css")); 
    // This used to crash the compiler in the back-end
    (null == newsheet[0]) and (false == newsheet[0].endsWith(".css")); 
}

