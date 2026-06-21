package com.mtk.map;


public class IntIntTable implements Const
{
    public IntIntTable()
    {
    }
    
    public void clear()
    {
        value().clear();
        code().clear();
    }    

    public IntIntTable(int size)
    {
        IntIntTable(size);
    }
    public void IntIntTable(int size)
    {
        value = new IntArray(size);
        code = new UniqueIntArray(size);
    }
    public IntArray value()
    {
        return value;
    }
    public UniqueIntArray code()
    {
        return code;
    }

    IntArray value;
    UniqueIntArray code;
    
    public void size(int val)
    {
        if (val<code.amount)
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

    public int get(int key)
    {
        int index = code.indexOf(key);
        if (index>=0)
            return value.elementAt(index);
        return NIL;
    }

    public boolean isEmpty()
    {
        return value.size()==0;
    }

    public int put(int key, int value)
    {
        int index = code.findMiddle(key);
        if ((index<code.amount)&&(code.arr[index]==key))
            return this.value.setElementAt(value,index);
        else
        {
            code.insertElementAt(key, index);
            this.value.insertElementAt(value, index);
            return NIL;
        }
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
