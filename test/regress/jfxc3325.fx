/**
 * JFXC-3325 : nativearray of byte.
 *
 * The following used to crash in the back-end with 
 * 
 * inconvertible types
 *   found   : java.lang.Byte[]
 *   required: byte[]
 *
 * @test
 */

function createByteBuffer( length:Integer ) :nativearray of Byte { 
    var result:Byte[]; 
    var remaining = length; 
    def zero:Byte = 0; 
    while( remaining > 0 ) { 
        insert zero into result; 
        remaining--; 
    } 

    return result as nativearray of Byte; 
} 
