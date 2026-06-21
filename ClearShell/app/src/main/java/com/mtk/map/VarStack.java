
package com.mtk.map;

import com.mtk.map.i.Item;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public class VarStack extends UniqueArray {
    
    public VarStack(){
        super(7);
    }
    
    /*public IVar pop(){
        return (IVar)super.pop();
    }*/

    /*public IVar lastElement(){
        return (IVar)super.lastElement();
    }*/

    
    public boolean isContain(Item p){
        return super.isContain(p);
        //return indexOf(p) >= 0;
    }


}
