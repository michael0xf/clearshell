package com.mtk.map;


/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */

public class Nodes extends UOITable {
    public Named named;
    public Nodes(int size, Named named){
        super(size);
        this.named = named;
    }
}
