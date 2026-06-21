
package com.mtk.map;


import java.util.Enumeration;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public class HTable<T0, T1> implements Const
{


    protected HashArray<T0> code;
    protected Array<T1> value;
    public HTable(int size){
        table(Object.class, Object.class, size);
    }
    public HTable(Class t0, Class t1, int size)
    {
        table(t0, t1, size);
    }
    public void table(Class t0, Class t1, int size)
    {
        code = new HashArray<T0>(t0, size);
        value = new Array<T1>(t1, size);
    }
    public void clear()
    {
        code().clear();
        value().clear();
    }

    public T0 keyAt(int i){
        return code.elementAt(i);
    }

    public T1 pop(){
        if (code.size() > 0){
            code.pop();
            return value.pop();
        }
        return null;
    }

    public T0 popKey(){
        if (code.size() > 0){
            value.pop();
            return code.pop();
        }
        return null;
    }
    public T1 valueAt(int i){
        return value.elementAt(i);
    }
    public HashArray<T0> code()
    {
        return code;
    }

    public Array<T1> value()
    {
        return value;
    }

    public boolean containsKey(T0 key)
    {
        return code.indexOfObject(key)>=0;
    }
    public boolean contains(T0 key)
    {
        return containsKey(key);
    }

    public Enumeration elements()
    {
        return value.elements();
    }


    public T1 get(T0 key)
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

    public T1 put(T0 key, T1 val)
    {
        //int index = code.indexOfObject(key);
        int a0 = code.amount;
        int index = code.put(key);
        if ((index < value.size())&&(a0 == code.amount))
        {
            T1 ret = value.elementAt(index);
            value.setElementAt(val,index);
            return ret;
        }
        value.insertElementAt(val,index);
        return null;
    }

    public T1 remove(T0 key)
    {
        int index = code.removeElement(key);
        return value.removeElementAt(index);
    }

    public int size()
    {
        return code.size();
    }
}
