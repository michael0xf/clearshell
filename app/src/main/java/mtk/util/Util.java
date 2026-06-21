package mtk.util;

import com.mtk.map.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */

public class Util {
        public static BIS openGlobalFile(String path)
        {

            String name = path;
            if (name != null){
                try {
                    File fsrc = new File(name);

                    if (fsrc.isFile()&&(fsrc.canRead())){
                        FileInputStream fi = new FileInputStream(fsrc);
                        BIS is = new FBIS(fsrc, fi);
                        return is;
                    }
                } catch (FileNotFoundException e) {
                    Log.error(e);
                } catch (IOException ex) {
                    Log.error(ex);
                }

            }else{
                Log.error("fileName not found");
            }
            return null;
        }

}
