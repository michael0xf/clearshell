package mtk.util;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */

public interface HttpConnection {
    int POST = 0;
    int GET = 1;
    int HTTP_OK = 200;

    int getResponseCode();

    void close();

    void setRequestProperty(String s, String s1);

    void setRequestMethod(int post);

    OutputStream openOutputStream();

    String getHeaderField(String location);

    InputStream openInputStream();

    int getLength();
}
