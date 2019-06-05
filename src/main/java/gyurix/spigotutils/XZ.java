package gyurix.spigotutils;

import gyurix.configfile.ConfigSerialization;
import gyurix.configfile.ConfigSerialization.StringSerializable;
import gyurix.protocol.Reflection;
import gyurix.protocol.utils.WrappedData;
import gyurix.spigotlib.SU;
import org.bukkit.Chunk;
import org.bukkit.block.Block;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * An utility class for storing an X and Z coordinate pair
 */
public class XZ implements StringSerializable, Comparable<XZ>, WrappedData {
  @ConfigSerialization.ConfigOptions(serialize = false)
  private static Class nmsClass = Reflection.getNMSClass("ChunkCoordIntPair");
  @ConfigSerialization.ConfigOptions(serialize = false)
  private static Constructor con = Reflection.getConstructor(nmsClass, int.class, int.class);
  @ConfigSerialization.ConfigOptions(serialize = false)
  private static Field xField = Reflection.getField(nmsClass, "x"), zField = Reflection.getField(nmsClass, "z");
  public int x, z;

  public XZ(String in) {
    String[] d = in.split(" ", 2);
    x = Integer.valueOf(d[0]);
    z = Integer.valueOf(d[1]);
  }

  public XZ(int x, int z) {
    this.x = x;
    this.z = z;
  }

  public XZ(Block bl) {
    x = bl.getX();
    z = bl.getZ();
  }

  public XZ(Object nms) {
    try {
      x = xField.getInt(nms);
      z = zField.getInt(nms);
    } catch (IllegalAccessException e) {
      SU.error(SU.cs, e, "SpigotLib", "gyurix");
    }
  }

  public XZ(Chunk c) {
    x = c.getX();
    z = c.getZ();
  }

  @Override
  public int compareTo(XZ o) {
    return ((Integer) hashCode()).compareTo(o.hashCode());
  }

  @Override
  public int hashCode() {
    return x << 16 + z;
  }

  @Override
  public boolean equals(Object obj) {
    XZ xz = (XZ) obj;
    return x == xz.x && z == xz.z;
  }

  @Override
  public String toString() {
    return x + " " + z;
  }

  @Override
  public Object toNMS() {
    try {
      return con.newInstance(x, z);
    } catch (Throwable e) {
      SU.error(SU.cs, e, "SpigotLib", "gyurix");
    }
    return null;
  }
}
