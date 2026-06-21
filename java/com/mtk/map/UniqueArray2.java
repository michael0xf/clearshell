
package com.mtk.map;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public class UniqueArray2<T> extends UniqueArray<T> {
    public UniqueArray2(int size){
        super(size);
    }
    public UniqueArray2(){
    }
    public UniqueArray2(Class c, int size){
        super(c, size);
    }
    public UniqueArray2(Class c){
        super(c);
    }


    public  int removeElement(T obj) {
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

    public int indexOfObject(T obj) {
        int code = hashCode(obj);
        int index = findMiddle(code);
        if ((index >= 0)&&(index < amount))
        {
            if (hashCode(arr[index]) == code){
                return index;
            }
        }
        return NIL;
    }

}