package com.mtk.map.i;

import com.mtk.map.Array;
import com.mtk.map.Key;
import com.mtk.map.Link;
import com.mtk.map.Tag;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public interface ISrc {

    public Item getAnyItem(Key key);
    public Item getLocalItem(Key key);
    public Item getGlobalItem(Key key);

    public Item getOrNewAttrByName(String name);
    public Tag getOrNewTagByName(String name);

    public Item getAttrByName(String name);
    public Tag getTagByName(String name);
    public Item getItemByName(String name);
    public Array getKeysByName(String name);
    public String getStringByName(String name);
    public int getIntByName(String name);
    public Object getObjectByName(String name, Class type);
    public Object getObjectByName(String name);
    public boolean equalsByName(String name, Object value, boolean orNew);
    public String getLocalStringByName(String name);
    public int getLocalIntByName(String name);
    public Object getLocalObjectByName(String name, Class type);
    public Object getLocalObjectByName(String name);


    public Object getOrNewObjectByName(String name, Class type);
    public Object getOrNewObjectByName(String name);
    public int getOrNewIntByName(String name);
    public String getOrNewStringByName(String name);
    public Item getOrNewItemByName(String name);

    public Object getByLink(String link, Class type);
    public Object getByLink(String link);
    public int getIntByLink(String link);
    public String getStringByLink(String link);
    public Item getItemByLink(String link);

    public Object getByLink(Link link, Class type);
    public Object getByLink(Link link);
    public int getIntByLink(Link link);
    public String getStringByLink(Link link);
    public Item getItemByLink(Link link);

    public Object getOrNewByLink(String link, Class type);
    public Object getOrNewByLink(String link);
    public int getOrNewIntByLink(String link);
    public String getOrNewStringByLink(String link);
    public Item getOrNewVarByLink(String link);
    public Tag getOrNewTagByLink(String link);
    public Tag getTagByLink(String link);

    public Object getOrNewByLink(Link link, Class type);
    public Object getOrNewByLink(Link link);
    public int getOrNewIntByLink(Link link);
    public String getOrNewStringByLink(Link link);
    public Item getOrNewVarByLink(Link link);
    public Tag getOrNewTagByLink(Link link);
    public Tag getTagByLink(Link link);


}
