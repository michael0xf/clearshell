package mtk.util;
import com.mtk.map.Array;
import com.mtk.map.Bytes;
import com.mtk.map.UTable;
/*
*   @author Nicolas De Loof 2006-2007 (QDParser), Mikhail Kravchenko 2008-2017 (QQDParser)
*   ~ Licensed under the Apache License, Version 2.0 (the "License");
*   ~ you may not use this file except in compliance with the License.
*   ~ You may obtain a copy of the License at
*   ~
*   ~      http://www.apache.org/licenses/LICENSE-2.0
*   ~
*  ~ Unless required by applicable law or agreed to in writing, software
*  ~ distributed under the License is distributed on an "AS IS" BASIS,
*  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  ~ See the License for the specific language governing permissions and
*  ~ limitations under the License.*   */
/** Quick and Dirty xml parser.  This parser is, like the SAX parser,
    an event based parser, but with much less functionality.  */
public class QQDParser implements Runnable, IFinalize {
  private  int popMode(Array st) {
    if(!(st.size()==0))
      return ((Integer)st.pop()).intValue();
    else
      return PRE;
  }
    public static boolean characterIsWhitespace(char ch) {
        return (ch <= 0x0020) &&
        (((((1L << 0x0009) |
        (1L << 0x000A) |
        (1L << 0x000C) |
        (1L << 0x000D) |
        (1L << 0x0020)) >> ch) & 1L) != 0);
    }

  private final static int 
    TEXT = 1,
    ENTITY = 2,
    OPEN_TAG = 3,
    CLOSE_TAG = 4,
    START_TAG = 5,
    ATTRIBUTE_LVALUE = 6,
    ATTRIBUTE_EQUAL = 9,
    ATTRIBUTE_RVALUE = 10,
    QUOTE = 7,
    IN_TAG = 8,
    SINGLE_TAG = 12,
    COMMENT = 13,
    DONE = 11,
    DOCTYPE = 14,
    PRE = 15,
    CDATA = 16;
  //public static boolean slow = false;
  //public static boolean verySlow = false;

    Array st;
    int depth;
    int mode;
    int c;
    int quotec;
    Bytes sb;
    Bytes etag;
    Bytes tagName;
    Bytes lvalue;
    Bytes rvalue;
    UTable attrs;
    int line, col;
    boolean eol;
    boolean indent = true;
    int level = 0;

    
    public void endDocument(int mode)
    {
        Finalizer.getFinalizers().removeElement(this);
        mtkThread.removeUpdated(this);
        free();
        try{
            doc.endDocument();
        }catch(Exception e){
            Log.error(e);
        }

        if (mode != DONE) {
            exc("missing end tag", line, col);
        }

    }

    int ration = 1024;
    public void setRation(int v){
        ration = v;
    }
    
    private boolean isAlive = true;




    public void run() {
        Array st = this.st;
        int depth = this.depth;
        int mode = this.mode;
        int c = this.c;
        int quotec = this.quotec;
        depth = this.depth;
        Bytes sb = this.sb;
        Bytes etag = this.etag;
        Bytes tagName = this.tagName;
        Bytes lvalue = this.lvalue;
        Bytes rvalue = this.rvalue;
        UTable attrs = this.attrs;
        DocHandler doc = this.doc;
        BIS0 rr = this.rr;
        int line = this.line, col = this.col;
        boolean eol = this.eol;

        try
        {
            for(int i=0;  isAlive && (i < ration) && ((c = rr.read()) != -1); i++)
            {
                Log.charc((char)c);
                input.write(c, rr.data().length);
                byte[] arr = input.data();
                int meter = input.meter - 1;

    
                // We need to map \r, \r\n, and \n to \n
                // See XML spec section 2.11
                if (c == '\n' && eol) 
                {
                    eol = false;
                    continue;
                } else if (eol) 
                {
                    eol = false;
                } else if (c == '\n') 
                {
                    line++;
                    col = 0;
        /*            if (mode == TEXT){
                        if (sb.size() > 0) 
                        {                            
                            doc.line(0, sb);
                            sb = new Bytes();
                        }
                        level = 0;
                        indent = true;
                    }*/
                } else if (c == '\r') 
                {
                    eol = true;
                    c = '\n';
                    line++;
                    col = 0;
                    /*if (mode == TEXT){
                        if (sb.size() > 0) 
                        {   
                            doc.line(0, sb);
                            sb = new Bytes();
                        }             
                        level = 0;
                        indent = true;                        
                    }*/
                } else {
                    //indent = false;
                    col++;
                }
                
                
                
                if (mode == DONE) 
                {
                    endDocument(DONE);
                    return;
                } else if (mode == TEXT) 
                {
                    if (c == '<') 
                    {
                        st.addElement(new Integer(mode));
                        mode = START_TAG;
                        if (sb.size() > 0) 
                        {
                            
                            doc.text(sb);
                            sb = new Bytes();
                        }
                    } else if (c == '&') 
                    {
                        st.addElement(new Integer(mode));
                        mode = ENTITY;
                        etag.clear();
                    } else
                    {
                        sb.increase(meter, arr);   
                    }
                    
    
                // we are processing a closing tag: e.g. </foo>
                } else if (mode == CLOSE_TAG) 
                {
                    if (c == '>') 
                    {
                        mode = popMode(st);
                        tagName.set(sb);
                        sb.clear();
                        depth--;
                        if (depth == 0)
                            mode = DONE;
                        doc.endElement(tagName);
                    //System.gc();
                    } else
                    {
                        sb.increase(meter, arr);
                    }
    
                // we are processing CDATA
                } else if (mode == CDATA) 
                {

                    if (c == '>' && sb.toString().endsWith("]]")) 
                    {

                        sb.amount(sb.size() - 2);

                        doc.text(sb);
                        sb = new Bytes();

                         mode = popMode(st);
                    } else
                    {
                        
                        sb.increase(meter, arr);
                        
                    }
    
                // we are processing a comment.  We are inside
                // the <!-- .... --> looking for the -->.
                } else if (mode == COMMENT) {
                
                    if (c == '>' && sb.toString().endsWith("--")) {
                        //sb.append(c, meter, arr);

                        sb.clear();

                        
                        mode = popMode(st);
                    } else
                    {
                        sb.append(c, meter, arr);
                
                    }
    
                // We are outside the root tag element
                } else if (mode == PRE) {
                        //sb.append(c, meter, arr);
                
                        sb.clear();
                
                    if (c == '<') {
                        mode = TEXT;
                        st.addElement(new Integer(mode));
                        mode = START_TAG;
                    }
    
                // We are inside one of these <? ... ?>
                // or one of these <!DOCTYPE ... >
                } else if (mode == DOCTYPE) {
                        //sb.append(c, meter, arr);
                
                        sb.clear();

                    if (c == '>') {
                        mode = popMode(st);
                        if (mode == TEXT)
                            mode = PRE;
                    }
    
                // we have just seen a < and
                // are wondering what we are looking at
                // <foo>, </foo>, <!-- ... --->, etc.
                } else if (mode == START_TAG) {
                    mode = popMode(st);
                    if (c == '/') {
                        st.addElement(new Integer(mode));
                        mode = CLOSE_TAG;
                        //sb.append(c, meter, arr);
                

                        sb.clear();


                    } else if (c == '?') {
                        //sb.append(c, meter, arr);
                

                        sb.clear();
                

                        mode = DOCTYPE;
                    } else {
                        st.addElement(new Integer(mode));
                        mode = OPEN_TAG;
                        tagName.clear();
                        attrs = null;//new HTable();

                        sb.append(c, meter, arr);
                

                    }
    
                // we are processing an entity, e.g. &lt;, &#187;, etc.
                } else if (mode == ENTITY) {
                    if (c == ';') {
                        mode = popMode(st);
                        int m = etag.offset();
                        if (etag.equals("lt")) {

                            sb.append('<', m, arr);
 
                        } else if (etag.equals("gt")) {

                            sb.append('>', m, arr);

                        } else if (etag.equals("amp")) {

                            sb.append('&', m, arr);

                        } else if (etag.equals("quot" )) {
 
                            sb.append('"', m, arr);

                        } else if (etag.equals( "apos" )) {

                            sb.append('\'', m, arr);
   
                        }else if (etag.get(0) == '#') 
                        {
                            //sb.clear();
                            int v = 0;                            
                            for (int ii = 1;ii<etag.size();ii++)
                            {
                                v = v * 10 + arr[m + ii]-48;
                            }
                            if (v < 256)
                                sb.append(v , m, arr);
                            else
                                sb.appendChar(v , m, arr);
                        } else 
                        {
                            exc("Unknown entity: &" + etag.toString() + ";", line, col);
                        }
                        etag.clear();
                    } else {
                        etag.append(c, meter, arr);
                    }
    
                // we have just seen something like this:
                // <foo a="b"/
                // and are looking for the final >.
                } else if (mode == SINGLE_TAG) {
                    if (tagName.size() == 0) {
                        tagName.set(sb);
                    }
                    if (c != '>') {
                        exc("Expected > for tag: <" + tagName + "/>", line, col);
                    }
                    doc.startElement(tagName, attrs);
                    doc.endElement(tagName);
                    if (depth == 0) {
                        endDocument(DONE);
                        return;
                    }
                        //sb.append(c, meter, arr);
                

                        sb.clear();

                    attrs = null;//new HTable();
                    tagName.clear();
                    mode = popMode(st);
    
                // we are processing something
                // like this <foo ... >.  It could
                // still be a <!-- ... --> or something.
                } else if (mode == OPEN_TAG) {
                    if (c == '>') {
                        if (tagName.size() == 0) {
                            tagName.set(sb);
                        }
                        //sb.append(c, meter, arr);
                        sb.clear();

                        depth++;
                        doc.startElement(tagName, attrs);
                        tagName.clear();
                        attrs = null;//new HTable();
                        mode = popMode(st);
                    } else if (c == '/') {
                        //sb.append(c, meter, arr);
                        if (tagName.size() == 0)
                            tagName.set(sb);
                        sb.clear();

                        mode = SINGLE_TAG;
                    } else if (c == '-' && sb.toString().equals("!-")) {
                        mode = COMMENT;
                        //sb.append(c, meter, arr);

                        sb.clear();

                    } else if (c == '[' && sb.toString().equals("![CDATA")) {
                        mode = CDATA;
                        //sb.append(c, meter, arr);

                        sb.clear();

                    } else if (c == 'E' && sb.toString().equals("!DOCTYP")) {

                        sb.append(c, meter, arr);

                        sb.clear();

                        mode = DOCTYPE;
                    } else if (characterIsWhitespace((char) c)) {
                        tagName.set(sb);
                        //sb.append(c, meter, arr);

                        sb.clear();

                        mode = IN_TAG;
                    } else {
                        sb.append(c, meter, arr);
                    }
    
                // We are processing the quoted right-hand side
                // of an element's attribute.
                } else if (mode == QUOTE) {
                    if (c == quotec) {
                        rvalue.set(sb);
                        //sb.append(c, meter, arr);

                        sb.clear();

                        if (attrs == null)
                            attrs = new UTable(8);
                        attrs.put(lvalue, rvalue);
                        lvalue = new Bytes();
                        rvalue = new Bytes();
                        mode = IN_TAG;
                    // See section the XML spec, section 3.3.3
                    // on normalization processing.
                    } else if (" \r\n\u0009".indexOf(c) >= 0) {

                        sb.append(' ', meter, arr);
   
                    } else if (c == '&') {
                        st.addElement(new Integer(mode));
                        mode = ENTITY;
                        //sb.append(c, meter, arr);
                        sb.clear();

                    } else {
                        sb.append(c, meter, arr);
                    }
    
                } else if (mode == ATTRIBUTE_RVALUE) {
                    sb.clear();
                    if (c == '"' || c == '\'') {
                        quotec = c;
                        mode = QUOTE;
                    } else if (characterIsWhitespace((char) c)) {
                        ;
                    } else {
                        exc("Error in attribute processing", line, col);
                    }
    
                } else if (mode == ATTRIBUTE_LVALUE) {
                    if (characterIsWhitespace((char) c)) {
                        lvalue.set(sb);                        
                        //sb.append(c, meter, arr);

                        sb.clear();

                        mode = ATTRIBUTE_EQUAL;
                    } else if (c == '=') {
                        lvalue.set(sb);
                        //sb.append(c, meter, arr);

                        sb.clear();

                        mode = ATTRIBUTE_RVALUE;
                    } else {

                        sb.append(c, meter, arr);

                    }
    
                } else if (mode == ATTRIBUTE_EQUAL) {
                        //sb.append(c, meter, arr);

                        sb.clear();

                    if (c == '=') {
                        mode = ATTRIBUTE_RVALUE;
                    } else if (characterIsWhitespace((char) c)) {
                        ;
                    } else {
                        exc("Error in attribute processing.", line, col);
                    }
    
                } else if (mode == IN_TAG) {
                    
                    if (c == '>') {
                        mode = popMode(st);
                        doc.startElement(tagName, attrs);
                        depth++;
                        tagName.clear();;
                        attrs = null;//new HTable();
                        //sb.append(c, meter, arr);

                        sb.clear();

                    } else if (c == '/') {
                        mode = SINGLE_TAG;
                        //sb.append(c, meter, arr);
  
                        sb.clear();

                    } else if (characterIsWhitespace((char) c)) {
                        //sb.append(c, meter, arr);

                        sb.clear();

                        ;
                    } else {
                        mode = ATTRIBUTE_LVALUE;

                        sb.append(c, meter, arr);
  
                    }
                }
            }
        }catch(Exception e)
        {
            Log.error(e, "qdparser 0");
            endDocument(mode);
            return;
        }
        if (c == -1)
        {
            endDocument(mode);
        }else
        {
            this.st = st;
            this.depth = depth;
            this.mode = mode;
            this.c = c;
            this.quotec = quotec;
            this.depth = depth;
            this.sb = sb;
            this.etag = etag;
            this.tagName = tagName;
            this.lvalue = lvalue;
            this.rvalue =rvalue;
            this.attrs = attrs;
            this.doc = doc;
            this.rr = rr;
            this.line=line;
            this.col=col;
            this.eol = eol;
        }
    }

    MtkThreads.MtkThread mtkThread;
    DocHandler doc;
    BIS0 rr;
    BIS0 input;
    public void parse(DocHandler doc, BIS0 rr, MtkThreads.MtkThread mtkThread) throws Exception {

        this.mtkThread = mtkThread;
        input = new BIS0(null);
        st = new Array();
        depth = 0;
        mode = PRE;
        c = 0;
        quotec = '"';
        sb = new Bytes();
        etag = new Bytes();
        tagName = new Bytes();
        lvalue = new Bytes();
        rvalue = new Bytes();
        attrs = null;
        this.doc = doc;
        this.rr = rr;
        doc.startDocument();
        line=1;
        col=0;
        eol = false;
        Finalizer.getFinalizers().addElement(this);
        mtkThread.addUpdated(this);



    }

    private void exc(String s, int line, int col)
    {
            /*throws Exception {
        throw new Exception(s + " near line " + line + ", column " + col);*/
            Log.error(s + " near line " + line + ", column " + col);
    }

    @Override
    public void close() {
        isAlive = false;
        free();
    }


    public void free()
    {
        if (rr != null)
            rr.close();
        if (input != null)
            input.close();

    }
    
    @Override
    public void finalize(){
        close();
    }
}

