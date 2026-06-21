package mtk.util;


import com.mtk.map.HTable;

import java.util.Enumeration;
import java.util.Vector;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */

public class MtkThreads {


    protected final static int PAUSE_PRIORITY = Thread.MIN_PRIORITY;

    //private final Hashtable data = new Hashtable(4);
    private final HTable data = new HTable(4);

    public static MtkThreads getInstance() {
        return instance;
    }

    public static void setInstance(MtkThreads mtkThreads) {
        instance = mtkThreads;
    }

    private static MtkThreads instance;

    public synchronized void allThreadsPause() {
            Enumeration e = threadsByName.elements();
            while (e.hasMoreElements()) {
                MtkThread t = (MtkThread) e.nextElement();
                t.pause();
            }

    }

    public synchronized void allThreadsSlow() {
            Enumeration e = threadsByName.elements();
            while (e.hasMoreElements()) {
                MtkThread t = (MtkThread) e.nextElement();
                t.setPriority(PAUSE_PRIORITY);
            }
    }


    public synchronized void allThreadsGo() {
        Enumeration e = threadsByName.elements();
        while (e.hasMoreElements()) {
            MtkThread t = (MtkThread) e.nextElement();
            t.resume();
        }
    }

    public synchronized void allThreadsPause(Thread without) {
        Enumeration e = threadsByName.elements();
        while (e.hasMoreElements()) {
            MtkThread t = (MtkThread) e.nextElement();
            if (t.t != without)
                t.pause();
        }
    }

    public synchronized void allThreadsStop(Thread without) {
        Enumeration e = threadsByName.elements();
        while (e.hasMoreElements()) {
            MtkThread t = (MtkThread) e.nextElement();
            t.stop();
        }
    }

    public synchronized MtkThread getOrNewThreadByName(final String name) {
            MtkThread t = getThreadByName(name);
            if (t == null) {
                t = new MtkThread(name, this);
                return t;
            } else
                return t;
    }



    public synchronized MtkThread getThreadByName(final String name) {
        return (MtkThread) threadsByName.get(name);
    }

    public synchronized boolean isThread(String name) {
            MtkThread t = (MtkThread) data.get(Thread.currentThread());
            return (t != null) && t.getName().equals(name);
    }

    public synchronized MtkThread getIThread() {
            return (MtkThread) data.get(Thread.currentThread());
    }


    //protected final Hashtable threadsByName = new Hashtable(4);
    private final HTable threadsByName = new HTable(4);

    protected synchronized Enumeration elements(){
        return threadsByName.elements();
    }
    
    protected synchronized void put(Thread t, MtkThread mt){
        data.put(t, mt);
    }

    /**
     * Created by MKRAFT on 0022 22 August.
     */
    public static class MtkThread implements Runnable {

        protected Vector updated = new Vector(8);
        protected Vector timed = new Vector(8);

        public void addTimed(final ITimed r) {
            timed.addElement(r);
        }

        public void addUpdated(final Runnable r) {
            updated.addElement(r);
        }

        public void removeUpdated(final Runnable r) {
            updated.removeElement(r);
        }

        public void removeTimed(final ITimed r) {
            timed.removeElement(r);
        }

        String name;
        protected MtkThreads mtkThreads;
        public MtkThread(String name, MtkThreads mtkThreads){
            this.name = name;
            this.mtkThreads = mtkThreads;
            mtkThreads.threadsByName.put(name, this);
        }

        public int desirablePriority() {
            return Thread.NORM_PRIORITY;
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return name;
        }


        public void resume(){
            pause = false;
            setPriority(desirablePriority());
        }

        public void start(){
            if (runing)
                return;
            pause = false;
            runing = true;
            t = new Thread(this);
            mtkThreads.put(t, this);
            setPriority(desirablePriority());
            t.start();
        }

        public void run() {
            runing = true;
            while (runing) {
                subrun0();
            }
            runing = false;
            t = null;
        }


        /*public VarStack getStack() {
            synchronized (threadsByName) {
                if (stack == null)
                    stack = new VarStack();
                return stack;
            }
        }*/

        public void waitForComplete(Bridge bridge, boolean toSleep) {
            while (!bridge.complete) {
                yeild();
                if (!toSleep)
                    subrun0();
            }
            bridge.dstEnd();
        }


        public Thread getThread() {
            return t;
        }

        public void putRunnable(Runnable runnable) {
            if (t == null){
                start();
            }
            events().addElement(runnable);
        }


        public Thread t;

        public Thread thread() {
            return t;
        }


        //int p = Thread.NORM_PRIORITY;
        public void setPriority(int p) {
            if (t != null)
                t.setPriority(p);
            //this.p = p;
        }

        public boolean isAlive() {
            if (!runing)
                return false;
            if (t == null)
                return false;
            return t.isAlive();
        }

        public boolean runing;
        public boolean pause = false;

        public void pause() {
            setPriority(PAUSE_PRIORITY);
            pause = true;
        }

        public void stop() {
            runing = false;
        }

        void nop() {
            yeild();
            long time = System.currentTimeMillis();
            for(int i = 0; runing && (i < timed.size()); i++){
                ITimed c = (ITimed) timed.elementAt(i);
                if (c != null) {
                    try {
                        c.update(time);
                    } catch (Exception ee) {
                        Log.error(ee, "error 1845675");
                    }
                }

            }
        }

        public  void yeild() {
            //Thread.currentThread().yield();
            try {
                Thread.currentThread().sleep(10);
            } catch (Exception e) {
                Thread.currentThread().yield();
            }
        }

        public Vector events;

        protected Vector events() {
            if (events == null)
                events = new Vector();
            return events;
        }

        public void subrun() {
            if (events != null){
                while (events.size() > 0) {
                    Object obj = events.elementAt(0);
                    events.removeElementAt(0);
                    checkEvent(obj);
                }
            }
            for(int i = 0; (!pause) && runing  && (i < updated.size()); i++){
                checkEvent(updated.elementAt(i));
                Thread.yield();
            }
            nop();
        }

        boolean processing = false;
        public boolean processing(){
            return processing;
        }

        public void subrun0() {
            try {
                if (!pause) {
                    processing = true;
                    subrun();
                    processing = false;
                    yeild();
                } else
                    nop();
            } catch (Exception e) {
                Log.error(e, this.toString());
                yeild();
            }

        }


        public boolean checkEvent(Object obj) {
            if (obj instanceof ITimed) {
                ITimed c = (ITimed) obj;
                if (c != null) {
                    try {
                        c.update(System.currentTimeMillis());
                        return true;
                    } catch (Exception e) {
                        Log.error(e, "error 1845675");
                    }
                }
            } else if (obj instanceof Runnable) {
                Runnable c = (Runnable) obj;
                if (c != null) {
                    try {
                        c.run();
                        return true;
                    } catch (Exception e) {
                        Log.error(e, "error 1845666");
                    }
                }
            }
            return false;
        }

    }


    public MtkThread getFileThread(){
        return getOrNewThreadByName("FileThread");
    }
}
