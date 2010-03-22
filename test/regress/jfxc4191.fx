/**
  * @test
  * @run
  */
import com.sun.tools.javafx.script.*;
public function run() {
    def context = new JavaFXScriptContext(jfxc4191.class.getClassLoader());
    def compilerField = context.getClass().getDeclaredField("compiler");
    compilerField.setAccessible(true);
    def compiler = compilerField.get(context) as JavaFXScriptCompiler;
    def script1 = compiler.compile('Script1', 'def xyz = 23', null, null, null, null);
    script1.eval(context);
    def script2 = compiler.compile('Script2', 'function xyzAdd(x:Integer):Integer \{x+xyz\}', null, null, null, null);
    script2.eval(context);
    def script3 = compiler.compile('Script3', 'print("xyz is "); println(xyzAdd(xyz+2))', null, null, null, null);
    script3.eval(context);
}