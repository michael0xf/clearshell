package com.mtk.map;

public class IOTable<T> implements Const {


    public IOTable(Class c)
    {
        value = new Array<T>(c);
        code = new UniqueIntArray(value.size());
    }

    public void clear()
    {
        value().clear();
        code().clear();
    }

    public IOTable(Class c, int size)
    {
        IOTable(c, size);
    }
    public void IOTable(Class c, int size)
    {
        value = new Array<T>(c, size);
        code = new UniqueIntArray(size);
    }
    public Array<T> value()
    {
        return value;
    }
    public UniqueIntArray code()
    {
        return code;
    }

    Array<T> value;
    UniqueIntArray code;

    public void size(int val)
    {
        if (val < code.amount)
        {
            code.amount = val;
            value.amount = val;
        }
    }

    public void removeAllElements()
    {
        value.removeAllElements();
        code.removeAllElements();
    }


    public boolean containsKey(int key)
    {
        return code.indexOf(key)>=0;
    }

    public T get(int key)
    {
        int index = code.indexOf(key);
        if (index >= 0)
            return value.elementAt(index);
        return null;
    }

    public boolean isEmpty()
    {
        return value.size()==0;
    }

    public T put(int key, T value)
    {
        int index = code.findMiddle(key);
        if ((index<code.amount)&&(code.arr[index]==key))
            return this.value.setElementAt(value, index);
        else
        {
            code.insertElementAt(key, index);
            this.value.insertElementAt(value, index);
            return null;
        }
    }

    public T remove(int key)
    {
        int index = code.removeElement(key);
        return value.removeElementAt(index);
    }

    public int size()
    {
        return code.size();
    }
}
