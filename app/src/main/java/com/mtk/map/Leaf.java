
package com.mtk.map;


import com.mtk.map.i.ILet;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public class Leaf extends Branch {
    Leaf next, prev;
    void delete(){
        Leaf prev = this.prev;
        Leaf next = this.next;
        if (next == null){
            if (prev == null)
                return;
            prev.next = null;
            this.prev = null;
        }else if (prev == null){
            next.prev = null;
            this.next = null;
        }else
        {
            next.prev = prev;
            prev.next = next;
        }
    }

    void insertAfterOf(Leaf knot){
        if (knot == null)
            return;
        Leaf n = knot.next;
        prev = knot;
        if (n != null){
            next = n;
            knot.next = this;
            n.prev = this;
        }else{
            knot.next = this;
        }
    }
    void insertBeforeOf(Leaf knot){
        if (knot == null)
            return;
        Leaf p = knot.prev;
        next = knot;
        if (p != null){
            prev = p;
            knot.prev = this;
            p.next = this;
        }else{
            knot.prev = this;
        }
    }

    public  Object getLastElement(){
        if (amount > 0)
            return arr[amount - 1];
        else{
            Leaf next = getNext();
            if (next != null)
                return next.getFirstElement();
        }
        return null;
    }

    public  Object getFirstElement(){
        if (amount > 0)
            return arr[0];
        else{
            Leaf prev = getPrev();
            if (prev != null)
                return prev.getLastElement();
        }
        return null;
    }
    public  Object removeLastElement(){
        if (amount > 0)
            return removeElementAt(amount - 1);
        else{
            Leaf next = getNext();
            this.remove();
            if (next != null)
                return next.removeFirstElement();
        }
        return null;
    }

    public  Object removeFirstElement(){
        if (amount > 0)
            return removeElementAt(0);
        else{
            Leaf prev = getPrev();
            this.remove();
            if (prev != null)
                return prev.removeLastElement();
        }
        return null;
    }


    public int superCopyFrom(Object[] from, int offset, int toindex, int len){
        return super.copyFrom(from, offset, toindex, len);
    }

    public  int free(){
        return arr.length - amount;
    }

    @Override
    public int copyFrom(Object[] from, int offset, int toindex, int len) {
        int count = 0;
        if (len <= free()){
            count += superCopyFrom(from, offset, toindex, len);
        }else{
            ((Leaf)newNext()).superCopyFrom(arr, toindex, 0, amount - toindex);
            int firstLen = len;
            int free = free();
            if (firstLen > free)
                firstLen = free;
            count += superCopyFrom(from, offset, toindex, firstLen);
            len -= firstLen;
            offset += firstLen;
            if (len > 0){
                Leaf leaf = (Leaf)getOrNewNext();
                if (leaf.free() >= len){
                    count += leaf.superCopyFrom(from, offset, 0, len);
                }else{
                    int max = arr.length;
                    while (len > max){
                        leaf = (Leaf)leaf.newNext();
                        count += leaf.superCopyFrom(from, offset, 0, max);
                        len -= max;
                        offset += max;
                    }
                    leaf = (Leaf)leaf.getOrNewNext();
                    if (leaf.free() < len)
                        leaf = (Leaf)leaf.newNext();
                    count += leaf.superCopyFrom(from, offset, 0, len);
                }
            }
        }
        return count;
    }


    Leaf(BaseRoot tipTop){
        super(tipTop);
    }

    Leaf(Branch node, int at){
        super(node, at);
    }


    void setArray(Object[] args){
        int size = 0;
        for(int i = 0; i < args.length; i++){
            if (args[i] != null){
                if (i > size){
                    args[size] = args[i];
                }
                size++;
            }
        }
        for(int i = size; i < args.length; i++){
            args[i] = null;
        }
        arr = args;
        amount = size;
    }


    public Leaf getPrev() {
        return prev;
    }

    public Leaf getNext() {
        return next;
    }

    public Branch newNext() {

        Leaf leaf = new Leaf(getOrNewNode(), getIndex() + 1);
        leaf.insertAfterOf(this);
        return leaf;
    }

    public Branch newPrev() {
        Leaf leaf = new Leaf(getOrNewNode(), getIndex());
        leaf.insertBeforeOf(this);
        return leaf;
    }


    public Branch getOrNewPrev() {
        if (prev == null)
            return newPrev();
        return prev;
    }

    @Override
    public Branch getOrNewNext() {
        if (next == null){
            return newNext();
        }
        return next;
    }



    @Override
    public  void remove(){
        super.remove();
        delete();
    }


/*    @Override
    public Branch newBranch(){
        return new Leaf(mtkThread);
    }*/

    public ILet getValue(Key key, int index) {
        
        if (key == null)
            return null;
        return getRoot().getOrNewKeyDict(key).getLet(this, index);
    }


    
   /* public Object getObject(String key, int index) {
        
        if (key == null)
            return null;
        ILet ret = Key.getOrNew(key).getLet(this, index);
        if (ret != null)
            return ret.getValue();
        else
            return null;
    }*/

    
/*    public Object getTypeForKey(Class type, String key, int index) {
        ILet ret = getValue(Key.getOrNew(type.getName() + " " + key), index);
        if (ret != null)
            return ret.getValue();
        ret = getValue(Key.getOrNew(key), index);
        if (ret == null)
            return null;
        return Main.ins.getTypeForValue(type, ret.getValue());
    }

    public Object getTypeForKey(Class type, Key key, int index) {
        ILet ret = getValue(Key.getOrNew(type.getName() + " " + key.toString()), index);
        if (ret != null)
            return ret.getValue();
        ret = getValue(key, index);
        if (ret == null)
            return null;
        return Main.ins.getTypeForValue(type, ret.getValue());
    }*/


   /* class Ant extends Pointer{
          public Leaf getLeaf(){
              return Leaf.this;
          }
    }

    Pointer anchor(){
        if (anchor == null)
            anchor = new Pointer();
        return  anchor;
    }*/

}




/*
    public  int getIndex(int foundedLevel){
        return anchor().getIndex(foundedLevel);

    }

    public  void insertToBranch(Branch branch, int index){
        anchor().insertToBranch(branch, index);
    }

    public  void transferFromBranch(final Branch union, final int from, int end, final int toIndex){
        if (from >= union.size())
            return;
        if (end < from)
            return;
        if (end > union.size())
            end = union.size();
        int len = end - from;
        if (union instanceof Branch){
            Branch union1 = (Branch) union;
            len = copyFrom(union1.arr, from, toIndex, len);
        }else{
            for(int i = from, counter = 0; i < end; i++, counter++){
                final Object o = union.elementAt(i);
                insertElementAt(o, toIndex + i);
            }
        }
        if (len > 0)
            super.enumerate(toIndex, toIndex + len);
    }


*/