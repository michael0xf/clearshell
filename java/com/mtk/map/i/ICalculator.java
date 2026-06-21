package com.mtk.map.i;

import com.mtk.map.Key;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public interface ICalculator {
    public boolean calculate(Item item);
    public String getStorageStringForValue(Item item);
}
