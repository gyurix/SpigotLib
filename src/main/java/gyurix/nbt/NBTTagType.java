package gyurix.nbt;

import com.google.gson.internal.Primitives;
import gyurix.protocol.Reflection;
import lombok.Getter;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gyurix.protocol.Reflection.getNMSClass;

@Getter
public enum NBTTagType {
    End(void.class),
    Byte(byte.class),
    Short(short.class),
    Int(int.class),
    Long(long.class),
    Float(float.class),
    Double(double.class),
    ByteArray(byte[].class),
    String(String.class),
    List(List.class, NBTList.class, NBTTag.class),
    Compound(Map.class, NBTCompound.class, String.class, NBTTag.class),
    IntArray(int[].class),
    LongArray(long[].class);
    private static final HashMap<Class, NBTTagType> classes = new HashMap<>();
    @Getter
    private static final Field nmsListTypeField;

    static {
        for (NBTTagType t : values()) {
            classes.put(t.dataClass, t);
            classes.put(t.nmsClass, t);
            classes.put(t.wrapperClass, t);
        }
        nmsListTypeField = Reflection.getField(List.nmsClass, "type");
    }

    private final Type[] dataTypes;
    private final Class nmsClass, dataClass, wrapperClass;
    private final Constructor nmsConstructor;
    private final Field nmsDataField;

    NBTTagType(Class dataCl) {
        this(dataCl, NBTPrimitive.class);
    }

    NBTTagType(Class dataCl, Class wrapperCl, Type... types) {
        dataClass = dataCl;
        nmsClass = getNMSClass("NBTTag" + name());
        wrapperClass = wrapperCl;
        dataTypes = types;
        Constructor nmsConst = Reflection.getConstructor(nmsClass, dataClass);
        if (nmsConst == null)
            nmsConst = Reflection.getConstructor(nmsClass);
        nmsConstructor = nmsConst;
        nmsDataField = Reflection.getFirstFieldOfType(nmsClass, dataClass);

    }

    public static NBTTagType of(Object o) {
        if (o == null)
            return null;
        if (o instanceof NBTPrimitive)
            o = ((NBTPrimitive) o).getData();
        if (o instanceof Map)
            return Compound;
        if (o instanceof Iterable || o.getClass().isArray())
            return List;
        return classes.get(Primitives.unwrap(o.getClass()));
    }

    public static NBTTag tag(Object o) {
        NBTTagType type = of(o);
        return type.makeTag(o);
    }

    public NBTTag makeTag(Object o) {
        try {
            if (o == null)
                return null;
            if (o instanceof NBTTag)
                return (NBTTag) o;
            if (o.getClass().getName().startsWith("net.minecraft"))
                o = nmsDataField.get(o);
            NBTTag tag = (NBTTag) wrapperClass.newInstance();
            if (tag instanceof NBTPrimitive)
                ((NBTPrimitive) tag).setData(o);
            else if (tag instanceof NBTList) {
                NBTList list = (NBTList) tag;
                if (o.getClass().isArray()) {
                    int len = Array.getLength(o);
                    for (int i = 0; i < len; ++i) {
                        Object o2 = Array.get(o, i);
                        list.add(of(o2).makeTag(o2));
                    }
                } else
                    for (Object o2 : (Iterable) o)
                        list.add(of(o2).makeTag(o2));
            } else {
                NBTCompound cmp = (NBTCompound) tag;
                for (Map.Entry<String, Object> e : ((Map<String, Object>) o).entrySet())
                    cmp.put(e.getKey(), of(e.getValue()).makeTag(e.getValue()));
            }
            return tag;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
}
