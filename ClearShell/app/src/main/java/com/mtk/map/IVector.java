
package com.mtk.map;
/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */

public interface IVector<T> {
    public T elementAt(int index);
    public int addElement(T obj);
    public int size();
    public void insertElementAt(T obj, int index);
    public T setElementAt(T o, int i);
    public T removeElementAt(int index);

    boolean contains(T t);

    public T[] toArray();
    public int copyTo(IVector buf);
    public int copyFrom(T[] from, int offset, int toindex, int len);
    public int insertArray(final T[] from, final int offset, final int toindex, int len);
    public int clearFromAt(int begin);
    public T[] cloneArray();

    public  void reverse(final int index, final int newindex);
    public T lastElement();

}
