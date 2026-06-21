package com.mtk.map;


/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public class UOITable<T> {
    public int pop()
    {
        code.pop();
        return value.pop();
    }
    public UOITable(int size)
    {
        table(size);
    }
    public void table(int size)
    {
        value = new IntArray(size);
        code = new UniqueArray(size);
    }
    public void clear()
    {
        code().clear();
    }

    public UniqueArray<T> code;
    public IntArray value;
    public UniqueArray code()
    {
        return code;
    }

    public IntArray value()
    {
        return value;
    }

    public int get(T key)
    {
        int index = code.indexOfObject(key);
        if (index>=0)
            return value.elementAt(index);
        return Const.NIL;
    }

    public boolean isEmpty()
    {
        return value.size()==0;
    }

    public int put(T key, int val)
    {
        int index = code.addElement(key);
        if (code.size() == value.size())
        {

            int ret = value.elementAt(index);
            value.setElementAt(val,index);
            return ret;
        }else if (code.size() == value().size() + 1){
            value.insertElementAt(val, index);
        }else{
            Log.error("UITable T put(T key, T val)");
        }
        return Const.NIL;
    }
    public void remove(int begin, int count)
    {
        code.removeElementsFrom(begin, count);
        value.removeElementsFrom(begin, count);
    }
    public int remove(T key)
    {
        int index = code.removeElement(key);
         int ret = value.removeElementAt(index);
        if (code.size() != value.size()){
            index = code.removeElement(key);
            ret = value.removeElementAt(index);
        }
        return ret;
    }
    public int remove(int key)
    {
        int index = code.removeElement(key);
        return value.removeElementAt(index);
    }

    public int size()
    {
        return code.size();
    }


}
