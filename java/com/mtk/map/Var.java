
package com.mtk.map;

import com.mtk.map.i.Item;
import com.mtk.map.i.Selector;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public class Var extends Cursor implements Item {

    Item attr;
    Key name;
    Object value;

    @Override
    public Item getLocalItem(Key key) {
        if (key.equals(getName()))
            return this;
        else
            return super.getLocalItem(key);
    }


    @Override
    public boolean isLocal(){
        return true;
    }



    @Override
    public int sortCode(){
        return getAddress();
    }

    public String toString(){
        return getDebugString();/*
        if (value != null){
            return name.toString() + "=" + value.toString();
        }
        return name.toString() + "=";*/
    }

    @Override
    public void getChildrenByName(Object name, Selector sel) {
        getNode().getChildrenByName(name, sel);
    }

    @Override
    public void getAllChildrenByName(Object name, Selector sel) {
        getNode().getAllChildrenByName(name, sel);
    }

    public String getStorageString(Cursor cursor){
        Key name = getName();
        if (name == null)
            return null;
        if (value instanceof Item){
            return null;
        }else{
            return name.toString() + "=" + getBaseRoot().getOrNewKeyDict(name).getValueStorageString(this);
        }
    }

    public String getDebugString(){
        Key name = getName();
        String node;
        if (attr != null){
            node = " node: " + attr.getTag().getName();
        }else{
            node = "";
        }
        if (name == null){
            try{
                return "" + name + "" + value + "" + node;
            }catch (NullPointerException e){
                return "Var: name == null";
            }
        }
        if (value == null)
            return name.toString() + "=null" + node;

        if (value instanceof Item){
            Item vv  = (Item)value;
            if (vv.getName().equals(name)){
                String s = vv.getString();
                if ((s == null) || (s.length() == 0)) {
                    return name.toString() + "=#" + Integer.toHexString(vv.getCursor().getAddress()) + node ;
                } else
                    return name.toString() + "=#" + Integer.toHexString(vv.getCursor().getAddress()) + "\"(" + s + ")"  + node ;

            }else {
                String s = vv.getString();
                if ((s == null) || (s.length() == 0)) {
                    return name.toString() + "=" + vv.getName().toString() + "#" + Integer.toHexString(vv.getCursor().getAddress())  + node ;
                } else
                    return name.toString() + "=" + vv.getName().toString() + "#" + Integer.toHexString(vv.getCursor().getAddress()) + "\"(" + s + ")"  + node ;
            }
        }else if (value instanceof Link){
            Item sub = this.getEndPointerByValue();//
            if ( sub == null){
                return name.toString() + "=\"" + value.toString()+ "\"(null)"  + node ;
            }else {
                String s = sub.getString();
                if ((s == null)||(s.length() == 0)){
                    return name.toString() + "=\"" + value.toString() + "\"(" + sub.getName().toString() + "#" + Integer.toHexString(sub.getCursor().getAddress()) + ")"  + node ;
                }else
                    return name.toString() + "=\"" + value.toString() + "\"(" + s + ")"  + node ;
            }
        }
        return name.toString() + "=\"" + value.toString() + "\""  + node ;
    }

    @Override
    public Item getOrNewPointerByValue() {
        return MapUtil.getEndItemByValue(this, true);
    }

    @Override
    public String getOrNewString(){
        Object o = getOrNewObject();
        if (o != null)
            return MapUtil.getStringFromText(o.toString());
        else
            return "";
    }

    @Override
    public int getOrNewInt(){
        getOrNewObject();
        return getInt();
    }

    @Override
    public Object getOrNewObject(Class c){
        Object o = getOrNewObject();
        if (c.isInstance(o))
            return o;    
        else
            return null;
    }

    public Tag getNode(){
        if (attr == null){
            return null;
        }
        return attr.getTag();
    }


    public Tag getTag(){
        return getNode();
    }

      @Override
    public Object getNativeValue(){
        return value;
    }

    @Override
    public String getString() {
	    Object o = getObject();
        if (o == null)
            return "";
        return MapUtil.getStringFromText(o.toString());
    }

    @Override
    public int getInt() {
	    Object o = getObject();
        if (o == null)
            return 0;
        if (o instanceof Integer)
            return ((Integer)o).intValue();
        else
            return Integer.parseInt(o.toString());

    }

    @Override
    public Object getObject() {
        if (value instanceof Item)
            return ((Item) value).getObject();
        else  if (value instanceof Link) {
            Item var = MapUtil.getEndItemByValue(this, false);
            if (var != null)
                return var.getNativeValue();
            else
                return null;
        }else
            return value;
    }

    @Override
    public Object getObject(Class c) {
        Object ret = getObject();
        if (c.isInstance(ret))
            return ret;
        else
            return null;
    }

    void init(Tag node, Object n, Object v, Leaf leaf, int index){
        super.init(leaf, index);
        name = Key.getOrNew(n);
        value = MapUtil.checkLink(v);
        if (node != null){
            attr = node.addAttribute(n, this);
        }else{
            attr = null;
        }
    }


    public Var(Tag node, Object n, Object v, Cursor p, boolean isRightFromPointer){
        if (isRightFromPointer){
            init(node, n, v, p.leaf, p.index + 1);
        }else
            init(node, n, v, p.leaf, p.index);
    }
    public Var(Tag node, Object n, Object v, Leaf leaf, int index){
        init(node, n, v, leaf, index);
    }


    @Override
    public Object getOrNewObject(){

        if (value == null){
            MapUtil.run(this);
            return getObject();
        }
        Item v = MapUtil.getPointer(this, true);
        if (v == this)
            return value;
        else
            return v.getOrNewObject();

    }

    @Override
    public boolean setValue(Object o) {
        if (isDeleted())
            return false;
        if (o != value) {
            Tag node = getNode();
            if (node != null){
                KeyDict t = getBaseRoot().getOrNewKeyDict(getName());
                t.preChange(this);
                value = MapUtil.checkLink(o);
                t.postChange(this);
            }else
                value = MapUtil.checkLink(o);
            return true;
        }
        return false;
    }


    public Key getName(){
        return name;
    }

    public void remove(){
        KeyDict t = getBaseRoot().getOrNewKeyDict(getName());
        t.removeFromValues(this);
        if (!attr.isDeleted()){
            attr.remove();
        }
        super.remove();

    }


    @Override
    public Cursor getCursor() {
        return this; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Item getEndPointerByValue() {
        return MapUtil.getPointer(this, false);
    }

    @Override
    public Tag getEndTagByValue() {
        Item ret = getEndPointerByValue();
        if (ret instanceof Tag)
            return (Tag)ret;
        else
            return ret.getTag();
    }

    @Override
    public Tag getOrNewEndTagByValue() {

        Item ret = getOrNewPointerByValue();
        if (ret instanceof Tag)
            return (Tag)ret;
        else
            return ret.getTag();

    }


    @Override
    public Item getAttrByName(String name) {
        if (getName().equals("name")){
            return this;
        }
        return getNode().getAttrByName(name);
    }

    @Override
    public String getLocalStringByName(String name) {
        if (getName().equals("name")){
            return this.getString();
        }
        return getNode().getLocalStringByName(name);
    }

    @Override
    public int getLocalIntByName(String name) {
        if (getName().equals("name")){
            return this.getInt();
        }
        return getNode().getLocalIntByName(name);
    }

    @Override
    public Object getLocalObjectByName(String name, Class type) {
        if (getName().equals("name")){
            return this.getObject(type);
        }
        return getNode().getLocalObjectByName(name, type);
    }

    @Override
    public Object getLocalObjectByName(String name) {
        if (getName().equals("name")){
            return this.getObject();
        }
        return getNode().getLocalObjectByName(name);
    }


    public Item getItem(){
        return this;
    }

    @Override
    public Item getItemByLink(String link){
        if (link.length() == 0)
            return null;
        Item var = MapUtil.getEndItemByValue(this, link, false);
        if (var != null)
            return var;
        else
            return null;
    }
}
