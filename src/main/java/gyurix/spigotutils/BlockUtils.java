package gyurix.spigotutils;

import gyurix.protocol.Reflection;
import gyurix.protocol.utils.Direction;
import gyurix.spigotlib.SU;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.lang.reflect.Method;
import java.util.Map;

import static gyurix.protocol.Reflection.getMethod;
import static gyurix.protocol.Reflection.getNMSClass;
import static java.lang.Math.*;

/**
 * Created by GyuriX on 2016. 07. 13..
 */
public class BlockUtils {
    private static Method getBlockByIdM, getBlockIdM, fromLegacyDataM, toLegacyDataM, getCombinedIdM;
    private static Map<Object, Integer> nmsBlockIDMap;

    static {
        try {
            Class regIDCl = getNMSClass("RegistryID");
            Class blCl = getNMSClass("Block");
            if (Reflection.ver.isBellow(ServerVersion.v1_11) && Reflection.ver.isAbove(ServerVersion.v1_8))
                nmsBlockIDMap = (Map<Object, Integer>) Reflection.getFieldData(regIDCl, "a", Reflection.getFirstFieldOfType(blCl, regIDCl).get(null));

            getBlockByIdM = getMethod(blCl, "getById", int.class);
            getBlockIdM = getMethod(blCl, "getId", blCl);
            getCombinedIdM = getMethod(blCl, "getCombinedId", getNMSClass("IBlockData"));
            fromLegacyDataM = getMethod(blCl, "fromLegacyData", int.class);
            toLegacyDataM = getMethod(blCl, "toLegacyData", getNMSClass("IBlockData"));
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
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

    public static Object getNMSBlock(int id, byte data) {
        try {
            return fromLegacyDataM.invoke(getBlockByIdM.invoke(null, id), (int) data);
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
        return null;
    }

    public static byte getNMSBlockData(Object nmsBlock) {
        try {
            Integer id = nmsBlockIDMap.get(nmsBlock);
            if (id == null) {
                SU.cs.sendMessage("§cBlock §e" + nmsBlock + "§c was not found.");
                return 0;
            }
            Object blockType = getBlockByIdM.invoke(null, id);
            return (byte) (int) toLegacyDataM.invoke(blockType, nmsBlock);
        } catch (Throwable e) {
            return 0;
        }
    }

    public static int getNMSBlockId(Object nmsBlock) {
        Integer i = nmsBlockIDMap.get(nmsBlock);
        return i == null ? -1 : i;
    }

    public static Object getNMSBlockType(int id) {
        try {
            return getBlockByIdM.invoke(null, id);
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
        return null;
    }

    public static int getNMSBlockTypeId(Object nmsBlock) {
        try {
            return (int) getBlockIdM.invoke(null, nmsBlock);
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
            return 0;
        }
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
