package gyurix.configfile;

import gyurix.configfile.ConfigSerialization.ConfigOptions;
import gyurix.configfile.ConfigSerialization.Serializer;
import gyurix.configfile.ConfigSerialization.StringSerializable;
import gyurix.nbt.NBTCompound;
import gyurix.nbt.NBTList;
import gyurix.nbt.NBTTag;
import gyurix.protocol.Primitives;
import gyurix.protocol.Reflection;
import gyurix.spigotlib.Main;
import gyurix.spigotlib.SU;
import gyurix.spigotutils.DualMap;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import static gyurix.configfile.ConfigData.serializeObject;
import static gyurix.protocol.Reflection.newInstance;
import static gyurix.spigotlib.Config.debug;

public class DefaultSerializers {
    public static final Type[] emptyTypeArray = new Type[0];
    public static int leftPad;

    public static void init() {
        HashMap<Class, Serializer> serializers = ConfigSerialization.getSerializers();
        NBTTag.NBTSerializer nbtSerializer = new NBTTag.NBTSerializer();
        serializers.put(NBTTag.class, nbtSerializer);
        serializers.put(String.class, new StringSerializer());
        serializers.put(Class.class, new ClassSerializer());
        serializers.put(UUID.class, new UUIDSerializer());
        serializers.put(ConfigData.class, new ConfigDataSerializer());

        NumberSerializer numSerializer = new NumberSerializer();
        serializers.put(Array.class, new ArraySerializer());
        serializers.put(Boolean.class, new BooleanSerializer());
        serializers.put(Byte.class, numSerializer);
        serializers.put(Character.class, new CharacterSerializer());
        serializers.put(Collection.class, new CollectionSerializer());
        serializers.put(NBTList.class, new CollectionSerializer(NBTTag.class, nbtSerializer));
        serializers.put(Double.class, numSerializer);
        serializers.put(Float.class, numSerializer);
        serializers.put(Integer.class, numSerializer);
        serializers.put(Long.class, numSerializer);
        serializers.put(Map.class, new MapSerializer());
        serializers.put(NBTCompound.class, new MapSerializer(String.class, NBTTag.class, nbtSerializer));
        serializers.put(Object.class, new ObjectSerializer());
        serializers.put(Pattern.class, new PatternSerializer());
        serializers.put(Short.class, numSerializer);
        serializers.put(SimpleDateFormat.class, new SimpleDateFormatSerializer());

        DualMap<Class, String> aliases = ConfigSerialization.getAliases();
        aliases.put(Array.class, "[]");
        aliases.put(Boolean.class, "bool");
        aliases.put(Byte.class, "b");
        aliases.put(Character.class, "c");
        aliases.put(Collection.class, "{}");
        aliases.put(Double.class, "d");
        aliases.put(Float.class, "f");
        aliases.put(Integer.class, "i");
        aliases.put(LinkedHashMap.class, "<L>");
        aliases.put(LinkedHashSet.class, "{LS}");
        aliases.put(List.class, "{L}");
        aliases.put(Long.class, "l");
        aliases.put(Map.class, "<>");
        aliases.put(Object.class, "?");
        aliases.put(Set.class, "{S}");
        aliases.put(Short.class, "s");
        aliases.put(String.class, "str");
        aliases.put(TreeMap.class, "<T>");
        aliases.put(TreeSet.class, "{TS}");
        aliases.put(UUID.class, "uuid");

        DualMap<Class, Class> ifbClasses = ConfigSerialization.getInterfaceBasedClasses();
        ifbClasses.put(List.class, ArrayList.class);
        ifbClasses.put(Set.class, HashSet.class);
        ifbClasses.put(Map.class, HashMap.class);
    }

    public static boolean shouldSkip(Class cl) {
        return cl == Class.class || cl.getName().startsWith("java.lang.reflect.") || cl.getName().startsWith("sun.");
    }

    public static class ArraySerializer implements Serializer {
        public Object fromData(ConfigData input, Class fixClass, Type... parameterTypes) {
            Class cl = Object.class;
            Type[] types = emptyTypeArray;
            if (parameterTypes.length >= 1) {
                if (parameterTypes[0] instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) parameterTypes[0];
                    cl = (Class) pt.getRawType();
                    types = pt.getActualTypeArguments();
                } else {
                    cl = (Class) parameterTypes[0];
                }
            }
            if (input.listData != null) {
                Object ar = Array.newInstance(cl, input.listData.size());
                int i = 0;
                for (ConfigData d : input.listData) {
                    Array.set(ar, i++, d.deserialize(cl, types));
                }
                return ar;
            } else {
                String[] sd = input.stringData.split(";");
                Object ar = Array.newInstance(cl, sd.length);
                int i = 0;
                for (String d : sd) {
                    Array.set(ar, i++, new ConfigData(d).deserialize(cl, types));
                }
                return ar;
            }
        }


        public ConfigData toData(Object input, Type... parameters) {
            Class cl = parameters.length >= 1 ? (Class) parameters[0] : Object.class;
            ConfigData d = new ConfigData();
            d.listData = new ArrayList<>();
            if (input instanceof Object[])
                for (Object o : Arrays.asList((Object[]) input)) {
                    d.listData.add(serializeObject(o, !(o instanceof ItemStack) && o.getClass() != cl));
                }
            else {
                int len = Array.getLength(input);
                for (int i = 0; i < len; ++i) {
                    Object o = Array.get(input, i);
                    d.listData.add(serializeObject(o, !(o instanceof ItemStack) && o.getClass() != cl));
                }
            }
            return d;
        }
    }

    public static class BooleanSerializer implements Serializer {
        public Object fromData(ConfigData input, Class cl, Type... parameters) {
            String s = input.stringData;
            return s != null && (s.equals("+") || s.equals("true") || s.equals("yes"));
        }

        public ConfigData toData(Object in, Type... parameters) {
            return new ConfigData((boolean) in ? "+" : "-");
        }
    }

    public static class CharacterSerializer implements Serializer {
        public Object fromData(ConfigData input, Class cl, Type... parameters) {
            return input.stringData.charAt(0);
        }

        public ConfigData toData(Object in, Type... parameters) {
            return new ConfigData(String.valueOf(in));
        }
    }

    private static class ClassSerializer implements Serializer {
        ClassSerializer() {
        }

        public Object fromData(ConfigData input, Class cl, Type... parameters) {
            try {
                return Class.forName(input.stringData);
            } catch (ClassNotFoundException e) {
                SU.error(SU.cs, e, "SpigotLib", "gyurix");
            }
            return null;
        }

        public ConfigData toData(Object input, Type... parameters) {
            return new ConfigData(((Class) input).getName());
        }
    }

    public static class CollectionSerializer implements Serializer {
        private Serializer childSerializer;
        private Class defaultKeyClass;

        public CollectionSerializer() {
            defaultKeyClass = Object.class;
        }

        public CollectionSerializer(Class<NBTTag> defaultKeyClass, Serializer childSerializer) {
            this.defaultKeyClass = defaultKeyClass;
            this.childSerializer = childSerializer;
        }

        public Object fromData(ConfigData input, Class fixClass, Type... parameterTypes) {
            try {
                Collection col = (Collection) ConfigSerialization.getNotInterfaceClass(fixClass).newInstance();
                Class cl;
                Type[] types;
                ParameterizedType pt;
                cl = defaultKeyClass;
                types = emptyTypeArray;
                if (parameterTypes.length >= 1) {
                    if (parameterTypes[0] instanceof ParameterizedType) {
                        pt = (ParameterizedType) parameterTypes[0];
                        cl = (Class) pt.getRawType();
                        types = pt.getActualTypeArguments();
                    } else {
                        cl = (Class) parameterTypes[0];
                    }
                }
                if (input.listData != null) {
                    for (ConfigData d : input.listData) {
                        col.add(d.deserialize(cl, types));
                    }
                } else if (input.stringData != null && !input.stringData.isEmpty()) {
                    for (String s : input.stringData.split("[;,] *"))
                        col.add(new ConfigData(s).deserialize(cl, types));
                }
                return col;
            } catch (Throwable e) {
                SU.error(SU.cs, e, "SpigotLib", "gyurix");
            }
            return null;
        }


        public ConfigData toData(Object input, Type... parameters) {
            Type[] types = emptyTypeArray;
            Class keyClass = defaultKeyClass;
            if (parameters.length >= 1) {
                if (parameters[0] instanceof ParameterizedType) {
                    ParameterizedType key = (ParameterizedType) parameters[0];
                    types = key.getActualTypeArguments();
                    keyClass = (Class) key.getRawType();
                } else {
                    keyClass = (Class) parameters[0];
                }
            }
            if (((Collection) input).isEmpty())
                return new ConfigData("");
            ConfigData d = new ConfigData();
            d.listData = new ArrayList<>();
            for (Object o : (Collection) input)
                d.listData.add(serializeObject(o, o.getClass() != keyClass, types));
            return childSerializer == null ? d : childSerializer.postSerialize(input, d);
        }
    }

    public static class ConfigDataSerializer implements Serializer {
        public Object fromData(ConfigData data, Class cl, Type... type) {
            return data;
        }

        public ConfigData toData(Object data, Type... type) {
            return (ConfigData) data;
        }
    }

    public static class MapSerializer implements Serializer {
        private Serializer child;
        private Class defaultKeyClass, defaultValueClass;

        public MapSerializer() {
            this.defaultKeyClass = Object.class;
            this.defaultValueClass = Object.class;
        }

        public MapSerializer(Class defaultKeyClass, Class defaultValueClass, Serializer child) {
            this.defaultKeyClass = defaultKeyClass;
            this.defaultValueClass = defaultValueClass;
            this.child = child;
        }

        public Object fromData(ConfigData input, Class fixClass, Type... parameterTypes) {
            try {
                Map map;
                if (fixClass == EnumMap.class)
                    map = new EnumMap((Class) parameterTypes[0]);
                else
                    map = (Map) fixClass.newInstance();
                Class keyClass;
                Type[] keyTypes;
                Class valueClass;
                Type[] valueTypes;
                ParameterizedType pt;
                if (input.mapData != null) {
                    keyClass = defaultKeyClass;
                    keyTypes = emptyTypeArray;
                    if (parameterTypes.length >= 1) {
                        if (parameterTypes[0] instanceof ParameterizedType) {
                            pt = (ParameterizedType) parameterTypes[0];
                            keyClass = (Class) pt.getRawType();
                            keyTypes = pt.getActualTypeArguments();
                        } else {
                            keyClass = (Class) parameterTypes[0];
                        }
                    }
                    boolean dynamicValueCl = ValueClassSelector.class.isAssignableFrom(keyClass);
                    valueClass = defaultValueClass;
                    valueTypes = emptyTypeArray;
                    if (!dynamicValueCl && parameterTypes.length >= 2) {
                        if (parameterTypes[1] instanceof ParameterizedType) {
                            pt = (ParameterizedType) parameterTypes[1];
                            valueClass = (Class) pt.getRawType();
                            valueTypes = pt.getActualTypeArguments();
                        } else {
                            valueClass = (Class) parameterTypes[1];
                        }
                    }
                    if (dynamicValueCl) {
                        for (Entry<ConfigData, ConfigData> e : input.mapData.entrySet()) {
                            try {
                                ValueClassSelector key = (ValueClassSelector) e.getKey().deserialize(keyClass, keyTypes);
                                map.put(key, e.getValue().deserialize(key.getValueClass(), key.getValueTypes()));
                            } catch (Throwable err) {
                                SU.cs.sendMessage("§cMap element deserialization error:\n§eKey = §f" + e.getKey() + "§e; Value = §f" + e.getValue());
                                SU.error(SU.cs, err, "SpigotLib", "gyurix");
                            }
                        }
                    } else {
                        for (Entry<ConfigData, ConfigData> e : input.mapData.entrySet()) {
                            try {
                                map.put(e.getKey().deserialize(keyClass, keyTypes), e.getValue().deserialize(valueClass, valueTypes));
                            } catch (Throwable err) {
                                SU.log(Main.pl, "§cMap element deserialization error:\n§eKey = §f" + e.getKey() + "§e; Value = §f" + e.getValue());
                                SU.error(SU.cs, err, "SpigotLib", "gyurix");
                            }
                        }
                    }
                }
                return map;
            } catch (Throwable e) {
                e.printStackTrace();
                //SU.error(SU.cs, e, "SpigotLib", "gyurix");
            }
            return null;
        }


        public ConfigData toData(Object input, Type... parameters) {
            try {
                if (((Map) input).isEmpty())
                    return new ConfigData();
                Class keyClass = defaultKeyClass;
                Class valueClass = defaultValueClass;
                Type[] keyTypes = emptyTypeArray;
                Type[] valueTypes = emptyTypeArray;
                if (parameters.length >= 1) {
                    if (parameters[0] instanceof ParameterizedType) {
                        ParameterizedType key = (ParameterizedType) parameters[0];
                        keyTypes = key.getActualTypeArguments();
                        keyClass = (Class) key.getRawType();
                    } else {
                        keyClass = (Class) parameters[0];
                    }
                }
                boolean valueClassSelector = keyClass.isAssignableFrom(ValueClassSelector.class);
                if (!valueClassSelector && parameters.length >= 2) {
                    if (parameters[1] instanceof ParameterizedType) {
                        ParameterizedType value = (ParameterizedType) parameters[1];
                        valueTypes = value.getActualTypeArguments();
                        valueClass = (Class) value.getRawType();
                    } else {
                        valueClass = (Class) parameters[1];
                    }
                }

                ConfigData d = new ConfigData();
                d.mapData = new LinkedHashMap();
                for (Entry<?, ?> e : ((Map<?, ?>) input).entrySet()) {
                    Object key = e.getKey();
                    Object value = e.getValue();
                    if (key != null && value != null)
                        d.mapData.put(serializeObject(key, key.getClass() != keyClass, keyTypes),
                                serializeObject(value, !valueClassSelector && value.getClass() != valueClass, valueTypes));
                }
                return child == null ? d : child.postSerialize(input, d);
            } catch (Throwable e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public static class NumberSerializer implements Serializer {
        public static final HashMap<Class, Method> methods = new HashMap();

        static {
            try {
                methods.put(Short.class, Short.class.getMethod("decode", String.class));
                methods.put(Integer.class, Integer.class.getMethod("decode", String.class));
                methods.put(Long.class, Long.class.getMethod("decode", String.class));
                methods.put(Float.class, Float.class.getMethod("valueOf", String.class));
                methods.put(Double.class, Double.class.getMethod("valueOf", String.class));
                methods.put(Byte.class, Byte.class.getMethod("valueOf", String.class));
            } catch (Throwable e) {
                SU.error(SU.cs, e, "SpigotLib", "gyurix");
            }
        }

        public Object fromData(ConfigData input, Class fixClass, Type... parameters) {
            Method m = methods.get(Primitives.wrap(fixClass));
            try {
                String s = StringUtils.stripStart(input.stringData.replace(" ", ""), "0");
                return m.invoke(null, s.isEmpty() ? "0" : s);
            } catch (Throwable e) {
                debug.msg("Config", "INVALID NUMBER: " + fixClass.getName() + " - " + input);
                debug.msg("Config", e);
                try {
                    return m.invoke(null, "0");
                } catch (Throwable e2) {
                    debug.msg("Config", "Not a number class: " + fixClass.getSimpleName());
                    debug.msg("Config", e);
                }
            }
            return null;
        }

        public ConfigData toData(Object input, Type... parameters) {
            String s = input.toString();
            int id = (s + ".").indexOf(".");
            return new ConfigData(StringUtils.leftPad(s, Math.max(leftPad + s.length() - id, 0), '0'));

        }
    }

    public static class ObjectSerializer implements Serializer {
        public Object fromData(ConfigData input, Class fixClass, Type... parameters) {
            if (Thread.currentThread().getStackTrace().length > 100) {
                SU.cs.sendMessage("§ePossible infinite loop - " + fixClass.getName());
                return null;
            }
            ConfigOptions co = (ConfigOptions) fixClass.getAnnotation(ConfigOptions.class);
            if (co != null && co.compress())
                input = input.decompress();
            try {
                if (fixClass.isEnum() || fixClass.getSuperclass() != null && fixClass.getSuperclass().isEnum()) {
                    if (input.stringData == null || input.stringData.equals(""))
                        return null;
                    for (Object en : fixClass.getEnumConstants()) {
                        if (en.toString().equals(input.stringData))
                            return en;
                    }
                    return null;
                }
                if (ArrayUtils.contains(fixClass.getInterfaces(), StringSerializable.class) || fixClass == BigDecimal.class || fixClass == BigInteger.class) {
                    if (input.stringData == null || input.stringData.equals(""))
                        return null;
                    return fixClass.getConstructor(String.class).newInstance(input.stringData);
                }
            } catch (Throwable e) {
                System.err.println("Error on deserializing \"" + input.stringData + "\" to a " + fixClass.getName() + " object.");
                SU.error(SU.cs, e, "SpigotLib", "gyurix");
                return null;
            }


            Object obj = newInstance(fixClass);
            if (input.mapData == null)
                return obj;
            for (Field f : Reflection.getAllFields(fixClass)) {
                f.setAccessible(true);
                try {
                    if (shouldSkip(f.getType()))
                        continue;
                    String fn = f.getName();
                    ConfigData d = input.mapData.get(new ConfigData(fn));
                    Class cl = Primitives.wrap(f.getType());
                    if (d != null) {
                        co = f.getAnnotation(ConfigOptions.class);
                        if (co != null && co.compress())
                            d = d.decompress();
                        Type[] types = f.getGenericType() instanceof ParameterizedType ?
                                ((ParameterizedType) f.getGenericType()).getActualTypeArguments() : cl.isArray() ? new Type[]{cl.getComponentType()} : emptyTypeArray;
                        Object out = d.deserialize(ConfigSerialization.getNotInterfaceClass(cl), types);
                        if (out != null)
                            f.set(obj, out);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            try {
                if (obj instanceof PostLoadable)
                    ((PostLoadable) obj).postLoad();
            } catch (Throwable e) {
                SU.log(Main.pl, "§cError on post loading §e" + fixClass.getName() + "§c object.");
                SU.error(SU.cs, e, SU.getPlugin(fixClass).getName(), "gyurix");
            }
            return obj;
        }

        public ConfigData toData(Object obj, Type... parameters) {
            if (Thread.currentThread().getStackTrace().length > 100) {
                SU.cs.sendMessage("§ePossible infinite loop - " + obj.getClass().getName());
                return null;
            }
            Class c = Primitives.wrap(obj.getClass());
            if (c.isEnum() || c.getSuperclass() != null && c.getSuperclass().isEnum() || ArrayUtils.contains(c.getInterfaces(), StringSerializable.class) || c == BigDecimal.class || c == BigInteger.class) {
                return new ConfigData(obj.toString());
            }
            ConfigOptions dfOptions = (ConfigOptions) c.getAnnotation(ConfigOptions.class);
            String dfValue = dfOptions == null ? "null" : dfOptions.defaultValue();
            boolean dfSerialize = dfOptions == null || dfOptions.serialize();
            String comment = dfOptions == null ? "" : dfOptions.comment();
            ConfigData out = new ConfigData();
            if (dfOptions != null && dfOptions.compress())
                out.compress = true;
            if (!comment.isEmpty())
                out.comment = comment;
            out.mapData = new LinkedHashMap();
            for (Field f : Reflection.getAllFields(c)) {
                try {
                    String dffValue = dfValue;
                    boolean compress = false;
                    comment = "";
                    ConfigOptions options = f.getAnnotation(ConfigOptions.class);
                    if (options != null) {
                        if (!options.serialize())
                            continue;
                        dffValue = options.defaultValue();
                        comment = options.comment();
                        compress = options.compress();
                    }
                    if (!dfSerialize)
                        continue;
                    Object o = f.get(obj);
                    if (o != null && !o.toString().matches(dffValue) && !((o instanceof Iterable) && !((Iterable) o).iterator().hasNext())) {
                        String cn = ConfigSerialization.calculateClassName(Primitives.wrap(f.getType()), o.getClass());
                        Class check = f.getType().isArray() ? f.getType().getComponentType() : f.getType();
                        if (shouldSkip(check))
                            continue;
                        String fn = f.getName();
                        Type t = f.getGenericType();
                        debug.msg("Config", "§bSerializing field §e" + f.getName() + "§b of class §e" + c.getName() + "§b having type §e" + o.getClass().getName());
                        ConfigData value = serializeObject(o, !cn.isEmpty(),
                                t instanceof ParameterizedType ?
                                        ((ParameterizedType) t).getActualTypeArguments() :
                                        ((Class) t).isArray() ?
                                                new Type[]{((Class) t).getComponentType()} :
                                                emptyTypeArray);
                        value.compress = compress;
                        out.mapData.put(new ConfigData(fn, comment), value);
                    }
                } catch (Throwable e) {
                    debug.msg("Config", e);
                }
            }
            return out;
        }
    }

    public static class PatternSerializer implements Serializer {
        public Object fromData(ConfigData data, Class paramClass, Type... paramVarArgs) {
            return Pattern.compile(data.stringData);
        }

        public ConfigData toData(Object pt, Type... paramVarArgs) {
            return new ConfigData(((Pattern) pt).pattern());
        }
    }

    public static class SimpleDateFormatSerializer implements Serializer {
        public static final Field patternF = Reflection.getField(SimpleDateFormat.class, "pattern");

        public Object fromData(ConfigData input, Class cl, Type... parameters) {
            return new SimpleDateFormat(input.stringData);
        }

        public ConfigData toData(Object input, Type... parameters) {
            try {
                return new ConfigData((String) patternF.get(input));
            } catch (Throwable e) {
                SU.error(SU.cs, e, "SpigotLib", "gyurix");
            }
            return new ConfigData();
        }
    }

    public static class StringSerializer implements Serializer {
        public Object fromData(ConfigData input, Class cl, Type... parameters) {
            return input.stringData;
        }

        public ConfigData toData(Object input, Type... parameters) {
            return new ConfigData((String) input);
        }
    }

    public static class UUIDSerializer implements Serializer {
        public Object fromData(ConfigData input, Class cl, Type... parameters) {
            return UUID.fromString(input.stringData);
        }

        public ConfigData toData(Object input, Type... parameters) {
            return new ConfigData(input.toString());
        }
    }

}