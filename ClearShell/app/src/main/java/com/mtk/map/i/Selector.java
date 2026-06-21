package com.mtk.map.i;

import com.mtk.map.Tag;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public interface Selector{
    public boolean addElement(ILet let);
    public void init(int size);
    public void clear();
    public void end();
}
