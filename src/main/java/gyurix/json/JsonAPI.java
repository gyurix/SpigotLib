package gyurix.json;

import com.google.common.primitives.Primitives;
import gyurix.configfile.ConfigSerialization.StringSerializable;
import gyurix.configfile.DefaultSerializers;
import gyurix.protocol.Reflection;
import gyurix.spigotlib.SU;

import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;

import static gyurix.protocol.Reflection.newInstance;
import static gyurix.spigotlib.Config.debug;

public class JsonAPI {
    public static final Type[] emptyTypeArray = new Type[0];

    public static int HextoDec(char c) {
        return c >= '0' && c <= '9' ? c - 48 : c >= 'A' && c <= 'F' ? c - 55 : c - 87;
    }

    private static Object deserialize(Object parent, StringReader in, Class cl, Type... params) throws Throwable {
        cl = Primitives.wrap(cl);
        char c = '-';
        if (in.hasNext())
            c = in.next();
        else in.id++;
        if (Map.class.isAssignableFrom(cl)) {
            if (c != '{') {
                throw new Throwable("JSONAPI: Error on deserializing Json " + new String(in.str) + ", expected {, found " + c + " (character id: " + in.id + ")");
            }
            Class keyClass = (Class) (params[0] instanceof ParameterizedType ? ((ParameterizedType) params[0]).getRawType() : params[0]);
            Type[] keyType = params[0] instanceof ParameterizedType ? ((ParameterizedType) params[0]).getActualTypeArguments() : emptyTypeArray;
            Class valueClass = (Class) (params[1] instanceof ParameterizedType ? ((ParameterizedType) params[1]).getRawType() : params[1]);
            Type[] valueType = params[1] instanceof ParameterizedType ? ((ParameterizedType) params[1]).getActualTypeArguments() : emptyTypeArray;
            Map map = cl == EnumMap.class ? new EnumMap<>(keyClass) : (Map) cl.newInstance();
            if (in.next() == '}')
                return map;
            else
                in.id -= 2;
            while (in.next() != '}') {
                Object key = deserialize(map, in, keyClass, keyType);
                if (in.next() != ':')
                    throw new Throwable("JSONAPI: Error on deserializing Json " + new String(in.str) + ", expected :, found " + in.last() + " (character id: " + (in.id - 1) + ")");
                map.put(key, deserialize(map, in, valueClass, valueType));
            }
            return map;
        } else if (Collection.class.isAssignableFrom(cl)) {
            if (c != '[') {
                throw new Throwable("JSONAPI: Error on deserializing Json " + new String(in.str) + ", expected {, found " + c + " (character id: " + in.id + ")");
            }
            Class dataClass = (Class) (params[0] instanceof ParameterizedType ? ((ParameterizedType) params[0]).getRawType() : params[0]);
            Type[] dataType = params[0] instanceof ParameterizedType ? ((ParameterizedType) params[0]).getActualTypeArguments() : emptyTypeArray;
            Collection col = (Collection) cl.newInstance();
            if (in.next() == ']')
                return col;
            else
                in.id -= 2;
            while (in.next() != ']') {
                col.add(deserialize(col, in, dataClass, dataType));
            }
            return col;
        } else if (cl.isArray()) {
            if (c != '[') {
                throw new Throwable("JSONAPI: Error on deserializing Json " + new String(in.str) + ", expected {, found " + c + " (character id: " + in.id + ")");
            }
            Class dataClass = cl.getComponentType();
            ArrayList col = new ArrayList();
            if (in.next() == ']') {
                return Array.newInstance(dataClass, 0);
            } else
                in.id -= 2;
            while (in.next() != ']') {
                col.add(deserialize(null, in, dataClass));
            }
            Object[] out = (Object[]) Array.newInstance(dataClass, col.size());
            return col.toArray(out);
        } else if (c == '{') {
            Object obj = newInstance(cl);
            if (in.next() == '}')
                return obj;
            else
                in.id -= 2;
            while (in.next() != '}') {
                String fn = readString(in);
                if (in.next() != ':')
                    throw new Throwable("JSONAPI: Error on deserializing Json " + new String(in.str) + ", expected :, found " + in.last() + " (character id: " + (in.id - 1) + ")");
                try {
                    Field f = Reflection.getField(cl, fn);
                    Type gt = f.getGenericType();
                    f.set(obj, deserialize(obj, in, f.getType(), gt instanceof ParameterizedType ? ((ParameterizedType) gt).getActualTypeArguments() : emptyTypeArray));
                } catch (Throwable e) {
                    SU.cs.sendMessage("§6[§eJSONAPI§6] §cField §f" + fn + "§e is declared in json, but it is missing from class §e" + cl.getName() + "§c.");
                    SU.error(SU.cs, e, "SpigotLib", "gyurix");
                }
            }
            try {
                Field f = Reflection.getField(cl, "parent");
                f.set(obj, parent);
            } catch (Throwable ignored) {
            }
            try {
                Field f = Reflection.getField(cl, "self");
                f.set(obj, obj);
            } catch (Throwable ignored) {
            }
            try {
                Field f = Reflection.getField(cl, "instance");
                f.set(obj, obj);
            } catch (Throwable ignored) {
            }
            return obj;
        } else {
            in.id--;
            String str = readString(in);
            try {
                return Reflection.getConstructor(cl, String.class).newInstance(str);
            } catch (Throwable ignored) {
            }
            try {
                return Reflection.getMethod(cl, "valueOf", String.class).invoke(null, str);
            } catch (Throwable ignored) {
            }
            try {
                Method m = Reflection.getMethod(cl, "fromString", String.class);
                if (cl == UUID.class && !str.contains("-"))
                    str = str.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
                return m.invoke(null, str);

            } catch (Throwable e) {
                SU.error(SU.cs, e, "SpigotLib", "gyurix");
            }
            throw new Throwable("JSONAPI: Error on deserializing Json " + new String(in.str) + ", expected " + cl.getName() + ", found String.");
        }
    }

    public static <T> T deserialize(String json, Class<T> cl, Type... params) {
        if (json == null)
            return null;
        StringReader sr = new StringReader(json);
        try {
            return (T) deserialize(null, sr, cl, params);
        } catch (Throwable e) {
            debug.msg("Json", "§cFailed to deserialize JSON §e" + json + "§c to class §e" + cl.getName());
            debug.msg("Json", e);
            return null;
        }
    }

    public static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public static String readString(StringReader in) {
        int start = in.id;
        int end = -1;
        boolean esc = false;
        boolean stresc = false;
        while (in.hasNext()) {
            char c = in.next();
            if (esc)
                esc = false;
            else if (c == '\\')
                esc = true;
            else if (c == '\"') {
                if (stresc) {
                    end = in.id - 1;
                    break;
                } else {
                    stresc = true;
                    start = in.id;
                }
            } else if (!stresc && (c == ']' || c == '}' || c == ',' || c == ':')) {
                in.id--;
                break;
            }
        }
        if (end == -1)
            end = in.id;
        return unescape(new String(in.str, start, end - start));
    }

    private static void serialize(StringBuilder sb, Object o) {
        if (o == null) {
            sb.append("null");
            return;
        }
        Class cl = o.getClass();
        if (o instanceof String || o instanceof UUID || o.getClass().isEnum() || o instanceof StringSerializable) {
            sb.append('\"').append(escape(o.toString())).append("\"");
        } else if (o instanceof Boolean || o instanceof Byte || o instanceof Short || o instanceof Integer || o instanceof Long || o instanceof Float || o instanceof Double) {
            sb.append(o);
        } else if (o instanceof Iterable || cl.isArray()) {
            sb.append('[');
            if (cl.isArray()) {
                int max = Array.getLength(o);
                for (int i = 0; i < max; i++) {
                    serialize(sb, Array.get(o, i));
                    sb.append(',');
                }
            } else {
                for (Object obj : (Iterable) o) {
                    serialize(sb, obj);
                    sb.append(',');
                }
            }
            if (sb.charAt(sb.length() - 1) == ',') {
                sb.setCharAt(sb.length() - 1, ']');
            } else {
                sb.append(']');
            }
        } else if (o instanceof Map) {
            sb.append('{');
            for (Entry<?, ?> e : ((Map<?, ?>) o).entrySet()) {
                String key = String.valueOf(e.getKey());
                sb.append('\"').append(escape(key)).append("\":");
                serialize(sb, e.getValue());
                sb.append(',');
            }
            if (sb.charAt(sb.length() - 1) == ',') {
                sb.setCharAt(sb.length() - 1, '}');
            } else {
                sb.append('}');
            }
        } else {
            if (DefaultSerializers.shouldSkip(cl)) {
                sb.append('\"').append(escape(o.toString())).append('\"');
                return;
            }
            sb.append('{');
            for (Field f : cl.getDeclaredFields()) {
                try {
                    f.setAccessible(true);
                    JsonSettings settings = f.getAnnotation(JsonSettings.class);
                    String fn = f.getName();
                    boolean serialize = !(fn.equals("self") || fn.equals("parent") || fn.equals("instance"));
                    String defaultValue = null;
                    if (settings != null) {
                        serialize = settings.serialize();
                        defaultValue = settings.defaultValue();
                    }
                    Object fo = f.get(o);
                    if (!serialize || fo == null || defaultValue != null && fo.toString().equals(defaultValue) || fo.getClass().getName().startsWith("java.lang.reflect.") || fo.getClass() == Class.class)
                        continue;
                    sb.append('\"').append(escape(fn)).append("\":");
                    serialize(sb, fo);
                    sb.append(',');
                } catch (Throwable e) {
                    debug.msg("Json", e);
                }
            }
            if (sb.charAt(sb.length() - 1) == ',') {
                sb.setCharAt(sb.length() - 1, '}');
            } else {
                sb.append('}');
            }
        }
    }

    public static String serialize(Object o) {
        StringBuilder sb = new StringBuilder();
        try {
            serialize(sb, o);
            return sb.toString();
        } catch (Throwable e) {
            debug.msg("Json", "Error on serializing " + o.getClass().getName() + " object.");
            debug.msg("Json", e);
            return "{}";
        }
    }

    public static String unescape(String s) {
        boolean esc = false;
        int utf = -1;
        int utfc = -1;
        StringBuilder out = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (esc) {
                switch (c) {
                    case 'b':
                        out.append('\b');
                        break;
                    case 'f':
                        out.append('\f');
                        break;
                    case 'n':
                        out.append('\n');
                        break;
                    case 'r':
                        out.append('\r');
                        break;
                    case 't':
                        out.append('\t');
                        break;
                    case 'u':
                        utf = 0;
                        utfc = 0;
                        break;
                    default:
                        out.append(c);
                }
                esc = false;
                continue;
            }
            if (utf >= 0) {
                utf = utf * 16 + HextoDec(c);
                if (++utfc != 4) continue;
                out.append((char) utf);
                utf = -1;
                utfc = -1;
                continue;
            }
            if (c == '\\') {
                esc = true;
                continue;
            }
            out.append(c);
        }
        return out.toString();
    }
}