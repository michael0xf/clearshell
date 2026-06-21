package com.mtk.map;

import com.mtk.map.i.IValue;
import com.mtk.map.i.Item;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public class Value extends Cursor implements IValue {
    Object o;
    void init(Object n, Leaf leaf, int index){
        super.init(leaf, index);
        o = n;
        getBaseRoot().getOrNewKeyDict(getNode().getName()).postChange(this);
    }

    @Override
    public void remove(){
       getBaseRoot().getOrNewKeyDict(getNode().getName()).removeFromValues(this);
       super.remove();
    }

    public Value(Object n, Leaf leaf, int index){
        init(n, leaf, index);
    }
    @Override
    public Object getNativeValue() {
        return o;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Item getItem() {
        return getNode();  //To change body of implemented methods use File | Settings | File Templates.
    }
}
