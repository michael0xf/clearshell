package mtk.util;

import com.mtk.map.UniqueArray;

import java.util.Enumeration;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */

public class Finalizer {
    static UniqueArray finalizers;

    public static UniqueArray getFinalizers() {
        if (finalizers == null)
            finalizers = new UniqueArray(32);
        return finalizers;
    }
    public static void closeAll(){

        if (finalizers != null) {
            Enumeration ee = finalizers.elements();
            while (ee.hasMoreElements()) {
                try {
                    IFinalize c = (IFinalize) ee.nextElement();
                    if (c != null)
                        c.close();
                } catch (Exception eee) {
                    Log.error(eee, "1434505");
                }
            }
        }
    }

}
