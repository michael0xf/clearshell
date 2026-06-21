package com.mtk.map.i;

import com.mtk.map.IPointer;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public interface IValue extends IPointer {
    public Object getNativeValue();
    public Item getItem();
}
