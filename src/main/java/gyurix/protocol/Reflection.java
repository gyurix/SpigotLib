package gyurix.protocol;

import com.google.common.primitives.Primitives;
import gyurix.spigotlib.SU;
import gyurix.spigotutils.ServerVersion;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static gyurix.spigotlib.Config.debug;
import static gyurix.spigotutils.ServerVersion.*;
import static org.apache.commons.lang.ArrayUtils.EMPTY_CLASS_ARRAY;
import static org.apache.commons.lang.ArrayUtils.EMPTY_OBJECT_ARRAY;

public class Reflection {
  public static final ReflectionFactory rf = ReflectionFactory.getReflectionFactory();
  private static final Map<Class, Field[]> allFieldCache = Collections.synchronizedMap(new WeakHashMap<>());
  private static final ConcurrentHashMap<String, String> nmsRenames = new ConcurrentHashMap();
  /**
   * The version of the current server
   */
  public static ServerVersion ver = UNKNOWN;
  public static String version;
  private static Field modifiersField;

  /**
   * Compares two arrays of classes
   *
   * @param l1 - The first array of classes
   * @param l2 - The second array of classes
   * @return True if the classes matches in the 2 arrays, false otherwise
   */
  public static boolean classArrayCompare(Class[] l1, Class[] l2) {
    if (l1.length != l2.length) {
      return false;
    }
    for (int i = 0; i < l1.length; i++) {
      if (l1[i] != l2[i])
        return false;
    }
    return true;
  }

  /**
   * Compares two arrays of classes
   *
   * @param l1 - The first array of classes
   * @param l2 - The second array of classes
   * @return True if each of the second arrays classes is assignable from the first arrays classes
   */
  public static boolean classArrayCompareLight(Class[] l1, Class[] l2) {
    if (l1.length != l2.length) {
      return false;
    }
    for (int i = 0; i < l1.length; i++) {
      if (!Primitives.wrap(l2[i]).isAssignableFrom(Primitives.wrap(l1[i])))
        return false;
    }
    return true;
  }

  public static <T> T convert(Object in, Class<T> to) {
    if (in == null)
      return null;
    to = Primitives.wrap(to);
    String inS = in.getClass().isEnum() ? ((Enum) in).name() : in.toString();
    try {
      Constructor<T> con = to.getConstructor(String.class);
      con.setAccessible(true);
      return con.newInstance(inS);
    } catch (Throwable ignored) {
    }
    try {
      Method m = to.getMethod("valueOf", String.class);
      m.setAccessible(true);
      return (T) m.invoke(null, inS);
    } catch (Throwable ignored) {
    }
    try {
      Method m = to.getMethod("fromString", String.class);
      m.setAccessible(true);
      return (T) m.invoke(null, inS);
    } catch (Throwable ignored) {
    }
    debug.msg("Reflection", "§cFailed to convert §f" + in + "§e(§f" + in.getClass().getName() + "§e)§c to class §f" + to.getName() + "§c.");
    return null;
  }

  public static Field[] getAllFields(Class c) {
    Field[] fs = allFieldCache.get(c);
    if (fs != null)
      return fs;
    ArrayList<Field> out = new ArrayList<>();
    while (c != null) {
      for (Field f : c.getDeclaredFields()) {
        out.add(setFieldAccessible(f));
      }
      c = c.getSuperclass();
    }
    Field[] oa = new Field[out.size()];
    out.toArray(oa);
    allFieldCache.put(c, oa);
    return oa;
  }

  /**
   * Gets a class or an inner class
   *
   * @param className - The name of the gettable class
   * @return The found class or null if it was not found.
   */
  public static Class getClass(String className) {
    try {
      String[] classNames = className.split("\\$");
      Class c = Class.forName(classNames[0]);
      for (int i = 1; i < classNames.length; i++)
        c = getInnerClass(c, classNames[i]);
      return c;
    } catch (Throwable ignored) {
    }
    debug.msg("Reflection", new Throwable("Class §f" + className + "§e was not found."));
    return null;
  }

  /**
   * Gets the constructor of the given class
   *
   * @param cl      - The class
   * @param classes - The parameters of the constructor
   * @return The found constructor or null if it was not found.
   */
  public static Constructor getConstructor(Class cl, Class... classes) {
    try {
      Constructor c = cl.getDeclaredConstructor(classes);
      c.setAccessible(true);
      return c;
    } catch (Throwable e) {
      debug.msg("Reflection", e);
    }
    return null;
  }

  public static Object getData(Object obj, List<Object> data) {
    try {
      Class ocl = obj.getClass();
      Object[] input = EMPTY_OBJECT_ARRAY;
      Class[] classes = EMPTY_CLASS_ARRAY;
      for (Object o : data) {
        Class cl = o.getClass();
        if (cl.isArray()) {
          input = (Object[]) o;
          classes = new Class[input.length];
          for (int i = 0; i < input.length; i++)
            classes[i] = input[i].getClass();
          continue;
        }
        for (String name : String.valueOf(o).split("\\.")) {
          if (input.length == 0) {
            Field f = getField(ocl, name);
            if (f != null) {
              obj = f.get(obj);
              ocl = obj.getClass();
              continue;
            }
          }
          Method m = getSimiliarMethod(ocl, name, classes);
          Class[] parCls = m.getParameterTypes();
          Object[] pars = new Object[parCls.length];
          for (int i = 0; i < parCls.length; i++) {
            pars[i] = convert(input[i], parCls[i]);
          }
          obj = m.invoke(obj, pars);
          if (obj == null) {
            throw new RuntimeException("Null return value of method call (method " + m.getName() + ", entered parameters: " + StringUtils.join(pars, ", ") + ".");
          }
          ocl = obj.getClass();
          input = EMPTY_OBJECT_ARRAY;
          classes = EMPTY_CLASS_ARRAY;
        }
      }
    } catch (Throwable e) {
      debug.msg("Reflection", "§cFailed to handle §f" + StringUtils.join(data, "§f.") + "§c request.");
      debug.msg("Reflection", e);
      return null;
    }
    return obj;
  }

  public static Object getEnum(Class enumType, String value) {
    try {
      return enumType.getMethod("valueOf", String.class).invoke(null, value);
    } catch (Throwable e) {
      debug.msg("Reflection", e);
      return null;
    }
  }

  public static Field getField(Class clazz, String name) {
    Field f = null;
    while (clazz != null) {
      try {
        return setFieldAccessible(clazz.getDeclaredField(name));
      } catch (NoSuchFieldException e) {
        clazz = clazz.getSuperclass();
      }
    }
    return null;
  }

  public static Object getFieldData(Class clazz, String name) {
    return getFieldData(clazz, name, null);
  }

  public static Object getFieldData(Class clazz, String name, Object object) {
    try {
      return setFieldAccessible(clazz.getDeclaredField(name)).get(object);
    } catch (Throwable e) {
      debug.msg("Reflection", e);
      return null;
    }
  }

  public static Field getFirstFieldOfType(Class clazz, Class type) {
    try {
      for (Field f : clazz.getDeclaredFields()) {
        if (f.getType().equals(type))
          return setFieldAccessible(f);
      }
      for (Field f : clazz.getDeclaredFields())
        if (type.isAssignableFrom(f.getType()))
          return setFieldAccessible(f);
    } catch (Throwable e) {
      debug.msg("Reflection", e);
    }
    return null;
  }

  public static Class getInnerClass(Class cl, String name) {
    try {
      name = cl.getName() + "$" + name;
      for (Class c : cl.getDeclaredClasses())
        if (c.getName().equals(name))
          return c;
    } catch (Throwable e) {
      debug.msg("Reflection", e);
    }
    return null;
  }

  public static Field getLastFieldOfType(Class clazz, Class type) {
    Field field = null;
    for (Field f : clazz.getDeclaredFields()) {
      if (f.getType().equals(type))
        field = f;
    }
    if (field == null) {
      for (Field f : clazz.getDeclaredFields())
        if (type.isAssignableFrom(f.getType()))
          field = f;
    }
    return setFieldAccessible(field);
  }

  public static Method getMethod(Class cl, String name, Class... args) {
    if (cl == null || name == null || ArrayUtils.contains(args, null))
      return null;
    String originalClassName = cl.getName();
    if (args.length == 0) {
      while (cl != null) {
        Method m = methodCheckNoArg(cl, name);
        if (m != null) {
          m.setAccessible(true);
          return m;
        }
        cl = cl.getSuperclass();
      }
      debug.msg("Reflection", "Method §f" + name + "§e with no parameters was not found in class §f" + originalClassName + "§e.");
    } else {
      while (cl != null) {
        Method m = methodCheck(cl, name, args);
        if (m != null) {
          m.setAccessible(true);
          return m;
        }
        cl = cl.getSuperclass();
      }
      StringBuilder sb = new StringBuilder();
      for (Class c : args)
        sb.append(", ").append(c.getName());
      debug.msg("Reflection", "Method §f" + name + "§e with parameters §f" + sb.substring(2) + "§e was not found in class §f" + originalClassName + "§e.");
    }
    return null;
  }

  public static Class getNMSClass(String className) {
    String newName = nmsRenames.get(className);
    if (newName != null) {
      className = newName;
      if (className.contains("."))
        return getClass(className);
    }
    return getClass("net.minecraft.server." + version + className);
  }

  public static Class getOBCClass(String className) {
    return getClass("org.bukkit.craftbukkit." + version + className);
  }

  public static Method getSimiliarMethod(Class ocl, String name, Class[] classes) {
    Method m = getMethod(ocl, name, classes);
    if (m == null) {
      m = getMethod(ocl, "get" + name, classes);
      if (m == null)
        m = getMethod(ocl, "is" + name, classes);
    }
    if (m != null)
      return m;
    name = name.toLowerCase();
    Class origCl = ocl;
    while (ocl != null) {
      for (Method m2 : ocl.getDeclaredMethods()) {
        if (m2.getParameterTypes().length != classes.length)
          continue;
        String mn = m2.getName().toLowerCase();
        if (mn.endsWith(name) && (mn.startsWith(name) || mn.startsWith("get") || mn.startsWith("is")))
          return m2;
      }
      ocl = ocl.getSuperclass();
    }
    debug.msg("Reflection", "§cFailed to get similiar method to §e" + name + "§c in class §e" + origCl.getName() + "§c.");
    return null;
  }

  public static Class getUtilClass(String className) {
    if (ver.isAbove(v1_8))
      return getClass(className);
    return getClass("net.minecraft.util." + className);
  }

  public static void init() {
    String name = Bukkit.getServer().getClass().getPackage().getName();
    version = name.substring(name.lastIndexOf('.') + 1);
    try {
      modifiersField = Field.class.getDeclaredField("modifiers");
      modifiersField.setAccessible(true);
      ver = valueOf(version.substring(0, version.length() - 3));
    } catch (Throwable ignored) {
    }
    SU.cs.sendMessage("§2[§aStartup§2]§e Detected server version:§a " + ver + "§e (§f" + version + "§e) - §f" + Bukkit.getServer().getVersion());
    if (Reflection.ver == v1_7) {
      nmsRenames.put("PacketLoginOutSetCompression", "org.spigotmc.ProtocolInjector$PacketLoginCompression");
      nmsRenames.put("PacketPlayOutTitle", "org.spigotmc.ProtocolInjector$PacketTitle");
      nmsRenames.put("PacketPlayOutPlayerListHeaderFooter", "org.spigotmc.ProtocolInjector$PacketTabHeader");
      nmsRenames.put("PacketPlayOutResourcePackSend", "org.spigotmc.ProtocolInjector$PacketPlayResourcePackSend");
      nmsRenames.put("PacketPlayInResourcePackStatus", "org.spigotmc.ProtocolInjector$PacketPlayResourcePackStatus");
    }
    if (!version.equals("v1_8_R1")) {
      nmsRenames.put("PacketPlayInLook", "PacketPlayInFlying$PacketPlayInLook");
      nmsRenames.put("PacketPlayInPosition", "PacketPlayInFlying$PacketPlayInPosition");
      nmsRenames.put("PacketPlayInPositionLook", "PacketPlayInFlying$PacketPlayInPositionLook");
      nmsRenames.put("PacketPlayInRelPositionLook", "PacketPlayInFlying$PacketPlayInPositionLook");
      nmsRenames.put("PacketPlayOutEntityLook", "PacketPlayOutEntity$PacketPlayOutEntityLook");
      nmsRenames.put("PacketPlayOutRelEntityMove", "PacketPlayOutEntity$PacketPlayOutRelEntityMove");
      nmsRenames.put("PacketPlayOutRelEntityMoveLook", "PacketPlayOutEntity$PacketPlayOutRelEntityMoveLook");
    }
    if (Reflection.ver.isAbove(v1_9))
      nmsRenames.put("WorldSettings$EnumGamemode", "EnumGamemode");
    version += ".";
  }

  private static Method methodCheck(Class cl, String name, Class[] args) {
    try {
      return cl.getDeclaredMethod(name, args);
    } catch (Throwable e) {
      Method[] mtds = cl.getDeclaredMethods();
      for (Method met : mtds)
        if (classArrayCompare(args, met.getParameterTypes()) && met.getName().equals(name))
          return met;
      for (Method met : mtds)
        if (classArrayCompareLight(args, met.getParameterTypes()) && met.getName().equals(name))
          return met;
      for (Method met : mtds)
        if (classArrayCompare(args, met.getParameterTypes()) && met.getName().equalsIgnoreCase(name))
          return met;
      for (Method met : mtds)
        if (classArrayCompareLight(args, met.getParameterTypes()) && met.getName().equalsIgnoreCase(name))
          return met;
      return null;
    }
  }

  private static Method methodCheckNoArg(Class cl, String name) {
    try {
      return cl.getDeclaredMethod(name);
    } catch (Throwable e) {
      Method[] mtds = cl.getDeclaredMethods();
      for (Method met : mtds)
        if (met.getParameterTypes().length == 0 && met.getName().equalsIgnoreCase(name))
          return met;
      return null;
    }
  }

  /**
   * Constructs a new instance of the given class
   *
   * @param cl      - The class
   * @param classes - The parameters of the constructor
   * @param objs    - The objects, passed to the constructor
   * @return The object constructed, with the found constructor or null if there was an error.
   */
  public static Object newInstance(Class cl, Class[] classes, Object... objs) {
    try {
      Constructor c = cl.getDeclaredConstructor(classes);
      c.setAccessible(true);
      return c.newInstance(objs);
    } catch (NoSuchMethodException e) {
      if (debug.isEnabled("Reflection")) {
        StringBuilder sb = new StringBuilder();
        for (Class c : classes)
          sb.append(", ").append(c.getName());
        debug.msg("Reflection", "§eConstructor §f" + cl.getName() + "§e( §f" + sb + "§e ) was not found.");
      }
    } catch (Throwable e) {
      SU.error(SU.cs, e, "SpigotLib", "gyurix");
    }
    return null;
  }

  /**
   * Constructs a new instance of the given class
   *
   * @param cl - The class
   */
  public static Object newInstance(Class cl) {
    try {
      try {
        return cl.newInstance();
      } catch (Throwable err) {
        return rf.newConstructorForSerialization(cl, Object.class.getDeclaredConstructor()).newInstance();
      }
    } catch (Throwable e) {
      SU.error(SU.cs, e, "SpigotLib", "gyurix");
    }
    return null;
  }

  public static Field setFieldAccessible(Field f) {
    if (f == null)
      return null;
    try {
      f.setAccessible(true);
      int modifiers = modifiersField.getInt(f);
      modifiersField.setInt(f, modifiers & -17);
      return f;
    } catch (Throwable e) {
      SU.error(SU.cs, e, "SpigotLib", "gyurix");
    }
    return null;
  }
}