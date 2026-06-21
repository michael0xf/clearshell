
package com.mtk.map;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public class IntArray  implements Const
{

    public void removeAllElements()
    {
        amount = 0;
    }

    public IntArray()
    {
//        array(32);
    }
    
    public int lastElement()
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


    public IntEnum qelements()
    {
        return getRange(0,amount);
    }
    public IntEnum qelementsFromEnd()
    {
        IntEnum q = getRange(0,amount);
        q.toLast();
        return q;
    }
    
    public IntEnum qelementsFromAt(int index)
    {
        return getRange(index,amount);        
    }

    public IntArray(int size)
   {
       array(size);
   }
   public void array(int size)
   {
       arr = new int[size];
   }
   public IntArray(int[] obj)
   {
       arr = obj;
   }
    public int copyTo(IntArray buf,int offset)
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
   public int[] arr;

   public int size()
   {
       return amount;
   }
   public int elementAt(int i)
   {
       if (i>=amount)
        return NIL;
       else
        return arr[i];

   }
   public int firstElement()
   {
       return arr[0];
   }
   public void clear()
   {
       amount = 0;
   }

   public   int addElement(int obj)
   {
       try
       {
           if (amount>=arr.length)
           {
               int[] newArr = new int[1+(amount<<1)];
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
                  
            int obj = arr[index];
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
   public  void insertElementAt(int obj, int index)
   {
       if (index<0)
           return;
       try
       {
          

           if (index>=arr.length)
               insertElementAtExt(obj,index);
           else
               insertElementAtInt(obj,index);
        }finally
        {
          
            
        }
   }


   public   int removeElementAt(int index)
   {
       try
       {
          

           int obj = NIL;
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
   public  void insertElementAtExt(int obj, int index)
   {
       int newSize = (amount<<1)+1;
       if (index>=newSize)
           newSize = index+1;
       int[] newArr = new int[newSize];
       System.arraycopy(arr,0,newArr,0,amount);
       arr = newArr;
       arr[index] = obj;
       amount = index+1;
   }
   public  void insertElementAtInt(int obj, int index)
   {
       if (amount>=arr.length)
       {
           int[] newArr = new int[1+(amount<<1)];
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

   public static int find(int[] arr, int obj, int amount)
   {
       for(int i=0;i<amount;i++)
       {
           if (arr[i]==obj)
               return i;
       }
       return NIL;
   }

   public int removeElement(int obj)
   {
       int i = find(arr,obj,amount);
       if (i>=0)
           removeElementAt(i);
       return i;
   }
   public int pop()
   {
       if (amount<=0)
           return NIL;
       amount--;
       return arr[amount];
   }
   public   int setElementAt(int obj, int index)
   {   
       try
       {
          

           int ret = NIL;
           if (index>=arr.length)
           {
               int[] newArr = new int[index+1];
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
   
   
   
   
   
   
   
    class Range implements IntEnum
    {
        public void free()
        {
            if (meter==NIL)
                return;
            meter=NIL;
            bufs.addElement(this);
        }        
        public int hashCode()
        {
            return buf.length;
        }
        int[] buf;
        public Range()
        {
            buf = new int[arr.length];
        }
        int size;
        public int meter=NIL;
        int offset;
        public int size()
        {
            return size;
        }
        public int previousPosition()
        {
            return meter-1+offset;
        }
                public int nextPosition()
        {
            return meter+1+offset;
        }
        public int nextElement()
        {
            while (meter < size) {
                int ret = buf[meter++];
                if (ret != NIL)
                    return ret;
            }
            return NIL;
        }
        public int prevElement()
        {
            while (meter >=0) {
                int ret = buf[meter--];
                if (ret != NIL)
                    return ret;
            }
            return NIL;
        }            
        public void toFirst()
        {
            meter=0;
        }
        public void toLast()
        {
            meter=size-1;
        }

    }
    static HashArray bufs = new HashArray(32);
    static class VoidRange implements IntEnum
    {
        public void toFirst()
        {
        }
        public void toLast()
        {
        }
        public void free()
        {
        }
        public int previousPosition()
        {
            return NIL;
        }
                public int nextPosition()
        {
            return NIL;
        }
        public int nextElement()
        {
            return NIL;
        }
        public int prevElement()
        {
            return NIL;
        }
        public int size()
        {
            return 0;
        }        
        
    }    
    static VoidRange voidRange = new VoidRange();
    public IntEnum getRangeFromAt(int first)
    {
        return getRange(first, amount-1);
    }
    public IntEnum getRangeTo(int last)
    {
        return getRange(0, last);
    }
    
    public IntEnum getRange(int first, int last)
    {
        if (last>=amount)
            last = amount-1;        
        if (first>last)
            return voidRange;
        Range range=null;    
        int size = last-first+1;
        int index = bufs.findMiddle(size);
        if (index<bufs.size())
        {
            range = (Range)bufs.elementAt(index);
            if ((range.buf.length>arr.length)||(range.meter!=NIL))
                range = null;
        }
        if (range==null)
            range = new Range();
        //System.arraycopy(range.buf,0,arr,first,size);
        System.arraycopy(arr,first,range.buf,0,size);
        range.meter=0;
        range.size = size;
        range.offset = first;
        return range;
    }

}
