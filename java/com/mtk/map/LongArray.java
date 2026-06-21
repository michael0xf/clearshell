
package com.mtk.map;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public class LongArray  implements Const
{

    public void removeAllElements()
    {
        amount = 0;
    }

    public LongArray()
    {
//        array(32);
    }

    public long lastElement()
    {
        try
        {
            return arr[amount];
        }
        catch(Exception e)
        {
            return NIL;
        }
    }



    public LongArray(int size)
    {
        array(size);
    }
    public void array(int size)
    {
        arr = new long[size];
    }
    public LongArray(long[] obj)
    {
        arr = obj;
    }
    public long copyTo(LongArray buf,int offset)
    {
        try
        {
            int len = amount-offset;
            if (len>0)
            {
                System.arraycopy(arr,offset,buf.arr,0,len);
                buf.amount = len;
            }
            return len;
        }finally
        {


        }

    }


    public int amount=0;
    public long[] arr;

    public int size()
    {
        return amount;
    }
    public long elementAt(int i)
    {
        if (i>=amount)
            return NIL;
        else
            return arr[i];

    }
    public long firstElement()
    {
        return arr[0];
    }
    public void clear()
    {
        amount = 0;
    }

    public   long addElement(long obj)
    {
        try
        {
            if (amount>=arr.length)
            {
                long[] newArr = new long[1+(amount<<1)];
                System.arraycopy(arr,0, newArr, 0, amount);
                arr = newArr;
            }
            arr[amount]=obj;
            amount++;
            return amount-1;
        }finally
        {


        }
    }

    public   void reverse(int index, int newindex)
    {

        if ((index<0)||(index>=amount))
            return;
        if ((newindex>=amount)||(newindex<0))
            return;

        try
        {

            long obj = arr[index];
            if (newindex<index)
            {
                int count = index-newindex;
                System.arraycopy(arr,newindex,arr,newindex+1,count);
                arr[newindex]=obj;
            }else if (index<newindex)
            {
                int count = newindex - index;
                if (count>0)
                    System.arraycopy(arr,index+1,arr,index,count);
                arr[newindex]=obj;
            }
        }finally
        {


        }
    }
    public  void insertElementAt(long obj, int index)
    {
        if (index<0)
            return;
        try
        {


            if (index>=arr.length)
                insertElementAtExt(obj,index);
            else
                insertElementAtLong(obj,index);
        }finally
        {


        }
    }


    public   long removeElementAt(int index)
    {
        try
        {


            long obj = NIL;
            if ((index>=0)&&(index<amount))
            {
                obj = arr[index];
                System.arraycopy(arr,index+1,arr,index,amount-index-1);
                amount--;
            }
            return obj;
        }finally
        {

        }

    }
    public  void insertElementAtExt(long obj, int index)
    {
        int newSize = (amount<<1)+1;
        if (index>=newSize)
            newSize = index+1;
        long[] newArr = new long[newSize];
        System.arraycopy(arr,0,newArr,0,amount);
        arr = newArr;
        arr[index] = obj;
        amount = index+1;
    }
    public  void insertElementAtLong(long obj, int index)
    {
        if (amount>=arr.length)
        {
            long[] newArr = new long[1+(amount<<1)];
            System.arraycopy(arr,0,newArr,0,index);
            System.arraycopy(arr,index,newArr,index+1,amount-index);
            arr = newArr;
        }else if (index<amount)
            System.arraycopy(arr, index, arr, index + 1, amount - index);
        arr[index] = obj;
        amount++;
        if (amount<=index)
            amount=index+1;
    }

    public static int find(long[] arr, long obj, int amount)
    {
        for(int i=0;i<amount;i++)
        {
            if (arr[i]==obj)
                return i;
        }
        return NIL;
    }

    public long removeElement(long obj)
    {
        int i = find(arr,obj,amount);
        if (i>=0)
            removeElementAt(i);
        return i;
    }
    public long pop()
    {
        if (amount<=0)
            return NIL;
        amount--;
        return arr[amount];
    }
    public   long setElementAt(long obj, int index)
    {
        try
        {


            long ret = NIL;
            if (index>=arr.length)
            {
                long[] newArr = new long[index+1];
                System.arraycopy(arr,0, newArr, 0, amount);
                arr = newArr;
            }
            if (index>=amount)
                amount = index+1;
            else
                ret = arr[index];
            arr[index] = obj;

            return ret;
        }finally
        {

        }

    }
    public void removeUpTo(int index)
    {
        removeElementsFrom(0, index+1);
    }

    public   void removeElementsFrom(int index, int len)
    {
        try
        {


            if ((index>=0)&&(index<amount))
            {
                if (index+len>amount)
                    len = amount-index;
                if (amount-index-len>0)
                    System.arraycopy(arr,index+len,arr,index,amount-index-len);
                amount-=len;
            }
        }finally
        {

        }

    }



}
