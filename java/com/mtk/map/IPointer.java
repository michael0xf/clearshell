package com.mtk.map;

import com.mtk.map.BaseRoot;
import com.mtk.map.Cursor;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public interface IPointer {
    public Cursor getCursor();
    public BaseRoot getBaseRoot();
    public boolean isDeleted();
}
