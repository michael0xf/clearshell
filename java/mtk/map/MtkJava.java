package mtk.map;

import com.mtk.map.*;
import com.mtk.map.i.ICalculator;
import com.mtk.map.i.Item;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:mtkravchenko@gmail.com">Mikhail Kravchenko</a>
 */
public class MtkJava<T>{

    protected U2Table fields;
    protected T bean;

    public T getBean(){
        return bean;
    }

    public void setBean(T bean){
        setBean(bean, true);
    }

    public void setUnion(T union){
        setBean(union, false);
    }

    public void setBean(T bean, boolean withoutDirectAccessToFields){
        this.bean = bean;
        final Class c = bean.getClass();
        final Field[] fields = c.getDeclaredFields();
        final U2Table table = new U2Table(fields.length);
        this.fields = table;
        for(Field field: fields){
            String name = field.getName();
            final Key key = Key.getOrNew(field.getName());
            String Name = Character.toUpperCase(name.charAt(0)) + name.substring(1, name.length());
            Class type = field.getType();
            Object setter, getter;
            try {
                setter = c.getMethod("set" + Name, type);
            } catch (NoSuchMethodException e) {
                if ( withoutDirectAccessToFields ){
                    setter = null;
                }else{
                    setter = field;
                }
            }
            try {
                getter = c.getMethod("get" + Name, type);
            } catch (NoSuchMethodException e) {
                if ( withoutDirectAccessToFields ){
                    getter = null;
                }else{
                    getter = field;
                }
            }
            table.put(key, setter, getter);
        }
    }

    public void setLocalValuesFrom(final Tag tag){
        final Tag.Attributes attributes  = tag.getAttributes();
        final U2Table table = fields;
        if (attributes.size() > table.size()){
            final UniqueArray codes = table.code();
            final int size = codes.size();
            for(int i = 0; i < size; i++){
                final Key k = (Key)codes.elementAt(i);
                final Tag.Attributes.Attr attr = (Tag.Attributes.Attr)attributes.get(k.hashCode());
                if (attr != null){
                    setField0(table.value0(), i, attr.getNativeValue());
                }
            }
        }else {
            final UniqueArray codes = table.code();
            final int size = attributes.size();
            for(int i = 0; i < size; i++){
                final Tag.Attributes.Attr attr = (Tag.Attributes.Attr)attributes.elementAt(i);
                final Key k = attr.getName();
                final int index = codes.getIndex(k.hashCode());
                if (index >= 0){
                    setField0(table.value0(), index, attr.getNativeValue());
                }
            }
        }
    }

    void setField0(Array a, int i, Object value){
        try{
            setField(a.elementAt(i), value);
        } catch (IllegalAccessException e) {
            a.setElementAt(null, i);  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            Log.error(e);  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    protected void setField(Object setter, Object value)throws IllegalAccessException, InvocationTargetException{
        if (setter instanceof Method){
            final Method method = (Method)setter;
            if (method.getParameterTypes()[0].isInstance(value)){
                    method.invoke(bean, value);
            }
        }else if (setter instanceof Field){
            final Field field = (Field)setter;
            if (field.getType().isInstance(value)){
                    field.set(bean, value);
            }
        }
    }

    public void setValueFrom(final Item item){
        final Key k = item.getName();
        U2Table table = fields;
        final int index = table.code().getIndex(k.hashCode());
        if (index >= 0){
                setField0(table.value0(), index, item.getNativeValue());
        }
    }

    public void writeValueTo(final Item item){
        final Key k = item.getName();
        U2Table table = fields;
        final int index = table.code().getIndex(k.hashCode());
        if (index >= 0){
            try {
                Object o = getValue(table.value1().elementAt(index));
                item.setValue(o);
            } catch (IllegalAccessException e) {
                table.value1().setElementAt(null, index);
                Log.error(e);
            } catch (InvocationTargetException e) {
                Log.error(e);
            }
        }
    }

    public void getOrNewValues(final Cursor cursor){
        final U2Table table = fields;
        final UniqueArray codes = table.code();
        final int size = codes.size();
        for(int i = 0; i < size; i++){
            final Key k = (Key)codes.elementAt(i);
            Item item = cursor.getOrNewItemByName(k.toString());
            if (item != null){
                try {
                    item.setValue(getValue(table.value1().elementAt(i)));
                } catch (IllegalAccessException e) {
                    table.value1().setElementAt(null, i);
                    Log.error(e);
                } catch (InvocationTargetException e) {
                    Log.error(e);
                }
            }
        }
    }               

    
    protected Object getValue(Object getter)throws IllegalAccessException, InvocationTargetException{
        if (getter instanceof Method){
            final Method method = (Method)getter;
                return method.invoke(bean);
        }else if (getter instanceof Field){
            final Field field = (Field)getter;
                return field.get(bean);
        }
        return null;
    }

    public void setValuesFrom(final Cursor cursor){
        final U2Table table = fields;
        final UniqueArray codes = table.code();
        final int size = codes.size();
        for(int i = 0; i < size; i++){
            final Key k = (Key)codes.elementAt(i);
            Item item = cursor.getItemByName(k.toString());
            if (item != null){
                setField0(table.value0(), i, item.getNativeValue());
            }
        }
    }
    public void setOrNewValuesFrom(final Cursor cursor){
        final U2Table table = fields;
        final UniqueArray codes = table.code();
        final int size = codes.size();
        for(int i = 0; i < size; i++){
            final Key k = (Key)codes.elementAt(i);
            Object o = table.value0().elementAt(i);
            try{
                if (o instanceof Method){
                    Method m = (Method)o;
                    Class ret = m.getReturnType();
                    Object value = cursor.getObjectByName(k.toString(), ret);
                    if ((value == null)&&(ret == Number.class)){
                        value = 0;
                    }
                    m.invoke(bean, value);
                }else if (o instanceof Field){
                    Field f = (Field)o;
                    Class ret = f.getType();
                    Object value = cursor.getObjectByName(f.getName(), ret);
                    if ((value == null)&&(ret == Number.class)){
                        value = 0;
                    }
                    f.set(bean, value);
                }else{
                    return;
                }
            } catch (IllegalAccessException e) {
                table.value0().setElementAt(null, i);  //To change body of catch statement use File | Settings | File Templates.
                Log.error(e);
            } catch (InvocationTargetException e) {
                Log.error(e);  //To change body of catch statement use File | Settings | File Templates.
            }

            setField0(table.value0(), i, o);
        }
    }

}
