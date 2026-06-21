
package com.mtk.map;

import java.util.Enumeration;
/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */

public class UTable<T> implements Const
{
    public UTable()
    {
        table(32);
    }
    public UTable(int size)
    {          
        table(size);
    }
    public void table(int size)
    {
        value = new Array(size);
        code = new UniqueArray2(size);
    }
    public void clear()
    {
        code().clear();
        value().clear();
    }

    protected Array<T> value;
    protected UniqueArray2<T> code;
    public UniqueArray2 code()
    {
        return code;
    }
    
    public Array value()
    {
        return value;
    }

    public boolean containsKey(T key)
    {
       return code.indexOfObject(key)>=0;
    }

    public Enumeration elements()
    {
        return value.elements();
    }

    public T get(int c)
    {
            int index = code.findMiddle(c);
            if (index < code.amount){
                if (code.arr[index].hashCode() == c)
                    return value.elementAt(index);
            }
            return null;
    }
    
    public T get(T key)
    {
        int index = code.indexOfObject(key);
        if (index >= 0)
            return value.elementAt(index);
        return null;
    }

    public boolean isEmpty()
    {
        return value.size()==0;
    }

    public T put(T key, T val)
    {
        int index = code.put(key);
        if (code.size() == value.size())
        {

            T ret = value.elementAt(index);
            value.setElementAt(val,index);
            return ret;
        }else if (code.size() == value().size() + 1){
            value.insertElementAt(val, index);
        }else{
            Log.error("HTable T put(T key, T val)");

        }
        return null;

    }

    public T remove(T key)
    {
        int index = code.removeElement(key);
        return value.removeElementAt(index);
    }

    public int size()
    {
        return code.size();
    }
}
