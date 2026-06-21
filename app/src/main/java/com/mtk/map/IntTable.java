
package com.mtk.map;
/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */

public class IntTable<T> {
    public int pop()
    {
        code.pop();
        return value.pop();
    }
    public IntTable(int size)
    {
        table(Object.class, size);
    }

    public IntTable(Class c, int size)
    {
        table(c, size);
    }
    public void table(Class c, int size)
    {
        value = new IntArray(size);
        code = new HashArray(c, size);
    }
    public void clear()
    {
        code().clear();
    }

    public HashArray<T> code;
    public IntArray value;
    public HashArray<T> code()
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
        value.insertElementAt(val, index);
        return index;
    }
    public void remove(int begin, int count)
    {
        code.removeElementsFrom(begin, count);
        value.removeElementsFrom(begin, count);
    }
    public int remove(T key)
    {
        int index = code.removeElement(key);
        return value.removeElementAt(index);
    }
    public int remove(int hashCode)
    {
        int index = code.removeElements(hashCode);
        return value.removeElementAt(index);
    }

    public int size()
    {
        return code.size();
    }


}
