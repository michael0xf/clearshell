
package com.mtk.map;

import com.mtk.map.i.ILet;
import com.mtk.map.i.Indent;
import com.mtk.map.i.Item;

import java.io.PrintWriter;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public class Log {
    public static void mess(String s){
        System.out.println(s);
    }
    public static void charc(char c){
        System.out.print(c);
    }
    public static void error(String s){
        String m = "Mtk error: " + s;
        System.out.println(m);
    }
    public static void error(Exception e){
        String s = "Mtk error: " + e.getMessage() + e.getStackTrace();
        System.out.println(s);
    }
    public static void error(Exception e, String m){
        String s = m + ": " + e.getMessage() + e.getStackTrace();
        System.out.println(s);

    }

    public static void error(Throwable e, String m){
        String s = m + ": " + e.getMessage() + e.getStackTrace();
        System.out.println(s);

    }

    static void writeIndent(int indentNum) {
        for (int i = 0; i < indentNum; i++) {
            System.out.print(' ');
        }
    }

    public static void printLog(Item root){
        String name = root.getName().toString();
        int indent = 0;
        System.out.println("-------begin " + root.getName() + "-------");
        Cursor p = new Cursor(root.getCursor());
        p.prev();
        int line = 0;
        do {
            String address = Integer.toHexString(p.getAddress());
            System.out.print("#" + count + " " + name + "(" + address + ")" + line++ + ": ");
            Object o = p.getNext();
            if (o != null) {
                int increase;
                if (o instanceof Indent){
                    increase = ((Indent)o).getIndent();
                    if (increase < 0){
                        indent += increase;
                        increase = 0;
                    }
                }else
                    increase = 0;
                if (o instanceof Item) {
                    Item var = (Item) o;
                    writeIndent(indent);
                    System.out.print(var.getDebugString());
                } else {
                    writeIndent(indent);
                    System.out.print(o.toString());
                }
                indent += increase;
            }
            System.out.println();
        } while (p.next() && (p.getAddress() < root.getTag().tail.getAddress()));
        p.remove();
        System.out.println("-------end thread " + root.getName() + "-------");
        count++;
    }

    protected static int count = 0;

}



