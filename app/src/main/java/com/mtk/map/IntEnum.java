/*
 * IntEnum.java
 *
 * � <your company here>, 2003-2005
 * Confidential and proprietary.
 */

package com.mtk.map;



/**
 * 
 */
public interface IntEnum {
    public int nextElement();
    public int prevElement();    
    public int previousPosition();    
    public int nextPosition();
    public void toFirst();
    public void toLast();
    public void free();
    public int size();
} 
