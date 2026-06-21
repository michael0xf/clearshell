package com.mtk.map;

import com.mtk.map.IntArray;

/**
 * 
 */
public class UniqueIntArray extends IntArray {
    public UniqueIntArray(int size)
    {
        array(size);
    }


    public int findMiddle(int code)
    {
        int min = 0, count = amount;
        while (count>0)
        {
            count = count>>1;
            int middle = min+count;
            if (middle<amount)
            {
                int hash = arr[middle];
                if (code>hash)
                    min = middle+1;
                else if (code == hash)
                    return middle;
            }else if (count==0)
                    return amount;
        }
        return min;
    }

    public int indexOf(int code)
    {
        int index=findMiddle(code);
        if (index<amount)
            if (arr[index]==code)
                return index;
        return NIL;
    }
    public int removeElement(int obj)
    {
        int i = indexOf(obj);
        if (i>=0)
            removeElementAt(i);
        return i;
    }
    public   int addElement(int obj)
    {
        try
        {
            int code = obj;
            int index = findMiddle(code);
	    
            if (index >= arr.length)
                insertElementAtExt(obj, index);
            else if ((index >= amount)||(arr[index]!=obj))
                insertElementAtInt(obj, index);

            return index;
        }finally
        {
           
        }
    }
    public  int reverseAt(int index, int newobj)
    {
        if (index < 0)
            return addElement(newobj);
        int newindex = findMiddle(newobj);
        try
        {
           

            arr[index] = newobj;

        }finally
        {
           
        }

        if (newindex == index)
            return newindex;
        reverse(index, newindex);
        return newindex;
    }
    
} 
