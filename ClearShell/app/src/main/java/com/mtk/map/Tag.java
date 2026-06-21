
package com.mtk.map;

import com.mtk.map.i.*;

import java.util.Properties;

import static com.mtk.map.MapUtil.checkLink;
//import static com.mtk.map.MapUtil.subRun;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public class Tag extends Cursor implements ILet, Item, Selector, Indent {
    public Item getItem(){
        return this;
    }


    @Override
    public boolean isLocal(){
        return false;
    }

    @Override
    public boolean isHead() {
        return true;
    }


    public void copy(Tag src, Tag dst){
        Object value = src.getNativeValue();
        if (value instanceof  String) {
            dst.setValue(value);
        }else if (value instanceof  Bytes) {
            dst.setValue(value);
        }else if (value instanceof  Link) {
            dst.setValue(value);
        }
        Attributes head = src.head;
        if (head != null) {
            for (int i = 0; i < head.size(); i++) {
                Attributes.LocalAttr a = (Attributes.LocalAttr) head.elementAt(i);
                Object o = a.getNativeValue();
                if (o instanceof  String) {
                    dst.addAttribute(a.getName().toString(), o, a.isLocal());
                }else if (o instanceof  Bytes) {
                    dst.addAttribute(a.getName().toString(), o, a.isLocal());
                }else if (o instanceof  Link) {
                    dst.addAttribute(a.getName().toString(), o, a.isLocal());
                }else if (o instanceof ICalculator) {
                    dst.addAttribute(a.getName().toString(), o, a.isLocal());
                }else if (o instanceof Var) {
                    dst.addAttribute(a.getName().toString(), ((Var)o).value, a.isLocal());
                }
            }
        }
    }

    public Tag cloneTo(Cursor ptrDst){
        UTable dict = new UTable(32);
        Tag src = this;
        Cursor ptrSrc = new Cursor(src);
        Tag dst = new Tag(src.getName(), ptrDst, false);
        dict.put(src, dst);
        copy(src, dst);
        Cursor srcEnd = src.getTail();
        br:while(ptrSrc.getAddress() < srcEnd.getAddress()){
            Tag.Tail tail = src.getTail();
            while(ptrSrc.getAddress() < tail.getAddress()){
                Object o = ptrSrc.getNext();
                if (o instanceof  String) {
                    ptrDst.addPrev(o);
                }else if (o instanceof  Bytes) {
                    ptrDst.addPrev(o);
                }else if (o instanceof  Link) {
                    ptrDst.addPrev(o);
                }else if (o instanceof Var){
                    Var var  = (Var)o;
                    Tag node = var.getNode();
                    if (node == null){
                        new Var(null, var.getName(), var.getNativeValue(), ptrDst, false);
                    }else{
                        Tag dstNode = (Tag)dict.get(node);
                        if (dstNode != null){
                            new Var(dstNode, var.getName(), var.getNativeValue(), ptrDst, false);
                        }else{
                            new Var(null, var.getName(), var.getNativeValue(), ptrDst, false);
                        }
                    }
                }else if (o instanceof Tag.Tail){
                    Tag.Tail newScrTail = (Tag.Tail)o;
                    Tag newSrcTag = newScrTail.getTag();
                    Tag dstTag = (Tag)dict.get(newSrcTag);
                    if (dstTag != null){
                        dstTag.tail.goToAt(ptrDst);
                        if (newSrcTag != this){
                            dict.remove(newSrcTag);
                        }
                    }else{
                        Log.error("clone: " + tail.getName() + " not found");
                        break br;
                    }
                }else if (o instanceof Tag){
                    src = (Tag) o;
                    dst = new Tag(src.getName(), ptrDst, false);
                    dict.put(src, dst);
                    copy(src, dst);
                    Cursor newSrcEnd = src.getTail();
                    if (newSrcEnd.getAddress() > srcEnd.getAddress()){
                        srcEnd = newSrcEnd;
                    }
                }
                ptrSrc.next();
            }
        }
        ptrSrc.remove();
        return (Tag)dict.get(this);
    }


    @Override
    public String getOrNewString( ){
        Object o = getOrNewObject();
        if (o == null){
            return "";
        }
        return MapUtil.getStringFromText(o.toString());
    }
    public String toString(){

        try{
            if (name == null)
                return super.toString();
            StringBuilder ret = new StringBuilder("<" + name.toString());
            if (value != null){
                ret.append("=" + value.toString());
            }
            if (head != null){
                for(int i = 0; i < head().size(); i++){
                    Object o = head.elementAt(i);
                    String s = o.toString();
                    ret.append(" " + s);
                }
            }
            if (tail != null) {
                if (tail.isNextFor(this))
                    ret.append("/>");
                else
                    ret.append(">");
            }
            return ret.toString();
        }finally {
        }
    }

    public String getDebugString(){

        if (name == null)
            return super.toString();
        StringBuilder ret = new StringBuilder("<" + name.toString());
        if (value != null){
            ret.append("=" + value.toString());
        }
        if (head != null){
            for(int i = 0; i < head().size(); i++){
                ret.append(" " + head.elementAt(i).getItem().getDebugString());
            }
        }
        if (tail.isNextFor(this))
            ret.append("/>");
        else
            ret.append(">");
        return ret.toString();
    }



    @Override
    public boolean addElement(ILet let) {
        
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void init(int size) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void clear() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void end() {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    public String getStorageString(Cursor cursor){
        if (name == null)
            return null;
        LiveVector lv = getUpper();
        String suffix = "";
        if (lv != null) {
            int address = getAddress();
            int tailAddress = tail.getAddress();
            for (int i = lv.size() - 1; i >= 0 ; i--) {
                ILet current = (ILet) lv.elementAt(i);
                Tag begin = current.getItem().getTag();
                Tag.Tail end = begin.getTail();
                int beginAddress = begin.getAddress();
                int endAddress = end.getAddress();
                if ((beginAddress < address)&&(endAddress > address)&&(endAddress < tailAddress)){
                    KeyDict dict = getBaseRoot().getKeyDict(name);
                    suffix = ":" + dict.createSuffix(this);
                    break;
                }
            }
        }
        
        StringBuilder ret = new StringBuilder("<" + name.toString() + suffix);
        if (value != null){
            ret.append(" " + name + "=\"" + getBaseRoot().getOrNewKeyDict(name).getValueStorageString(this) + "\"");
        }
        if (head != null){
            for(int i = 0; i < head().size(); i++){
                String s = head.elementAt(i).getItem().getStorageString(cursor);
                if (s != null)
                    ret.append(" " + s);
            }
        }
        if (tail.isNextFor(this) || cursor.isNextFor(tail))
            ret.append("/>");
        else
            ret.append(">");
        return ret.toString();

    }
    
    
    public Item getOrNewVar(String s){
        return this.getOrNewVar(s);
    }
    @Override
    public boolean isTail(){
        return false;
    }

    @Override
    public int sortCode(){
        return getAddress();
    }

    @Override
    public Tag getEndTagByValue(){
        Item ret = getEndPointerByValue();
        if (ret instanceof Tag)
            return (Tag)ret;
        else
            return null;
    }

    @Override
    public Tag getOrNewEndTagByValue() {
        Item ret = getOrNewPointerByValue();
        if (ret instanceof Tag)
            return (Tag)ret;
        else
            return null;
    }

    @Override
    public Item getOrNewPointerByValue() {
        return MapUtil.getEndItemByValue(this, true);
    }


    @Override
    public int getOrNewInt( ) {
        getOrNewObject();
        return getInt();
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
    public Object getNativeValue(){
        return value;
    }

    @Override
    public String getString() {
        Object o = getObject();
        if (o != null)
            return MapUtil.getStringFromText(o.toString());
        else
            return "";
    }

    @Override
    public int getInt() {

        Object o = getOrNewObject();
        if (o == null)
            return 0;
        if (o instanceof Integer)
            return ((Integer)o).intValue();
        else
            return Integer.parseInt(o.toString());


    }

    @Override
    public Object getObject(Class c)
    {
        Object o = getObject();
        if (o != null){
            if (c.isInstance(o))
                return o;
        }
        return null;
    }

    Attributes head;
    
    public Attributes getAttributes(){
        return head();
    }

    Attributes head(){
        if (head == null)
            head = new Attributes();
        return head;
    }

    Upper upper;


    public boolean isChild(Named o){
        if (head == null)
            return false;
        Attributes.LocalAttr attr = head.getValue2(o.getName());
        return (attr != null)&&(attr.value == o);
    }


    @Override
    public Tag.Tail getTail(){
        return tail;
    }

    @Override
    public Tag getTag(){
        return this;
    }
    public boolean isInternalFor(Tag tag){
        if (tag == this)
            return false;
        return super.isInternalFor(tag);
    }
    public boolean isInternalOrOverlapFor(Tag tag){
        if (tag == this)
            return false;
        return super.isInternalFor(tag) || tail.isInternalFor(tag);
    }


    @Override
    public Item getAttrByName(String n) {
        if (isDeleted())
            return null;
        Key k = Key.getOrNew(n);
        return this.getLocalItem(k);
    }


    public Item getAttrByName(Key k) {
        if (isDeleted())
            return null;
        if (k.equals(name))
            return this;
        if (head != null){
            Attributes.LocalAttr ret = head.getValue2(k);
            if (ret != null) {
                return ret;
            }
        }
        return null;
    }

    @Override
    public String getLocalStringByName(String name) {
        Item v = getAttrByName(name);
        if (v != null)
            return v.getString();
        else
            return "";
    }

    @Override
    public int getLocalIntByName(String name) {
        Item v = getAttrByName(name);
        if (v != null)
            return v.getInt();
        else
            return 0;
    }

    @Override
    public Object getLocalObjectByName(String name, Class type) {
        Item v = getAttrByName(name);
        if (v != null)
            return v.getObject(type);
        else
            return null;
    }

    @Override
    public Object getLocalObjectByName(String name) {
        Item v = getAttrByName(name);
        if (v != null)
            return v.getObject();
        else
            return null;
    }


    @Override
    public Item getItemByName(String name){
        Item ret = getAnyItem(Key.getOrNew(name));
        if (ret != null)
            return ret.getEndPointerByValue();
        return null;
    }



    @Override
    public Item getOrNewAttrByName(String n){
        if (isDeleted())
            return null;
        Key k = Key.getOrNew(n);
        Item let = this.getLocalItem(k);
        if (let == null){
            return addAttribute(k, null);
        }else
            return let;
    }


    @Override
    public ILet getHead(){
        return this;
    }

    @Override
    public int getIndent() {
        return 1;  //To change body of implemented methods use File | Settings | File Templates.
    }


    public class Attributes extends UniqueArray {

        Attributes(){
            super(2);
        }

        public class LocalAttr implements Item {
            Key name;
            Object value;


            @Override
            public boolean isLocal(){
                return true;
            }

            @Override
            public Item getItem(){
                return this;
            }

            @Override
            public BaseRoot getBaseRoot(){
                return getCursor().getBaseRoot();
            }


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
            public Object getOrNewObjectByName(String name) {
                return Tag.this.getOrNewObjectByName(name);
            }

            @Override
            public int getOrNewIntByName(String name) {
                return Tag.this.getOrNewIntByName(name);
            }

            @Override
            public String getOrNewStringByName(String name) {
                return  Tag.this.getOrNewStringByName(name);
            }

            @Override
            public Item getOrNewItemByName(String name) {
                return Tag.this.getOrNewItemByName(name);
            }

            @Override
            public Object getByLink(String name, Class type){
                return Tag.this.getByLink(name,  type);
            }


            @Override
            public Object getByLink(String name){
                return Tag.this.getByLink(name);
            }
            @Override
            public int getIntByLink(String name){
                return Tag.this.getIntByLink(name);
            }

            @Override
            public String getStringByLink(String name){
                return Tag.this.getStringByLink(name);
            }

            @Override
            public Item getItemByLink(String name){
                return Tag.this.getItemByLink(name);
            }

            @Override
            public Object getOrNewByLink(String link, Class type) {
                return Tag.this.getOrNewByLink(link, type);
            }

            @Override
            public Object getOrNewByLink(String link) {
                return Tag.this.getOrNewByLink(link);
            }

            @Override
            public int getOrNewIntByLink(String link) {
                return Tag.this.getOrNewIntByLink(link);
            }

            @Override
            public String getOrNewStringByLink(String link) {
                return Tag.this.getOrNewStringByLink(link);
            }

            @Override
            public Item getOrNewVarByLink(String link) {
                return Tag.this.getOrNewVarByLink(link);
            }


            @Override
            public Object getByLink(Link name, Class type){
                return Tag.this.getByLink(name,  type);
            }

            @Override
            public Object getByLink(Link name){
                return Tag.this.getByLink(name);
            }
            @Override
            public int getIntByLink(Link name){
                return Tag.this.getIntByLink(name);
            }

            @Override
            public String getStringByLink(Link name){
                return Tag.this.getStringByLink(name);
            }

            @Override
            public Item getItemByLink(Link name){
                return Tag.this.getItemByLink(name);
            }

            @Override
            public Object getOrNewByLink(Link link, Class type) {
                return Tag.this.getOrNewByLink(link, type);
            }

            @Override
            public Object getOrNewByLink(Link link) {
                return Tag.this.getOrNewByLink(link);
            }

            @Override
            public int getOrNewIntByLink(Link link) {
                return Tag.this.getOrNewIntByLink(link);
            }

            @Override
            public String getOrNewStringByLink(Link link) {
                return Tag.this.getOrNewStringByLink(link);
            }

            @Override
            public Item getOrNewVarByLink(Link link) {
                return Tag.this.getOrNewVarByLink(link);
            }




            public void set(String s, Object o) {
                Key name = Key.getOrNew(s);
                KeyDict t = getBaseRoot().getOrNewKeyDict(getName());
                Item v = t.getLet(leaf, index).getItem();
                if(v == null) {
                    v = getCursor().newAttr(name);
                }
                v.setValue(o);
            }


            public String toString(){
                return getDebugString();
            }

            @Override
            public void getChildrenByName(Object name, Selector sel) {
                Tag.this.getChildrenByName(name, sel);
            }
            @Override
            public void getAllChildrenByName(Object name, Selector sel) {
                Tag.this.getAllChildrenByName(name, sel);
            }

            public String getDebugString(){
                try{
                    if (name == null)
                        return super.toString();
                    if (value == null)
                        return name.toString() + "=null";
                    if (value instanceof Item){
                        Item vv  = (Item)value;
                        if (vv.getName().equals(name)){
                            String s = vv.getString();
                            if ((s == null) || (s.length() == 0)) {
                                return name.toString() + "=#" + Integer.toHexString(vv.getCursor().getAddress());
                            } else
                                return name.toString() + "=#" + Integer.toHexString(vv.getCursor().getAddress()) + "\"(" + s + ")";

                        }else {
                            String s = vv.getString();
                            if ((s == null) || (s.length() == 0)) {
                                return name.toString() + "=" + vv.getName().toString() + "#" + Integer.toHexString(vv.getCursor().getAddress());
                            } else
                                return name.toString() + "=" + vv.getName().toString() + "#" + Integer.toHexString(vv.getCursor().getAddress()) + "(" + s + ")";
                        }
                    }else if (value instanceof Link){
                        Item sub = this.getEndPointerByValue();//
                        if ( sub == null){
                            return name.toString() + "=\"" + value.toString()+ "\"(null)";
                        }else {
                            String s = sub.getString();
                            if ((s == null)||(s.length() == 0)){
                                return name.toString() + "=\"" + value.toString() + "\"(" + sub.getName().toString() + "#" + Integer.toHexString(sub.getCursor().getAddress()) + ")";
                            }else
                                return name.toString() + "=\"" + value.toString() + "\"(" + s + ")";
                        }
                    }
                    return name.toString() + "=\"" + value.toString() + "\"";
                }finally {
                }
            }


            public String getStorageString(Cursor cursor){
                if (name == null)
                    return null;
                if (value instanceof Item){
                    return null;
                }else
                    return name.toString() + "=\"" + getBaseRoot().getOrNewKeyDict(name).getValueStorageString(this) + "\"";

            }


            @Override
            public Tag getOrNewTagByLink(String link){
                return Tag.this.getOrNewTagByLink(link);
            }
            @Override
            public Tag getOrNewTagByLink(Link link){
                return Tag.this.getOrNewTagByLink(link);
            }

            @Override
            public Tag getTagByLink(String link){
                return Tag.this.getTagByLink(link);
            }

            @Override
            public Tag getTagByLink(Link link){
                return Tag.this.getTagByLink(link);
            }

            @Override
            public Tag getEndTagByValue(){
                Item ret = getEndPointerByValue();
                if (ret instanceof Tag)
                    return (Tag)ret;
                else
                    return null;
            }

            @Override
            public Tag getOrNewEndTagByValue() {
                Item ret = getOrNewPointerByValue();
                if (ret instanceof Tag)
                    return (Tag)ret;
                else
                    return null;
            }


            @Override
            public Item getAnyItem(Key key) {
                if (key.equals(name))
                    return this;
                return Tag.this.getAnyItem(key);
            }

            @Override
            public Item getLocalItem(Key k) {
                if (k.equals(name))
                    return this;
                else
                    return null;
            }

            @Override
            public Item getGlobalItem(Key key) {
                return Tag.this.getGlobalItem(key);
            }

            @Override
            public Item getOrNewAttrByName(String n){
                return Tag.this.getOrNewAttrByName(n);
            }

            @Override
            public Tag getOrNewTagByName(String name) {
                return Tag.this.getOrNewTagByName(name);
            }

            @Override
            public Item getAttrByName(String name) {
                if (this.name.equals("name")){
                    return this;
                }
                return Tag.this.getAttrByName(name);
            }

            @Override
            public String getLocalStringByName(String name) {
                if (this.name.equals("name")){
                    return this.getString();
                }

                return Tag.this.getLocalStringByName(name);
            }

            @Override
            public int getLocalIntByName(String name) {
                if (this.name.equals("name")){
                    return this.getInt();
                }
                return Tag.this.getLocalIntByName(name);
            }

            @Override
            public Object getLocalObjectByName(String name, Class type) {
                if (this.name.equals("name")){
                    return this.getObject(type);
                }
                return Tag.this.getLocalObjectByName(name, type);
            }

            @Override
            public Object getLocalObjectByName(String name) {
                if (this.name.equals("name")){
                    return this.getObject();
                }
                return Tag.this.getLocalObjectByName(name);
            }

            @Override
            public Item getItemByName(String name) {
                return Tag.this.getItemByName(name);
            }

            @Override
            public Tag getTagByName(String name) {
                return Tag.this.getTagByName(name);
            }

            @Override
            public Array getKeysByName(String name) {
                return Tag.this.getKeysByName(name);
            }

            @Override
            public String getStringByName(String name) {
                return Tag.this.getStringByName(name);
            }

            @Override
            public int getIntByName(String name) {
                return Tag.this.getIntByName(name);
            }

            @Override
            public Object getObjectByName(String name, Class type) {
                return Tag.this.getObjectByName(name, type);
            }

            @Override
            public Object getObjectByName(String name) {
                return Tag.this.getObjectByName(name);
            }

            @Override
            public Object getOrNewObjectByName(String name, Class c) {
                return Tag.this.getOrNewObjectByName(name, c);
            }

            @Override
            public Tag getTag(){
                return Tag.this;
            }


            @Override
            public Object getOrNewObject(Class c){
                Object o = getOrNewObject();
                if (c.isInstance(o))
                    return o;
                else
                    return null;
            }

            @Override
            public Object getOrNewObject() {
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
            public Object getObject() {
                if (value instanceof Item)
                    return ((Item) value).getObject();
                else if (value instanceof Link) {
                    Item var = MapUtil.getEndItemByValue(this, false);
                    if (var != null)
                        return var.getNativeValue();
                    else
                        return null;
                }
                return value;
            }


            @Override
            public Object getNativeValue(){
                return value;
            }


            @Override
            public String getString() {
                Object o = getObject();
                if (o != null)
                    return MapUtil.getStringFromText(o.toString());
                else
                    return "";
            }

            @Override
            public int getInt() {
                Object o = getObject();
                if (o instanceof Link){
                    o = getObject();
                }
                if (o == null)
                    return 0;
                if (o instanceof Integer)
                    return ((Integer)o).intValue();
                else
                    return Integer.parseInt(o.toString());

            }

            @Override
            public Object getObject(Class c) {
                Object o = getObject();
                if (o != null){
                    if (c.isInstance(o))
                        return o;
                }
                return null;
            }

            @Override
            public boolean isInternalFor(Tag tag) {
                if (tag == Tag.this){
                    return true;
                }else
                    return Tag.this.isInternalFor(tag);
            }

            @Override
            public String getOrNewString(){
                Object o = getOrNewObject();
                if (o != null)
                    return MapUtil.getStringFromText(o.toString());
                return "";
            }


            public Tag getNode(){
                return Tag.this;
            }


            LocalAttr(Object k, Object v){
                name = Key.getOrNew(k);
                value = MapUtil.checkLink(v);
                KeyDict t = getBaseRoot().getOrNewKeyDict(getName());
                t.postChange(this);
                Attributes.this.addElement(this);
            }



            @Override
            public Item getOrNewPointerByValue() {
                return MapUtil.getPointer(this, true);
            }

            @Override
            public int getOrNewInt( ) {

                  getOrNewObject();
                return getInt();

            }

            @Override
            public Item getEndPointerByValue() {
                return MapUtil.getPointer(this, false);
            }

            @Override
            public Cursor getCursor() {
                return Tag.this;
            }

            @Override
            public boolean setValue(Object o) {
                if (isDeleted())
                    return false;
                if (o != value) {
                    KeyDict t = getBaseRoot().getOrNewKeyDict(getName());
                    t.preChange(this);
                    value = checkLink(o);
                    t.postChange(this);
                    return true;
                }
                return false;

            }


            @Override
            public Key getName() {
                return name; //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void remove(){
                KeyDict t = getBaseRoot().getOrNewKeyDict(getName());
                t.removeFromValues(this);
                Attributes.this.removeElement(this.getName());
            }

            @Override
            public boolean isDeleted() {
                return Tag.this.isDeleted() || !Tag.this.containIs(name);  //To change body of implemented methods use File | Settings | File Templates.
            }

        }

        public class Attr extends LocalAttr implements ILet{
            Upper upper;
            Nodes nodes;
            Nodes nodes(){
                if (nodes == null){
                    nodes = new Nodes(4, this);
                }
                return nodes;
            }

            @Override
            public boolean isLocal(){
                return false;
            }


            @Override
            public boolean isHead() {
                return true;
            }


            @Override
            public boolean isTail(){
                return false;
            }


            @Override
            public ILet getHead(){
                return this;
            }

            public Nodes getNodes(){
                return nodes;
            }
            public ILet getTail(){
                return tailA;
            }
            @Override
            public ILet getPrevLet(){
                return tailA.getRealLet();
            }

            @Override
            public ILet getRealLet(){
                return this;
            }


            class TailA implements ILet, IValue {



                @Override
                public BaseRoot getBaseRoot(){
                    return getCursor().getBaseRoot();
                }

                @Override
                public boolean isHead() {
                    return false;
                }

                @Override
                public ILet getHead(){
                    return Attr.this;
                }
                @Override
                public boolean isTail(){
                    return true;
                }
                Nodes nodes;
                Nodes nodes(){
                    if (nodes == null){
                        nodes = new Nodes(4, this);
                    }
                    return nodes;
                }

                @Override
                public Nodes getNodes(){
                    return nodes;
                }

                @Override
                public ILet getTail(){
                    return tailA;
                }

                @Override
                public ILet getRealLet(){
                    if (upper != null) {
                        ILet let = (ILet) upper.lastElement();
                        while(let !=  null) {
                            ILet ret = let.getRealLet();
                            if (ret == let)
                                return ret;
                            else
                                let = ret;
                        }
                    }
                    return null;

                }

                @Override
                public ILet getPrevLet(){
                     ILet let = getRealLet();
                     if (let != null)
                         return let.getPrevLet();
                     return null;
                }
                @Override
                public void fillProperties(Properties properties,  String... args){
                    Tag.this.getTail().fillProperties(properties,  args);
                }

                @Override
                public Cursor getCursor() {
                    return Tag.this.tail; //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public void setIndex(IVector v, int val) {
                        nodes().put(v, val); //To change body of generated methods, choose Tools | Templates.
                }


                @Override
                public void remove(IVector v) {
                        nodes().remove(v);
                }

                @Override
                public void remove(){
                    KeyDict t = getBaseRoot().getOrNewKeyDict(getName());
                    t.removeFromValues(this);
                    if (nodes != null){
                        MapUtil.clearNodes(nodes);
                    }

                    if (upper != null){
                        upper.removeAllElements();
                        upper = null;
                    }
                    t.removeElement(this);
                }

                @Override
                public boolean isDeleted() {
                    return Tag.this.isDeleted() || !Tag.this.containIs(name);  //To change body of implemented methods use File | Settings | File Templates.
                }

                Upper upper;
                @Override
                public Upper getUpper() {
                    return upper; //To change body of generated methods, choose Tools | Templates.
                }
                @Override
                public void addUpper(ILet upper){
                    LiveVector v = getUpper();
                    if (v.size() != 0){
                        int stop = 1;
                    }
                    LiveVector all = upper.getUpper();
                    all.addTo(v);
                    if(upper != Attr.this){
                        if (upper.getTail() != upper)
                            v.addElement(upper);
                    }

              /*      for(int i = 0; i < upper.size() ; i++){
                        ILet l = (ILet)upper.elementAt(i);
                        if (l.getCursor().getAddress() > getAddress()){
                            IO.log.error("l.getCursor().getAddress() > getAddress()");
                        }
                    }*/
                    v.removeElement(Attr.this);
                }

                public TailA(){
                    upper = new Upper(2, this);
                    KeyDict t = getBaseRoot().getOrNewKeyDict(getName());
                    t.add(this);
                    t.setRegion(Attr.this, Tag.this, Tag.this.tail);
                    t.postChange(this);
                }


                @Override
                public Key getName() {
                    return Attr.this.getName(); //To change body of generated methods, choose Tools | Templates.
                }


                @Override
                public Object getNativeValue() {
                    return value;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public Item getItem() {
                    return Attr.this;  //To change body of implemented methods use File | Settings | File Templates.
                }
            }


            @Override
            public void fillProperties(Properties properties,  String... args){
                Tag.this.fillProperties(properties,  args);
            }

            TailA tailA;

            Attr(Object k, Object v){
                super(k, v);
                upper = new Upper(2, this);
                KeyDict t = getBaseRoot().getOrNewKeyDict(getName());
                t.add(this);
                tailA = new TailA();
                Attributes.this.addElement(this);
            }


            @Override
            public boolean setValue(Object o) {
                if (isDeleted())
                    return false;
                if (o != value) {
                    KeyDict t = getBaseRoot().getOrNewKeyDict(getName());
                    t.preChange(this);
                    t.preChange(tailA);
                    value = checkLink(o);
                    t.postChange(this);
                    t.postChange(tailA);
                    return true;
                }
                return false;

            }

            @Override
            public void remove(){
                KeyDict t = getBaseRoot().getOrNewKeyDict(getName());
                t.removeFromValues(this);
                t.removeElement(this);
                Attributes.this.removeElement(this.getName());
                tailA.remove();
                if (nodes != null){
                    MapUtil.clearNodes(nodes);
                }
                if (upper != null){
                    upper.removeAllElements();
                    upper = null;
                }
            }

            @Override
            public Upper getUpper() {
                return upper; //To change body of generated methods, choose Tools | Templates.
            }

            public void addUpper(ILet upper){
                LiveVector v = getUpper();
                if (v.size() != 0){
                    int stop = 1;
                }

                LiveVector all = upper.getUpper();
                all.addTo(v);
                if (upper.getTail() != upper)
                    v.addElement(upper);
            }

            @Override
            public void setIndex(IVector v, int val) {
                nodes().put(v, val); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void remove(IVector v) {
                nodes().remove(v); //To change body of generated methods, choose Tools | Templates.
            }
        }

        @Override
        public int hashCode(Object o){
            if ( o == null)
            {
                return NIL;
            }else
                return ((Named)o).getName().hashCode();
        }

        @Override
        boolean equals(Object attr, Object key){
            return ((Named)attr).getName() == key;
        }

        public Attributes.LocalAttr getValue2(Key key) {
            return (LocalAttr)this.get(key);
        }

        public Object getValue2(Object k) {
            Attr attr = (Attr)this.get(Key.getOrNew(k));
            if (attr == null)
                return null;
            else
                return attr.value;
        }

        public Item setValue3(Key k, Object o) {
            if (isDeleted())
                return null;
            Attr attr = (Attr)this.get(k);
            if (attr == null){
                attr = new Attr(k, o);//To change body of generated methods, choose Tools | Templates.
            }else{
                attr.setValue(o);
            }
            return attr;
        }

        public LocalAttr addValue3(Key k, Object o, boolean isLocal) {
            if (isDeleted())
                return null;
            LocalAttr attr = (Attr) this.get(k);
            if (attr == null) {
                if (isLocal){
                    attr = new LocalAttr(k, o);
                }else{
                    attr = new Attr(k, o);//To change body of generated methods, choose Tools | Templates.
                }
            } else {
                Object old = attr.getNativeValue();
                if (old != o) {
                    attr.setValue(o);
                }else
                    return attr;
            }
            return attr;

        }

        public int getSize() {
            return super.size();//To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Item elementAt(int i) {
            return (Item)super.elementAt(i); //To change body of generated methods, choose Tools | Templates.
        }

    }

    //Pointer beginOfFamily;
    Tail tail;
    Key name;
    Object value;


    public Tag() {

    }

    public Tag(Object fullName, Cursor end, boolean endIsRight) {
        init(fullName, end, false, end, endIsRight, null, null);
    }

    public Tag(Object fullName, Cursor end, boolean endIsRight, Object attrs, Object value) {
        init(fullName, end, false, end, endIsRight, attrs, value);
    }
    public Tag(Object fullName, Cursor b, boolean beginIsLeft, Cursor e, boolean endIsRight, Object attrs, Object value ) {
        init(fullName, b, beginIsLeft, e, endIsRight, attrs, value);
    }

    public StringBuilder getTextBody(){
        Cursor p = new Cursor(Tag.this);
        StringBuilder sb = new StringBuilder();
        while(p.getAddress() < tail.getAddress()){
            Object o = p.getNext();
            if (o instanceof Character){
                sb.append(o);
            }else if (o instanceof StringBuilder){
                sb.append(o);
            }else if (o instanceof String){
                sb.append(o);
            }else if (o instanceof Bytes){
                sb.append(o);
            }
            p.next();
        }
        p.remove();
        return sb;
    }


    public Tag(Object fullName, Tag.Tail tail) {
        init(fullName, tail, false, tail, true, null, null);
    }


    public Tag(Object fullName, Tail tail, Object args) {
        init(fullName, tail, false, tail, true, args, null);
    }

    void init(Object fullName, Cursor begin, boolean beginIsLeft, Cursor end, boolean endIsRight, Object args, Object v){
        if (end.getAddress() < begin.getAddress()){
            Log.error("end.getAddress() < begin.getAddress()");
            return;

        }
        name = Key.getOrNew(fullName);
        if (begin != this) {
            if (beginIsLeft)
                super.init(begin.getLeaf(), begin.getIndex() + 1);
            else
                super.init(begin.getLeaf(), begin.getIndex());
        }
        KeyDict t = getBaseRoot().getOrNewKeyDict(getName());
        value = MapUtil.checkLink(v);
        t.postChange(this);

        upper = new Upper(2, this);
        t.add(this);

        if (beginIsLeft && (end == begin)) {
            tail = new Tail(getLeaf(), getIndex() + 1);
        }else {
            if (endIsRight)
                tail = new Tail(end.getLeaf(), end.getIndex());
            else
                tail = new Tail(end.getLeaf(), end.getIndex() + 1);
        }
        setArgs(args);
    }

    void setArgs(Object args){
        if (args instanceof String){
            Log.error("String args not supported!");
            /*Bytes n = new Bytes(((String)args).getBytes(), false);
            for(Bytes line = n.removeFirstLine(); line != null; line = n.removeFirstLine()){
                Bytes value = line.divide('=');
                if (value != null){
                    addAttribute(line, value);
                }
            }*/
        }else if (args instanceof UTable){
            addAttributes((UTable)args);
        }else if (args instanceof Array){
            Array attrs = (Array)args;
            for (int i = 0; i < attrs.size(); i++) {
                Attributes.Attr a = (Attributes.Attr)attrs.elementAt(i);
                addAttribute(a.getName(), a.getNativeValue());
            }
        }else if (args instanceof Object[]){

                for (int i = 0; i < ((Object[]) args).length; i += 2) {
                    Object k = ((Object[]) args)[i];
                    Object v = ((Object[]) args)[i + 1];
                    addAttribute(k, v);
                }
        }
    }

    public Tag(Object fullName, Cursor ptr, boolean endIsRight, Object args) {
        init(fullName, ptr, false, ptr, endIsRight, args, null);
    }


    public void addAttributes(UTable args){
        if (isDeleted())
            return;
        if (args == null)
            return;
        for( int i = 0; i < args.size(); i ++){
            Object k = args.code().elementAt(i);
            Object v = args.value().elementAt(i);
            if (v instanceof Bytes){
                if (((Bytes) v).size() != 0)
                    addAttribute(k, v);
            }else if (v instanceof String){
                if (((String) v).length() != 0)
                    addAttribute(k, v);
            }else
                addAttribute(k, v);

        }
    }

    public Tag(Object fullName, Cursor ptr, boolean endIsRight, UTable args) {
        this.init(fullName, ptr, false, ptr, endIsRight, args, null);
    }

    public Item addLocal(Object fullName, Object value){
        if (isDeleted())
            return null;
        Key k = Key.getOrNew(fullName);
        if (k.equals(name)){
            this.value = checkLink(value);
            return this;
        }
        try {
            return head().addValue3(k, value, true);
        }finally{
            // IO.threads.printLog(Tag.this);
        }
    }
    public Item addAttribute(String attrName, Object value, boolean isLocal){
        if (attrName.length() == 0)
            return null;

        Key k = Key.getOrNew(attrName);
        if (k.equals(name)){
            this.value = checkLink(value);
            return this;
        }
        try {
            return head().addValue3(k, value, isLocal);
        }finally{
            // IO.threads.printLog(Tag.this);
        }
    }

    public Item addAttribute(Object fullName, Object value){
        if (isDeleted())
            return null;
        boolean isLocal = false;
        String attrName = fullName.toString();
        if (attrName.charAt(0) == '%'){
            attrName = attrName.substring(1, attrName.length());
            return addAttribute(attrName, value, true);
        }else{
            return addAttribute(attrName, value, false);
        }
    }


    public Item addVar(Key key, Object o){
        if (isDeleted())
            return null;
        return setValue(key.o, o);
    }


    public void addAttribute(Item let){
        if (isDeleted())
            return;
        if (let.getName().equals(name)){
            value = let;
            return;
        }
        Attributes.LocalAttr v = head().getValue2(let.getName());
        if ((v != null)&&(v.value == let))
            return;
        head().addValue3(let.getName(), let, let.isLocal());
    }

    public void removeAttribute(Object fulleName){
        Attributes.Attr attr = (Attributes.Attr)head().get(Key.getOrNew(fulleName));
        if (attr == null){
            return;
        }
        attr.remove();
    }

    public void removeAttribute(Item let){
       Attributes.Attr attr = (Attributes.Attr)head().get(let.getName());
       if (attr == null){
           return;
       }
       if ((let == attr)||(let == attr.value)){
           attr.remove();
       }
    }

    @Override
    public void getChildrenByName(Object name, Selector sel) {
        Key k = Key.getOrNew(name);
        KeyDict t = getBaseRoot().getOrNewKeyDict(k);
        t.getChildren(sel, this, false);
        sel.end();
    }
    @Override
    public void getAllChildrenByName(Object name, Selector sel) {
        Key k = Key.getOrNew(name);
        KeyDict t = getBaseRoot().getOrNewKeyDict(k);
        t.getChildren(sel, this, true);
        sel.end();
    }


    @Override
    public void remove(IVector v) {
        if (v instanceof Leaf){
            super.remove(v);
        }else{
            nodes().remove(v);
        }
    }


    @Override
    public Object getOrNewObject(Class c){
        Object o = getOrNewObject();
        if (c.isInstance(o))
            return o;
        else
            return null;
    }



    @Override
    public Cursor getCursor() {
        return Tag.this;
    }

    @Override
    public boolean setValue(Object o) {
        if (isDeleted())
            return false;
        if (o != value) {
            if (o.equals("root")){
                int stop = 1;
            }
            KeyDict t = getBaseRoot().getOrNewKeyDict(getName());
            t.preChange(this);
            t.postChange(tail);
            value = checkLink(o);
            t.postChange(this);
            t.postChange(tail);
            return true;
        }
        return false;
    }


    @Override
    public Item getEndPointerByValue() {
        return MapUtil.getPointer(this, true);
    }

    public Tag reSize(Cursor b, boolean beginIsLeft, Cursor e, boolean endIsRight){
        Tag ret = new Tag(name, b, beginIsLeft, e, endIsRight, head(), value);
        remove();
        return ret;
    }

    public Tag overAt(Tag tag){
        Tag ret = new Tag(name, tag, false, tag.getTail(), false, head(), value);
        remove();
        return ret;
    }
    public Tag overAt(Var var){
        Tag ret = new Tag(name, var, false, var, false, head(), value);
        remove();
        return ret;
    }

    @Override
    public void goToAt(Cursor ptr){
        if (ptr.leaf == null)
            return;

        if (ptr.getAddress() >= tail.getAddress())
        {
            Cursor p = new Cursor(tail);
            reSize(p, false, ptr, true);
            p.remove();
        }else if (!ptr.isNextFor(this)){
            while (ptr.getAddress() > getAddress()) {
                next();
            }
            while (ptr.getAddress() < getAddress()) {
                prev();
            }
            /*Pointer p = new Pointer(tail);
            reSize(ptr, false, p, true);
            p.remove();*/
        }

    }


    @Override
    public void goTo(Cursor ptr){
        if (ptr.leaf == null)
            return;

        if (ptr.getAddress() >= tail.getAddress())
        {
            Cursor p = new Cursor(tail);
            reSize(p, false, ptr, false);
            p.remove();
        }else if (!ptr.isPrevFor(this)){
            while (ptr.getAddress() < getAddress()) {
                prev();
            }
            while (ptr.getAddress() > getAddress()) {
                next();
            }
            /*Pointer p = new Pointer(tail);
            reSize(ptr, true, p, false);
            p.remove();*/
        }

    }

    public void clearBody(){

        while(!isAgainst(tail)){
            Object o = this.getNext();
            if (o instanceof Item){
                Item let = (Item)o;
                let.remove();
            }else {
                removeNext();
            }
        }
    }

    public void deleteBody(){
        while(!isAgainst(tail)){
            this.removeNext();
        }
    }


    public void clearAttributes(){
        if (head != null){
            while(head.size() > 0){
                Attributes.LocalAttr child = (Attributes.LocalAttr)head.lastElement();
                child.remove();
            }
        }
    }

    public void remove(){
        if (getNode() == null){
            return;
        }

        KeyDict t = getBaseRoot().getOrNewKeyDict(getName());

        t.removeFromValues(this);
        clearAttributes();
        if (nodes != null){
            MapUtil.clearNodes(nodes);
        }
        if (upper != null){
            upper.removeAllElements();
            upper = null;
        }

        t.removeElement(this);
        tail.remove();
        super.remove();

    }

    public void wholeDelete(){
        clearBody();
        remove();
    }


    public Array<String> getText(){
        if (!Tag.this.isAgainst(tail)){
            Cursor ptr = new Cursor(Tag.this);
            Array<String> ret = new Array(8);
            do{
                Object o = ptr.getNext();
                if (!(o instanceof ILet)){
                    ret.addElement(o.toString());
                }
                if (!ptr.next())
                    break;
            }while((!ptr.isAgainst(tail)));
            ptr.remove();
            return ret;
        }
        return null;
    }


    @Override
    public Key getName() {
        return name;//To change body of generated methods, choose Tools | Templates.
    }

    public Item newVar(Key name){
        if (isDeleted())
            return null;
        return /*getIThread().getRoot().*/addAttribute(name, null);
    }

    public Item getLocalItem(Key key) {
        if (key.equals(name))
            return this;
        if (head != null){
            Attributes.LocalAttr ret = head.getValue2(key);
            if (ret != null) {
                return ret;
            }
        }
        return null;
    }

    public Item setValue(Object k, Object o) {
        if (isDeleted())
            return null;
        Key n = Key.getOrNew(k);
        if (n.equals(name)){
            setValue(o);
            return this;
        }
        return head().setValue3(n, o);
    }



    @Override
    public Upper getUpper() {
        return upper; //To change body of generated methods, choose Tools | Templates.
    }

    public class Tail extends Cursor implements ILet, IValue, IStorage, Indent {

        @Override
        public Item getOrNewAttrByName(String name) {
            return Tag.this.getOrNewAttrByName(name);
        }


        public Tag getTag(){
            return Tag.this;
        }


        public Item getGlobalLetForValue() {
          return super.getGlobalItem(name);
        }



        @Override
        public boolean isTail(){
            return true;
        }

        public String getDebugString(){
            return toString();
        }

        @Override
        public String toString(){
            return "</" + name.toString() + ">";
        }

        @Override
        public String getStorageString(Cursor cursor){
            if (this.isNextFor(Tag.this) || cursor.isNextFor(Tag.this))
                return null;
            else {
                KeyDict dict = getBaseRoot().getKeyDict(name);
                if (dict.hasSuffix(Tag.this)){
                    return "</" + name.toString() + ":" + dict.createSuffix(Tag.this) + ">";
                }else{
                    return toString();
                }
            }
        }

        @Override
        public ILet getHead(){
            return Tag.this;
        }
        @Override
        public ILet getTail(){
                return tail;
        }


        @Override
        public boolean isHead(){
            return false;
        }


        @Override
        public ILet getRealLet(){
            if (upper != null) {
                ILet let = (ILet) upper.lastElement();
                while(let !=  null) {
                    ILet ret = let.getRealLet();
                    if (ret == let)
                        return ret;
                    else
                        let = ret;
                }
            }
            return null;
        }

        @Override
        public ILet getPrevLet(){
             ILet let = getRealLet();
             if (let != null)
                 return let.getPrevLet();
             return null;
        }

        Nodes nodes;
        Nodes nodes(){
            if (nodes == null){
                nodes = new Nodes(4, this);
            }
            return nodes;
        }

        public Nodes getNodes(){
            return nodes;
        }
/*        public int getAddress(){
            if (leaf != null)
                return leaf.getAddress(getIndex());
            else
                return -1;
        }*/


        @Override
        public Cursor getCursor() {
            return this; //To change body of generated methods, choose Tools | Templates.
        }


        @Override
            public void setIndex(IVector v, int val) {
                if (v instanceof Leaf)
                    super.setIndex(v, val);
                else
                    nodes().put(v, val); //To change body of generated methods, choose Tools | Templates.
            }


        @Override
        public void remove(IVector v) {
            if (v instanceof Leaf){
                super.remove(v);
            }else{
                nodes().remove(v);
            }
        }

        @Override
        public void remove(){
            KeyDict t = getBaseRoot().getOrNewKeyDict(getName());
            t.removeElement(this);
            t.removeFromValues(this);
            if (nodes != null){
                MapUtil.clearNodes(nodes);
            }
            if (upper != null){
                upper.removeAllElements();
                upper = null;
            }
            super.remove();
        }



        Upper upper;
        public Upper getUpper() {
            return upper; //To change body of generated methods, choose Tools | Templates.
        }
        public void addUpper(ILet upper){
            LiveVector v = getUpper();
            if (v.size() != 0){
                int stop = 1;
            }
            LiveVector all = upper.getUpper();
            all.addTo(v);
            if(upper != Tag.this){
                if (upper.getTail() != upper)
                    v.addElement(upper);
            }
/*            for(int i = 0; i < upper.size() ; i++){
                ILet l = (ILet)upper.elementAt(i);
                if (l.getCursor().getAddress() > getAddress()){
                    IO.log.error("l.getCursor().getAddress() > getAddress()");
                }
            }*/
            v.removeElement(Tag.this);
        }

        public Tail(Leaf leaf, int index){
            super(leaf, index);
            upper = new Upper(2, this);
            KeyDict t = getBaseRoot().getOrNewKeyDict(getName());
            t.add(this);
            t.setRegion(Tag.this, Tag.this, this);
            t.postChange(this);
        }


        public void superGoTo(Cursor p){
            super.goTo(p);
        }
        @Override
        public void goToAt(Cursor ptr){
            if (ptr.leaf == null)
                return;
            if (ptr.getAddress() < Tag.this.getAddress()) {
                Cursor p = new Cursor(Tag.this);
                reSize(ptr, false, p, false);
                p.remove();
            }else if (!ptr.isNextFor(tail)) {
                while (ptr.getAddress() > getAddress()) {
                    next();
                }
                while (ptr.getAddress() < getAddress()) {
                    prev();
                }
/*                Pointer  p = new Pointer(Tag.this);
                reSize(p, false, ptr, true);
                p.remove();*/
            }

        }


        @Override
        public  void goTo(Cursor ptr){
            if (ptr.leaf == null)
                return;
            if (ptr.getAddress() < Tag.this.getAddress())
            {
                Cursor p = new Cursor(Tag.this);
                reSize(ptr, true, p, false);
                p.remove();
            }else if (!ptr.isPrevFor(tail)){
                while (ptr.getAddress() < getAddress()) {
                    prev();
                }
                while (ptr.getAddress() > getAddress()) {
                    next();
                }
/*                Pointer  p = new Pointer(Tag.this);
                reSize(p, false, ptr, true);
                p.remove()*/
            }
        }

        @Override
        public Key getName() {
            return Tag.this.getName(); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isDeleted() {
            return Tag.this.isDeleted(); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean next(){

            Object o = getNext();
            int nextAddress = super.getNextAddress();
            KeyDict t = getBaseRoot().getOrNewKeyDict(name);
            int index = t.getObjectIndex(nextAddress);
            if (index != NIL){
                ILet let = t.reverseWithPrev(index);
                let.getUpper().addElement(getHead());
                if (let.isTail()){
                    getUpper().removeElement(let.getHead());
                }else{
                    getUpper().addElement(let.getHead());
                }
            }
            if (head != null){
                for(int i = 0; i < head.getSize(); i++){
                    Attributes.LocalAttr localAttr = (Attributes.LocalAttr)head.elementAt(i);
                    if (localAttr instanceof Attributes.Attr){
                        Attributes.Attr attr = (Attributes.Attr)localAttr;
                        t = getBaseRoot().getOrNewKeyDict(attr.name);
                        index = t.getObjectIndex(nextAddress);
                        if (index != NIL){
                            ILet let = t.reverseWithPrev(index);
                            let.getUpper().addElement(attr);
                            if (let.isTail()){
                                attr.getTail().getUpper().removeElement(let.getHead());
                            }else{
                                attr.getTail().getUpper().addElement(let.getHead());
                            }
                        }
                    }
                }
            }
            if (!super.next())
                return false;
            else
                return true;
        }

        @Override
        public boolean prev(){

            int prevAddress = super.getPrevAddress();
            BaseRoot baseRoot = getBaseRoot();
            if (baseRoot == null){
                baseRoot = getBaseRoot();
            }
            KeyDict t = baseRoot.getOrNewKeyDict(name);
            int index = t.getObjectIndex(prevAddress);
            if (index != NIL){
                ILet let = (ILet)t.reverseWithNext(index);
                let.getUpper().removeElement(getHead());
                if (let.isTail()){
                    getUpper().addElement(let.getHead());
                }else{
                    getUpper().removeElement(let.getHead());
                }

            }

            if (head != null){
                for(int i = 0; i < head.getSize(); i++){
                    Attributes.LocalAttr localAttr = (Attributes.LocalAttr)head.elementAt(i);
                    if (!localAttr.isLocal()){
                        Attributes.Attr attr = (Attributes.Attr)localAttr;
                        t = getBaseRoot().getOrNewKeyDict(attr.name);
                        index = t.getObjectIndex(prevAddress);
                        if (index != NIL){
                            ILet let = t.reverseWithNext(index);
                            let.getUpper().removeElement(attr);
                            if (let.isTail()){
                                attr.getTail().getUpper().addElement(let.getHead());
                            }else{
                                attr.getTail().getUpper().removeElement(let.getHead());
                            }
                        }
                    }
                }
            }
            if (!super.prev())
                return false;
            else
                return true;
        }

        @Override
        public Object getNativeValue() {
            return value;
        }

        @Override
        public Item getItem() {
            return Tag.this;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public int getIndent() {
            return -1;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }


    public boolean containIs(Key key){
        return head.isContain(key)||name.equals(key);
    }


    @Override
    public void addUpper(ILet upper){
        LiveVector v = getUpper();
        if (v.size() != 0){
            int stop = 1;
        }
        LiveVector all = upper.getUpper();
        if(all == null){
            Log.error("all == null");
            return;
        }
        all.addTo(v);
        if (upper.getTail() != upper)
            v.addElement(upper);

/*        for(int i = 0; i < upper.size() ; i++){
            ILet l = (ILet)upper.elementAt(i);
            if (l.getCursor().getAddress() > getAddress()){
                IO.log.error("l.getCursor().getAddress() > getAddress()");
            }
        }*/
    }


    @Override
    public boolean next(){

        int nextAddress = super.getNextAddress();
        KeyDict t = getBaseRoot().getOrNewKeyDict(name);
        int index = t.getObjectIndex(nextAddress);
        if (index != NIL){
            ILet let = t.reverseWithPrev(index);
            let.getUpper().removeElement(getHead());
            if (let.isTail()){
                getUpper().addElement(let.getHead());
            }else{
                getUpper().removeElement(let.getHead());
            }
        }

        if (head != null){
            for(int i = 0; i < head.getSize(); i++){
                Attributes.LocalAttr localAttr = (Attributes.LocalAttr)head.elementAt(i);
                if (!localAttr.isLocal()){
                    Attributes.Attr attr = (Attributes.Attr)localAttr;
                    t = getBaseRoot().getOrNewKeyDict(attr.name);
                    index = t.getObjectIndex(nextAddress);
                    if (index != NIL){
                        ILet let = t.reverseWithPrev(index);
                        let.getUpper().removeElement(attr);
                        if (let.isTail()){
                            attr.getUpper().addElement(let.getHead());
                        }else{
                            attr.getUpper().removeElement(let.getHead());
                        }
                    }
                }
            }
        }

        if (!super.next())
            return false;
        else
            return true;

    }


    @Override
    public boolean prev(){

       int prevAddress = super.getPrevAddress();
       KeyDict t = getBaseRoot().getOrNewKeyDict(name);
       int index = t.getObjectIndex(prevAddress);
       if (index != NIL){
            ILet let = t.reverseWithNext(index);
            let.getUpper().addElement(getHead());
            if (let.isTail()){
                getUpper().removeElement(let.getHead());
            }else{
                getUpper().addElement(let.getHead());
            }

       }

        if (head != null){
           for(int i = 0; i < head.getSize(); i++){
               Attributes.LocalAttr localAttr = (Attributes.LocalAttr)head.elementAt(i);
               if (!localAttr.isLocal()){
                   Attributes.Attr attr = (Attributes.Attr)localAttr;
                   t = getBaseRoot().getOrNewKeyDict(attr.name);
                   index = t.getObjectIndex(prevAddress);
                   if (index != NIL){
                       ILet let = t.reverseWithNext(index);
                       let.getUpper().addElement(attr);
                       if (let.isTail()){
                            getUpper().removeElement(let.getHead());
                       }else{
                            getUpper().addElement(let.getHead());
                       }
                   }
               }
           }
       }

        if (!super.prev())
            return false;
        else
            return true;
   }

   @Override
   public ILet getRealLet(){
       return this;
   }

   @Override
   public ILet getPrevLet(){
       return tail.getRealLet();
   }

   public Attributes.Attr elementAt(int index){
       if  (head == null)
           return null;
       return (Attributes.Attr)head.elementAt(index);
   }

   public int size(){
       if  (head == null)
           return 0;
       return head.size();

   }

    Nodes nodes;
    Nodes nodes(){
        if (nodes == null){
            nodes = new Nodes(4, this);
        }
        return nodes;
    }

    public Nodes getNodes(){
        return nodes;
    }


    @Override
    public void setIndex(IVector v, int val) {
        if (v instanceof Leaf)
            super.setIndex(v, val);
        else
            nodes().put(v, val); //To change body of generated methods, choose Tools | Templates.
    }

    public void clearSize(){
        int size = getIntByName("size");
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                Key name = Key.getOrNew(Integer.toString(i));
                Item attr = head().getValue2(name);
                if (attr != null) {
                    Item v = MapUtil.getEndItemByValue(attr, false);
                    if (v != null) {
                            v.remove();
                    }
                    attr.remove();
                }
            }
        }
    }

    public void clearByPrefix(String p){
        if (head != null){
            for(int i = head.size() - 1; i >= 0; i--){
                Attributes.LocalAttr child = (Attributes.LocalAttr)head.elementAt(i);
                if (child.getName().toString().startsWith(p))
                    child.remove();
            }
        }

    }

    @Override
    public Tag getOrNewTagByName(String name){
        if (this.name.equals(name))
            return this;
        Item let = getAnyItem(Key.getOrNew(name));
        if (let != null){
            if (let instanceof Tag)
                return (Tag)let;
        }
        return new Tag(name, getTail());
    }


    @Override
    public Item getItemByLink(String link){
        if (link.length() == 0)
            return null;
        return getItemByLink(new Link(link));
    }

    @Override
    public Object getByLink(Link link, Class type) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
        return getOrNewTagByLink(new Link(s));
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
    public Item getOrNewVarByLink(String link){
        if (link.length() == 0)
            return null;
        return getOrNewVarByLink(new Link(link));
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

    public String getOrNewStringByLink(Link name){
        Item var = MapUtil.getEndItemByValue(this, name, true);
        if (var != null)
            return var.getOrNewString();
        else
            return "";
    }


    public Item getOrNewVarByLink(Link link){
        Item var = MapUtil.getEndItemByValue(this, link, true);
        if (var != null)
            return var;
        else
            return null;
    }

    public Item getLetByValue(String name, Object value){
        KeyDict t = getBaseRoot().getOrNewKeyDict(Key.getOrNew(name));
        return t.getLetByValue(this, value);
    }


    public void rename(Key newName){
        if (isDeleted()){
            name = newName;
            return;
        }
        KeyDict t = getBaseRoot().getOrNewKeyDict(name);
        t.preChange(this);
        t.preChange(tail);
        
        t.removeFromValues(this);
        if (nodes != null){
            MapUtil.clearNodes(nodes);
        }
        if (upper != null){
            upper.removeAllElements();
        }
        t.removeElement(this);
        t.removeElement(tail);
        t.removeFromValues(tail);
        if (tail.nodes != null){
            MapUtil.clearNodes(tail.nodes);
        }

        if (tail.upper != null){
            tail.upper.removeAllElements();
        }
        name = newName;

        t = getBaseRoot().getOrNewKeyDict(newName);        
        t.add(this);
        t.add(tail);
        t.postChange(this);
        t.postChange(tail);
    }

    @Override
    public Item newAttr(Key name){
        return addAttribute(name, null);
    }


    public Tag getNextTag(){
        Item item = MapUtil.getNextChild(tail, name);
        if (item != null)
            return item.getTag();
        return null;
    }


    public Tag getPrevTag(){
        return MapUtil.getPrevChild(this, name).getTag();
    }
}