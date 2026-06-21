
package com.mtk.map;

import com.mtk.map.i.*;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */

public class KeyDict {


    Object calculator_factory;

    public void setFactory(IFactory factory){
        calculator_factory = factory;
    }

    public void setCalculator(ICalculator calculator){
        calculator_factory = calculator;
    }

    public String getValueStorageString(Item item){
        ICalculator calculator = getCalculator();
        if (calculator == null){
            Object value = item.getNativeValue();
            if (value instanceof String){
                return (String)value;
            }else if (value instanceof Bytes){
                return value.toString();
            }else if (value instanceof Link){
                return value.toString();
            }else
                return "";
        }else
            return calculator.getStorageStringForValue(item);

    }

    public ICalculator getCalculator(){
        if (calculator_factory == null){
            return null;
        }else if (calculator_factory instanceof ICalculator){
            return (ICalculator)calculator_factory;
        }else if (calculator_factory instanceof IFactory){
            ICalculator c = ((IFactory)calculator_factory).getCalculator(key);
            calculator_factory = c;
            return c;
        }
        return null;
    }

    
    IntIntTable suffix;
    public void removeFromPrefixes(Tag tag){
        if (suffix == null)
            return;
        suffix.remove(tag.hashCode());
    }

    public boolean hasSuffix(Tag tag){
        if (suffix == null)
            return false;
        return suffix.containsKey(tag.hashCode());
    }
    
    public int createSuffix(Tag tag){
        int code = tag.hashCode();
        int ret;
        if (suffix == null){
            suffix = new IntIntTable(4);
            ret = suffix.size();
            suffix.put(code, ret);
        }else{
            if (suffix.containsKey(code)){
                return suffix.get(code);
            }else{
                ret = suffix.size();
                suffix.put(code, ret);
            }
        }
        return ret;
    }



    public void indexedOn(){
        values = new HashArray(32) {
            @Override
            public int hashCode(Object o) {
                o = ((IValue) o).getNativeValue();
                if (o == null)
                    return 0;
                else
                    return o.hashCode();
            }
        };
    }

    public void indexedOff(){
        values = null;
    }
    public KeyDict(Key key, boolean isIndexed) {
        this.key = key;
        if (isIndexed){
            indexedOn();
        }
    }

    public int hashCode() {
        return key.hashCode();
    }

    boolean isIndexed;
    Key key;
    private UniqueArray2 valuesByLet;



    public void getValues(Tag tag, Object value, ValueSelector sel){
        if (values == null){
            return;
        }
        int i = values.firstIndexOfRange(value.hashCode());
        int i0 = i;
        int i1 = i + 1;
        br0:while(i0 >= 0){
            IValue let = (IValue) values.elementAt(i0);
            if (let.getCursor().isInternalFor(tag)) {
                Object v = let.getNativeValue();
                if ((v.hashCode() == value.hashCode())&&(v.equals(value))) {
                    sel.addElement(let);
                } else {
                    break br0;
                }
            }
            i0--;
        }
        br1:while(i1 < values.size()){
            IValue let = (IValue) values.elementAt(i1);
            if (let.getCursor().isInternalFor(tag)) {
                Object v = let.getNativeValue();
                if ((v.hashCode() == value.hashCode())&&(v.equals(value))) {
                    sel.addElement(let);
                } else {
                    break br1;
                }
            }
            i1++;
        }
    }

    public void getValues(Tag tag, int code, ValueSelector sel){
        if (values == null){
            return;
        }
        int i = values.firstIndexOfRange(code);
        int i0 = i;
        int i1 = i + 1;
        br0:while(i0 >= 0){
            IValue let = (IValue) values.elementAt(i0);
            if (let.getCursor().isInternalFor(tag)) {
                Object v = let.getNativeValue();
                if (v.hashCode() == code) {
                    sel.addElement(let);
                } else {
                    break br0;
                }
            }
            i0--;
        }
        br1:while(i1 < values.size()){
            IValue let = (IValue) values.elementAt(i1);
            if (let.getCursor().isInternalFor(tag)) {
                Object v = let.getNativeValue();
                if (v.hashCode() == code) {
                    sel.addElement(let);
                } else {
                    break br1;
                }
            }
            i1++;
        }
    }


    public void getValues(Tag tag, int firstCode, int lastCode, ValueSelector sel){
        if (values == null){
            return;
        }
        int iFirst = values.firstIndexOfRange(firstCode);
        int i = iFirst;
        br:while(i >= 0){
            IValue let = (IValue) values.elementAt(i);
            if (let.getCursor().isInternalFor(tag)) {
                Object v = let.getNativeValue();
                if (v.hashCode() == firstCode) {
                    sel.addElement(let);
                } else {
                    break br;
                }
            }
            i--;
        }
        int iLast = values.firstIndexOfRange(lastCode);
        i = iFirst + 1;
        br:while(i < iLast){
            IValue let = (IValue) values.elementAt(i);
            if (let.getCursor().isInternalFor(tag)) {
                Object v = let.getNativeValue();
                if (v.hashCode() == lastCode) {
                    sel.addElement(let);
                } else {
                    break br;
                }
            }
            i++;
        }

        i = iLast + 1;
        br:while(i < values.size()){
            IValue let = (IValue) values.elementAt(i);
            if (let.getCursor().isInternalFor(tag)) {
                Object v = let.getNativeValue();
                if (v.hashCode() == lastCode) {
                    sel.addElement(let);
                } else {
                    break br;
                }
            }
            i++;
        }


    }


    public void removeFromValues(IValue value) {
        if (values == null)
            return;
        values.removeElement(value);
    }

    public void addToValues(IValue value) {
        if (values != null){
            values.addElement(value);
            valuesByLet.check();
        }
    }

    public IValue getFromValues(Tag tag, Object value){
        if (values == null){
            return null;
        }
        int i = values.firstIndexOfRange(value.hashCode());
        int i0 = i;
        int i1 = i + 1;
        br0:while(i0 >= 0){
            IValue let = (IValue) values.elementAt(i0);
            if (let.getCursor().isInternalFor(tag)) {
                Object v = let.getNativeValue();
                if ((v.hashCode() == value.hashCode())&&(v.equals(value))) {
                    return let;
                } else {
                    break br0;
                }
            }
            i0--;
        }
        br1:while(i1 < values.size()){
            IValue let = (IValue) values.elementAt(i1);
            if (let.getCursor().isInternalFor(tag)) {
                Object v = let.getNativeValue();
                if ((v.hashCode() == value.hashCode())&&(v.equals(value))) {
                    return let;
                } else {
                    break br1;
                }
            }
            i1++;
        }
        return null;
    }



    public IValue getShortlyFromValues(Object value){
        if (values == null){
            return null;
        }
        int i = values.findMiddle(values.hashCode(value));
        if (i == values.size())
            i = values.size() - 1;
        return (IValue) values.elementAt(i);
    }

    
    public Item getLetByValue(Tag tag, Object value){
        if (values != null){
           IValue ret = getFromValues(tag, value);
           if (ret != null)
               return ret.getItem();
        }
        if (valuesByLet == null)
            return null;
        int begin = valuesByLet.findMiddle(tag.getAddress());
        int end = valuesByLet.findMiddle(tag.getTail().getAddress());
        for(int i = begin; i < end; i++){
            ILet l = (ILet)valuesByLet.elementAt(i);
            Item v = l.getItem();
            Object o = v.getNativeValue();
            if ((o == value) || ((o != null) && o.equals(value))){
                return v;
            }
            v = MapUtil.getEndItemByValue(v, false);
            if (v != null){
                o = v.getNativeValue();

                if ((o == value) || ((o != null) && o.equals(value))){
                    return v;
                }
            }
        }
        return null;
    }



    public int letsCount(){
        return valuesByLet.size();
    }

    public int getObjectIndex(int address) {
        if (valuesByLet == null)
            return Const.NIL;
        if (valuesByLet.size() == 0)
            return Const.NIL;
        int i = valuesByLet.findMiddle(address);
        if (i == valuesByLet.amount)
            return Const.NIL;
        ILet let = (ILet) valuesByLet.elementAt(i);
        if (let.getCursor().getAddress() == address)
            return i;
        return Const.NIL;
    }

    public ILet getLetForValue(int address) {
        ILet ret = getLetForAddress(address);
        if (ret != null)
            return ret.getRealLet();
        return null;
    }

    public void getChildren(Selector selector, Tag tag, boolean scanNode) {
        if (valuesByLet == null)
            return;
        if (valuesByLet.size() == 0)
            return;
        if (tag.getName() == key){
            if (!selector.addElement(tag))
                return;
        }
        int beginAddress = tag.getAddress();
        int endAddress = tag.getTail().getAddress();
        int begin = valuesByLet.findMiddle(beginAddress);
        int end = valuesByLet.findMiddle(endAddress);
        if (selector != null) {
            selector.init(end - begin);
        }
        for(int i = begin; i < end; i++){
            ILet let = (ILet)valuesByLet.elementAt(i);
            if (let.isTail()) {
                if (let.getTail().getHead().getCursor().getAddress() >= beginAddress){
                    if (!selector.addElement(let)){
                        return;
                    }
                }
            }else {
                if (!selector.addElement(let)){
                    return;
                }
            }

        }
        if (scanNode)


        {
            Tag node = tag.getNode();
            if (node != null) {
                if (node == tag) {
                    node = tag.getNode();
                }
                getChildren(selector, node, true);
            } else {
//                      IO.log.error("node == null #4");
//                      tag.getNode();
                return;
            }
        }
    }
    public Item getChild(Tag tag, boolean scanNode){
        return getInternalChild(tag.getAddress(), tag, scanNode);
    }



    public Item getInternalChild(Tag tag, boolean scanNode) {
        if (valuesByLet == null)
            return null;
        if (valuesByLet.size() == 0)
            return null;

        int begin = valuesByLet.findMiddle(tag.getAddress());
        int end = tag.getTail().getAddress();
        end = valuesByLet.findMiddle(end);
        for(int i = begin; i < end; i++){
            ILet let = (ILet)valuesByLet.elementAt(i);
            return let.getItem();
        }
        if (scanNode)
        {
            Tag node = tag.getNode();
            if (node != null) {
                if (node == tag) {
                    tag.getNode();
                    return null;
                }
                return getInternalChild(node, true);
            } else {
                return null;
            }
        }
        return null;

    }


    public Item getInternalChild(int address, Tag tag, boolean scanNode) {

        if (valuesByLet == null)
            return null;
        if (valuesByLet.size() == 0)
            return null;

        int begin = valuesByLet.findMiddle(address);

        int end = tag.getTail().getAddress();
        end = valuesByLet.findMiddle(end);
        for(int i = begin; i < end; i++){
            ILet let = (ILet)valuesByLet.elementAt(i);
            Item v = let.getItem();
            if (scanNode)
                return v;
            if (address < v.getTag().getAddress())
                return v;
        }
        end = begin;
        begin = valuesByLet.findMiddle(tag.getAddress());
        for(int i = begin; i < end; i++){
            ILet let = (ILet)valuesByLet.elementAt(i);
            Item v = let.getItem();
            if (scanNode)
                return v;
            if (address < v.getTag().getAddress())
                return v;
        }
        if (scanNode)
        {
            Tag node = tag.getNode();
            if (node != null) {
                if (node == tag) {
                    tag.getNode();
                    return null;
                }
                return getInternalChild(tag.getAddress(), node, true);
            } else {
                return null;
            }
        }
        return null;

    }


    public void getChildren(Selector selector, int beginAddress, int endAddress) {
        if (valuesByLet == null)
            return;
        if (valuesByLet.size() == 0)
            return;
        int begin = valuesByLet.findMiddle(beginAddress);
        int end = valuesByLet.findMiddle(endAddress);
        if (selector != null) {
            selector.init(end - begin);
        }
        for(int i = begin; i < end; i++){
            ILet let = (ILet)valuesByLet.elementAt(i);
            if (let.isTail()) {
                if (let.getTail().getHead().getCursor().getAddress() >= beginAddress){
                    if (!selector.addElement(let)){
                        return;
                    }
                }
            }else {
                if (!selector.addElement(let)){
                    return;
                }
            }

        }
    }
    public Item getFirstChild(Tag tag){
        return getFirstChild(tag.getAddress(), tag.getTail().getAddress());
    }



    public Item getFirstChild(int beginAddress, int endAddress) {
        if (valuesByLet == null)
            return null;
        if (valuesByLet.size() == 0)
            return null;

        int begin = valuesByLet.findMiddle(beginAddress);
        int end = valuesByLet.findMiddle(endAddress);
        for(int i = begin; i < end; i++){
            ILet let = (ILet)valuesByLet.elementAt(i);
            if (!let.isTail())
                return let.getItem();
        }
        return null;

    }


    public Item getLastChild(int beginAddress, int endAddress) {
        if (valuesByLet == null)
            return null;
        if (valuesByLet.size() == 0)
            return null;

        int begin = valuesByLet.findMiddle(beginAddress);
        int end = valuesByLet.findMiddle(endAddress);
        for(int i = end - 1; i >= begin; i--){
            ILet let = (ILet)valuesByLet.elementAt(i);
            return let.getRealLet().getItem();
        }
        return null;

    }

    public Item getNextTag(int address, Tag.Tail tail) {
        int i = valuesByLet.findMiddle(address + 1);
        for(;i < valuesByLet.size(); i++){
            ILet let = (ILet)valuesByLet.elementAt(i);
            if (let == tail){
                return null;
            }
            if ((!let.isTail())&&(let.getCursor().getAddress() != address))
                return let.getItem();
        }
        return null;
    }


    public Item getPrevTag(int address, Tag tag) {
        int i = valuesByLet.findMiddle(address - 1);
        for(;i > 0; i--){
            ILet let = (ILet)valuesByLet.elementAt(i);
            if (let == tag){
                return null;
            }
            if ((!let.isTail())&&(let.getCursor().getAddress() != address))
                return let.getItem();
        }
        return null;
    }


    public Item getChild(int address, Item item, boolean scanNode) {
        Tag tag = item.getTag();
        Item v = tag.getLocalItem(key);
        if (v != null) {
            if ((v == item)||(v == tag)){
                v = null;
            } else {
                return v;
            }
        }
        ILet ret = getLetForValue(address);
        if (ret == null) {
            return getInternalChild(address, tag, scanNode);
        }else {
            if (scanNode) {
                return ret.getItem();
            }else {
                v = ret.getItem();
                if (v.isInternalFor(tag)) {
                    return ret.getItem();
                }else
                    return getInternalChild(address, tag, scanNode);
            }

        }
    }

    public ILet getLet(Leaf leaf, int index) {
        return getLetForValue(leaf.getAddress(index));
    }

    public ILet getLetForAddress(Leaf leaf, int index) {
        return getLetForAddress(leaf.getAddress(index));
    }

    public ILet getLetForAddress(int address) {
        if (valuesByLet == null)
            return null;
        if (valuesByLet.size() == 0)
            return null;
        int i = valuesByLet.findMiddle(address);
        ILet let;
        if (i == 0)
            return null;
        else if (i >= valuesByLet.amount) {
            let = (ILet) valuesByLet.lastElement();
        } else {
            let = (ILet) valuesByLet.elementAt(i - 1);
        }
        return let;
    }

    HashArray values;
    public void preChange(IValue v){
        if (values != null){
            Object old = v.getNativeValue();
            if (old == null)
                return;
            if (old instanceof Link){

            }else if (old instanceof Item){

            }else{
                removeFromValues(v);
            }
        }
    }
    public void postChange(IValue v){
        Object o = v.getNativeValue();
        if (o == null)
            return;
        if (o instanceof Link){

        }else if (o instanceof Item){

        }else{
            addToValues(v);
        }
    }




    public void add(ILet o) {
        if (valuesByLet == null) {
            valuesByLet = new UniqueArray2(4) {
                @Override
                public int hashCode(Object o) {
                    return ((ILet) o).getCursor().getAddress();
                }
            };
        }

        Item v = o.getItem();
        postChange(v);

        if (valuesByLet.size() > 0) {
            if (valuesByLet.isContain(o))
                return;
            int address = o.getCursor().getAddress();
            int i = valuesByLet.findMiddle(address);
            ILet prev;
            if (i > 0) {
                prev = (ILet) valuesByLet.elementAt(i - 1);
                if (prev != null) {
                    o.addUpper(prev);
                }
            }
            valuesByLet.insertElementAt(o, i);
        }else{
            valuesByLet.addElement(o);
        }
        valuesByLet.check();
    }

    public void check(){
        valuesByLet.check();
    }

    public void removeElement(ILet let) {
        if (let.getName() == key) {
            if (let instanceof Tag){
                removeFromPrefixes((Tag)let);
            }
            preChange(let.getItem());
            valuesByLet.removeElement(let);
            Upper p = let.getUpper();
            if (p != null)
                p.clear();
            valuesByLet.check();
        }
    }

    public ILet reverseWithPrev(int index) {
        ILet let = (ILet) valuesByLet.elementAt(index);
        valuesByLet.reverse(index - 1, index);
        return let;
    }

    public ILet reverseWithNext(int index) {
        ILet let = (ILet) valuesByLet.elementAt(index);
        valuesByLet.reverse(index + 1, index);
        return let;
    }

    public void setRegion(ILet to, Cursor from, Cursor end) {
        int address0 = from.getAddress();
        int address1 = end.getAddress();
        if (valuesByLet != null) {
            int start = valuesByLet.firstIndexOfRange(address0);
            int stop = valuesByLet.firstIndexOfRange(address1);
            for (int i = start; i < stop; i++) {
                ILet let = (ILet) valuesByLet.elementAt(i);
                LiveVector lv = let.getUpper();
                if (let != to)
                    lv.addElement(to);
            }
        }
        valuesByLet.check();
    }

  /*  public void removeRegion(ILet removed, Pointer begin, Pointer end) {
        int address0 = begin.getAddress();
        int address1 = end.getAddress();
        if (removed.getName() != key)
            return;
        if (valuesByLet == null)
            return;
        int start = valuesByLet.firstIndexOfRange(address0);
        int stop = valuesByLet.firstIndexOfRange(address1);
        for (int i = start; i < stop; i++) {
            ILet let = (ILet) valuesByLet.elementAt(i);
            let.getUpper().removeElement(removed);
        }
    }*/

    public ILet getPrev(ILet before) {
        if (valuesByLet.size() < 2)
            return null;
        int address = before.getCursor().getAddress();
        int i = valuesByLet.findMiddle(address);
        if (i > 0)
            return (ILet) valuesByLet.elementAt(i - 1);
        else
            return (ILet) valuesByLet.elementAt(valuesByLet.size() - 1);
    }
}
