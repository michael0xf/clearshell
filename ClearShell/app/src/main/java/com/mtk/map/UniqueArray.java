
package com.mtk.map;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public class UniqueArray<T> extends HashArray<T>
{
    public UniqueArray(Class c){
        super(c);
    }

    public UniqueArray(){

    }

    @Override
    public boolean contains(T o){
        return isContain(o);
    }

    public boolean isContain(T o){
        int i = findMiddle(hashCode(o));
        return (i < amount)&&(equals(arr[i],o));
    }

    public boolean isContain(int code){
        T[] arr = this.arr;
        int amount = this.amount;
        int min = 0, count = amount;
        while (count > 0)
        {
            count = count>>1;
            int middle = min + count;
            if (middle < amount)
            {
                int hash = hashCode(arr[middle]);
                if (code > hash)
                    min = middle + 1;
                else if (code == hash)
                    return true;
            }else if (count == 0)
                return false;
        }
        return false;
    }

    public int getIndex(int code){
        T[] arr = this.arr;
        int amount = this.amount;
        int min = 0, count = amount;
        while (count > 0)
        {
            count = count >> 1;
            int middle = min + count;
            if (middle < amount)
            {
                int hash = hashCode(arr[middle]);
                if (code > hash)
                    min = middle + 1;
                else if (code == hash)
                    return middle;
            }else if (count == 0)
                return -1;
        }
        return -1;
    }


    public  UniqueArray clone()
    {
        UniqueArray ret = new UniqueArray(c, amount);
        copyTo(ret);
        return ret;
    }    
    public  void cloneTo(UniqueArray set)
    {
        if (set.arr.length < amount)
        {
            set.arr = newArray(amount);
        }
        copyTo(set);
    }    
    
    public UniqueArray(Class c, int size)
    {
        super(c, size);
    }


    public UniqueArray(int size)
    {
        super(size);
    }

    public T get(int code)
    {
        int index = findMiddle(code);

        if ((index>=0)&&(index<amount))
        {
            if (hashCode(arr[index]) == code)
                return arr[index];
        }
        return null;
    }

    public int findMiddle(int code)
    {
        T[] arr = this.arr;
        int amount = this.amount;

        int min = 0, count = amount;
        while (count>0)
        {
            count = count>>1;
            int middle = min + count;
            if (middle < amount)
            {
                int hash = hashCode(arr[middle]);
                if (code > hash)
                    min = middle + 1;
                else if (code == hash)
                    return middle;
            }else if (count == 0)
                    return amount;
        }
        return min;
    }
  /* public int indexOf(T obj)
    {
        int i = indexOf(obj.hashCode());
        if (i>=0)
        {
            if (arr[i].equals(obj))
                return i;
        }
        return NIL;
    }*/
    public int indexOf(int code)
    {
        int index=findMiddle(code);
        if (index<amount)
            if (hashCode(arr[index])==code)
                return index;
        return NIL;
    }
    public int removeElement(int code)
    {
        int i = indexOf(code);
        if (i >= 0)
            removeElementAt(i);
        return i;
    }    
    public int removeElement(T obj)
    {
        int i = indexOfObject(obj);
        if (i >= 0)
            removeElementAt(i);
        return i;
    }

    public   int addElement( final T obj )
    {
        int code = hashCode(obj);
        int index = findMiddle(code);
        int amount = this.amount;
        T[] arr = this.arr;
        if ((index < amount)&&(hashCode(arr[ index ]) == code)){
            arr[index] = obj;
            return index;
        }
        insertElementAt(obj, index);
        return index;
    }

}

/**
 * 
 */

