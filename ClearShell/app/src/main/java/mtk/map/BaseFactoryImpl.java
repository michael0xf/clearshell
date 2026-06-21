package mtk.map;

import com.mtk.map.*;
import com.mtk.map.i.*;
import mtk.util.Bridge;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public class BaseFactoryImpl implements IFactory {

    @Override
    public ICalculator getCalculator(Key name) {


        try {
            final Method method = getClass().getMethod(name.toString(), Item.class);
            if (method != null) {
                ICalculator calculator = new ICalculator() {

                    @Override
                    public boolean calculate(Item item) {
                        try {
                            method.invoke(null, item);
                            return true;
                        } catch (IllegalAccessException e) {
                            Log.error(e);
                        } catch (IllegalArgumentException e) {
                            Log.error(e);
                        } catch (InvocationTargetException e) {
                            Log.error(e);
                        }
                        return false;
                    }

                    @Override
                    public String getStorageStringForValue(Item item) {
                        Object value = item.getNativeValue();
                        if (value instanceof String) {
                            return (String) value;
                        } else if (value instanceof Bytes) {
                            return value.toString();
                        } else if (value instanceof Link) {
                            return value.toString();
                        } else
                            return "";
                    }
                };
                return calculator;
            }
        } catch (NoSuchMethodException ex) {
            Log.mess("Method " + name + " not found");
        } catch (SecurityException ex) {
            Log.error(ex);
        }
        return null;
    }

    @Override
    public boolean isIndexed(Key key) {
        return false;
    }

    public static void copy(Item var) {
        Object o;
        Tag src = var.getTagByName("from");//get Tag by link which as value of any attribute "from", from any level
        if (src != null) {
            Item dst = var.getItemByName("to");//get Attribute|Tag|Var by link which as value of any attribute "copyTo", from any level
            if (dst == null)
                dst = var;
            Tag c = src.cloneTo(dst.getTag().getTail());
            var.setValue(c);
        }
    }

    public static void If(Item var) {
        Item sw = var.getItemByName("switch");//get Attribute|Tag|Var by link which as value of any attribute "switch", from any level
        if (sw == null)
            return;
        String name = sw.getString();
        if (name.length() == 0)
            return;
        Item c = MapUtil.getChild(var, Key.getOrNew(name), false);//get first Attribute|Tag|Var by name from internal space of var
        if (c != null)
            MapUtil.run(c);
    }

    public static void remove(Item var) {
        Item target = var.getItemByName("target");
        if (target == null)
            return;
        String name = var.getStringByName("name");//get String which as value of any attribute "name", from any level
        if ((target != null) && (name.length() > 0)) {
            Item v = target.getAttrByName(name);
            if (v != null)
                v.remove();
        }
    }

    public static void wholeDelete(Item var) {
        Item target = var.getItemByName("target");
        if (target == null)
            return;
        String name = var.getStringByName("name");//get String which as value of any attribute "name", from any level
        if ((target != null) && (name.length() > 0)) {
            Item v = target.getAttrByName(name);
            if (v != null) {
                v.getTag().clearBody();
                v.remove();
            }
        }
    }

    public static void add(Item var) {
        Item target = var.getItemByName("target");
        if (target == null){
            return;
        }
        String name = var.getStringByName("name");
        Object value = var.getOrNewObjectByName("value");//Get Object which as value of any attribute "value", from any level, if it'll not found, then create it from IFactory, which was defined in BaseRoot
        if ((target != null) && (name.length() > 0)) {
            target.getOrNewAttrByName(name).setValue(value);
        }
    }

    public static void set(Item var) {
        Item target = var.getItemByName("target");
        if (target == null)
            return;
        String name = var.getStringByName("name");
        Object value = var.getOrNewObjectByName("value");
        if ((target != null) && (name.length() > 0)) {
            Item v = target.getAttrByName(name);
            if (v != null) {
                v.setValue(value);
            }
        }
    }



    public static void mirror(final Item var) {
        final String objectLink = '@' + var.getOrNewStringByName("src");
        var.getTag().clearBody();
        String tName = var.getStringByLink("@sourceThread");//Get String by global reference, from any branch and any level, with the advantage of immediate relatives

        final MapThreads.MapThread srcThread = MapThreads.getInstance().getThreadByName(tName);
        final MapThreads.MapThread dstThread = MapThreads.getMtkThread(var);

        final MapThreads.MapThread ct = MapThreads.getInstance().getIThread();
        if (ct != dstThread) {
            Log.error("ct != dstThread");
            return;
        }

        final Tag.Tail dstTail = var.getTag().getTail();

        Bridge r = new Bridge() {
            @Override
            public void run() {
                Item srcVar = MapUtil.getEndItemByValue(srcThread.getRoot(), objectLink, false);
                srcVar.getTag().cloneTo(dstTail);
                ok();
            }

            @Override
            public void dstEnd() {

            }
        };
        r.waitForComplete(srcThread, dstThread);
        //notify(var, true);
    }

    static Key NEXT = Key.getOrNew("next");

    public static void next(Item var) {
        Item n = var.getCursor().getLocalItem(NEXT);
        if (n != null) {
            MapUtil.run(MapUtil.getEndItemByValue(n, true));
        }
    }

    public static void call(Item var) {
        Item target = var.getItemByName("target");
        if (target == null)
            return;
        String name = var.getStringByName("name");
        if ((target != null) && (name.length() > 0)) {
            MapUtil.runEndItem(target, name);
        }
    }

    public static void clearBody(final Item var) {
        Cursor end = var.getTag().getTail();
        if (!end.isNextFor(var.getTag())) {
            final Cursor p = new Cursor(var.getTag());
            while (p.getAddress() < end.getAddress()) {
                Object obj = p.getNext();
                if (obj instanceof Link) {
                    Item v = MapUtil.getEndItemByLink(var, (Link) obj, false);
                    v.getTag().clearBody();
                }
                p.next();
            }
            p.remove();
        }
    }



    public static void removeAll(final Item var) {
        Selector l = new Selector() {
            Array buf;
            @Override
            public boolean addElement(ILet let) {
                if (let.isTail()) {
                    return true;
                }
                buf.addElement(let.getItem());
                return true;
            }

            @Override
            public void init(int size) {
                buf = new Array(size >> 1);
            }

            @Override
            public void clear() {
            }

            @Override
            public void end() {
                if (buf != null){
                    for(int i = 0; i < buf.size(); i ++){
                        Item item = (Item)buf.elementAt(i);
                        item.remove();
                    }
                }
            }
        };
        Cursor end = var.getTag().getTail();
        if (!end.isNextFor(var.getTag())) {
            final Cursor p = new Cursor(var.getTag());
            while (p.getAddress() < end.getAddress()) {
                Object obj = p.getNext();
                if (obj instanceof Bytes) {
                    var.getAllChildrenByName(obj, l);
                }
                p.next();
            }
            p.remove();
        }

    }

    public static void removeChildren(final Item var) {
        Item target = var.getItemByName("target");

        Selector l = new Selector() {
            Array buf;
            @Override
            public boolean addElement(ILet let) {
                if (let.isTail()) {
                    return true;
                }
                buf.addElement(let.getItem());
                return true;
            }

            @Override
            public void init(int size) {
                buf = new Array(size >> 1);
            }

            @Override
            public void clear() {
            }

            @Override
            public void end() {
                if (buf != null){
                    for(int i = 0; i < buf.size(); i ++){
                        Item item = (Item)buf.elementAt(i);
                        item.remove();
                    }
                }
            }
        };
        Cursor end = var.getTag().getTail();
        if (!end.isNextFor(var.getTag())) {
            final Cursor p = new Cursor(var.getTag());
            while (p.getAddress() < end.getAddress()) {
                Object obj = p.getNext();
                if (obj instanceof Bytes) {
                    target.getChildrenByName(obj, l);
                }
                p.next();
            }
            p.remove();
        }

    }

    public static void clearChildren(final Item var) {
        Item target = var.getItemByName("target");
        Selector l = new Selector() {
            @Override
            public boolean addElement(ILet let) {
                if (let.isTail()) {
                    return true;
                }
                Item v = MapUtil.getEndItemByValue(let.getItem(), false);
                if (v != null)
                    v.setValue(null);
                return true;
            }

            @Override
            public void init(int size) {
            }

            @Override
            public void clear() {
            }

            @Override
            public void end() {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        };
        Cursor end = var.getTag().getTail();
        if (!end.isNextFor(var.getTag())) {
            final Cursor p = new Cursor(var.getTag());
            while (p.getAddress() < end.getAddress()) {
                Object obj = p.getNext();
                if (obj instanceof Bytes) {
                    target.getChildrenByName(obj, l);
                }
                p.next();
            }
            p.remove();
        }

    }


    public static void clearAll(final Item var) {
        Cursor end = var.getTag().getTail();
        Selector l = new Selector() {
            @Override
            public boolean addElement(ILet let) {
                Item v = MapUtil.getEndItemByValue(let.getItem(), false);
                if (v != null){
                    v.setValue(null);
                }
                return true;
            }

            @Override
            public void init(int size) {
            }

            @Override
            public void clear() {
            }

            @Override
            public void end() {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        };
        if (!end.isNextFor(var.getTag())) {
            final Cursor p = new Cursor(var.getTag());
            while (p.getAddress() < end.getAddress()) {
                Object obj = p.getNext();
                if (obj instanceof Bytes) {
                    var.getAllChildrenByName(obj, l);
                }
                p.next();
            }
            p.remove();
        }

    }

    //We often use instanceOf because data store it is intended to various of type data pell-mell with system objects.
    public static void runByType(Object obj, Item var, Cursor p) {
        if (obj instanceof Tag) {
            MapUtil.run((Tag) obj, null);
            p.goTo(((Tag) obj).getTail());
        } else if (obj instanceof Link) {
            MapUtil.run(var, (Link) obj);
            p.next();
        } else if (obj instanceof Var) {
            MapUtil.run((Var) obj, null);
            p.next();
        } else if ((obj instanceof Bytes) || (obj instanceof String)) {
            String s = obj.toString();
            String name = p.getStringByName(s);
            if (name.length() == 0){
                name = s;
            }
            Item target = var.getItemByName("target");
            if (target == null)
                target = var;
            MapUtil.runEndItem(target, Key.getOrNew(name));
            p.next();
        } else
            p.next();
    }

    public static void run(final Item var) {
        Cursor end = var.getTag().getTail();
        if (!end.isNextFor(var.getTag())) {
            final Cursor p = new Cursor(var.getTag());
            while (p.getAddress() < end.getAddress()) {
                Object obj = p.getNext();
                runByType(obj, var, p);
            }
            p.remove();
        }

        Item v = var.getTag().getItemByName("deleteAndGo");
        if (v != null){
           // Item n = var.getItemByLink((Link)v.getNativeValue());
            var.getTag().wholeDelete();
            MapUtil.run(v);
        }
    }
}

