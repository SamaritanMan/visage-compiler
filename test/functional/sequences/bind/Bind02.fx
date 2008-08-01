/**
 * Functional test: Binding a sequence
 * @test
 * @run
 */


import java.lang.*;

var UOE:Class = Class.forName("com.sun.javafx.runtime.BindingException");
var a:Integer[] = bind [12,234,45,9];
class Data {
	var pass =0;
	var fail =0;
	var a = bind [12,234,45,9];
	var b = bind { for(k in [1..10] where (k%2 == 0)) k };
	var c:Integer[] = bind [];
	var d:Integer[] = bind [a,b,c];
	var e:Integer[] = bind [a,[b,d],c];
	var f:Integer[] = bind [0..<9];
	var g:Integer[] = bind f[4..];
	var h:Integer[] = bind [0..9];
	var i:Integer[] = bind g[0..<];
}
var data = Data{};

function check(k:Class, f:function()) {
	try { f(); data.fail++; System.out.println("Expected {k.getName()} not thrown"); }
        catch (t:Throwable) {
	        if (k.isAssignableFrom(t.getClass())) { data.pass++;}
                else { data.fail++; t.printStackTrace();}
        }
};
System.out.println(data.a); //[ 12, 234, 45, 9 ]
check(UOE,  function() : Void { delete data.a;});
check(UOE,  function() : Void { delete 0 from data.a;});
check(UOE,  function() : Void { delete data.a[-1];});
check(UOE,  function() : Void { delete data.a[0..Integer.MAX_VALUE];});
check(UOE,  function() : Void { data.a[-1] = 0;});
check(UOE,  function() : Void { data.a[0] = 0;});
check(UOE,  function() : Void { data.a[(sizeof data.a-1)/2] = Integer.MIN_VALUE;});
check(UOE,  function() : Void { data.a[(sizeof data.a-1)] = Integer.MIN_VALUE;});
check(UOE,  function() : Void { data.a[200] = 0;});
check(UOE,  function() : Void { insert 23 into data.a;});
check(UOE,  function() : Void { insert 23 before data.a[-1];});
check(UOE,  function() : Void { insert 23 before data.a[0];});
check(UOE,  function() : Void { insert 23 before data.a[100];});
check(UOE,  function() : Void { insert 23 after data.a[-1];});
check(UOE,  function() : Void { insert 23 after data.a[0];});
check(UOE,  function() : Void { insert 23 after data.a[100];});
System.out.println(data.b); //[ 2, 4, 6, 8, 10 ]
check(UOE,  function() : Void { delete data.b;});
check(UOE,  function() : Void { delete 0 from data.b;});
check(UOE,  function() : Void { delete data.b[-1];});
check(UOE,  function() : Void { delete data.b[0..Integer.MAX_VALUE];});
check(UOE,  function() : Void { data.b[-2] = 9;});
check(UOE,  function() : Void { data.a[(sizeof data.b-1)/2] = Integer.MIN_VALUE;});
check(UOE,  function() : Void { data.a[(sizeof data.b-1)] = Integer.MIN_VALUE;});
check(UOE,  function() : Void { data.b[4] = 0;});
check(UOE,  function() : Void { data.b[200] = 0;});
check(UOE,  function() : Void { insert 23 into data.b;});
check(UOE,  function() : Void { insert 23 before data.b[-1];});
check(UOE,  function() : Void { insert 23 before data.b[0];});
check(UOE,  function() : Void { insert 23 before data.b[20];});
check(UOE,  function() : Void { insert 23 after data.b[-1];});
check(UOE,  function() : Void { insert 23 after data.b[0];});
check(UOE,  function() : Void { insert 23 after data.b[20];});
System.out.println(data.c);//[ ]
check(UOE,  function() : Void { delete data.c;});
check(UOE,  function() : Void { delete 0 from data.c;});
check(UOE,  function() : Void { delete data.c[-1];});
check(UOE,  function() : Void { delete data.c[0..Integer.MAX_VALUE];});
check(UOE,  function() : Void { data.c[-2] = 9;});
check(UOE,  function() : Void { data.c[0] = 0;});
check(UOE,  function() : Void { data.c[200] = 0;});
check(UOE,  function() : Void { insert 23 into data.c;});
check(UOE,  function() : Void { insert 23 before data.c[-1];});
check(UOE,  function() : Void { insert 23 before data.c[0];});
check(UOE,  function() : Void { insert 23 after data.c[-1];});
check(UOE,  function() : Void { insert 23 after data.c[0];});
System.out.println(data.d);//[ 12, 234, 45, 9, 2, 4, 6, 8, 10 ]
check(UOE,  function() : Void { delete data.d;});
check(UOE,  function() : Void { delete 0 from data.d;});
check(UOE,  function() : Void { delete data.d[-1];});
check(UOE,  function() : Void { delete data.d[0..Integer.MAX_VALUE];});
check(UOE,  function() : Void { data.d[-1] = 0;});
check(UOE,  function() : Void { data.d[0] = 0;});
check(UOE,  function() : Void { data.a[(sizeof data.d-1)/2] = Integer.MIN_VALUE;});
check(UOE,  function() : Void { data.a[(sizeof data.d-1)] = Integer.MIN_VALUE;});
check(UOE,  function() : Void { data.d[200] = 0;});
check(UOE,  function() : Void { insert 23 into data.d;});
check(UOE,  function() : Void { insert 23 before data.d[-1];});
check(UOE,  function() : Void { insert 23 before data.d[0];});
check(UOE,  function() : Void { insert 23 before data.d[20];});
check(UOE,  function() : Void { insert 23 after data.d[-1];});
check(UOE,  function() : Void { insert 23 after data.d[0];});
check(UOE,  function() : Void { insert 23 after data.d[20];});
System.out.println(data.e);//[ 12, 234, 45, 9, 2, 4, 6, 8, 10, 12, 234, 45, 9, 2, 4, 6, 8, 10 ]
check(UOE,  function() : Void { delete data.e;});
check(UOE,  function() : Void { delete 0 from data.e;});
check(UOE,  function() : Void { delete data.e[-1];});
check(UOE,  function() : Void { delete data.e[0..Integer.MAX_VALUE];});
check(UOE,  function() : Void { data.e[-1] = 0;});
check(UOE,  function() : Void { data.e[0] = 0;});
check(UOE,  function() : Void { data.a[(sizeof data.e-1)/2] = Integer.MIN_VALUE;});
check(UOE,  function() : Void { data.a[(sizeof data.e-1)] = Integer.MIN_VALUE;});
check(UOE,  function() : Void { data.e[200] = 0;});
check(UOE,  function() : Void { insert 23 into data.e;});
check(UOE,  function() : Void { insert 23 before data.e[-1];});
check(UOE,  function() : Void { insert 23 before data.e[0];});
check(UOE,  function() : Void { insert 23 before data.e[20];});
check(UOE,  function() : Void { insert 23 after data.e[-1];});
check(UOE,  function() : Void { insert 23 after data.e[0];});
check(UOE,  function() : Void { insert 23 after data.e[20];});
System.out.println(data.f);//[ 0, 1, 2, 3, 4, 5, 6, 7, 8 ]
check(UOE,  function() : Void { delete data.f;});
check(UOE,  function() : Void { delete 0 from data.f;});
check(UOE,  function() : Void { delete data.f[-1];});
check(UOE,  function() : Void { delete data.f[0..Integer.MAX_VALUE];});
check(UOE,  function() : Void { data.f[-1] = 0;});
check(UOE,  function() : Void { data.f[0] = 0;});
check(UOE,  function() : Void { data.a[(sizeof data.f-1)/2] = Integer.MIN_VALUE;});
check(UOE,  function() : Void { data.a[(sizeof data.f-1)] = Integer.MIN_VALUE;});
check(UOE,  function() : Void { data.f[200] = 0;});
check(UOE,  function() : Void { insert 23 into data.f;});
check(UOE,  function() : Void { insert 23 before data.f[-1];});
check(UOE,  function() : Void { insert 23 before data.f[0];});
check(UOE,  function() : Void { insert 23 before data.f[20];});
check(UOE,  function() : Void { insert 23 after data.f[-1];});
check(UOE,  function() : Void { insert 23 after data.f[0];});
check(UOE,  function() : Void { insert 23 after data.f[20];});
System.out.println(data.g);//[ 4, 5, 6, 7, 8 ]
check(UOE,  function() : Void { delete data.g;});
check(UOE,  function() : Void { delete 0 from data.g;});
check(UOE,  function() : Void { delete data.g[-1];});
check(UOE,  function() : Void { delete data.g[0..Integer.MAX_VALUE];});
check(UOE,  function() : Void { data.g[-1] = 0;});
check(UOE,  function() : Void { data.g[0] = 0;});
check(UOE,  function() : Void { data.a[(sizeof data.g-1)/2] = Integer.MIN_VALUE;});
check(UOE,  function() : Void { data.a[(sizeof data.g-1)] = Integer.MIN_VALUE;});
check(UOE,  function() : Void { data.g[200] = 0;});
check(UOE,  function() : Void { insert 23 into data.g;});
check(UOE,  function() : Void { insert 23 before data.g[-1];});
check(UOE,  function() : Void { insert 23 before data.g[0];});
check(UOE,  function() : Void { insert 23 before data.g[20];});
check(UOE,  function() : Void { insert 23 after data.g[-1];});
check(UOE,  function() : Void { insert 23 after data.g[0];});
check(UOE,  function() : Void { insert 23 after data.g[20];});
System.out.println(data.h);//[ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ]
check(UOE,  function() : Void { delete data.h;});
check(UOE,  function() : Void { delete 0 from data.h;});
check(UOE,  function() : Void { delete data.h[-1];});
check(UOE,  function() : Void { delete data.h[0..Integer.MAX_VALUE];});
check(UOE,  function() : Void { data.h[-1] = 0;});
check(UOE,  function() : Void { data.h[0] = 0;});
check(UOE,  function() : Void { data.a[(sizeof data.h-1)/2] = Integer.MIN_VALUE;});
check(UOE,  function() : Void { data.a[(sizeof data.h-1)] = Integer.MIN_VALUE;});
check(UOE,  function() : Void { data.h[200] = 0;});
check(UOE,  function() : Void { insert 23 into data.h;});
check(UOE,  function() : Void { insert 23 before data.h[-1];});
check(UOE,  function() : Void { insert 23 before data.h[0];});
check(UOE,  function() : Void { insert 23 before data.h[20];});
check(UOE,  function() : Void { insert 23 after data.h[-1];});
check(UOE,  function() : Void { insert 23 after data.h[0];});
check(UOE,  function() : Void { insert 23 after data.h[20];});
System.out.println(data.i);//[ 4, 5, 6, 7 ]
check(UOE,  function() : Void { delete data.i;});
check(UOE,  function() : Void { delete 0 from data.i;});
check(UOE,  function() : Void { delete data.i[-1];});
check(UOE,  function() : Void { delete data.i[0..Integer.MAX_VALUE];});
check(UOE,  function() : Void { data.i[-1] = 0;});
check(UOE,  function() : Void { data.i[0] = 0;});
check(UOE,  function() : Void { data.a[(sizeof data.i-1)/2] = Integer.MIN_VALUE;});
check(UOE,  function() : Void { data.a[(sizeof data.i-1)] = Integer.MIN_VALUE;});
check(UOE,  function() : Void { data.i[200] = 0;});
check(UOE,  function() : Void { insert 23 into data.i;});
check(UOE,  function() : Void { insert 23 before data.i[-1];});
check(UOE,  function() : Void { insert 23 before data.i[0];});
check(UOE,  function() : Void { insert 23 before data.i[20];});
check(UOE,  function() : Void { insert 23 after data.i[-1];});
check(UOE,  function() : Void { insert 23 after data.i[0];});
check(UOE,  function() : Void { insert 23 after data.i[20];});

System.out.println("Pass count: {data.pass}");
if(data.fail > 0){ throw new Exception("Test failed"); }
