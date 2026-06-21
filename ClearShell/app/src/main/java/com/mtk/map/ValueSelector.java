package com.mtk.map;

import com.mtk.map.i.IValue;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public interface ValueSelector {
        public boolean addElement(IValue value);
        public void clear();
}
