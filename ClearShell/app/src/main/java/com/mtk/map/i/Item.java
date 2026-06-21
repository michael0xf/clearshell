package com.mtk.map.i;

import com.mtk.map.*;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public interface Item extends IValue, IPointer, Named, ISrc, IStorage {

    public boolean isLocal();

    public void getChildrenByName(Object name, Selector sel);
    public void getAllChildrenByName(Object name, Selector sel);

    public String getDebugString();
    public boolean setValue(Object k);
    public Tag getTag();
    public void remove();
    public boolean isDeleted();

    public String getOrNewString();
    public int getOrNewInt();
    public Object getOrNewObject(Class c);
    public Object getOrNewObject();
    public Object getObject();
    public String getString();
    public int getInt();
    public Object getObject(Class c);

    public boolean isInternalFor(Tag tag);
    public Item getEndPointerByValue();
    public Tag getEndTagByValue();
    public Tag getOrNewEndTagByValue();


    public Item getOrNewPointerByValue();


}
