package mtk.map;

import com.mtk.map.Cursor;
import com.mtk.map.Var;
import com.mtk.map.i.ILet;
import com.mtk.map.i.Indent;
import com.mtk.map.i.Item;
import com.mtk.map.Tag;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public class Log extends com.mtk.map.Log {


    static void writeIndent(int indentNum) {
        for (int i = 0; i < indentNum; i++) {
            System.out.print(' ');
        }
    }
    public static void printLog(Item root) {
        String name = root.getName().toString();
        System.out.println("-------mtk.map.Log.printLog " + root.getName() + "-------");
        Cursor p = new Cursor(root.getCursor());
        p.prev();
        int line = 0;
        int indent = 0;
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
        } while (p.next() && (p.getAddress() < root.getTag().getTail().getAddress()));
        p.remove();
        System.out.println("-------end " + root.getName() + "-------");
        count++;
    }

}
