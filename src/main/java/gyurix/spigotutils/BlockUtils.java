package gyurix.spigotutils;

import gyurix.protocol.utils.Direction;
import gyurix.spigotlib.SU;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static gyurix.protocol.Reflection.*;
import static java.lang.Math.*;

/**
 * Created by GyuriX on 2016. 07. 13..
 */
public class BlockUtils {
  private static Method getBlockByIdM;
  private static Method getCombinedIdM;
  private static Field matIdF, bdStateF, blockF;
  private static Map<Integer, Material> materialById;
  private static Method nmsBlockToMaterialM;

  static {
    try {
      Class<?> blCl = getNMSClass("Block");
      Class<?> bdCl = getNMSClass("IBlockData");
      blockF = getFirstFieldOfType(bdCl, blCl);
      matIdF = getField(Material.class, "id");
      materialById = new HashMap<>();
      for (Material m : Material.values())
        materialById.put(matIdF.getInt(m), m);
      if (ver.isAbove(ServerVersion.v1_13)) {
        bdStateF = getField(getOBCClass("block.data.CraftBlockData"), "state");
        nmsBlockToMaterialM = getMethod(getOBCClass("util.CraftMagicNumbers"), "getMaterial", blCl);
      }
      getBlockByIdM = getMethod(blCl, "getByCombinedId", int.class);
      getCombinedIdM = getMethod(blCl, "getCombinedId", bdCl);
    } catch (Throwable e) {
      SU.error(SU.cs, e, "SpigotLib", "gyurix");
    }
  }

  public static BlockData combinedIdToBlockData(int id) {
    if (ver.isAbove(ServerVersion.v1_13)) {
      try {
        return new BlockData((Material) nmsBlockToMaterialM.invoke(null, blockF.get(combinedIdToNMSBlockData(id))));
      } catch (Throwable e) {
        SU.error(SU.cs, e, "SpigotLib", "gyurix");
      }
    }
    return new BlockData(materialById.get(id & 4095), (short) (id >> 12 & 15));
  }

  public static Object combinedIdToNMSBlockData(int id) {
    try {
      return getBlockByIdM.invoke(null, id);
    } catch (Throwable e) {
      SU.error(SU.cs, e, "SpigotLib", "gyurix");
    }
    return null;
  }

  public static int getCombinedId(BlockData bd) {
    if (ver.isAbove(ServerVersion.v1_13)) {
      try {
        return getCombinedId(bdStateF.get(bd.getType().createBlockData()));
      } catch (Throwable e) {
        SU.error(SU.cs, e, "SpigotLib", "gyurix");
      }
    }
    return (((int) bd.data) << 12) + bd.getType().getId();
  }

  public static int getCombinedId(Object nmsBlock) {
    try {
      return (int) getCombinedIdM.invoke(null, nmsBlock);
    } catch (Throwable e) {
      e.printStackTrace();
    }
    return 0;
  }

  public static Vector getDirection(float yaw, float pitch) {
    double xz = cos(toRadians(pitch));
    return new Vector(-xz * sin(toRadians(yaw)), -sin(toRadians(pitch)), xz * cos(toRadians(yaw)));
  }

  public static float getPitch(Vector vec) {
    double x = vec.getX();
    double z = vec.getZ();
    if (x == 0 && z == 0)
      return vec.getY() == 0 ? 0 : vec.getY() > 0 ? 90 : -90;
    return (float) toDegrees(atan(-vec.getY() / sqrt(x * x + z * z)));
  }

  public static byte getSignDurability(float yaw) {
    yaw -= 168.75f;
    while (yaw < 0)
      yaw += 360;
    return (byte) (yaw / 22.5f);
  }

  public static Direction getSimpleDirection(float yaw, float pitch) {
    while (yaw < 45)
      yaw += 360;
    yaw -= 45;
    return pitch > 45 ? Direction.DOWN : pitch < -45 ? Direction.UP : Direction.values()[(int) (yaw / 90 + 2)];
  }

  public static Location getYMax(World world, int minx, int minz) {
    for (int y = 255; y > 0; y--) {
      Block b = world.getBlockAt(minx, y, minz);
      if (b.getType().isSolid())
        return b.getLocation();
    }
    return new Location(world, minx, 1, minz);
  }

  public static float getYaw(Vector vec) {
    return (float) ((toDegrees(atan2(-vec.getX(), vec.getZ())) + 720) % 360);
  }
}
