package gyurix.configfile;

import gyurix.spigotlib.SU;
import gyurix.spigotutils.DualMap;
import org.apache.commons.lang.ClassUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;


public class ConfigSerialization {
  private static final DualMap<Class, String> aliases = new DualMap<>();
  private static final DualMap<Class, Class> interfaceBasedClasses = new DualMap<>();
  private static final HashMap<Class, Serializer> serializers = new HashMap<>();

  static {
    DefaultSerializers.init();
  }

  public static String calculateClassName(Class type, Class objectClass) {
    if (!objectClass.getName().equals(type.getName())) {
      Class c = interfaceBasedClasses.get(type);
      c = c == null ? type : c;
      if (c != objectClass) {
        return '-' + getAlias(objectClass);
      }
    }
    return "";
  }

  public static String getAlias(Class c) {
    if (c.isArray())
      c = Array.class;
    String al = aliases.get(c);
    if (al == null) {
      Class i = interfaceBasedClasses.getKey(c);
      if (i != null) {
        al = aliases.get(i);
        return al == null ? i.getName() : al;
      }
      return c.getName();
    }
    return al;
  }

  public static DualMap<Class, String> getAliases() {
    return aliases;
  }

  public static DualMap<Class, Class> getInterfaceBasedClasses() {
    return interfaceBasedClasses;
  }

  public static Class getNotInterfaceClass(Class cl) {
    Class c = interfaceBasedClasses.get(cl);
    return c == null ? cl : c;
  }

  public static Serializer getSerializer(Class cl) {
    Class c = cl;
    if (cl.isArray()) {
      return serializers.get(Array.class);
    }
    Serializer s = serializers.get(c);
    for (; ; ) {
      if (s != null)
        return s;
      c = c.getSuperclass();
      if (c == null || c == Object.class)
        break;
      s = serializers.get(c);
    }
    for (Class i : (List<Class>) ClassUtils.getAllInterfaces(cl)) {
      s = serializers.get(i);
      if (s != null) {
        return s;
      }
    }
    return serializers.get(Object.class);
  }

  public static HashMap<Class, Serializer> getSerializers() {
    return serializers;
  }

  public static Class realClass(String alias) {
    Class alC = aliases.getKey(alias);
    try {
      Class c = alC == null ? Class.forName(alias) : alC;
      Class c2 = interfaceBasedClasses.get(c);
      return c2 == null ? c : c2;
    } catch (Throwable e) {
      SU.error(SU.cs, e, "SpigotLib", "gyurix");
    }
    return null;
  }

  @Target({ElementType.FIELD, ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface ConfigOptions {
    String comment() default "";

    String defaultValue() default "null";

    boolean serialize() default true;
  }

  public interface Serializer {
    Object fromData(ConfigData paramConfigData, Class paramClass, Type... paramVarArgs);

    default ConfigData postSerialize(Object o, ConfigData data) {
      return data;
    }

    default Class resolveType(ConfigData data) {
      return Object.class;
    }

    ConfigData toData(Object paramObject, Type... paramVarArgs);
  }

  public interface StringSerializable {
    String toString();
  }
}