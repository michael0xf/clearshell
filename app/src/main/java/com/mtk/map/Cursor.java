
package com.mtk.map;

import com.mtk.map.i.*;

import java.util.Properties;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public class Cursor implements LiveVector.Element, Const, ISrc, IPointer{


    public int sortCode() {
        return getAddress();
    }

    int index = 0;
    Leaf leaf;


    @Override
    public boolean equalsByName(String name, Object value, boolean orNew){
        Object o;
        if (orNew)
            o = getOrNewObjectByName(name);
        else
            o = getObjectByName(name);
        if (o == null) {
            return value == null;
        }else{
            return o.equals(value);
        }
    }



    @Override
    public Tag getTagByLink(String s) {
        if (s.length() == 0)
            return null;
        return getTagByLink(new Link(s));
    }

    @Override
    public Tag getOrNewTagByLink(String s){
        if (s.length() == 0)
            return null;
        Link l = new Link(s);
        return getOrNewTagByLink(l);
    }

    @Override
    public Object getByLink(String name){
        if (name.length() == 0)
            return null;
        return getByLink(new Link(name));
    }

    @Override
    public Object getOrNewByLink(String name){
        if (name.length() == 0)
            return null;
        return getOrNewByLink(new Link(name));
    }


    @Override
    public int getIntByLink(String name){
        if (name.length() == 0)
            return 0;
        return getIntByLink(new Link(name));
    }

    @Override
    public int getOrNewIntByLink(String name){
        if (name.length() == 0)
            return 0;
        return getOrNewIntByLink(new Link(name));
    }



    @Override
    public String getStringByLink(String name){
        if (name.length() == 0)
            return "";
        return getStringByLink(new Link(name));
    }

    @Override
    public String getOrNewStringByLink(String name){
        if (name.length() == 0)
            return "";
        return getOrNewStringByLink(new Link(name));
    }


    @Override
    public Item getItemByLink(String s){
        if (s.length() == 0)
            return null;
        return getItemByLink(new Link(s));
    }


    @Override
    public Item getOrNewVarByLink(String link){
        if (link.length() == 0)
            return null;
        return getOrNewVarByLink(new Link(link));
    }

    public UniqueArray getDict(){
        if (leaf != null)
            return leaf.getDict();
        else
            return null;
    }
    public String toString(){
        return Integer.toString(getAddress());
    }
    
    public Cursor(BaseRoot baseRoot){
        init(baseRoot.leaf, baseRoot.index + 1);
    }


    public Cursor(){
    }

    public Cursor(Leaf leaf, int at) {
        init(leaf, at);
    }

    public Cursor(Cursor ptr) {
        init(ptr.leaf, ptr.index + 1);
    }
    
    public void init(Leaf leaf, int at) {
        leaf.insertElementAt(this, at);
    }

    @Override
    public void remove(){
        if (leaf != null)
            leaf.removeElementAt(index);
    }

    @Override
    public void remove(IVector liveVector){
        if (liveVector == leaf){
            leaf = null;
            index = 0;
        }
    }

    @Override
    public void setIndex(IVector v, int val){
        this.leaf = (Leaf)v;
        this.index = val;
    }

    public int getIndex(){
        return index;
    }


    public Leaf getLeaf(){
        return leaf;
    }


    public boolean isInternalFor(Tag tag){
        int address = getAddress();
        return ((tag.getAddress() <= address) && (tag.getTail().getAddress() >= address));
    }

    public int getAddress(){
        if (leaf != null)
            return leaf.getAddress(getIndex());
        else
            return -1;
    }

    int getNextAddress(){
        Leaf leaf = getLeaf();
        if (leaf == null)
            return NIL;
            int i = index + 1;
            if (i < leaf.size())
                return leaf.getAddress(i);
            else{
                Leaf next = leaf.getNext();
                if (next != null){
                    return next.getAddress(0);
                }
            }
            return NIL;
    }
    int getPrevAddress(){
        Leaf leaf = getLeaf();
        if (leaf == null)
            return NIL;
            int i = index - 1;
            if (i >= 0)
                return leaf.getAddress(i);
            else{
                Leaf prev = leaf.getPrev();
                if (prev != null){
                    if (prev.size() > 0)
                        return prev.getAddress(prev.size() - 1);
                }
            }
            return NIL;
    }

    
    public  Object getNext(){
        Leaf leaf = getLeaf();
        if (leaf == null)
            return null;
            int i = index + 1;
            if (i < leaf.size())
                return leaf.elementAt(i);
            else{
                Leaf next = leaf.getNext();
                if (next != null)
                    return next.getFirstElement();
            }
            return null;

    }

    public void fillProperties(Properties properties, String... args){
        for(String s: args){
            String v = getStringByName(s);
            if (v != null)
                properties.put(s, v);
        }
    }

    public  Object getPrev(){
        Leaf leaf = getLeaf();
        if (leaf == null)
            return null;
            int i = index - 1;
            if (i >= 0)
                return leaf.elementAt(i);
            else{
                Leaf prev = leaf.getPrev();
                if (prev != null)
                    return prev.getLastElement();
            }
            return null;
    }

    
    public Leaf getNextObjectLeaf(){
        if (leaf == null)
            return null;
        if (index == leaf.luxuriance())
            return leaf.getNext();
        else
            return leaf;
    }

    public Leaf getPrevObjectLeaf(){
        if (leaf == null)
            return null;
        if (index == leaf.luxuriance())
            return leaf.getNext();
        else
            return leaf;
    }
    
    
    public StringBuilder getStrUpToPtr(Cursor to){
        if ((leaf == null)||(to.leaf == null))
            return null;
        if (to.getAddress() < getAddress()){
            return to.getStrUpToPtr(this );
        }
        int upToIndex = to.index; 
        Leaf upToLeaf = to.leaf;
        if (upToIndex == 0){
            upToLeaf = upToLeaf.getPrev();
            if (upToLeaf == null)
               return null;
            upToIndex = upToLeaf.size() - 1;
        }else{
            upToIndex--;
        }
        StringBuilder builder = new StringBuilder();
        Cursor ptr = new Cursor(leaf, index + 1);
        while((ptr.index != upToIndex)&&(ptr.leaf != upToLeaf)){
            Object o = ptr.getNext();
            if (o instanceof Bytes){
                builder.append(((Bytes)o).toString());
            }else
                builder.append(o);
            if (!ptr.next())
                break;
        }
        return builder;
    }

    public boolean isAgainst(Cursor ptr){
        if ((leaf == null)||(ptr.leaf == null))
            return true;
        return (ptr.getPrevAddress() == getAddress())||(getPrevAddress() == ptr.getAddress());
    }
    

    public boolean isPrevFor(Cursor ptr){
        if ((leaf == null)||(ptr.leaf == null))
            return false;
        return (ptr.getPrevAddress() == getAddress());
    }

    public boolean isNextFor(Cursor ptr){
        if ((leaf == null)||(ptr.leaf == null))
            return false;
        return (getPrevAddress() == ptr.getAddress());
    }
    
    
    public boolean next(){
        final Leaf leaf = getLeaf();
        if (leaf == null)
            return false;
            int index = this.index;
            if (index < leaf.size() - 1 )
                leaf.reverse(index, index + 1);
            else{
                Leaf next = leaf.getNext();
                while (next != null) {
                    if(next.size() == 0) {
                        Leaf toRemove = next;
                        next = next.getNext();
                        toRemove.remove();
                    }else
                        break;
                }
                if (next == null)
                    return false;
                Object o = next.elementAt(0);
                leaf.setElementAt(o, index);
                next.setElementAt(this, 0);
            }
        return  true;
    }

    public boolean prev(){
        final Leaf leaf = getLeaf();
        if (leaf == null)
            return false;
            int index = this.index;
            if (index > 0 )
                leaf.reverse(index, index - 1);
            else{
                Leaf prev = leaf.getPrev();
                while (prev != null) {
                    if(prev.size() == 0) {
                        Leaf toRemove = prev;
                        prev = prev.getPrev();
                        toRemove.remove();
                    }else
                        break;
                }
                if (prev == null)
                    return false;
                int i = prev.size() - 1;
                Object o = prev.elementAt(i);
                leaf.setElementAt(o, 0);
                prev.setElementAt(this, i);
            }
        return true;
    }
//Leaf b, int newIndex

    
    public boolean run(String name){

        Item let = getItemByLink(name);
        if (let == null){
            return false;
        }
        //IVar var = let.getPointerByValue();
        //goTo(var.getCursor());
        //let.getContainer().getTag().addAttribute("anchor", this);
        Tag tag = let.getEndTagByValue();
        return MapUtil.run(tag);
    }
/*
    */
    
    public final static Key CALLER = Key.getOrNew("caller");

    @Override
    public Cursor getCursor() {
        return this;
    }

    public static interface Runner{
        public void run(Object obj, Cursor p);
        public void end(Cursor p);
    }

    @Override
    public boolean isDeleted(){
        return false;//leaf != null;
    }

    @Override
    public Object getByLink(String name, Class type){
        if (name.length() == 0)
            return null;
        return getByLink(new Link(name), type);
    }

    @Override
    public Object getOrNewByLink(String name, Class type){
        if (name.length() == 0)
            return null;
        return getOrNewByLink(new Link(name), type);
    }


    @Override
    public Object getByLink(Link name, Class type){
        Item var = MapUtil.getEndItemByValue(this, name, false);
        if (var != null)
            return var.getObject(type);
        else
            return null;
    }

    @Override
    public Object getOrNewByLink(Link name, Class type){
        Item var = MapUtil.getEndItemByValue(this, name, true);
        if (var != null)
            return var.getOrNewObject(type);
        else
            return null;
    }

    public  void goTo(String to){
        Item p = this.getItemByName(to);
        if (p != null)
            goTo(p.getCursor());
   }
    public void goToAt(Cursor to){
        if ( to == this)
            return;
        if (to != null) {
            if (to.isNextFor(this))
                return;
            Leaf l = to.leaf;
            if (l == null)
                return;
            if (to.isDeleted())
                return;
            remove();

            l.insertElementAt(this, to.index);
        }

    }

    public  void goTo(Cursor to){
        if ( to == this)
            return;
        if (to != null){
            if (to.isPrevFor(this))
                return;
            Leaf l = to.leaf;
            if (l == null)
                return;
            if (to.isDeleted())
                return;
            remove();
            
            l.insertElementAt(this, to.index + 1);
        }
            
    }
    

    public Item getAnyLetForValue(String s) {
        return getAnyItem(Key.getOrNew(s));

    }

    @Override
    public Item getAnyItem(Key key) {
        Item ret = getLocalItem(key);
        if (ret == null)
            return getGlobalItem(key);
        else
            return ret;
    }

    @Override
    public Item getLocalItem(Key key) {
        return null;
    }

    @Override
    public BaseRoot getBaseRoot(){
        return leaf.getRoot();
    }

    @Override
    public Item getGlobalItem(Key key) {

        if (key == null)
            return null;
        if (leaf == null)
            return null;
        KeyDict t = getBaseRoot().getOrNewKeyDict(key);
        if (t != null){
            ILet ret = t.getLet(leaf, index);
            if (ret != null)
                return ret.getItem();
            else
                return null;
        }else {
            return null;
        }
    }


    public ILet getGlobalLet(Key k) {

        if (k == null)
            return null;
        if (leaf == null)
            return null;
        KeyDict t = getBaseRoot().getOrNewKeyDict(k);
        if (t != null) {
            return t.getLet(leaf, index);
        }
        return null;
    }

/*    public ILet getBaseLetForAddress(Key key) {
        if (key == null)
            return null;
        if (leaf == null)
            return null;
        KeyDict t = getBaseRoot().getOrNewKeyDict(key);
        return t.getBaseLetForValue(leaf, index);
    }*/


    public Item getLetForAddress(Key key) {
        
        if (key == null)
            return null;
        if (leaf == null)
            return null;
        KeyDict t = getBaseRoot().getOrNewKeyDict(key);
        ILet ret = t.getLetForAddress(leaf, index);
        if (ret != null)
            return ret.getItem();
        return null;
    }

    public ILet getBaseLetForAddress(Key key) {
        if (key == null)
            return null;
        if (leaf == null)
            return null;
        KeyDict t = getBaseRoot().getOrNewKeyDict(key);
        return t.getLetForAddress(leaf, index);
    }


    public Item addVar(Key key, Object o){
         Item node = getOrNewItemByName(key.toString());
          return new Var(node.getTag(), key.o, o, leaf, index + 1);
                
    }

    public int addPrev(Object obj){
        int index = this.index;
        leaf.insertElementAt(obj, index);
       // push1(obj);
       // setCurrentsAsChanged();
        return index;
    }


    public int addNext(Object obj){
        int index = this.index;
        leaf.insertElementAt(obj, index + 1);
        return index;
    }

    public  Object removeNext(){
        Leaf leaf = getLeaf();
        if (leaf == null)
            return null;
            int i = index + 1;
            if (i < leaf.size()){
       //         setCurrentsAsChanged();
                Object o = leaf.elementAt(i);
                if (o instanceof IPointer){
                    ((IPointer) o).getCursor().remove();
                    return o;
                }else
                    return leaf.removeElementAt(i);
            }else{
                Leaf next = leaf.getNext();
                if (next != null){
                    Object o = next.firstElement();
                    if (o instanceof IPointer){
                        ((IPointer) o).getCursor().remove();
                        return o;
                    }else
                        return next.removeFirstElement();
                }
            }
            return null;

    }

    public  Object removePrev(){
        Leaf leaf = getLeaf();
        if (leaf == null)
            return null;
            int i = index - 1;
            if (i >= 0){
           //     setCurrentsAsChanged();
                return leaf.removeElementAt(i);
            }else{
                Leaf prev = leaf.getPrev();
                if (prev != null){
             //       setCurrentsAsChanged();
                    return prev.removeLastElement();
                }
            }
            return null;

    }
    
    public boolean is(String name){
        return this.getAnyItem(Key.getOrNew(name)) != null;
    }
    public boolean is(Key name){
        return this.getAnyItem(name) != null;
    }

    public final static Key PROPERTIES = Key.getOrNew("Properties");
    public final static Key HTML = Key.getOrNew("HTML");
    public final static Key JSON = Key.getOrNew("JSON");
    public boolean isHtmlStyle(){
        return is(HTML);
    }
    public boolean isJSON(){
        return is(JSON);
    }
    public boolean isProperties(){
        return is(PROPERTIES);
    }

    public Item newAttr(Key name){
        return leaf.getRoot().addAttribute(name, null);
    }

    @Override
    public Item getOrNewItemByName(String name) {
        Key k = Key.getOrNew(name);
        Item v = getAnyItem(k);
        if (v == null)
            return newAttr(k);
        else
            return v.getOrNewPointerByValue();
    }

    public Object newObject(Key name) {
        Item var = newAttr(name);
        return var.getOrNewObject();
    }

    public void set(String s, Object o) {
        Key name = Key.getOrNew(s);
        Item let = getAnyItem(name);
        if(let == null) {
            newAttr(name).setValue(o);
        }else
            let.setValue(o);
    }


    @Override
    public String getOrNewStringByName(String name) {
        Item v = getOrNewItemByName(name);
        if (v == null)
            return "";
        else
            return v.getOrNewString();
    }




    @Override
    public int getIntByName(String s) {
        Item v = getItemByName(s);
        if (v == null)
            return 0;
        return v.getInt();
    }

    @Override
    public int getOrNewIntByName(String s) {
        Item v = this.getOrNewItemByName(s);
        return v.getOrNewInt();
    }
    public static Key ROOT = Key.getOrNew("root");

    @Override
    public String getStringByName(String name) {
        Item let = getAnyLetForValue(name);
        if (let != null)
            return let.getString();
        else
            return "";
    }


    @Override
    public Object getObjectByName(String name, Class c) {

        Item let = getAnyLetForValue(name);
        if (let != null)
            return let.getObject(c);
        else
            return null;
    }

    @Override
    public Object getObjectByName(String name) {
        Item v = this.getItemByName(name);
        if (v != null)
            return v.getObject();
        else
            return null;

    }
    @Override
    public Object getOrNewObjectByName(String name, Class c){
        return this.getOrNewItemByName(name).getOrNewObject(c);
    }

    @Override
    public Object getOrNewObjectByName(String name) {
        return this.getOrNewItemByName(name).getOrNewObject();
    }



    @Override
    public Tag getTagByName(String k){
        Item let = getItemByName(k);
        if (let != null){
            if (let instanceof Tag)
                return (Tag)let;
        }
        return null;
    }

    @Override
    public Tag getOrNewTagByName(String name){
        Item let = getAnyItem(Key.getOrNew(name));
        if (let != null){
            if (let instanceof Tag)
                return (Tag)let;
        }
        return new Tag(name, this, false);
    }

    @Override
    public Item getAttrByName(String name) {
        Item item = getItemByName(name);
        return item.getAttrByName(name);
    }


    @Override
    public String getLocalStringByName(String name) {
        Item item = getItemByName(name);
        return item.getLocalStringByName(name);
    }

    @Override
    public int getLocalIntByName(String name) {
        return getItemByName(name).getLocalIntByName(name);
    }

    @Override
    public Object getLocalObjectByName(String name, Class type) {
        return getItemByName(name).getLocalObjectByName(name, type);
    }

    @Override
    public Object getLocalObjectByName(String name) {
        return getItemByName(name).getLocalObjectByName(name);
    }


    @Override
    public Item getItemByName(String name){
        Item ret = getAnyItem(Key.getOrNew(name));
        if (ret != null)
            return ret.getEndPointerByValue();
        return null;
    }

    public Item getItemByName(Key name){
        Item ret = getAnyItem(name);
        if (ret != null)
            return ret.getEndPointerByValue();
        return null;
    }


    @Override
    public Item getOrNewAttrByName(String name) {
        Item ret = getOrNewItemByName(name);
        ret.setValue(name);
        return ret;
    }
    public Array getKeysByName(String name){
        String s = getStringByName(name);
        if (s.length() > 0){
            Bytes b = new Bytes(s.getBytes(), false);
            Array a = new Array(4);
            for(Bytes w = b.removeFirstWord(','); w != null;w = b.removeFirstWord(',')){
                a.addElement(Key.getOrNew(w));
            }
            return a;
        }else
            return null;
    }

  /*  public Item getPrevTag(){
        return getBaseRoot().getOrNewKeyDict(ROOT).getPrevTag(getAddress(), null);
    }


    public Item getNextTag(){
        return getBaseRoot().getOrNewKeyDict(ROOT).getNextTag(getAddress(), null);
    }

    public Item getPrevTag(Tag tag){
        return getBaseRoot().getOrNewKeyDict(ROOT).getPrevTag(getAddress(), tag);
    }


    public Item getNextTag(Tag.Tail tail){
        return getBaseRoot().getOrNewKeyDict(ROOT).getNextTag(getAddress(), tail);
    }*/




    public Tag getNode(){
        Item root = getGlobalItem(ROOT);
        if (root != null){
            return root.getTag();
        }
        return null;

    }



    @Override
    public Tag getTagByLink(Link s) {
        Item var = MapUtil.getEndItemByValue(this, s, false);
        if (var instanceof Tag){
            return (Tag)var;
        }else
            return null;
    }

    @Override
    public Tag getOrNewTagByLink(Link l){
        Item var = MapUtil.getEndPointerByLink(this, l, false);//v2
        if (var.getName().equals(((Link.TLink)l.lastElement()).key)) {
            if (var instanceof Tag)
                return (Tag) var;
            else {
                Object o = var.getNativeValue();
                Tag ret = new Tag(((Link.TLink)l.lastElement()).key, var.getCursor(), true, var.getCursor(), false);
                ret.setValue(o);
                var.remove();
            }
        }
        Tag ret = new Tag(((Link.TLink)l.lastElement()).key, var.getCursor(), true, var.getCursor(), false, null, null);
        return ret;
    }

    @Override
    public Object getByLink(Link name){
        Item var = MapUtil.getEndItemByValue(this, name, false);
        if (var != null)
            return var.getNativeValue();
        return null;
    }

    @Override
    public Object getOrNewByLink(Link name){
        Item var = MapUtil.getEndItemByValue(this, name, true);
        if (var != null)
            return var.getOrNewObject();
        return null;
    }


    @Override
    public int getIntByLink(Link name){
        Item var = MapUtil.getEndItemByValue(this, name, false);
        if (var != null)
            return var.getInt();
        else
            return 0;
    }

    @Override
    public int getOrNewIntByLink(Link name){
        Item var = MapUtil.getEndItemByValue(this, name, true);
        if (var != null)
            return var.getOrNewInt();
        else
            return 0;
    }



    @Override
    public String getStringByLink(Link name){
        Item var = MapUtil.getEndItemByValue(this, name, false);
        if (var != null)
            return var.getString();
        else
            return "";
    }

    @Override
    public String getOrNewStringByLink(Link name){
        Item var = MapUtil.getEndItemByValue(this, name, true);
        if (var != null)
            return var.getOrNewString();
        else
            return "";
    }


    @Override
    public Item getItemByLink(Link link){
        Item var = MapUtil.getEndItemByValue(this, link, false);
        if (var != null)
            return var;
        else
            return null;
    }


    @Override
    public Item getOrNewVarByLink(Link link){
        Item var = MapUtil.getEndItemByValue(this, link, true);
        if (var != null)
            return var;
        else
            return null;
    }

}
