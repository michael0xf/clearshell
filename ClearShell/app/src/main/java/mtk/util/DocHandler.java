package mtk.util;

import com.mtk.map.Bytes;
import com.mtk.map.UTable;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public interface DocHandler {
  public void startElement(Bytes tag, UTable u);
  public void endElement(Bytes tag);
  public void startDocument();
  public void endDocument();
  public void text(Bytes str);
  XParserImpl getXParserImpl();
}
