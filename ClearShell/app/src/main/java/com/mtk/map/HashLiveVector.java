package com.mtk.map;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */

public abstract class HashLiveVector<T> extends LiveVector<T> {
    public HashLiveVector(int size){
           super(size);
    }
    public HashLiveVector(Class c, int size){
        super(c, size);
    }

    public  T get(int code)
    {
        return lastObjectOfRange(code);
    }

    abstract public int hashCode(T o);

    public  int getLastIndex(int code)
    {
        int index = findMiddle(code);
        if (code == amount)
            return NIL;
        return findLastIndexOfRange(index, code);
    }


    public  T get(T code)
    {
        int index = indexOfObject(code);
        if (index == NIL)
            return null;
        return elementAt(index);
    }

    public  int findMiddle(int code) {
        int min = 0, count = amount;
        while (count > 0) {
            count = count >> 1;
            int middle = min + count;
            if (middle < amount) {
                int hash = hashCode(arr[middle]);
                if (code > hash)
                    min = middle + 1;
                else if (code == hash)
                    return middle;
            } else if (count == 0)
                return amount;
        }
        return min;
    }

    public int indexOfObject(T obj) {
        int code = hashCode(obj);
        int index = findMiddle(code);
        if ((index >= 0)&&(index < amount))
        {
            int il = index;
            for (; (il >= 0) && (hashCode(arr[il]) == code); il--)
            {
                if (arr[il].equals(obj))
                    return il;
            }
            int ir = index+1;
            for (; (ir < amount) && (hashCode(arr[ir]) == code); ir++)
            {
                if (arr[ir].equals(obj))
                    return ir;
            }
        }
        return NIL;
    }
    public int findBeginOfRange(int index, int code)
    {
        while((index > 0)&&(hashCode(arr[index]) == code))
        {
            index--;
        }
        return index;
    }

    public int findLastIndexOfRange(int index, int code)
    {
        while((index < amount)&&(hashCode(arr[index]) == code))
        {
            index++;
        }
        return index;
    }

    public  int firstIndexOfRange(int code)
    {
        int index = findMiddle(code);
        if (index < amount){
            if (hashCode(arr[index]) == code)
                return findBeginOfRange(index, code);
        }
        return index;
    }
    public  T firstObjectOfRange(int code)
    {
        if (amount>0)
            return arr[firstIndexOfRange(code)];
        else
            return null;
    }
    public  int lastIndexOfRange(int code)
    {
        int index = findMiddle(code);
        if (index < amount){
            if (hashCode(arr[index]) == code)
                return findLastIndexOfRange(index, code);
        }
        return index;
    }
    public  T lastObjectOfRange(int code)
    {
        if (amount > 0){
            int index = findMiddle(code);
            if (index < amount){
                if (hashCode(arr[index]) == code)
                    return arr[findLastIndexOfRange(index, code)];
            }
        }
        return null;
    }
    public  int removeElement(T obj) {
        int i = indexOfObject(obj);
        if ((i >= 0) && arr[i].equals(obj))
            removeElementAt(i);
        return i;
    }
    public  T popElement(int code) {
        int i = lastIndexOfRange(code);
        if ((i < amount) && (hashCode(arr[i]) == code))
            return removeElementAt(i);
        return null;
    }

    public int reverse(T obj, int newcode)
    {
        int index = indexOfObject(obj);
        if (index<0)
            return addElement(obj);
        int newindex = findMiddle(newcode);
        if (newindex==index)
            return newindex;
        reverse(index,newindex);
        return newindex;
    }
    public   int removeElements(int code)
    {
        try
        {


            int index = findMiddle(code);
            int count=0;
            if (index<amount)
            {
                int il = index;
                for (; (il >= 0) && (hashCode(arr[il]) == code); il--);
                int ir = index+1;
                for (; (ir < amount) && (hashCode(arr[ir]) == code); ir++);
                count = ir - il - 1;
                if (count > 0)
                {
                    if (ir<amount)
                        System.arraycopy(arr, ir , arr, il+1, amount - ir);
                    amount -= count;
                    enumerate(index, index + count);
                }
            }
            return count;
        }finally
        {


        }
    }


    public   int removeElements(int begincode,int endcode)
    {
        if (endcode<=begincode)
            return 0;
        try
        {


            int count=0;
            int begin = firstIndexOfRange(begincode);
            if (begin<amount)
            {
                int end = firstIndexOfRange(endcode);
                count = end-begin;
                if (count > 0)
                {
                    if (end<amount)
                        System.arraycopy(arr, end, arr, begin, amount-end);
                    amount -= count;
                    enumerate(begin, end);

                }
            }
            return count;
        }finally
        {


        }
    }

    public   int addElement( final T obj )
    {
        int code = hashCode(obj);
        int index = lastIndexOfRange(code);
        insertElementAt(obj, index);
        return index;
    }

}
