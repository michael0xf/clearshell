package com.mtk.map;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 2/6/17
 * Time: 1:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class U2Table {


        public U2Table(int size)
        {
            table3(size);
        }
        public void table3(int size)
        {
            value0 = new Array(size);
            value1 = new Array(size);
            code = new UniqueArray2(size);
        }

        public void clear()
        {
            value0.clear();
            value1.clear();
            code.clear();
        }

        Array value0;
        Array value1;
        UniqueArray2 code;

        public UniqueArray2 code(){
            return code;
        }
        public Array value0(){
            return value0;
        }
        public Array value1(){
            return value1;
        }

        public void removeAllElements()
        {
            clear();
        }


        public boolean containsKey(int key)
        {
            return code.isContain(key);
        }

        public int put(Object key, Object val0, Object val1)
        {
            int index = code.put(key);
            value0.insertElementAt(val0, index);
            value1.insertElementAt(val1, index);
            return index;
        }

        public int size()
        {
            return code.size();
        }
}
