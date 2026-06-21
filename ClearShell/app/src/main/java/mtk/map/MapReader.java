package mtk.map;

import com.mtk.map.*;
import com.mtk.map.i.ILet;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Properties;

import mtk.util.*;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */

public class MapReader implements DocHandler {
    public static interface MapReaderListener{
        public void run(Tag root);
    }
    MapReaderListener endListener;
    Cursor cursor;
    Tag root;
    XParserImpl xParserImpl;


    public MapReader(final MapReaderListener endListener, final Cursor destination, XParserImpl xParserImpl) {
        this.endListener = endListener;
        this.cursor = new Cursor(destination);
        this.xParserImpl = xParserImpl;
    }

    Tag justTag = null;
    @Override
    public void startElement(final Bytes n, final UTable args) {
        if (n == null)
            return;
        final Bytes name = n.clone();
        Tag tag = new Tag(name, cursor, false, args);
        justTag = tag;
        if (root == null)
            root = tag;
    }

    @Override
    public void endElement(Bytes nn) {
        if (nn == null)
            return;
        justTag = null;
        final Key name = Key.getOrNew(nn.clone());
        moveTail(findTail(name));
    }

    Tag.Tail findTail(Key name){
        ILet current = cursor.getGlobalLet(name);
        if (current == null) {
            mtk.util.Log.error("current " + name.toString() + " == null ");
            return null;
        }
        if (current instanceof Tag) {
            return ((Tag) current).getTail();
        } else {
            return findUpperTail(current);
        }
    }
    
    Tag.Tail findUpperTail(ILet current){
        LiveVector lv = current.getUpper(); //the upper items named by needed name only
        if (lv != null) {
            for (int i = lv.size() - 1; i >= 0 ; i--) {
                current = (ILet) lv.elementAt(i);
                Tag begin = current.getItem().getTag();
                if (begin.getName().equals(current.getName())){ //because maybe current item is attribute and his tag has other name
                    Tag.Tail end = begin.getTail();
                    if (end.getAddress() > cursor.getAddress()){
                        return end;
                    }
                }
            }
        }
        return null;
    }
    
    
    
    public void moveTail(Tag.Tail tail){
        if (tail != null) {
            if (tail.getAddress() < cursor.getAddress()) {
                if (!tail.isPrevFor(cursor)) {
                    tail.goToAt(cursor);
                }
            } else {
                if (!tail.isNextFor(cursor)) {
                    tail.goToAt(cursor);
                }else{
                    cursor.next();
                }
            }
            rename(tail.getTag());
        }
    }

    public void rename(Tag tag) {
        Key name = tag.getName();
        int i = name.indexOf(':');
        if (i > 0) {
            tag.rename(name.subKey(0, i));
        }
    }

    @Override
    public void startDocument() {
        mtk.util.Log.mess("Start...");
    }


    @Override
    public void endDocument() {
        cursor.remove();
        endListener.run(root);
    }

    @Override
    public void text(Bytes text) {
        if (cursor.isHtmlStyle()) {
            cursor.addPrev(text.clone());
            return;
        }
        if (justTag == null){
            justTag = cursor.getNode();
        }
        if (cursor.isJSON()) {
            try {
                JSONObject jsonObject = new JSONObject(text.toString());
                new Var(justTag, Cursor.JSON, jsonObject, cursor.getLeaf(), cursor.getIndex());
            } catch (JSONException e) {
                Log.error(e);
            }
            return;
        }
        Bytes n = text.clone();
        n.trimBegin();
        if (n.size() == 0) {
            return;
        }
        for (Bytes line = n.removeFirstLine(); line != null; line = n.removeFirstLine()) {
            line.trimBegin();
            if (line.size() > 0) {
                int i = line.find('=');
                if (i >= 0) {
                    Bytes value = line.divideAt(i);
                    if ((value != null) && (value.size() > 0)) {
                        new Var(justTag, line, value, cursor.getLeaf(), cursor.getIndex());
                    } else {
                        new Var(justTag, line, null, cursor.getLeaf(), cursor.getIndex());
                    }
                } else {
                    cursor.addPrev(com.mtk.map.MapUtil.checkLink(line));
                }
            }
        }
    }


    @Override
    public XParserImpl getXParserImpl() {
        return xParserImpl;
    }



}
