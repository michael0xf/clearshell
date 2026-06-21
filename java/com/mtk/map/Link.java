package com.mtk.map;

import com.mtk.map.i.Item;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public class Link extends Array/*LiveVector*/{
    public abstract static class TLink{
        Key key;
        TLink(Key k){
            key = k;
        }
        public boolean isLocal(){
            return false;
        }
        public boolean isFar(){
            return false;
        }
        public boolean isCurrent(){
            return false;
        }
        public boolean isInternal(){
            return false;
        }
        public boolean isPrev(){
            return false;
        }
        public boolean isNext(){
            return false;
        }

        abstract public Item extract(Item var);
        abstract public Item create(Item var);

        public abstract int getType();

        public String toString(){
            return "" + (char)getType() + key.toString();
        }
    }
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size(); i ++){
            TLink tl = (TLink)elementAt(i);
            sb.append(tl.toString());
        }
        return sb.toString();
    }

    public class Current extends TLink {
        public Link getLink(){
            return Link.this;
        }
        Current(Key k){
            super(k);
        }
        public boolean isCurrent(){
            return true;
        }
        public int getType(){
            return '$';
        }

        public Item extract(Item var){
            if (var.getName().equals(key))
                return var.getCursor().getGlobalItem(key);
            else
                return var.getCursor().getAnyItem(key);
        }

        public Item create(Item var){
            return var.getTag().addAttribute(key, null);
        }

        /*@Override
        public void setIndex(IVector v, int val) {
            if (v == Link.this) {
                index = val;
                if (var != null)
                    var.setIndex(Link.this, index);
            }
        }

        @Override
        public void remove(IVector v) {
            if (v == Link.this) {
                var.remove(Link.this);
                index = Const.NIL;
            }

        }

        @Override
        public void remove() {
            removeElementAt(index);
        }*/
    }

    public class Internal extends TLink{
        Internal(Key k){
            super(k);
        }
        public boolean isInternal(){
            return true;
        }
        public int getType(){
            return '#';
        }

        public Item extract(Item var){
            return MapUtil.getChild(var, key, false);
        }

        public Item create(Item var){
            return new Var(var.getTag(), key, null, var.getCursor(), true);
        }

    }

    public class Previous extends TLink{
        Previous(Key k){
            super(k);
        }
        public boolean isPrev(){
            return true;
        }
        public int getType(){
            return '^';
        }

        public Item extract(Item var){
            return var.getCursor().getAnyItem(key);
        }

        public Item create(Item var){
            return new Tag(key, var.getTag(), false, var.getTag(), true, null, null);
        }
    }


    public class Next extends TLink{
        Next(Key k){
            super(k);
        }
        public boolean isNext(){
            return true;
        }
        public int getType(){
            return '*';
        }
        public Item extract(Item var){
            return MapUtil.getNextChild(var.getCursor(), key);
        }

        public Item create(Item prev){
            return new Tag(key, prev.getTag(), true, prev.getTag(), false, null, null);
        }

    }
    public class Far extends TLink{

        public Far(Key k){
            super(k);
        }
        public boolean isFar(){
            return true;
        }
        public int getType(){
            return '@';
        }

        public Item extract(Item var){
            return MapUtil.getChild(var, key, true);
        }

        public Item create(Item var){
            BaseRoot baseRoot = var.getCursor().getLeaf().getRoot();
            return baseRoot.addAttribute(key, null);
        }

    }
    public class Local extends TLink{
        Local(Key k){
            super(k);
        }
        public boolean isLocal(){
            return true;
        }
        public int getType(){
            return '%';
        }
        public Item extract(Item var){
            return var.getCursor().getLocalItem(key);
        }

        public Item create(Item var){
            return var.getTag().addLocal(key, null);
        }

    }

    public static int[] LINK_FIX = {'$', '@', '#', '%', '^', '*'};
    public static boolean checkFix(int c){
        return (c == '$') || (c == '@') || (c == '#') || (c == '%') || (c == '^') || (c == '*');
    }
    public void init(Bytes b){
        int size = 0;
        for(int begin = b.find(LINK_FIX, 0, b.size()); (begin >= 0)&&(begin < b.size()); begin = b.find(LINK_FIX, begin + 1, b.size())){
            size++;
        }
        super.expand(size);
        while(b.size() > 0 ){
            if (b.get(0) == '$'){
                b.increaseOffset();
                Bytes word = b.removeFirstWord(LINK_FIX);
                addElement(new Current(Key.getOrNew(word)));
            }else if (b.get(0) == '%'){
                b.increaseOffset();
                Bytes word = b.removeFirstWord(LINK_FIX);
                addElement(new Local(Key.getOrNew(word)));
            }else if (b.get(0) == '@'){
                b.increaseOffset();
                Bytes word = b.removeFirstWord(LINK_FIX);
                addElement(new Far(Key.getOrNew(word)));
            }else if (b.get(0) == '#'){
                b.increaseOffset();
                Bytes word = b.removeFirstWord(LINK_FIX);
                addElement(new Internal(Key.getOrNew(word)));
            }else if (b.get(0) == '^'){
                b.increaseOffset();
                Bytes word = b.removeFirstWord(LINK_FIX);
                addElement(new Previous(Key.getOrNew(word)));
            }else if (b.get(0) == '*'){
                b.increaseOffset();
                Bytes word = b.removeFirstWord(LINK_FIX);
                addElement(new Next(Key.getOrNew(word)));
            }else{
                Log.error("bad link " + b.toString());
                return;
            }
        }
    }


    public Link(Bytes b){
        init(b);
    }

    public Link(String b){
        init(new Bytes(b.getBytes(), false));
    }
}
