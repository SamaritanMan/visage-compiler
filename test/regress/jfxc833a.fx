/*
 * Regression test for JFXC-833 : single object not equal to sequence of single object
 *
 * @test
 * @run
 */

import java.lang.System;

System.out.println("{(1 as Integer) == [(1 as Integer)]}" ); // prints 'true'
System.out.println("{(1 as Integer) == [[(1 as Integer)]]}" ); // prints 'true'
System.out.println("{[(1 as Integer)] == [[(1 as Integer)]]}" ); // prints 'true'
System.out.println("{(1 as Integer) == []}" ); // prints 'false'

System.out.println("{[(1 as Integer)] == (1 as Integer)}" ); // prints 'true'
System.out.println("{[[(1 as Integer)]] == (1 as Integer)}" ); // prints 'true'
System.out.println("{[[(1 as Integer)]] == [(1 as Integer)]}" ); // prints 'true'
System.out.println("{[] == (1 as Integer)}" ); // prints 'false'

System.out.println("{(1 as Short) == [(1 as Short)]}" ); // prints 'true'
System.out.println("{(1 as Short) == [[(1 as Short)]]}" ); // prints 'true'
System.out.println("{[(1 as Short)] == [[(1 as Short)]]}" ); // prints 'true'
System.out.println("{(1 as Short) == []}" ); // prints 'false'

System.out.println("{[(1 as Short)] == (1 as Short)}" ); // prints 'true'
System.out.println("{[[(1 as Short)]] == (1 as Short)}" ); // prints 'true'
System.out.println("{[[(1 as Short)]] == [(1 as Short)]}" ); // prints 'true'
System.out.println("{[] == (1 as Short)}" ); // prints 'false'

System.out.println("{(1 as Float) == [(1 as Float)]}" ); // prints 'true'
System.out.println("{(1 as Float) == [[(1 as Float)]]}" ); // prints 'true'
System.out.println("{[(1 as Float)] == [[(1 as Float)]]}" ); // prints 'true'
System.out.println("{(1 as Float) == []}" ); // prints 'false'

System.out.println("{[(1 as Float)] == (1 as Float)}" ); // prints 'true'
System.out.println("{[[(1 as Float)]] == (1 as Float)}" ); // prints 'true'
System.out.println("{[[(1 as Float)]] == [(1 as Float)]}" ); // prints 'true'
System.out.println("{[] == (1 as Float)}" ); // prints 'false'

System.out.println("{(1 as Double) == [(1 as Double)]}" ); // prints 'true'
System.out.println("{(1 as Double) == [[(1 as Double)]]}" ); // prints 'true'
System.out.println("{[(1 as Double)] == [[(1 as Double)]]}" ); // prints 'true'
System.out.println("{(1 as Double) == []}" ); // prints 'false'

System.out.println("{[(1 as Double)] == (1 as Double)}" ); // prints 'true'
System.out.println("{[[(1 as Double)]] == (1 as Double)}" ); // prints 'true'
System.out.println("{[[(1 as Double)]] == [(1 as Double)]}" ); // prints 'true'
System.out.println("{[] == (1 as Double)}" ); // prints 'false'

System.out.println("{(1 as Character) == [(1 as Character)]}" ); // prints 'true'
System.out.println("{(1 as Character) == [[(1 as Character)]]}" ); // prints 'true'
System.out.println("{[(1 as Character)] == [[(1 as Character)]]}" ); // prints 'true'
System.out.println("{(1 as Character) == []}" ); // prints 'false'

System.out.println("{[(1 as Character)] == (1 as Character)}" ); // prints 'true'
System.out.println("{[[(1 as Character)]] == (1 as Character)}" ); // prints 'true'
System.out.println("{[[(1 as Character)]] == [(1 as Character)]}" ); // prints 'true'
System.out.println("{[] == (1 as Character)}" ); // prints 'false'

System.out.println("{(1 as Byte) == [(1 as Byte)]}" ); // prints 'true'
System.out.println("{(1 as Byte) == [[(1 as Byte)]]}" ); // prints 'true'
System.out.println("{[(1 as Byte)] == [[(1 as Byte)]]}" ); // prints 'true'
System.out.println("{(1 as Byte) == []}" ); // prints 'false'

System.out.println("{[(1 as Byte)] == (1 as Byte)}" ); // prints 'true'
System.out.println("{[[(1 as Byte)]] == (1 as Byte)}" ); // prints 'true'
System.out.println("{[[(1 as Byte)]] == [(1 as Byte)]}" ); // prints 'true'
System.out.println("{[] == (1 as Byte)}" ); // prints 'false'

System.out.println("{'1' == ['1']}" ); // prints 'true'
System.out.println("{'1' == [['1']]}" ); // prints 'true'
System.out.println("{['1'] == [['1']]}" ); // prints 'true'
System.out.println("{'1' == []}" ); // prints 'false'

System.out.println("{['1'] == '1'}" ); // prints 'true'
System.out.println("{[['1']] == '1'}" ); // prints 'true'
System.out.println("{[['1']] == ['1']}" ); // prints 'true'
System.out.println("{[] == '1'}" ); // prints 'false'