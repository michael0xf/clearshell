
package com.mtk.map;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */

public class Branch extends LiveVector implements LiveVector.Element{



    final static int SIZE = 8;
    final static int LENGTH = 1 << SIZE;


    void setArray(Object[] args){
        int size = 0;
        for(int i = 0; i < args.length; i++){
            if (args[i] != null){
                if (i > size){
                    args[size] = args[i];
                }
                size ++;
            }
        }
        for(int i = size; i < args.length; i++){
            args[i] = null;
        }
        arr = args;
        amount = size;
    }
    Branch(BaseRoot tipTop){
        branch = tipTop;
        array(LENGTH);
    }

    Branch(Branch node, int at){
        array(LENGTH);
        if (at < node.size()){
            node.insertElementAt(this, at);
        }else
            node.addElement(this);
    }

    @Override
    public void remove(){
        Branch branch = getPlace();
        if (branch != null)
            branch.removeElementAt(index);
    }

    public int getMaxBlockLength(){
        return LENGTH;
    }

    public Branch getOrNewNode(){
        Branch node = getPlace();
        if (node == null){
            node = newNode();
            node.addElement(this);
        }
        return node;
    }

    @Override
    public Object removeElementAt(int index){
          Object ret = super.removeElementAt(index);
          if (amount == 0){
              remove();
          }
          return ret;
    }


    public UniqueArray getDict(){
        Object b = branch;
        while (b instanceof Branch){
             b = ((Branch)b).branch;
        }
        return ((BaseRoot)b).getDict();
    }
    
    public BaseRoot getRoot(){
        Object b = branch;
        while (b instanceof Branch){
            b = ((Branch)b).branch;
        }
        return (BaseRoot)b;

    }

    public Branch newNode(){
        return new Branch(getRoot());
    }

    /*public Branch newBranch(){
        return new Branch();
    } */
    public Branch newNext() {
        return new Branch(getOrNewNode(), getIndex() + 1);
    }
    public Branch getOrNewNext(){
        Branch branch = (Branch)getOrNewNode().elementAt(getIndex() + 1);
        if (branch == null)
            return  newNext();
        else
            return branch;
    }

    @Override
    public void insertElementAt(Object obj, int index)
    {
        if (index > amount) {
            index = amount;
        }
        if (amount < arr.length)
            super.insertElementAt(obj, index);
        else {
            Branch branch = getOrNewNext();
            if (branch.amount == branch.arr.length)
                branch = newNext();
            if (index == amount) {
                branch.insertElementAt(obj, 0);
            }else {
                branch.insertElementAt(pop(), 0);
                if (amount == arr.length) {
                    int stop = 1;
                }
                super.insertElementAt(obj, index);
            }
        }
    }

    @Override
    public int addElement(Object obj)
    {
        insertElementAt(obj, amount);
        return amount;
    }



    @Override
    public int copyFrom(Object[] from, int offset, int toindex, int len){
        if (len == 0)
            return 0;
        if (offset >= from.length)
            return 0;
        if (offset + len > from.length){
            len = from.length - offset;
        }
        int amount = this.amount;
        Object arr[] = this.arr;
        final int newlen = toindex + len;
        if (newlen > arr.length){
            throw new ArrayIndexOutOfBoundsException();
        }
        if (newlen > amount){
            amount = toindex + len;
            this.amount = amount;
        }
        System.arraycopy(from, offset, arr, toindex, len);
        if (len > 0){
            enumerate(toindex);
        }
        return len;
    }

    @Override
    protected void expand() {
        if (arr == null){
            arr = new Object[LENGTH];
        }else {
            throw new IndexOutOfBoundsException("Branch expand");
        }
    }

    @Override
    protected void expandTo(int index){
        if (index >= LENGTH)
            throw new IndexOutOfBoundsException("Branch expandTo " + index);
        else if (arr == null)
            expand();
    }


    int index = 0;
    Object branch;
    @Override

    public void remove(IVector liveVector){
        if (liveVector == branch){
            branch = null;
            index = 0;
        }
    }

    public void setIndex(IVector v, int val){
        //Object prev = branch;
       /* if ((prev != v) && (prev != null))
            remove();*/
        this.branch = v;
        this.index = val;
    }

    public int getIndex(){
        return index;
    }


    public Branch getPlace(){

        if (branch instanceof Branch)
            return (Branch)branch;
        else
            return null;
    }

    public int getLevel(){
        Branch branch = getPlace();
        int level = 0;
        while (branch !=null){
            level++;
            branch = branch.getPlace();
        }
        return level;
    }



    int getAddress(int fromIndex){
        int address = fromIndex;
        int level = 1;
        Branch branch = getPlace();
        int index = this.index;
        while (branch != null){
            address += (index << (level * Branch.SIZE));
            /*if (this instanceof Leaf){
                if (((Leaf)this).prev != null){
                    int stop = 1;
                }

            } */
            level++;
            index = branch.index;
            branch = branch.getPlace();
        }
        return address;
    }





}

