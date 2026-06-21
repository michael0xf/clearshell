package mtk.util;

import java.io.InputStream;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */

public interface IFIS {
    public String getFileName();
    public InputStream getInputStream();
}
