/* regression test for JFXC-933
 *
 * @test
 * @run
 */

import java.lang.System;
import java.util.*;

import javafx.util.StringLocalizer;

// save the default locale for testing
var curLoc = Locale.getDefault();

try {
    // set the default locale to Japan
    Locale.setDefault(Locale.JAPAN);

    // Japan locale tests
    var ja = new StringLocalizer();
    System.out.println(ja.localizedString);
    ja = StringLocalizer{ };
    System.out.println(ja.localizedString);
    ja = StringLocalizer{ defaultString: "defaultString" };
    System.out.println(ja.localizedString);
    ja = StringLocalizer{ key: "EXISTENT" defaultString: "defaultString" };
    System.out.println(ja.localizedString);
    ja = StringLocalizer{ key: "NON_EXISTENT" defaultString: "defaultString" };
    System.out.println(ja.localizedString);


    // English locale tests
    Locale.setDefault(Locale.ENGLISH);
    var en = new StringLocalizer();
    System.out.println(en.localizedString);
    en = StringLocalizer{ defaultString: "defaultString" };
    System.out.println(en.localizedString);
    en = StringLocalizer{ key: "NON_EXISTENT" 
                          defaultString: "defaultString" };
    System.out.println(en.localizedString);


} finally {
    // restore the default locale
    Locale.setDefault(curLoc);
}
