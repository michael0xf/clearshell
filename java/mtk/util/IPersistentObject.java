/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mtk.util;
import java.util.*;
/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */

public interface IPersistentObject {
    public void clear();
    public void commit();
    public Object getSynchObject();
    public void removeString(String key);
    public void removeBytes(String key);
    public void removeAllBytes();
    public byte[] getBytes(String key);
    public int[] getMem(String key);
    public void setMem(String key, int[] val);
    
    //public byte[] getBytes(String key);
    public String getString(String key);
    public void setBytes(String key, byte[] val);
    public void setString(String key, String val);
    public int size();
    public Enumeration keys();
    
}
