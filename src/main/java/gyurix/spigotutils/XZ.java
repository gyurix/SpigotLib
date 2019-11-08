package gyurix.spigotutils;

import gyurix.configfile.ConfigSerialization.ConfigOptions;
import gyurix.configfile.ConfigSerialization.StringSerializable;
import gyurix.protocol.Reflection;
import gyurix.protocol.utils.WrappedData;
import gyurix.spigotlib.SU;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * An utility class for storing an X and Z coordinate pair
 */
public class XZ implements StringSerializable, Comparable<XZ>, WrappedData {
  @ConfigOptions(serialize = false)
  private static final Class nmsClass = Reflection.getNMSClass("ChunkCoordIntPair");
  @ConfigOptions(serialize = false)
  private static final Constructor con = Reflection.getConstructor(nmsClass, int.class, int.class);
  @ConfigOptions(serialize = false)
  private static final Field xField = Reflection.getField(nmsClass, "x"), zField = Reflection.getField(nmsClass, "z");
  public int x, z;

  public XZ(String in) {
    String[] d = in.split(" ", 2);
    x = Integer.parseInt(d[0]);
    z = Integer.parseInt(d[1]);
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
    return Integer.compare(hashCode(), o.hashCode());
  }

  @Override
  public int hashCode() {
    return x << 16 + z;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof XZ))
      return false;
    XZ xz = (XZ) obj;
    return x == xz.x && z == xz.z;
  }

  @Override
  public String toString() {
    return x + " " + z;
  }

  public UnlimitedYArea toArea() {
    return new UnlimitedYArea((x << 4), (z << 4), (x << 4) + 15, (z << 4) + 15);
  }

  public Chunk toChunk(World w) {
    return w.getChunkAt(x, z);
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
