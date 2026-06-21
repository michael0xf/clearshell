

package com.mtk.map;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public interface IBytes extends Const {
    public int unsignedAt(int i);
 public int len();
 public int offset();
     public void set(int to, int from);
     public void setByte(int num, int c);
   public void setData(IBytes data, int begin, int len);
    public byte[] data();
    public int find( int c, int begin, int end);
    public int findBack( int val1, int val2, int begin, int end);
    public int find( int c);
    public int findOfCharsBack( int[] chars, int begin, int end);
    public int findOfChars( int begin, int end);
    public int findOfTextOrEnd( int begin, int end,int endSymbol);
    public int findOfLetter( int begin, int end);
    public boolean allByteCompare( int[] chars, int begin, int end);
    public int get(int index);
    public int nofind( int[] chars, int begin, int end);
    public int nofindBack( int[] chars, int begin, int end);
    public boolean equalsWithoutReg(StringBuffer str);
    public boolean equalsWithoutReg(int c);
    public boolean subEqualsWithoutReg( IBytes d, int begin);
    public boolean subEqualsWithoutReg( StringBuffer d);
    public boolean subEqualsWithoutReg( String d);
    public boolean subEqualsWithoutReg( StringBuffer d, int begin);
    public boolean subEqualsWithoutReg( String d, int begin);
    public boolean wordEqualsWithoutReg( IBytes d, int begin);
    public boolean subEquals( StringBuffer str, int begin);
public int collapse(int begin, int ignore, int len);
public int replace(StringBuffer data, int c);
public boolean equals(StringBuffer name,int begin,int end);
public void replace(int begin, StringBuffer[] replaceable, int[] replacement, int def,int end);
public int find( StringBuffer d, int begin, int end);
public int find( String d, int begin, int end);
public int find( IBytes d, int begin, int end);
    public int findOfNonDigit( int begin, int end);
    public void divide(IBytes temp, int begin, int end, int endSymbol);
   public void divideNumber(IBytes temp, int begin, int end, int endSymbol);
} 
