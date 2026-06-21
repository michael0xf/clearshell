package mtk.util;

import java.io.InputStream;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */

public class BIS0 extends InputStream{
    public void write(int c, int len)
    {
        if (data==null)
        {
            data = new byte[len];
        }
        if (data.length == meter)
        {
            if (len > data.length)
            {
                byte[] d2 =new byte[len];
                System.arraycopy(data,0,d2,0,data.length);
                data = d2;
            }
        }
        
        data[meter] = (byte)c;
        meter++;
    }    
    public BIS0(byte[] data)
    {
        this.data = data;
    }
    byte[] data;
    public int getLength()    
    {
        return data.length;
    }
    public void setPosition(int p)
    {
        meter = p;
    }
    public int meter=0;
    public byte[] readFully()
    {
        return data;
    }
    //public QDParser bred;
    public void free()
    {
        close();
        data = null;
        //System.gc();
    }
    
   /* public int readLeInt()
    {
        int ret = 0;
        int b0 = read();
        int b1 = read();
        int b2 = read();
        int b3 = read();
        if (b0 == -1)
            b0 = 0;
        if (b1 == -1)
            b1 = 0;        
        if (b2 == -1)
            b2 = 0;        
        if (b3 == -1)
            b3 = 0;     
        ret = (b3 << 24) + (b2 << 16) + (b1 << 8) + b0;
        return ret;
    }
    
    public int readLeShort()
    {
        int ret = 0;
        int b0 = read();
        int b1 = read();
        if (b0 == -1)
            b0 = 0;
        if (b1 == -1)
            b1 = 0;        
        ret = (b1 << 8) + b0;
        return ret;
        
    }*/
    
    public int skipBytes(int n)
    {
        return (int)skip(n);
    }
    
    public int available()
    {
        return data.length;
    }
    public void close()
    {
        close = true;
    }
    boolean close = false;    
    int mark = 0;
    public void  mark(int readlimit)
    {
        mark = meter-1;
    }
    public boolean markSupported()
    {
        return true;
    }
    public int read()
    {
        if (close)
        {
            Log.error("closed stream");
            int stop=1;
        }
        if (meter>=data.length)
            return -1;        
        try
        {

            int ret = data[meter];
            if (ret < 0)
                ret = 256 + ret;
            meter++;
            return ret;   
        }catch(Exception e)
        {
            Log.error(e, "64576");
            return -1;
        }

    }
    public int read(byte[] dst, int offset, int len)
    {
        if (close)
        {
            Log.error("closed stream");
            int stop=1;
        }
        
        try
        {
            if (meter >= data.length)
                return -1;
            if (len <= 0)
                return 0;                
            if (len + meter > data.length)
                len = data.length - meter;            
            int end = len + meter;
            System.arraycopy(data, meter, dst, offset, len);
            meter += len;
            return len;
        }catch(Exception e)
        {
            Log.error(e, "7067");
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
        if (meter + n > data.length)
            n = data.length - meter;
        meter += n;
        return n;

    }
    public byte[] data()
    {
        return data;
    }    
     
} 
