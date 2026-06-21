

package com.mtk.map;

import com.mtk.map.i.ILet;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public class Upper extends HashLiveVector {
    public Named let;
    public Upper(int size, ILet let) {
        super(size);
        this.let = let;
    }

    @Override
    public int hashCode(Object o) {
        return ((IPointer)o).getCursor().getAddress(); //To change body of generated methods, choose Tools | Templates.
    }

    public int addElement(Object t){
        return super.addElement(t);
    }
    
    public IPointer getValue(Cursor ptr){
        int i = findMiddle(ptr.getAddress());
        if (i >= amount)
            return (IPointer)lastElement();
        else{
            if (i == 0)
                return null;
            else
                return (IPointer)elementAt(i - 1);
        }
            
    }
    
}
