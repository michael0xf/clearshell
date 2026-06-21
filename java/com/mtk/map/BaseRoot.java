
package com.mtk.map;

import com.mtk.map.i.IFactory;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public class BaseRoot extends Tag{
    UniqueArray dict = new UniqueArray(32);
    IFactory factory;
    @Override
    public UniqueArray getDict(){
        return dict;
    }
    public String getNodeName(){
        return this.getAttrByName("nodeName").getString();
    }

    public BaseRoot(String nodeName, IFactory factory){
        this.factory = factory;
        leaf = new Leaf(this);
        leaf.addElement(this);
        init("root", this, false, this, false, new String[]{"nodeName", nodeName}, null);
    }

    public KeyDict getOrNewKeyDict(Key key) {
        UniqueArray dict = getDict();
        KeyDict keyDict = (KeyDict) dict.get(key.hashCode());
        if (keyDict == null) {
            keyDict = new KeyDict(key, factory.isIndexed(key));
            keyDict.setFactory(factory);
            dict.addElement(keyDict);
        }
        return keyDict;
    }
    public KeyDict getKeyDict(Key key) {
        return (KeyDict) dict.get(key.hashCode());
    }


}
