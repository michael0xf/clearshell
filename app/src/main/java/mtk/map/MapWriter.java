package mtk.map;

import com.mtk.map.*;
import com.mtk.map.IStorage;
import com.mtk.map.i.Indent;
import com.mtk.map.i.Item;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Properties;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */

public class MapWriter {
    public static void save(final Item root, final String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            PrintWriter writer = new PrintWriter(file, "UTF-8");
            Cursor p = new Cursor(root.getCursor());
            p.prev();
            Object prev = null;
            int indentNum = 0;
            do {
                Object o = p.getNext();
                if (o != null) {
                    int increase;
                    if (o instanceof Indent){
                        int indent = ((Indent)o).getIndent();
                        if (indent < 0){
                            increase = 0;
                            indentNum += indent;
                        }else
                            increase = indent;
                    }else
                        increase = 0;

                    if (o instanceof IStorage) {
                        IStorage var = (IStorage) o;
                        String s = var.getStorageString(p);
                        if ((s != null) && (s.length() > 0)) {
                            if (prev != null) {
                                writeIndent(writer, indentNum);
                            }
                            writer.print(s);
                        }
                    } else {
                        if (o instanceof Character) {
                            if (!(prev instanceof Character)) {
                                writeIndent(writer, indentNum);
                            }
                        } else {
                            writeIndent(writer, indentNum);
                        }
                        String s = o.toString();
                        if (s != null) {
                            writer.print(s);
                        }
                    }
                    indentNum += increase;
                }
                prev = o;
            } while (p.next());
            p.remove();
            writer.close();
        } catch (IOException e) {
            Log.error(e);
        }

    }

    static void writeIndent(PrintWriter writer, int indentNum) {
        writer.print((char) 0x0A);
        for (int i = 0; i < indentNum; i++) {
            writer.print((char) 9);
        }
    }

    static boolean notEmpty(String s){
        return (s != null) && (s.length() > 0);
    }

    public static void printBegin(String name, PrintWriter writer){
        writer.println('<');
        writer.print(name);
        writer.print('>');
    }

    public static void printEnd(String name, PrintWriter writer){
        writer.println('<');
        writer.print(name);
        writer.print("/>");
    }
    public static void printBegin(Key name, PrintWriter writer) {
        printBegin(name.toString(), writer);
    }
    public static void printEnd(Key name, PrintWriter writer) {
        printEnd(name.toString(), writer);
    }


}
