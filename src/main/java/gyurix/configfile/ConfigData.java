package gyurix.configfile;

import com.google.common.primitives.Primitives;
import gyurix.configfile.ConfigSerialization.Serializer;
import gyurix.mysql.MySQLDatabase;
import gyurix.spigotlib.Main;
import gyurix.spigotlib.SU;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import static gyurix.configfile.DefaultSerializers.leftPad;

public class ConfigData implements Comparable<ConfigData> {
  public String comment;
  public ArrayList<ConfigData> listData;
  public LinkedHashMap<ConfigData, ConfigData> mapData;
  public Object objectData;
  public String stringData;
  public Type[] types;

  public ConfigData() {
  }

  public ConfigData(String stringData) {
    this.stringData = stringData;
  }

  public ConfigData(Object obj) {
    objectData = obj;
  }

  public ConfigData(String stringData, String comment) {
    this.stringData = stringData;
    if (comment != null && !comment.isEmpty())
      this.comment = comment;
  }

  public static String escape(String in) {
    StringBuilder out = new StringBuilder();
    String escSpace = "\n:>";
    char prev = '\n';
    for (char c : in.toCharArray()) {
      switch (c) {
        case ' ':
          out.append(escSpace.contains(String.valueOf(prev)) ? "\\" + c : c);
          break;
        case '‼':
          if (prev == '\\')
            out.deleteCharAt(out.length() - 1);
          out.append('‼');
          break;
        case '\t':
          out.append("\\t");
          break;
        case '\r':
          out.append("\\r");
          break;
        case '\b':
          out.append("\\b");
          break;
        case '\\':
          out.append("\\\\");
          break;
        default:
          out.append(c);
      }
      prev = c;
    }
    if (prev == '\n' && out.length() != 0) {
      out.setCharAt(out.length() - 1, '\\');
      out.append('n');
    }
    return out.toString();
  }

  public static ConfigData serializeObject(Object obj, boolean className, Type... parameters) {
    if (obj == null)
      return null;
    Class c = Primitives.wrap(obj.getClass());
    if (c.isArray())
      parameters = new Type[]{c.getComponentType()};
    Serializer s = ConfigSerialization.getSerializer(c);
    ConfigData cd = parameters == null ? s.toData(obj) : s.toData(obj, parameters);
    if (cd.stringData != null && cd.stringData.startsWith("‼"))
      cd.stringData = '\\' + cd.stringData;
    if (className && !c.isEnum() && !c.getSuperclass().isEnum()) {
      StringBuilder prefix = new StringBuilder('‼' + ConfigSerialization.getAlias(obj.getClass()));
      for (Type t : parameters) {
        prefix.append('-').append(ConfigSerialization.getAlias((Class) t));
      }
      prefix.append('‼');
      cd.stringData = prefix + (cd.stringData == null ? "" : cd.stringData);
    }
    return cd;
  }

  public static ConfigData serializeObject(Object obj, Type... parameters) {
    return serializeObject(obj, false, parameters);
  }

  public static String unescape(String in) {
    StringBuilder out = new StringBuilder(in.length());
    String uchars = "0123456789abcdef0123456789ABCDEF";
    boolean escape = false;
    int ucode = -1;
    for (char c : in.toCharArray()) {
      if (ucode != -1) {
        int id = uchars.indexOf(c) % 16;
        if (id == -1) {
          out.append((char) ucode);
          ucode = -1;
        } else {
          ucode = ucode * 16 + id;
          continue;
        }
      }
      if (escape) {
        switch (c) {
          case 'u':
            ucode = 0;
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
          case 'b':
            out.append('\b');
            break;
          case ' ':
          case '-':
          case '>':
          case '\\':
            out.append(c);
        }
        escape = false;
      } else if (!(escape = c == '\\')) {
        out.append(c);
      }
    }
    if (ucode != -1)
      out.append((char) ucode);
    return out.toString().replaceAll("\n +#", "\n#");
  }

  @Override
  public int compareTo(@Nonnull ConfigData o) {
    return toString().compareTo(o.toString());
  }

  public <T> T deserialize(Class<T> c, Type... types) {
    try {
      c = Primitives.wrap(c);
      this.types = types;
      if (objectData != null)
        return (T) objectData;
      String str = stringData == null ? "" : stringData;

      if (str.startsWith("‼")) {
        str = str.substring(1);
        int id = str.indexOf("‼");
        if (id != -1) {
          str = str.substring(0, id);
          String[] classNames = str.split("-");
          c = ConfigSerialization.realClass(classNames[0]);
          types = new Type[classNames.length - 1];
          for (int i = 1; i < classNames.length; i++) {
            types[i - 1] = ConfigSerialization.realClass(classNames[i]);
          }
          stringData = stringData.substring(id + 2);
          Serializer ser = ConfigSerialization.getSerializer(c);
          objectData = ser.fromData(this, c, types);
        }
      } else {
        Serializer ser = ConfigSerialization.getSerializer(c);
        objectData = ser.fromData(this, c, types);
      }
      stringData = null;
      mapData = null;
      listData = null;
      return (T) objectData;
    } catch (Throwable e) {
      SU.log(Main.pl, "§eError on deserializing \"§f" + this + "§e\" to class §f" + c.getName() + "§e.");
      SU.error(SU.cs, e, "SpigotLib", "gyurix");
      return null;
    }
  }

  public ConfigData getUnWrapped() {
    if (objectData != null)
      return serializeObject(objectData, types);
    ConfigData out = new ConfigData(stringData);
    if (mapData != null) {
      out.mapData = new LinkedHashMap<>();
      for (Entry<ConfigData, ConfigData> e : mapData.entrySet())
        out.mapData.put(e.getKey().getUnWrapped(), e.getValue().getUnWrapped());
    }
    if (listData != null) {
      out.listData = new ArrayList<>();
      for (ConfigData cd : listData)
        out.listData.add(cd.getUnWrapped());
    }
    return out;
  }

  public int hashCode() {
    return stringData == null ? objectData == null ? listData == null ? mapData == null ? 0 :
            mapData.hashCode() : listData.hashCode() : objectData.hashCode() : stringData.hashCode();
  }

  public boolean equals(Object obj) {
    return obj instanceof ConfigData && ((ConfigData) obj).stringData.equals(stringData);
  }

  public String toString() {
    StringBuilder out = new StringBuilder();
    if (objectData != null)
      return getUnWrapped().toString();
    if (stringData != null && !stringData.isEmpty())
      out.append(escape(stringData));
    if (mapData != null && !mapData.isEmpty()) {
      for (Entry<ConfigData, ConfigData> d : mapData.entrySet()) {
        ConfigData v = d.getValue();
        String value = d.getValue().toString();
        if (d.getKey().comment != null)
          out.append("\n#").append(d.getKey().comment.replace("\n", "\n#")).append('\n');
        if (v.mapData != null)
          value = value.replace("\n", "\n  ");
        if (!value.startsWith("\n") && !value.isEmpty())
          value = " " + value;
        String key = d.getKey().toString();
        out.append('\n');
        if (key.contains("\n"))
          out.append("> ").append(key).append("\n:").append(value);
        else
          out.append(key).append(":").append(value);
      }
    }
    if (listData != null && !listData.isEmpty()) {
      for (ConfigData d : listData) {
        String data = d.toString();
        if (d.comment != null)
          out.append('\n').append('#').append(d.comment.replace("\n", "\n#"));
        out.append('\n').append("- ").append(data.replace("\n", "\n  "));
      }
    }
    return out.toString();
  }

  public boolean isEmpty() {
    return (stringData == null || stringData.isEmpty()) && listData == null && mapData == null && objectData == null;
  }

  public void saveToMySQL(ArrayList<String> l, String dbTable, String args, String key) {
    leftPad = 16;
    ConfigData cd = objectData == null ? this : serializeObject(objectData, types);
    if (cd.mapData != null) {
      if (!key.isEmpty())
        key += ".";
      for (Entry<ConfigData, ConfigData> e : cd.mapData.entrySet()) {
        e.getValue().saveToMySQL(l, dbTable, args.replace("<key>", MySQLDatabase.escape(key) + "<key>"), e.getKey().toString());
      }
    } else {
      String value = cd.toString();
      if (value != null)
        l.add("INSERT INTO  `" + dbTable + "` VALUES (" + args.replace("<key>", MySQLDatabase.escape(key)).replace("<value>", MySQLDatabase.escape(value)) + ')');
    }
    leftPad = 0;
  }

  public void unWrap() {
    if (objectData == null)
      return;
    ConfigData cd = serializeObject(objectData, types);
    objectData = null;
    types = null;
    listData = cd.listData;
    mapData = cd.mapData;
    stringData = cd.stringData;
  }

  public void unWrapAll() {
    if (objectData != null) {
      unWrap();
      return;
    }
    if (mapData != null)
      for (Entry<ConfigData, ConfigData> e : mapData.entrySet()) {
        e.getKey().unWrapAll();
        e.getValue().unWrapAll();
      }
    if (listData != null)
      for (ConfigData cd : listData)
        cd.unWrapAll();
  }
}