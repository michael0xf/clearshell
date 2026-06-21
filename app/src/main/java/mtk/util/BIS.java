package mtk.util;

import com.mtk.map.Const;

import java.io.InputStream;
import java.io.*;


/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */

public class BIS extends BIS0 implements IFinalize, Const
{

    protected HttpConnection http;
    protected InputStream is;
    public BIS() { 
        super(null);
    }
    public BIS(InputStream is) {
        super(null);
        this.is = is;
    }
    public BIS(InputStream is, int length) {
        super(new byte[length]);
        this.is = is;
    }
    
    String url;
    public void set(byte[] b)
    {
        data = b;
    }
    
            
    String exception;
    String exception(Exception e)
    {
        close();
        exception = e.toString();
        return exception;
    }
    String exception(String e)
    {
        close();
        exception = e;
        return e;
    }    
    public String exception()
    {
        return exception;
    }
    boolean bred= false;
    public byte[] readFully()
    {
  //      long time = 0;
        
        
        if (!close)
        {
             bred = true;
//            time = System.currentTimeMillis();
            //log.log(">> begin readFully stream" + url);
        }
        readTo(MAX_VALUE);
        /*if (!close)
        {
            
            log.log("<< end of readFully stream" + url +" time: "+(int)(System.currentTimeMillis() - time));
        }else if( bred)
        {
            log.log("<< BRED of readFully stream" + url +" time: "+(int)(System.currentTimeMillis() - time));
        }*/
        bred = false;
        
        close();
        if (amount < data.length)
        {
            byte[] ret = new byte[amount];
            System.arraycopy(data,0,ret,0,amount);
            data = ret;
        }
        return data;
      /*  byte[] d = data;
        close();
        return d;*/
    }
    boolean cachable = false;
    public final static String ifms = "If-Modified-Since";
    final static String lm = "Last-Modified";
    String ms;

    public String open2(String url, boolean cachable, String desk, IPersistentObject cache)
    {
        return open(url, cachable, 0, desk, null, cache);
    }
    public String toString()
    {
        if (data != null)
            return data.toString();
        else            
            return "null " + url;
    }
    String desk;
    
    final static int MAX_VALUE = 0xffffff;
    public String post(String url, boolean cachable, String desk/*, boolean clrfOff*/, String args, IPersistentObject cache)
    {

        /*if (IO.MEGAFON)
            return open(url+"?"+args, cachable, 0, desk, null);
        else*/
            return open(url, cachable, 0, desk/*, clrfOff*/, args, cache);
    }

    static boolean UDEBUG = false;

    String open(String url, boolean cachable, int attempt, String desk/*, boolean clrfOff*/, String args, IPersistentObject cache)
    {
        /*if (IO.DEMO)
        {
            if (url.indexOf(mtk.lib.bb.IOUI.WIFI_SUF)<0)
                return "DEMO";
        }*/
        /*if (args!=null)
            log.log("0 open : " + url + " " + args);*/
        //log.log("args : " + args);
/*        if ((http != null) || (is != null))
        {
            log.error("((http != null) || (is != null))");
        }*/
        this.desk = desk;
        if (attempt > 1)
           return exception("Incrorrect redirect");
        OutputStream os = null;
        try
        {
            if (cachable)
                this.cachable = true;                          
            this.url = url;
            if (url.startsWith("https"))
                http = (HttpsConnection) Connector.open(url);
            else
            {

                http = (HttpConnection)Connector.open(url);

            }

            /*if (IO.MEGAFON && DeviceInfo.isSimulator())
            {
                http.setRequestProperty("X-NOKIA-MSISDN", "79219567049");
            }*/
            if (args != null)
            {
                http.setRequestMethod(HttpConnection.POST);
                byte[] b = args.getBytes();
                http.setRequestProperty("Content-Length", "" + b.length);                
                http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                os = http.openOutputStream();
                os.write(b);
                os.flush();
            }
            br:if (cachable)
            {
                
                byte[] obj= cache.getBytes(url);
                if (obj != null)
                {                        
                    try
                    {     
                        String s0 = cache.getString(ifms + url);
                        if (s0 != null)
                        {
                            http.setRequestMethod(HttpConnection.GET);
                            http.setRequestProperty(ifms, (String)s0);
                            int rc = http.getResponseCode();
                            if (rc == HttpConnection.HTTP_OK) 
                            {
                                try
                                {
                                    cache.removeBytes(url);
                                    cache.removeString(ifms + url);                                
                                }catch(Exception e)
                                {
                                    Log.error(e, "BIS 393");
                                }finally
                                {
                                    break br;
                                }
                            }
                            
                            if ((rc == 302)||(rc==301))
                            {
                                String fname = http.getHeaderField("location");
                                if ((fname != null)&&(attempt == 0))
                                {
                                    return open(fname, cachable, attempt+1, desk, args, cache);
                                }else
                                    return exception("Incrorrect redirect");
                            }
                            data = obj;
                            amount = data.length;
                            try
                            {
                                close();
                            }catch(Exception eeee)
                            {
                                return exception(eeee);                                   
                            }                           
                            return null;
                        }
                    }catch(Exception eee)
                    {
                        Log.error(eee, "error 600");
                        break br;
                    }
                }
                int rc = http.getResponseCode();
                if ((rc == 302)||(rc==301))
                {
                    String fname = http.getHeaderField("location");
                    if ((fname != null)&&(attempt == 0))
                    {
                        return open(fname, cachable, attempt+1, desk, args, cache);
                    }else
                        return exception("Incrorrect redirect");
                }
                if (rc != HttpConnection.HTTP_OK)
                {
                    return exception("" + rc);
                }
            }else
            {
                int rc = http.getResponseCode();
                //log.log("2 response code: " + rc);
                if ((rc == 302)||(rc==301))
                {
                    String fname = http.getHeaderField("location");
                    if ((fname != null)&&(attempt == 0))
                    {
                        try
                        {
                            http.close();
                        }catch(Exception e)
                        {
                        }
                        return open(fname, cachable, attempt+1, desk, args, cache);
                    }else
                        return exception("Incrorrect redirect");
                }
    
                if (rc != HttpConnection.HTTP_OK)
                    return exception("" + rc);
            }
            Finalizer.getFinalizers().addElement(this);
            ms = http.getHeaderField("Last-Modified");
            is = http.openInputStream();
            int length = http.getLength();

            //System.gc();
            if (length >= 0)
                data = new byte[length];
            amount = 0;
            Log.mess(" ok open : " + url + " " + args);
            if (UDEBUG)
            {
                //test
                if (url.indexOf("xml")>=0)
                {
                    Log.mess("len="+length+" "+url);
                    int len0 = 300;
                    String ret = readTo(len0);
                    if (ret!=null)
                        Log.mess("error "+ret+" "+url);
                    String s = new String(data,0,amount);
                    Log.mess("content "+url);
                    Log.mess("--------------");
                    Log.mess(s);
                    Log.mess("--------------");
                }
            }
            
            return null;

        }catch(Exception e)
        {
            Log.mess(" error open : " + url + " " + args+" " + e.toString());
            close();
            return exception(e);
        }finally
         {
             try
             {
                 //log.log("41 open");
                if (os!=null)
                        os.close();
                 //log.log("42 open: " + url);
              }catch(Exception e)
              {
                }
         } 
    }
    
    public int available()
    {
        try
        {
            if (is == null)
                return amount - meter;
            if ( meter == amount)
                return is.available();
            else
                return 
                    amount - meter + is.available();
        }catch(Exception e)
        {
            Log.error(e, "exc available");
            return 0;
        }
    }
    public void free()
    {
        close();
        data = null;
        url = null;
        //System.gc();
    }
    public int amount()
    {
        return amount;
    }
    public int meter()
    {
        return meter;
    }
    public boolean isClosed()
    {
        return close;
    }
    public void close(IPersistentObject cache)
    {
        try
        {
            Finalizer.getFinalizers().removeElement(this);
        }catch(Exception e)
        {
            Log.error(e, "exc 33");
        }
        close = true;
        try
        {
            if (data!=null)
            {
                if (amount == data.length)
                {
                    if (cachable&&(ms != null))
                    {
                        try{
                            cache.setBytes(url, data);
                            cache.setString(ifms + url, ms);
                        }catch(Exception e)
                        {
                            //log.log("exc 7");
                        }                        
                    }
                    cachable = false;
                }
                //data = null;                
            }
        }catch(Exception e)
        {
            Log.error(e, "66");
        }
            
        /*data = null;   
        opened = false;     
        //log.log("data == null CLOSE CLOSE CLOSE");
        meter=0;
        mark =0;*/
        try
        {
            if (is!=null)
            {
                is.close();
                is = null;
            }
        }catch(Exception e)
        {
            Log.error(e, "exc 34");
        }
        try
        {
            if (http!=null)
            {
                http.close();
                http = null;
            }
        }catch(Exception e)
        {
            Log.error(e, "exc 35");
        }
/*        mark = 0;
        meter = 0;*/
        //IO.ins.progressClose(this);
    }
    int amount;
    public void  mark(int readlimit)
    {
        mark = meter-1;
    }
    public boolean markSupported()
    {
        return true;
    }
    private int read0()
    {
        /*if (close)
        {
            IO.log.error("closed stream");
            int stop=1;
        }*/
        try
        {
            if (meter >= amount)
                return -1;
            int ret = data[meter];
            if (ret<0)
                ret=256+ret;
            meter++;
            return ret;   
        }catch(Exception e)
        {
            Log.error(e, "64");
            return -1;
        }
    }
    /*public boolean check()
    {
        if (data==null)
        {
            Exception e = open(url);
            if (e!=null)
            {
                log.log(e);
                return false;
            }
        }        
        return true;
    }*/

    public int read()
    {
        /*if (close)
        {
            IO.log.error("closed stream");
            int stop=1;
        }*/
     
      /*  if (!check())
        return -1;*/
        try
        {
            if (data==null)
            {
                if (close)
                    return -1;                 
            }
            if (meter < amount)
            {
                return read0();
            }else /*if (meter < length)*/
            {
                //log.log("rr2");
                readTo(meter);
                //log.log("rr3");
                return read0();
            }/*else
                return -1;*/
        }catch(Exception e)
        {
            Log.error(e, "65");
            return -1;
        }
    }
    //boolean clrfOff = false;
    /*void checkCLRF(int i)
    {
        if (data[i] == 0xA) 
        {
            data[i] = 32;            
        }else if (data[i] == 0xD)
        {
            data[i] = 32;
        }
    }*/
    int bredcounter = 1;
    public String readTo(int pos)
    {
        
        int i = amount;
        boolean close = false;
        try
        {
            if (is==null)
                return null;
            pos++;
            /*if ((pos < 0) || (pos >= length))
                pos = length;*/
            
            int avail = is.available();
            //int prevProgress = 0;
            for(;i < pos;)
            {
                if (avail > 0)
                {
/*                    if (bkgrd&&(!IO.getFileThread.runing))
                        return null;*/
                    if (data == null)
                    {
                        if (i==0)
                            data = new byte[avail];
                        else
                            return "BIS ((i==0)&&(data == null))";
                    }
                    if (data.length - i < avail)
                    {
                        int newb = avail;
                        if (avail == 1)
                        {
                            newb = bredcounter;
                            bredcounter = bredcounter << 1;
                            if (bredcounter > data.length)
                                bredcounter = data.length;
                        }
                        byte[] d2 = new byte[i + newb];
                        System.arraycopy(data, 0, d2, 0, i);
                        data = d2;
                    }
                    int len2 = is.read(data, i, avail); 
                    /*if (IO.DEBUG)
                        Thread.yield();*/
                    if (len2 < 0)                    
                    {
                        close = true;
                        return null;
                    }
                    else
                    {
                        /*if (clrfOff)
                        {
                            int ii = i - 1;
                            if (ii < 0)
                                ii = 0;       
                            int newlen = i + len2;                         
                            for(;ii < newlen; ii++)
                            {
                                checkCLRF(ii);
                            }
                        }*/
                        i += len2;
                        
                    }
                     /*if (i - prevProgress > 1024)
                     {
                        IO.ins.progressComplete(this, i);
                        prevProgress = i;
                     }*/
                     
                     /*try
                     {
                         Thread.sleep(1000);
                     }catch(Exception e)
                     {
                         
                     }*/
                     
                }else 
                {
                    /*int bred=1;
                    break;*/
                    int val = is.read();
                    /*if (IO.DEBUG)
                        Thread.yield();                    */
                    if (val < 0)
                    {
                        close = true;
                        return null;
                    }
                    if (data == null)
                    {
                        if (i==0)
                            data = new byte[0xff];
                        else
                            return "BIS ((i==0)&&(data == null))";
                    }
                        
                    if (i >= data.length)
                    {                        
                        int newb = bredcounter;
                        bredcounter = bredcounter << 1;
                        if (bredcounter > data.length)
                            bredcounter = data.length;                        
                        byte[] d2 = new byte[data.length + newb];
                        System.arraycopy(data, 0, d2, 0, data.length);
                        data = d2;
                    }                   
                         
                    data[i] = (byte)val;
                    /*if (clrfOff)
                        checkCLRF(i);*/
                        
                    i ++;
                }
                avail = is.available();
            }
            
        }catch(Exception e)
        {
            close = true;            
            return exception(e);
        }finally
        {
            amount = i;
            if (close)
                close();            
           /* if (amount == data.length)
                close();            */
        }
        return null;
    }
    public byte[] data()
    {
        if ((data==null)&&(!close))
            readTo(1);
        return data;
    }
    
    /*private String readTo(int pos)
    {
     
        int i = amount;
        int avail = 0;
        try
        {
            int attempt = 0;
            if (is==null)
                return null;
            pos++;
            Thread.yield();
            try
            {
                avail = is.available();
                log.log("0 avail="+avail+" "+url);
            }catch(Exception e)
            {
                log.log("0 exeption " + e.toString());
                return exception(e);
            }            
            //int prevProgress = 0;
            //int prevByte = -1;
            for(;i < pos;)
            {
                if (avail > 0)
                {
                    attempt = 0;
                    if (data == null)
                    {
                        data = new byte[avail + 1];
                    }else
                    if (data.length - i < avail)
                    {
                        byte[] d2 = new byte[i + avail + 1];
                        System.arraycopy(data, 0, d2, 0, i);
                        data = d2;
                    }
                    data[data.length-1] = 0;
                    Thread.yield();
                    int len2 = 0;
                    try
                    {
                        len2 = is.read(data, i, avail); 
                    }catch(Exception e)
                    {
                        log.log("1 exeption " + e.toString() +" i " + i);
                        if (i > 0)
                            return null;
                        else                        
                            return exception(e);
                    }                      
                    if (len2 < 0)
                    {
                        log.log("1 len2 " + len2 + " > 0 ?");
                        return null;
                    }
                    else
                        i += len2;
                     
                }else 
                {
                    int n = is.read();
                    log.log("is.read =" + n);
                    if (n > 0)
                    {
                        data[i] = (byte)n; // we allways alloc on 1 byte more then needed
                        i++;
                    }
                    if (attempt > 2 || n < 0)
                    {
                        log.log("end if stream a " + attempt);
                        return null;
                    }
                }
                Thread.yield();
                try
                {
                    avail = is.available();
                    log.log("1 avail="+avail+" "+url);
                    if (avail == 1)
                    {
                        int n = is.read();
                        log.log("was readed 13, 10 only - exit seems like EOF");
//                        log.log("2 is.read = {" + data[i-4] + "," + data[i-3] + "," + data[i-2] + "," + data[i-1] + "," + n + "}");
                        amount = i-1;
                        return null;
                    }
                }catch(Exception e)
                {
                        log.log("1 exeption " + e.toString());
                        if (i > 0)
                            return null;
                        else                    
                            return exception(e);
                }
            }
            
        }catch(Exception e)
        {
            log.log("3 exeption " + e.toString());
            return exception(e);
        }finally
        {
            amount = i;
        }
        return null;
    }*/
    

    public int read(byte[] dst, int offset, int len)
    {
        /*if (close)
        {
            IO.log.error("closed stream");
            int stop=1;
        }*/
      
     /*   if (!check())
            return -1;*/
        try
        {
            /*if (meter >= length)
                return -1;*/
            /*if (len + meter > length)
                len = length - meter;*/
            if (len <= 0)
                return 0;
            int end = len + meter;
            if (end > amount)            
                readTo(end);
            int rest = amount - meter;
            if (rest < len)
                len = rest;
            if (len <= 0)
                return -1;
            System.arraycopy(data, meter, dst, offset, len);
            meter += len;
            return len;
        }catch(Exception e)
        {
            Log.error(e, "70");
            return -1;
        }
        
    }
    
    
    
    
    public void reset()
    {
        meter = mark;
        if (meter < 0)
            meter = 0 ;
    }
    public long skip(long n)
    {
        /*if (close)
        {
            IO.log.error("closed stream");
            int stop=1;
        }*/
      
        int bakmeter = meter;
        readTo((int)(meter + n));
        meter += n;
        if (meter > amount)
            meter = amount;
        return meter - bakmeter;
   }
    
    
}
