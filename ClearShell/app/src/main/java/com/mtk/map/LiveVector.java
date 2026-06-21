
package com.mtk.map;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public class LiveVector<T> extends Array<T> {
    public static interface Element {
        public void setIndex(IVector v, int val);
        public void remove(IVector v);
        public void remove();
    }

    @Override
    public  void removeAllElements()
    {
        clear();
    }


    public LiveVector(Class c){
        super(c);
    }
    public LiveVector(int size) {super(size);    }

    public LiveVector(){

    }
    public LiveVector(Class c, int size) {super(c, size);    }
    public void clear(){
        nilling(0, amount);
        amount = 0;
    }

    public   int copyFrom(T[] from, int offset, int toindex, int len)
    {
        int count = super.copyFrom(from, offset, toindex, len);
        if (count > 0){
            enumerate(toindex);
        }
        return count;
    }

    public  void enumerate(int from)
    {
        int amount  = size();
        for(int i = from; i < amount; i++)
        {
            T o = elementAt(i);
            if ((o != null) && (o instanceof Element)){
                Element e = (Element)o;
                e.setIndex(this, i);
            }
        }           
    }
    public void enumerate(int from, int end)
    {
        if (from < end)
        {
            for(int i = from;i < end; i++)
            {
                T o = elementAt(i);
                if ((o != null) && (o instanceof Element)){
                    Element e = (Element)o;
                    e.setIndex(this, i);
                }
            }           
        }else
        {
            for(int i = end - 1; i >= from; i--)
            {
                T o = elementAt(i);
                if ((o != null) && (o instanceof Element)){
                    Element e = (Element)o;
                    e.setIndex(this, i);
                }
            }                       
        }
    }
    
    public void nilling(int from, int end)
    {
        int amount  = size();
        if (end > amount)
            end = amount;
        for(int i = from;i < end; i++)
        {
            T o = elementAt(i);
            if ((o != null) && (o instanceof Element)){
                Element e = (Element)o;
                e.remove(this);
            }
        }           
    }

   public   T removeElementAt(int index)
   {
        int amount  = size();
        T obj = null;
        if ((index>=0)&&(index<amount))
        {
            obj = elementAt(index);
            if (obj!=null){
                if (obj instanceof Element)
                    ((Element)obj).remove(this);
            }
            super.removeElementAt(index);
            enumerate(index);
        }
        return obj;
   }

    public    void removeElementsFrom(final int index, int len){
        super.removeElementsFrom(index, len);
        enumerate(index);
    }
   public  T setElementAt(T obj, int index)
   {
       T o = super.setElementAt(obj, index);
       enumerate(index, index + 1);
       if ((o != null)&&(o instanceof Element)) {
           Element e = (Element)o;
           e.remove(this);
       }
       return o;
   }
   public  void insertElementAt(T obj, int index)
   {
/*        if (setIndex == size())
        {
            addElement(obj);
            return;
        }else if (setIndex > size())
            setSize(setIndex+1);
        else if (elementAt(setIndex) == null)
        {
            super.setElementAt(obj, setIndex);
            enumerate(setIndex);
            return;
        }*/
        super.insertElementAt(obj, index);
        enumerate(index);
   }
   public boolean reverseElements(int index0, int index1)
   {
       if ((index1 < amount) && (index0 < amount))
       {
           T o0 = arr[index0];
           T o1 = arr[index1];
           arr[index0] = o1;
            if (o1 != null){
                if (o1 instanceof Element)
                    ((Element)o1).setIndex(this, index0);
            }
           
           arr[index1] = o0;
            if (o0 != null){
                if (o0 instanceof Element)
                    ((Element)o0).setIndex(this, index1);
            }
           return true;
       }
       return false;
   }
    public   int addElement(T obj)
   {
        int amount  = size();
        int ret = super.addElement(obj);
        if (obj instanceof Element)
            ((Element)obj).setIndex(this, amount);
        return ret;
   }

    public     void reverse(final int index, final int newindex){
        super.reverse(index, newindex);
        if (index < newindex)
            enumerate(index, newindex + 1);
        else
            enumerate(newindex, index + 1);

    }


}
