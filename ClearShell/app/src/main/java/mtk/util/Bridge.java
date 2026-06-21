package mtk.util;


import mtk.util.MtkThreads;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */

public class Bridge implements Runnable{
    public boolean complete = false;


    public void waitForComplete(MtkThreads.MtkThread srcMtkThread, MtkThreads.MtkThread dstMtkThread) {
        start(srcMtkThread);
        dstMtkThread.waitForComplete(this, true);
    }

    public void start(MtkThreads.MtkThread t) {
        t.putRunnable(this);
    }

    public void run() {
        ok();
        dstEnd();
    }

    public void dstEnd() {

    }

    public boolean isComplete() {
        return complete;
    }

    public void ok() {
        complete = true;

    }

}
