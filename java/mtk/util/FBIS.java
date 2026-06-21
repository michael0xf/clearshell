package mtk.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */

public class FBIS extends BIS implements IFIS {

    File fsrc;
    public FBIS(File fsrc, FileInputStream fi) throws IOException {
        super(fi, (int)fi.getChannel().size());
        this.fsrc = fsrc;
    }
    @Override
    public String getFileName() {
        return fsrc.getName();
    }

    @Override
    public InputStream getInputStream() {
        return is;
    }
}
