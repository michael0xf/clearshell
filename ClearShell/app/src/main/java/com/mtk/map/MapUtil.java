package com.mtk.map;

import com.mtk.map.i.ICalculator;
import com.mtk.map.i.Item;
import com.mtk.map.i.Selector;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public class MapUtil {


    public static void clearNodes(Nodes nodes){
        int prev = nodes.size();
        while(nodes.size() != 0)
            /*for (int i = 0; i < nodes.size(); i++)*/{
            IVector v = (IVector)nodes.code().elementAt(nodes.size() - 1);
            v.removeElementAt(nodes.value().elementAt(nodes.size() - 1));
            if (nodes.size() == prev){
                Log.error("nodes.size() == prev");
            }else
                prev = nodes.size();
        }

    }

    public static boolean run(Item item, Link link){
        if ((item == null)&&(link == null))
            return false;
        Item runnable;
        if (link == null){
            runnable = MapUtil.getEndItemByLink(item, link, true);
        }else{
            runnable = MapUtil.getEndItemByLink(item, link, false);
        }
        if (runnable != null) {
            return runEndItem(runnable);
        }
        return false;
    }

    public static boolean setAndRun(Item item, Link link, String name, Object value){
        if ((item == null)&&(link == null))
            return false;
        Item runnable = MapUtil.getEndItemByLink(item, link, true);
        if (runnable != null) {
            runnable.getOrNewAttrByName(name).setValue(value);
            return runEndItem(runnable);
        }
        return false;
    }

    public static Key getMethodName(Tag item){
        String methodName = item.getLocalStringByName("method");
        if (methodName.length() > 0){
            return Key.getOrNew(methodName);
        }
        return item.getName();

    }
    
    public static boolean runEndItem(Item item){

        if (item.getTag() == item){
            Key name = getMethodName((Tag)item);
            return runEndItem(item, name);
        }else{
            return runEndItem(item, item.getName());
        }
    }

    public static boolean run(Item item){
        item = MapUtil.getEndItemByValue(item, false);
        return runEndItem(item);
    }

    public static boolean runEndItem(Item item, String methodName){
        return runEndItem(item, Key.getOrNew(methodName));
    }

    public static boolean runEndItem(Item item, Key methodName){
        KeyDict kd = item.getCursor().getBaseRoot().getOrNewKeyDict(methodName);
        ICalculator m = kd.getCalculator();
        if (m != null)
            return m.calculate(item);
        else
            return false;

    }


    public static ICalculator getCalculator(Cursor current, String name){
        Key methodName = Key.getOrNew(name);
        Item item = current.getAnyItem(methodName);
        if (item != null){
            item = MapUtil.getEndItemByValue(item, false);
            if (item.getTag() == item){
                methodName = getMethodName((Tag)item);
            }else{
                methodName = item.getName();
            }
        }
        return current.getBaseRoot().getOrNewKeyDict(methodName).getCalculator();

    }


    public static Item getChild(Item var, Key k, boolean scanNode){
        Cursor p = var.getCursor();
        KeyDict t = p.getBaseRoot().getOrNewKeyDict(k);
        return t.getChild(var.getCursor().getAddress(), var, scanNode);
    }


    public static Item getEndItemByValue(Item var, boolean orNew) {
        return getEndItemByLink(var, getLink(var.getNativeValue()), orNew);
    }

    public static Item getEndItemByValue(IPointer var, Object linkObject, boolean orNew) {
        Link link = MapUtil.getLink(linkObject);
        return getEndItemByLink(var, link, orNew);
    }

    public static Item getFinalVar(Item var){
        Object o = var.getNativeValue();
        while(o instanceof Item){
            var = (Item)o;
            o = var.getNativeValue();
        }
        return var;
    }
   /* public static Item getEndPointerByLink(Item var, Object linkObject, boolean orNew) {
        Link link = MapUtil.getLink(linkObject);
        return getEndPointerByLink(var, link, orNew);
    }*/
    


    public static Item getFirstChild(Item var, Key k){
        Cursor p = var.getCursor();
        KeyDict t = p.getBaseRoot().getOrNewKeyDict(k);
        return t.getFirstChild(p.getAddress(), var.getTag().getTail().getAddress());
    }

    public static Item getNextChild(Cursor p, Key k){
        KeyDict t = p.getBaseRoot().getOrNewKeyDict(k);
        return t.getFirstChild(p.getAddress(), p.getBaseRoot().getTail().getAddress());
    }

    public static Item getPrevChild(Cursor p, Key k){
        KeyDict t = p.getBaseRoot().getOrNewKeyDict(k);
        return t.getLastChild(p.getAddress(), p.getBaseRoot().getTail().getAddress());
    }



    public static Item getEndPointerByLink(Cursor p, Link link, boolean orNew) {
        Item var;
        if (p instanceof Item){
            var = getFinalVar((Item)p);
        }else{
            var = null;
        }
        IntTable table = null;
        int i = 0;
        do {
            for (; (link != null) && (i < link.size()); i++) {
                Link.TLink tl = (Link.TLink) link.elementAt(i);
                Item prev = var;
                var = tl.extract(var);
                if ((var == null)&&(orNew)) {
                    var = tl.create(prev);
                }
                if (var != null) {
                    var = getFinalVar(var);
                    Object o = checkLink(var.getNativeValue());
                    if (o instanceof Link) {
                        if (i < link.size() - 1) {
                            if (table == null) {
                                table = new IntTable(2);
                            }
                            table.put(link, ++i);
                        }
                        link = (Link) o;
                        i = -1;
                    }
                }else
                    return prev;
            }
            if ((table != null) && (table.size() > 0)) {
                link = (Link) table.code().lastElement();
                i = table.pop();
            } else {
                if (var != null) {
                    link = getLink(var.getNativeValue());
                    if (link == null)
                        break;
                }else
                    break;
            }
        }while(true);
        return var;


    }
    public static Item getEndItemByLink(IPointer p, Link link, boolean orNew) {
        Item var;
        if (p instanceof Item){
            var = getFinalVar((Item)p);
        }else{
            var = null;
        }
        IntTable table = null;
        int i = 0;
        do {
            int counter = 0;
            for (; (link != null) && (i < link.size()); i++) {
                Link.TLink tl = (Link.TLink) link.elementAt(i);
                Item prev = var;
                var = tl.extract(var);
                if ((var == null)&&(orNew)) {
                    var = tl.create(prev);
                }
                if (var != null) {
                    var = getFinalVar(var);
                    Object o = checkLink(var.getNativeValue());
                    if (o instanceof Link) {
                        if (i < link.size() - 1) {
                            if (table == null) {
                                table = new IntTable(2);
                            }
                            table.put(link, ++i);
                        }
                        link = (Link) o;
                        i = -1;
                    }
                }else{
                    return null;
                }
                counter++;
                if (counter > 0xff){
                    throw new StackOverflowError(var.getName().toString());
                }
            }
            if ((table != null) && (table.size() > 0)) {
                link = (Link) table.code().lastElement();
                i = table.pop();
            } else {
                if (var != null) {
                    link = getLink(var.getNativeValue());
                    if (link == null)
                        break;
                }else
                    break;
            }
        }while(true);
        return var;


    }

    public static String getTextFromString(String s){
        if ((s != null)&&(s.length() > 0)){
            if (Link.checkFix(s.charAt(0))){
                return s.charAt(0) + s;
            }
        }
        return s;
    }

    public static String getStringFromText(String s){
        if ((s != null)&&(s.length() > 1)){
            if (Link.checkFix(s.charAt(0))&&(Link.checkFix(s.charAt(1)))){
                return s.substring(1, s.length());
            }
        }
        return s;
    }

    public static Link getLink(Object o){
        Object o2 = checkLink(o);
        if (o2 instanceof Link)
            return (Link)o2;
        else
            return null;
    }
    public static Object checkLink(Object o) {
        if (o instanceof String) {
            String s = (String) o;
            if ((s.length() > 1) && Link.checkFix(s.charAt(0)) && (!Link.checkFix(s.charAt(1)))) {
                Bytes b = new Bytes(s.getBytes(), false);
                return new Link(b);
            } else
                return o;
        } else if (o instanceof Bytes) {
            Bytes s = (Bytes) o;
            if ((s.size() > 1) && Link.checkFix(s.get(0)) && (!Link.checkFix(s.get(1)))) {
                return new Link(s.clone());
            } else
                return o;

        }
        return o;
    }

    public static Item getPointer(Item var, boolean orNew) {
        return getEndItemByValue(var, orNew);
    }
   /* public static void getChildren(Selector selector, Tag tag, Key k, boolean scanNode){
        KeyDict t = tag.getBaseRoot().getOrNewKeyDict(k);
        t.getChildren(selector, tag, scanNode);
    }*/

    public static void getChildren(Selector selector, Item var, Key k, boolean scanNode){
        Cursor p = var.getCursor();
        KeyDict t = p.getBaseRoot().getOrNewKeyDict(k);
        t.getChildren(selector, var.getTag(), scanNode);
    }
}
