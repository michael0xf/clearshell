package mtk.util;

import java.io.InputStream;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */

public class XParserImpl implements XParser
{
    public Runnable getParser()
    {
        return QQDParser;
    }
    QQDParser QQDParser;
    public void parse(InputStream is, DocHandler dh, MtkThreads.MtkThread mtkThread)
    {
        QQDParser = null;
        BIS0 bis;
        try {
            try {
                QQDParser = new QQDParser();
                if (is instanceof BIS0)
                {
                    bis = (BIS0)is;
                    QQDParser.parse(dh, bis, mtkThread);
                }else
                {
                    bis = new BIS(is, is.available());
                    QQDParser.parse(dh, bis, mtkThread);
                }
            } catch (Throwable err) {
                Log.error(err, "exception in SAXparser init[" + err + "]");

            }
        } catch (Exception e) {
            Log.error(e, "exception in SAXparser init[" + e + "]");
        }finally
        {

        }
    }
}
