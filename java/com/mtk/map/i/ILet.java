package com.mtk.map.i;

import com.mtk.map.*;

import java.util.Properties;


/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public interface ILet extends LiveVector.Element, Named, IPointer {

    
    public ILet getHead();
    public void remove();
    public boolean isDeleted();
    public Nodes getNodes();
    public void addUpper(ILet upper);
    public Upper getUpper();

    public Item getItem();


    public boolean isHead();
    public boolean isTail();
    public ILet getTail();

    public ILet getRealLet();
    public ILet getPrevLet();
    public void fillProperties(Properties properties,  String... args);
   
}
