package com.mtk.map.i;

import com.mtk.map.Key;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public interface IFactory {
    public ICalculator getCalculator(Key name);
    public boolean isIndexed(Key className);
}
