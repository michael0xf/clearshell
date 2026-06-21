

package com.mtk.map;
/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public class Bytes implements Const {
    
    public Bytes subBytes(int begin, int end){
        return new Bytes(arr, offset + begin, end - begin, utf16);
    }
    
    public void copy(byte[] src, int srcOffset, int count, int to)
    {
        System.arraycopy(src, srcOffset, arr, offset + to, count);
    }
    
    public void setElementAt(int c, int index){
        if (index < amount){
            arr[offset + index] = (byte)c;
            hash = 0;
        }
    }

    public int indexOf(int ch){
        byte[] arr = this.arr;
        int offset = this.offset;
        int amount = this.amount;
        for(int i = 0; i < amount; i++){
            if (arr[offset + i] == ch)
                return i;
        }
        return -1;

    }

    public int lastIndexOf(int ch){
        byte[] arr = this.arr;
        int offset = this.offset;
        int amount = this.amount;
        for(int i = amount - 1; i >= 0; i--){
            if (arr[offset + i] == ch)
                return i;
        }
        return -1;

    }


    public void set(byte[] arr, int offset, int amount, boolean utf16)
    {
        this.arr = arr;
        this.offset = offset;
        this.amount = amount;
        //bred();
        this.utf16 = utf16;
        hash = 0;
        if (amount != 0)
            hashCode();
    }


    public void increaseOffset(){
        if ((offset < arr.length)&&(amount > 0)){
            offset++;
            amount--;
            hash = 0;
        }
    }
    public void decreaseOffset(){
        if (offset > 0){
            offset--;
            amount++;
            hash = 0;
        }
    }

    @Override
    public Bytes clone(){
        final Bytes b = new Bytes();
        b.utf16 = utf16;
        b.offset = offset;
        b.amount = amount;
        b.arr = arr;
        b.hash = hash;
        return b;
    }

    public void clear()
    {
        utf16 = false;
        this.offset = NIL;
        this.amount = 0;
        arr = null;
        hash = 0;        
    }
    

    public void set(Bytes b)
    {
        set(b.arr, b.offset, b.amount, b.utf16);
    }

    public boolean isUtf16(){
        return utf16;
    }

    boolean utf16 = false;

    public String toString(int start, int len){
        if (arr == null)
            return "null";
        else
        {
            try
            {
                if (utf16)
                {

                    String ret = "";
                    for(int i = start; i < len;i++)
                    {

                        int c = arr[offset + i];
                        if ((c == 0)&&(i + 2 < amount))
                        {
                            i++;
                            int c1 = arr[offset + i];
                            i++;
                            int c0 = arr[offset + i];
                            char cc = (char)((c1 << 8) + c0);
                            ret += cc;
                        }else
                            ret += (char)c;
                    }
                    return ret;
                }
                else
                    return new String(arr, offset + start, len, ENC2);
            }catch(Exception e)
            {
                return new String(arr, offset + start, len);
            }
        }

    }

    public String toString()
    {
        if (amount == 0)
            return "";
        if (arr == null)
             return "";
        else
        {
            try
            {
                //bred();
                if (utf16)
                {

                        String ret = "";
                        for(int i=0;i<amount;i++)
                        {
                            
                            int c = arr[offset + i];
                            if ((c == 0)&&(i + 2 < amount))
                            {                            
                                i++;
                                int c1 = arr[offset + i];
                                i++;
                                int c0 = arr[offset + i];
                                char cc = (char)((c1 << 8) + c0);
                                ret += cc;
                            }else
                            ret += (char)c;
                        }
                        return ret;
                }
                else
                    return new String(arr, offset, amount, ENC2);
            }catch(Exception e)
            {
                return new String(arr, offset, amount);
            }
        }
    }
    
    public void appendChar(int c, int meter, byte[] arr)
    {
        utf16 = true;
        int c0 = c & 0x000000ff;
        int c1 = (c >> 8)  & 0x000000ff;
        append(0, meter, arr);
        append(c1, meter, arr);
        append(c0, meter, arr);
    }
    
    public void append(int c, int meter, byte[] arr)
    {
        if (offset == NIL)
        {
            offset = meter;
            amount = 0;
            this.arr = arr;
        }
        this.arr = arr;
        
        this.arr[offset + amount] = (byte)c;
        
        hash = c + ((hash << 5) - hash);
        amount++;
        //bred();
    }
    public int get(int num)
    {
        return arr[offset + num];
    }
    public boolean equals(final Object obj)
    {
        if (obj instanceof String)
        {
                String s = (String)obj;
                if ( s.hashCode() == hash)
                {
                    int len = s.length();
                    if (len != amount)
                        return false;
                    for(int i = 0, i2 = offset; i < len; i++, i2++ )
                    {
                        char c = s.charAt(i);
                        if (c != arr[i2])
                            return false;
                    }
                    return true;
                }
        }else if (obj instanceof Bytes)
        {
            Bytes s = (Bytes)obj;
                if ( s.hashCode() == hash)
                {
                    int len = s.amount;
                    if (len != amount)
                        return false;
                    for(int i = 0, i2 = offset; i < len; i++, i2++ )
                    {
                        int c = s.get(i);
                        if (c != arr[i2])
                            return false;
                    }
                    return true;
                }            
        }
        return false;
    }
    public void increase(int meter, byte[] arr)
    {
        if (offset == NIL)
        {
            offset = meter;
            amount = 0;
        }
        this.arr = arr;

        if (meter != offset + amount)
            arr[offset + amount] = arr[meter];
        hash = arr[offset + amount] + ((hash << 5) - hash);
        //hash = 31*hash + arr[offset + amount];
        amount++;
        
        //bred();
    }
    /*public boolean bred()
    {
        if (arr == null)
            return false;
        if (offset + amount > arr.length)
        {
            int stop = 1;
            return true;
        }          
        return false;
    }*/
  /*  public void setLength(int l)
    {
        amount = l;
    }*/

    public Bytes(byte[] arr, int offset, int amount, boolean utf16)
    {
        set(arr, offset, amount, utf16);
    }
    public Bytes(){

    }

    public byte[] arr;
    private int offset = NIL;
    private int amount = 0;
    public int hash = 0;
    public void amount(int v)
    {
        amount = v;    
        //bred();
    }
    public int size()
    {
        return amount;
    }
    public int offset()
    {
        return offset;
    }
    
    public int hashCode()
    {
        //String s = toString();
        if (hash == 0)
        {
            int h = 0;
            int end = offset + amount;
            for (int i = offset; i < end; i++) {
                //h = 31*h + arr[i]; //
                h = arr[i] + ((h << 5) - h);
            }
            hash = h;
        }
        return hash;
    }
    
    
    
    ////////////////////////////////////////////////////////
        public static final int UTF8=2;
    public final static String encoding[] =
    {
        null,
        new String("ISO-8859-1"),
        new String("UTF-8"),
        new String("UTF-16BE"),
        new String("GB2312")
    };
    public static String utf8()
    {
        return encoding[UTF8];
    }

//    public final static byte[] fixChars ={0,10,13,32,'=','>',':',';'};
//    public final static int fclimit = 63;

//    public final static byte[] fixChars2 ={0,10,13,32};

    public final static int endSymbol = '>';

    public final static int startSymbol = ' ';

    
        public static StringBuffer[] replaceable = {new StringBuffer("nbsp"),
                                               new StringBuffer("amp"),
                                               new StringBuffer("lt"),
                                               new StringBuffer("gt"),
                                               new StringBuffer("quot")};
    public static int[] replacement = {' ', '&', '<', '>', '\"'};
    public static int space = ' ', amp = '&', semicolon = ';';


    public static String tohtml(String s)
    {
        String ret="";
        int len = s.length();
        if (len==0)
            return ret;
        int meter = 0;
        while (meter < len) {
            char c = s.charAt(meter);
            int coinc = 0;
            br1:for (int i = 1; i < replacement.length; i++) {
                if (replacement[i] == c) {
                    coinc = i;
                    break br1;
                }
            }
            if (coinc == 0)
                ret += c;
            else
                ret += '&'+replaceable[coinc].toString()+';';
            meter++;
        }
        return ret;
    }

    static public int num(Bytes str, StringBuffer arr[])
    {
        for(int i=0;i<arr.length;i++)
        {
            if (str.equalsWithoutReg(arr[i]))
                return i;
        }
        return NIL;
    }

    
    public static int toInt( Bytes name )
    {
        return toInt(name.arr, name.offset(),name.amount);
    }

    public static int toInt( byte[] c, int begin, int len )
    {
            if (len>0)
            {
                    int i=begin, val=0;
                    boolean sign = false;
                    if (c[begin]=='-')
                    {
                            sign= true;
                            i++;
                    }else if(c[begin]=='+')
                        i++;
                    for (;(i<begin+len)&&((c[i]>=48)&&(c[i]<=58));i++)
                            val=(val*10)+(c[i]-48);
                    if (sign)
                        return -val;
                    else
                        return val;
            } else
                    return 0;
    }

    
    
    
    
    
    
        public int unsignedAt(int i) 
   {       
       if ((i>=amount)||(i<0))
        return NIL;
       else
       {
         
         int ret = arr[offset + i];
         if (ret<0)
            return 256 + ret;
         else 
            return ret;
        }
   }
    public void setLen(int val)
    {
        amount = val;
        hash = 0;
    }
    public void offset(int val)
    {
        offset=val;
        hash = 0;
    }
    
    public void nulling()
    {
        arr = null;
        offset = 0;
        amount = 0;
        hash = 0;
    }
    public Bytes(int len, boolean utf16)
    {
        New(len, utf16);
    }
    public void New(int len, boolean utf16)
    {
        nulling();
        arr = new byte[len];
        amount = len;
        this.utf16 = utf16;
    }
    public Bytes(Bytes data, int begin, int len)
    {
        nulling();
        setData(data, begin, len);
    }
  /*  public void replaceMusor(int begin, int end, byte beginIndex, byte endIndex, byte substitute)
    {
        begin+=offset;
        end+=offset;
        for (int i=begin;i<end;i++)
        {
            if ((arr[i]<=endIndex)&&(arr[i]>=beginIndex))
                arr[i]=substitute;
         }
    }*/
    public int parsenum(int begin, int size)
    {
        if (size >= amount)
            size = amount;        
        int ptr = offset + begin + size - 1;
        begin += offset;
        int ret = 0;
        for(;ptr >= begin; ptr--)
        {
            ret = ret << 8;
            int v = arr[ptr];            
            if (v < 0)
                ret += 256 + v;
            else
                ret += v;
        }
        return ret;
    }
    public void setData(Bytes data, int begin, int len)
    {
        
        amount = len;
        offset = begin+data.offset();
        arr = data.arr;
        utf16 = this.utf16;
        hash = 0;
    }
    public Bytes(byte[] data, boolean utf16)
    {
        nulling();
        setData(data, utf16);
    }
    public void setData(byte[] data, boolean utf16)
    {
        amount = data.length;
        offset = 0;
        arr = data;
        utf16 = this.utf16;
        hash = 0;

    }
    public int getAbs(int num)
    {
        return arr[num];
    }

    public void setByte(int num, int c)
    {
        arr[offset+num]=(byte)c;
        hash = 0;
    }

    public void set(int to, int from)
    {
            arr[offset+to]=arr[offset+from];
            hash = 0;
    }

    public int find( int[] c, int begin, int end)
    {
        begin += offset;
        end += offset;
        for(int i=begin;i<end;i++)
        {
            for(int j = 0; j < c.length; j++) {
                if (arr[i] == c[j])
                    return i - offset;
            }
        }
        return NIL;
    }

    public int find( int c, int begin, int end)
    {
        begin += offset;
        end += offset;
        for(int i = begin;i < end;i++)
        {
            if (arr[i] == c)
                return i - offset;
        }
        return NIL;
    }
    public int findBack( int val1, int val2, int begin, int end)
    {
        begin+=offset;
        end+=offset;
        for(int i=end;i>=begin;i--)
        {
            int c = arr[i];
            if ((c==val1)||(c==val2))
                return i-offset;
        }
        return NIL;
    }


/*    public void multiReplace( int c0, int c1)
    {
        for(int i=0;i<arr.length;i++)
        {
            if (arr[i]==c0)
                arr[i]=c1;
        }
    }*/

    public int find( int c)
    {
        return find( c, 0, amount );
    }
    public int find( int[] c)
    {
        return find( c, 0, amount );
    }

    public int findOfCharsBack( int[] chars, int begin, int end)
    {
        end+=offset;
        begin+=offset;
        for (int i=end; i>=begin; i--)
        {
                        int c = arr[i];
                for (int j=0; j<chars.length; j++)
                {
                        if (c==chars[j])
                                return i-offset;
                }
        }
        return NIL;
    }


/*    public int findOfChars( byte[] chars, int limit, int begin, int end)
    {
        begin+=offset;
        end+=offset;
        for (int i=begin; i<=end; i++)
        {
            int c = arr[i];
            if (c<limit)
            {
                for (int j=0; j<chars.length; j++)
                {
                        if (c==chars[j])
                                return i-offset;
                }
            }
        }
        return NIL;
    }*/

/*    public int findOfChars( int begin, int end)
    {
        begin+=offset;
        end+=offset;
        for (int i=begin; i<=end; i++)
        {
            int c = arr[i];
            if ((c<33)||((c>38)&&(c<42))||((c>57)&&(c<65)))
                return i-offset;
        }
        return NIL;
    }*/
    public int findOfTextOrEnd( int begin, int end,int endSymbol)
    {
        begin+=offset;
        end+=offset;
        for (int i=begin; i<=end; i++)
        {
            int c = arr[i];
            if (((c>32)&&(c<39))||((c>41)&&(c<58))||(c>64)||(c==endSymbol))
                return i-offset;
        }
        return NIL;
    }

    public int findOfLetter( int begin, int end)
    {
        begin+=offset;
        end+=offset;
        for (int i=begin; i<=end; i++)
        {
            int c = arr[i];
            if (((c>47)&&(c<58))||(c>64))
                return i-offset;
        }
        return NIL;
    }


 



    public boolean allByteCompare( int[] chars, int begin, int end)
    {
        begin+=offset;
        end+=offset;
        for (int i=begin; i<=end; i++)
        {
            boolean find = false;
            int c = arr[i];
            br:for (int j=0; j<chars.length; j++)
            {
                if (c==chars[j])
                {
                    find = true;
                    break br;
                }
            }
            if (!find)
                return false;
        }
        return true;
    }

    public int nofind( int[] chars, int begin, int end)
    {
        begin+=offset;
        end+=offset;
        for (int i=begin; i<=end; i++)
        {
            boolean find = false;
            int c = arr[i];
            br:for (int j=0; j<chars.length; j++)
            {
                if (c==chars[j])
                {
                    find = true;
                    break br;
                }
            }
            if (!find)
                return i-offset;
        }
        return NIL;
    }

    public int nofindBack( int[] chars, int begin, int end)
    {
        begin+=offset;
        end+=offset;
        for (int i=end; i>=begin; i--)
        {
            int c = arr[i];
            boolean find = false;
            br:for (int j=0; j<chars.length; j++)
            {
                if (c==chars[j])
                {
                    find = true;
                    break br;
                }
            }
            if (!find)
                return i-offset;
        }
        return NIL;
    }

    public boolean equalsWithoutReg(StringBuffer str)
    {
        if (str.length()==amount)
        {
                int len = amount + offset;
                for (int i=offset, j =0;i<len;i++, j++)
                {
                    if (arr[i]!=str.charAt(j))
                        if (!compare(arr[i],(byte)str.charAt(j)))
                            return false;
                }
                return true;
        } else
            return false;
    }

    public boolean equalsWithoutReg(int c)
    {
        return (amount == 1)&&compare(arr[offset],(byte)c);
    }



    public boolean subEqualsWithoutReg( IBytes d, int begin)
    {
        int len = d.len();
        if (begin+len<=amount)
        {
                begin+=offset;
                len+=d.offset();
                byte[] data0 = d.data();
                for (int i=begin, i0 = d.offset();i0<len;i++, i0++)
                    if (!compare(arr[i],data0[i0]))
                        return false;
                return true;
        } else
                return false;
    }
    public boolean subEqualsWithoutReg( StringBuffer d)
    {
        return subEqualsWithoutReg( d, 0);
    }
    public boolean subEqualsWithoutReg( String d)
    {
        return subEqualsWithoutReg( d, 0);
    }
    
    public boolean subEqualsWithoutReg( StringBuffer d, int begin)
    {
        int len = d.length();
        if (begin+len<=amount)
        {
                begin+=offset;
                for (int i=begin, i0 = 0;i0<len;i++, i0++)
                        if (!compare(arr[i],(byte)d.charAt(i0)))
                            return false;
                return true;
                
        } else
                return false;
    }
    public boolean subEqualsWithoutReg( String d, int begin)
    {
        int len = d.length();
        if (begin+len<=amount)
        {
                begin+=offset;
                for (int i=begin, i0 = 0;i0<len;i++, i0++)
                        if (!compare(arr[i],(byte)d.charAt(i0)))
                            return false;
                return true;
                
        } else
                return false;
    }
    
    public boolean wordEqualsWithoutReg( IBytes d, int begin)
    {
        int len = d.len();
        if (begin+len<=amount)
        {
                begin+=offset;
                len+=d.offset();
                byte[] data0 = d.data();
                int i=begin;
                for (int i0 = d.offset();i0<len;i++, i0++)
                    if (!compare(arr[i],data0[i0]))
                        return false;
                if (i<amount)
                {
                    int c = arr[i];
                    if ((c>64)||((c>47)&&(c<58)))
                        return false;                        
                }
                return true;
        } else
                return false;
    }

    public boolean subEquals( StringBuffer str, int begin)
    {
        int len = str.length();
        if (begin+len<=amount)
        {
            begin+=offset;
            for (int i=begin, i0 = 0;i0<len;i++, i0++)
                if (arr[i]!=str.charAt(i0))
                    return false;
            return true;
        } else
            return false;
    }

    static boolean compare(int c1, int c2)
    {
        if (isLetter(c1)&&isLetter(c2))
            return (Math.abs(c1-c2)==32)||(c1==c2);
        else
            return c1==c2;
    }

    static boolean isLetter(int c)
    {
        if ((c>=65)&&(c<=90))
            return true;
        if ((c>=97)&&(c<=122))
            return true;
        return false;
    }


public int collapse(int begin, int ignore, int len)
{
        int i;
        int meter=-1;
        if (len>amount)
                len=amount;

        begin += offset;
        len+=offset;
        for (i = begin; i<len; i++)
        {
                if (arr[i]==ignore)
                {
                        if (meter<0)
                                meter=i;
                } else if (meter>=0)
                {
                        arr[meter]=arr[i];
                        arr[i]=arr[ignore];
                        hash = 0;
                        meter++;
                }
        }
        if (meter>-1)
                return meter-offset;
        else
                return len-offset;
}

public int replace(StringBuffer data, int c)
        {

                int len = data.length();
                int finded =-1;
                int begin = 0;
                int meter=0;
                do
                {
                        finded = find(data,begin,amount);
                        if (finded>=begin)
                        {
                                meter++;
                                begin = finded;
                                int i=begin+offset;
                                int end = i+len;
                                arr[begin]=(byte)c;
                                for(i++;i<end; i++)
                                {
                                    arr[i]=(byte)0;
                                }
                        }
                } while(finded>=begin);
                amount = collapse(0,(byte)0,amount);
                return meter;
        }


public boolean equals(StringBuffer name,int begin,int end)
{
    int len=end-begin+1;
    if (name.length()==len)
    {
        begin+=offset;
        end+=offset;
        for (int i=begin,j=0;i<=end;i++,j++)
        {
            if (name.charAt(j)!=arr[i])
                return false;
        }
        return true;
    }
    return false;
}

public void replace(int begin, StringBuffer[] replaceable, int[] replacement, int def,int end)
{
    int start = find(begin);
    if (start>=0)
    {
        int meter=start,actualMeter=meter;
        br0:for(;meter<amount;meter++,actualMeter++)
        {
            if (get(meter)==begin)
            {
                int findEnd = find(end,meter,amount);
                br:if (findEnd>meter)
                {
                    for(int j=0;j<replaceable.length;j++)
                    {
                        if (equals(replaceable[j],meter+1,findEnd-1))
                        {
                            setByte(actualMeter,replacement[j]);
                            break br;
                        }
                    }
                    setByte(actualMeter,def);
                }else
                    break br0;
                meter = findEnd;
            } else if (actualMeter<meter)
                set(actualMeter,meter);
        }
        amount = actualMeter;
    }
}


public int find( StringBuffer d, int begin, int end)
        {
            int len = d.length();
                if ((len>0)&&(begin<amount))
                {
                        if (amount<=end)
                                end = amount;
                        int beginEquals = -1;
                        int meterEquals = 0;
                        begin+=offset;
                        end+=offset;
                        for (int i=begin;(i<=end)&&(meterEquals<len);i++)
                        {
                                if (arr[i]==d.charAt(meterEquals))
                                {
                                        meterEquals++;
                                        if (beginEquals<0)
                                                beginEquals = i;
                                } else
                                {
                                        beginEquals = -1;
                                        meterEquals = 0;
                                        if (arr[i]==d.charAt(meterEquals))
                                        {
                                                meterEquals++;
                                                beginEquals = i;
                                        }
                                }
                        }
                        if (meterEquals==len)
                                return beginEquals-offset;
                }
                return NIL;
        }
        
public int find( String d, int begin, int end)
        {
            int len = d.length();
                if ((len>0)&&(begin<amount))
                {
                        if (amount<=end)
                                end = amount;
                        int beginEquals = -1;
                        int meterEquals = 0;
                        begin+=offset;
                        end+=offset;
                        for (int i=begin;(i<=end)&&(meterEquals<len);i++)
                        {
                                if (arr[i]==d.charAt(meterEquals))
                                {
                                        meterEquals++;
                                        if (beginEquals<0)
                                                beginEquals = i;
                                } else
                                {
                                        beginEquals = -1;
                                        meterEquals = 0;
                                        if (arr[i]==d.charAt(meterEquals))
                                        {
                                                meterEquals++;
                                                beginEquals = i;
                                        }
                                }
                        }
                        if (meterEquals==len)
                                return beginEquals-offset;
                }
                return NIL;
        }
        
public int find( IBytes d, int begin, int end)
        {
            int len = d.len();
                if ((len>0)&&(begin<amount))
                {
                        if (amount<=end)
                                end = amount;
                        int beginEquals = -1;
                        int meterEquals = 0;

                        begin+=offset;
                        end+=offset;

                        byte[] data0 = d.data();
                        len+=d.offset();
                        meterEquals+=d.offset();

                        for (int i=begin;(i<=end)&&(meterEquals<len);i++)
                        {
                                if (arr[i]==data0[meterEquals])
                                {
                                        meterEquals++;
                                        if (beginEquals<0)
                                                beginEquals = i;
                                } else
                                {
                                        beginEquals = -1;
                                        meterEquals = 0;
                                        if (arr[i]==data0[meterEquals])
                                        {
                                                meterEquals++;
                                                beginEquals = i;
                                        }
                                }
                        }
                        if (meterEquals==len)
                                return beginEquals-offset;
                }
                return NIL;
        }

    public int findOfNonDigit( int begin, int end)
    {
        begin+=offset;
        end+=offset;
        for (int i=begin; i<=end; i++)
        {
            int c = arr[i];
            if ((c<0x30)||(c>0x39))
                return i-offset;
        }
        return NIL;
    }
    
    public Bytes divide(int endSymbol)
    {
        return divideAt(find(endSymbol));
    }
    public Bytes divideAt(int i)
    {
        if (i > 0){
            int len2 = i;
            Bytes ret = new Bytes(arr, offset + len2 + 1, amount - len2 - 1, utf16);
            amount = len2;
            hash = 0;
            trim();
            ret.trim();
            return ret;
        }
        return null;
    }

    public void trim(){
        int end = offset + amount;
        int i;
        for(i = offset; i < end;){
            int c = arr[i];
            if (c < 33){
                i++;
            }else{
                break;
            }
        }
        if (i != offset){
            offset = i;
            amount = end - i;
            hash = 0;
        }
        end = offset + amount;
        for(i = end - 1; i >= offset;){
            int c = arr[i];
            if (c < 33){
                i--;
            }else{
                break;
            }
        }
        if (i != end - 1){
            amount = i - offset + 1;
            hash = 0;
        }

    }


    public void trimBegin(){
        int end = offset + amount;
        int i;
        for(i = offset; i < end;){
            int c = arr[i];
            if (c < 33){
                i++;
            }else{
                break;
            }
        }
        if (i != offset){
            offset = i;
            amount = end - i;
            hash = 0;
        }
    }

    public Bytes removeFirstLine()
    {
        if (amount == 0)
            return null;
        int i = find('\n');
        if (i < 0){
            i = amount;
        }
        Bytes ret = new Bytes(arr, offset, i, utf16);
        amount = amount - i - 1;
        offset = offset + i + 1;
        if (amount < 0)
            amount = 0;
        hash = 0;
        ret.trim();
        return ret;
    }

    public Bytes removeLastWord(int fix)
    {
        if (amount == 0)
            return null;
        int i = find(fix);
        if (i < 0){
            i = amount;
        }
        if (i == 0)
            return null;
        Bytes ret = new Bytes(arr, offset, i, utf16);
        //Bytes ret = new Bytes(arr, offset + len2 + 1, amount - len2 - 1, utf16);
        amount = amount - i - 1;
        offset = offset + i + 1;
        if (amount < 0)
            amount = 0;
        hash = 0;
        ret.trim();
        if (ret.size() == 0)
            return null;
        return ret;
    }

    public Bytes removeFirstWord(int[] fix) {
        if (amount == 0)
            return null;
        int i = find(fix);
        if (i < 0){
            i = amount;
        }
        if (i == 0)
            return null;
        Bytes ret = new Bytes(arr, offset, i, utf16);
        //Bytes ret = new Bytes(arr, offset + len2 + 1, amount - len2 - 1, utf16);
        amount = amount - i;
        offset = offset + i;
        if (amount < 0)
            amount = 0;
        hash = 0;
        ret.trim();
        if (ret.size() == 0)
            return null;
        return ret;
    }


    public Bytes removeFirstWord(int fix) {
        if (amount == 0)
            return null;
        int i = find(fix);
        if (i < 0){
            i = amount;
        }
        if (i == 0)
            return null;
        Bytes ret = new Bytes(arr, offset, i, utf16);
        //Bytes ret = new Bytes(arr, offset + len2 + 1, amount - len2 - 1, utf16);             
        amount = amount - i - 1;
        offset = offset + i + 1;
        if (amount < 0)
            amount = 0;
        hash = 0;
        ret.trim();
        if (ret.size() == 0)
            return null;
        return ret;
    }




    public int htmlFind(int f, int begin, int end)
    {
        begin+=offset;
        end+=offset;

        br:for (int i=begin, actualMeter=i; i<=end; i++, actualMeter++)
        {
            int c = arr[i];
            if (i!=actualMeter)
                arr[actualMeter] = (byte)c;
            if (c == amp)
            {
                int findEnd = find(semicolon, i, end);
                br1:if (findEnd > i)
                {
                    for (int j = 0; j < replaceable.length; j++) {
                        if (equals(replaceable[j], i + 1,
                                        findEnd - 1)) {
                            arr[actualMeter]=(byte)replacement[j];
                            break br1;
                        }
                    }
                    arr[actualMeter]=(byte)space;
                } else
                    break br;
                i = findEnd;
            }else if (c==f)
            {
                if (i!=actualMeter)
                    for(;i>actualMeter;i--)
                    {
                        arr[i]=(byte)space;
                    }
                return actualMeter - offset;
            }
        }
        return NIL;
    }


}
