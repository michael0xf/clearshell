package mtk.util;

import java.io.InputStream;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */

public interface XParser extends MainParser
{
    void parse(InputStream is, DocHandler dh, MtkThreads.MtkThread mtkThread);
}
