package mtk.map;

import com.mtk.map.*;
import com.mtk.map.i.IFactory;
import com.mtk.map.i.Item;
import mtk.util.Bridge;
import mtk.util.Util;
import mtk.util.XParserImpl;
import mtk.util.MtkThreads;

import java.io.*;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public class MapThreads extends MtkThreads {
    IFactory factory;
    public MapThreads(IFactory factory){
        this.factory = factory;
        setInstance(this);
    }

    public MapThreads(){
        this.factory = new BaseFactoryImpl();
        setInstance(this);
    }
    public static MapThreads.MapThread getMtkThread(Item var) {
        String nodeName = var.getCursor().getBaseRoot().getNodeName();
        MapThreads.MapThread t = MapThreads.getInstance().getThreadByName(nodeName);
        return t;
    }

    public static MapThreads getInstance() {
        return (MapThreads)MtkThreads.getInstance();
    }

    public synchronized MapThread getIThread() {
        return (MapThread) super.getIThread();
    }

    public synchronized MapThread getThreadByName(final String name) {
        return (MapThread) super.getThreadByName(name);
    }

    public synchronized MapThread getOrNewThreadByName(final String name, IFactory factory) {
        MapThread t = getThreadByName(name);
        if (t == null) {
            t = new MapThread(name, this, factory);
            return t;
        } else
            return t;
    }

    public void printLog() {
        MapThread t = getIThread();
        Log.printLog(t.getRoot());
    }

    public MapReader openXmlFile(String path, MapReader.MapReaderListener endListener) {

        InputStream is = Util.openGlobalFile(path);
        MapReader handler = new MapReader(endListener, getFileThread().getRoot(), new XParserImpl());
        handler.getXParserImpl().parse(is, handler, getFileThread());
        return handler;
    }

    @Override
    public MapThread getFileThread() {
        return getOrNewThreadByName("FileThread", factory);
    }

    public static interface LoadListener{
        public void complete(Tag dst);
    }

    public class MapThread extends MtkThread {

        BaseRoot root;

        IFactory factory;

        public MapThread(String name, MapThreads mtkMapThreads1, IFactory factory) {
            super(name, mtkMapThreads1);
            this.factory = factory;
            root = new BaseRoot(getName(), factory);
        }

        public void load(final String fileName, LoadListener loadListener) {
            load(fileName, root.getTail(), loadListener);

        }

        class Bridge2 extends Bridge{
            Bridge2(Tag src, Cursor dst){
                this.src = src;
                this.dst = dst;
            }
            Tag ret, src;
            Cursor dst;
            @Override
            public void run() {
                ret = (Tag)src.cloneTo(dst);
                src.clearBody();
                src.remove();
                ok();
            }
        }

        public void load(final String fileName, final Cursor dst, final LoadListener loadListener) {
            final MapThread srcThread = getFileThread();
            final MapReader.MapReaderListener starter = new MapReader.MapReaderListener() {
                public void run(final Tag src) {
                    MapThread.this.putRunnable(new Runnable() {
                        @Override
                        public void run() {
                            Bridge2 r = new Bridge2(src, dst);
                            r.waitForComplete(srcThread, MapThread.this);
                            loadListener.complete(r.ret);
                        }
                    });
                }
            };
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    openXmlFile(fileName, starter);
                }
            };
            srcThread.putRunnable(r);

        }

        public BaseRoot getRoot() {
            return root;
        }

        public void autoRun(){
            getRoot().run("@autoRun");
        }
    }
}
