
package com.mtk.map;

import java.util.Enumeration;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public class Array<T extends Object> implements Const, IVector<T> {


    public boolean contains(T o){
        return indexOf(o) >=0;
    }

    public int indexOf(T o){
        for(int i = 0; i < amount; i++){
            if (o.equals(arr[i]))
                return i;
        }
        return -1;
    }

    public T[] newArray(int amount){
        return (T[])java.lang.reflect.Array.newInstance(c, amount);
    }

    public  T[] cloneArray(){

        T[] newArr = newArray(amount);
        System.arraycopy(arr, 0, newArr, 0, amount);
        return newArr;

    }

    public int amount = 0;
    public  void removeAllElements()
    {
        amount = 0;
    }

    public Array(Class c)
    {
        this.c = c;
        array(32);
    }

    public Array()
    {
        this.c = Object.class;
        array(32);
    }


    public  int  copyTo(IVector buf)
    {
        return buf.copyFrom(arr, 0, 0, amount);
    }

    public  void copyTo(T[] buf)
    {
        System.arraycopy(arr, 0, buf, 0, amount);
    }


    public  int  addTo(IVector buf)
    {
        return buf.copyFrom(arr, 0, buf.size(), amount);
    }


    public   int copyFrom(T[] from, int offset, int toindex, int len)
    {
        if (len == 0)
            return 0;
        if (offset >= from.length)
            return 0;
        if (offset + len > from.length){
            len = from.length - offset;
        }
        int amount = this.amount;
        T arr[] = this.arr;
        final int newlen = toindex + len;
        if (newlen > arr.length) {
            int len2 = newlen;
            if (len2 < arr.length << 1)
                len2 = arr.length << 1;
            final T[] newArr = newArray(len2);
            if (toindex >= amount)
                System.arraycopy(arr, 0, newArr, 0, amount);
            else
                System.arraycopy(arr, 0, newArr, 0, toindex);
            arr = newArr;
            this.arr = arr;
        }
        if (newlen > amount){
            amount = toindex + len;
            this.amount = amount;
        }
        System.arraycopy(from, offset, arr, toindex, len);
        return len;
    }
    public  int insertArray(final T[] from, final int offset, final int toindex, int len) {
        if (len == 0)
            return 0;
        if (offset >= from.length)
            return 0;
        if (offset + len > from.length){
            len = from.length - offset;
        }
        int amount = this.amount;
        T arr[] = this.arr;
        int newlen;
        if (toindex <= amount)
            newlen = amount + len;
        else
            newlen = toindex + len;
        if (newlen > arr.length) {
            int len2 = newlen;
            if (len2 < arr.length << 1)
                len2 = arr.length << 1;
            final T[] newArr = newArray(len2 );
            if (toindex<amount) {
                System.arraycopy(arr, 0, newArr, 0, toindex);
                System.arraycopy(arr, toindex, newArr, toindex + len, amount - toindex);
            }else
                System.arraycopy(arr, 0, newArr, 0, amount);
            arr = newArr;
            this.arr = arr;
        }else if (toindex < amount)
            System.arraycopy(arr, toindex, arr, toindex + len, amount - toindex);
        System.arraycopy(from, offset, arr, toindex, len);
        amount = newlen;
        this.amount = amount;
        return len;
    }

    public  int clearFromAt(int begin) {
        int amount = this.amount;
        if (begin < amount) {
            int ret = amount - begin;
            amount = begin;
            this.amount = amount;
            return ret;
        }else
            return 0;
    }


    public  T lastElement()
    {
        int amount = this.amount;
        if (amount > 0)
            return arr[amount - 1];
        else return null;
    }

    class Enumerator implements Enumeration
    {
         int index = 0;
         T[] a;
         Enumerator()
         {
             a = newArray(amount);
             System.arraycopy(arr, 0, a, 0, a.length);
         }
         public boolean hasMoreElements()
         {
            return index < a.length;
         }
         public T nextElement()
         {
             return a[ index++ ];
         }

        public void reset()
        {
            index = 0;
        }
    }

    public  Enumeration elements()
    {

        return new Enumerator();
    }
    Class c;
    public Array(int size){
        this.c = Object.class;
        array(size);
    }


    public Array(Class c, int size){
       this.c = c;
        array(size);
   }
   public  void array(int size)
   {
       arr = newArray(size );
       amount = 0;
   }
   public Array(T[] obj)
   {
       arr = obj;
   }
    public Array(T[] obj, int amount)
    {
        arr = obj;
        this.amount = amount;
    }

    public T[] arr;

   public  int size()
   {
       return amount;
   }
   public T elementAt(int i)
   {
            if (i >= amount)
                return null;
            try
            {

                return (T)arr[ i ];
            }catch (java.lang.ArrayIndexOutOfBoundsException e)
            {
                return null;
            }finally
        {

        }
   }
   public T firstElement()
   {
       if (amount > 0)
        return arr[ 0 ];
       else
       return null;
   }
   public  void clear()
   {
       amount = 0;
   }
   public int getMaxBlockLength(){
       return 65536;
   }
   public   int addElement( final T obj )
   {
       int amount = this.amount;
       int newAmount = amount + 1;
       expand();
       this.arr[amount]=obj;
       this.amount = newAmount;
       return newAmount;
   }
    public     void reverse(final int index, final int newindex)
    {
        final int amount = this.amount;
        final T arr[] = this.arr;

        if ((index < 0)||(index >= amount))
            return;
        if ((newindex >= amount)||(newindex < 0))
                return;

        final T obj = arr[index];
        if (newindex < index)
        {
            final int count = index - newindex;
            System.arraycopy(arr, newindex, arr, newindex + 1, count);
            arr[newindex] = obj;
        }else if (index < newindex)
        {
            final int count = newindex - index;
            if (count > 0)
                System.arraycopy(arr, index + 1, arr, index, count);
            arr[newindex]=obj;
        }
    }

    public  int luxuriance()
    {
        return arr.length;
    }

    protected void expandTo(int index){
        T[] arr = this.arr;
        if ((arr != null)&&(index >= arr.length)){
            int diff = index - arr.length + 1;
            if (diff < getMaxBlockLength() && (diff < arr.length))
                diff = arr.length;
            expand(arr.length + diff);
        }else
            expand();
    }
    public   void insertElementAt(final T obj, final int index)
    {
       expandTo(index);
       T[] arr = this.arr;
       int amount = this.amount;
       if (index < amount)
           System.arraycopy(arr, index, arr, index + 1, amount - index);
       arr[ index ] = obj;
       amount++;
       if (amount <= index)
           amount = index + 1;
       this.amount = amount;
    }


   public    T removeElementAt(final int index)
   {
       int amount = this.amount;
       final T arr[] = this.arr;
       T obj = null;
       if ((index >= 0)&&(index < amount))
       {
           obj = arr[index];
           if (amount - index - 1 > 0)
            System.arraycopy(arr,index + 1, arr, index, amount - index - 1);
           amount--;
           this.amount = amount;
       }
       return obj;
   }

    public void removeUpTo(int index)
    {
        removeElementsFrom(0, index+1);
    }

   public    void removeElementsFrom(final int index, int len)
   {
       int amount = this.amount;
       final T arr[] = this.arr;
       if ((index >= 0)&&(index < amount))
       {
           if (index + len > amount)
            len = amount - index;
           if (amount - index - len > 0)
            System.arraycopy(arr, index + len, arr, index, amount - index - len);
           amount -= len;
           this.amount = amount;

       }
   }


   protected void expand(int newSize){
       final T[] newArr = newArray(newSize );
       if (arr != null) {
           int amount = this.amount;
           if (amount > 0) {
               if (amount > newSize) {
                   amount = newSize;
                   this.amount = amount;
               }
               System.arraycopy(this.arr, 0, newArr, 0, amount);
           }
       }
       this.arr = newArr;

   }
   protected void expand(){
       T[] arr = this.arr;
       int additionalSize;
       int size;
       if (arr == null){
           size = 0;
           additionalSize = 2;
           amount = 0;
       }else{
           size = arr.length;
           if (size > amount)
               return;
           additionalSize = size;
       }
       if (additionalSize > getMaxBlockLength())
           additionalSize = getMaxBlockLength();
       expand( size + additionalSize );
   }

   public static int find2(final Object[] arr, final Object obj, final int amount)
   {
       for(int i = 0; i< amount;i++)
       {
           Object o = arr[i];
           if (o == null)
           {
               if (obj == null)
                    return i;
           }else if (o.equals(obj))
               return i;
       }
       return NIL;
   }
   public static int find3(final Object[] arr, final Object obj, final int amount)
   {
        for(int i = amount - 1;i >= 0;i--)
        {
           Object o = arr[i];
           if (o == null)
           {
               if (obj == null)
                    return i;
           }else if (o.equals(obj))
               return i;
        }
        return NIL;
   }

   public static int find(final Object[] arr, final Object obj, final int amount)
   {
       for(int i=0;i<amount;i++)
       {
           if (arr[i].equals(obj))
               return i;
       }
       return NIL;
   }

   public   int removeElement(final T obj)
   {
        int i = find3(arr,obj,amount);
        if (i >= 0)
            removeElementAt(i);
        return i;
   }
   public  T pop()
   {
        int amount = this.amount;
        if (amount <= 0)
            return null;
        amount--;
        this.amount = amount;
        return arr[amount];

   }
   public   T setElementAt(final T obj, final int index)
   {
       expandTo(index);
       int amount = this.amount;
       T arr[] = this.arr;
       T ret = null;
       if (index >= amount){
           for(int i = amount; i < index; i++)
               arr[ i ] = null;
           amount = index + 1;
           this.amount = amount;
       }else
           ret = arr[ index ];
       arr[ index ] = obj;
       return ret;

   }
    public  T[] toArray()
    {
        T[] t = newArray(amount);
        copyTo(t);
        return t;
    }

    public  int offset()
    {
        return 0;
    }
}

