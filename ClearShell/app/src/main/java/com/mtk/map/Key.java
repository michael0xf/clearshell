
package com.mtk.map;
                    /*
import mtk.io.IO;
import mtk.io.Main;
                      */

import com.mtk.map.i.ILet;
import com.mtk.map.i.Item;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */

public class Key implements Named{



    protected Object o;

    protected Key(String s){
        this.o = s;
        all().put(s, this);
    }
    protected Key(Bytes b){
        this.o = b;
        all().put(b, this);
    }


    public String toString(){
        return o.toString();
    }



    @Override
    public Key getName(){
        return this;
    }


    public int indexOf(int c){
        if (o instanceof Bytes){
            return ((Bytes)o).indexOf(c);
        }else {
            return (o.toString()).indexOf(c);
        }
    }
    public Key subKey(int begin, int end){

        if (o instanceof Bytes){
            return Key.getOrNew(((Bytes)o).subBytes(begin, end));
        }else {
            return Key.getOrNew((o.toString()).substring(begin, end));
        }
    }

    public int length(){
        if (o instanceof Bytes){
            return ((Bytes)o).size();
        }else if (o instanceof String){
            return ((String)o).length();
        }
        return 0;

    }

    //public final String str;
    public boolean equals(Object o){
        if (o == this)
            return true;
        if (o instanceof Bytes){
            return o.equals(this.o);
        }else
            return this.o.equals(o);
    }



    static HTable all;
    public synchronized static HTable all(){
        if (all == null)
            all = new HTable(32);
        return all;
    }

    public synchronized static Key get(Object o){


        if (o == null)
            return null;
        if (o instanceof Bytes)
            return get((Bytes)o);
        else if (o instanceof String)
            return get((String)o);
        else return null;
    }


    public synchronized static Key get(String o){
        if (o == null)
            return null;
        return (Key)all().get(o);
    }

    public synchronized static Key get(Bytes o){
        if (o == null)
            return null;
        return (Key)all().get(o);
    }


    public synchronized static Key getOrNew(String o){

        if (o == null)
            return null;
        Key k = (Key)all().get(o);
        if (k == null){
            k = new Key(o);
        }
        return k;
    }
    public synchronized static Key getOrNew(Bytes o){

        if (o == null)
            return null;
        Key k = (Key)all().get(o);
        if (k == null){
            k = new Key(o);
        }
        return k;
    }
    public synchronized static Key getOrNew(ILet o){
        if (o == null)
            return null;
        return o.getName();
    }

    public synchronized  static Key getOrNew(Object o){
        if (o == null)
            return null;
        if (o instanceof Key)
            return (Key)o;
        if (o instanceof Bytes)
            return getOrNew((Bytes)o);
        else if (o instanceof String)
            return getOrNew((String)o);
        else if (o instanceof ILet)
            return ((ILet)o).getName();
        else
            return getOrNew(o.toString());
    }



}




