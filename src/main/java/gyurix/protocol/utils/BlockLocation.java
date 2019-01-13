package gyurix.protocol.utils;

import gyurix.configfile.ConfigSerialization.StringSerializable;
import gyurix.protocol.Reflection;
import gyurix.spigotlib.SU;
import gyurix.spigotutils.LocationData;
import gyurix.spigotutils.ServerVersion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class BlockLocation implements WrappedData, StringSerializable {
    public static final BlockLocation notDefined = new BlockLocation(0, 0, 0);
    private static final Class baseCl = Reflection.getNMSClass("BaseBlockPosition");
    private static final Class cl = Reflection.getNMSClass("BlockPosition");
    private static final Constructor con = Reflection.getConstructor(cl, int.class, int.class, int.class);
    private static final Method getX = Reflection.getMethod(Reflection.ver.isAbove(ServerVersion.v1_9) ? cl : baseCl, "getX");
    private static final Method getY = Reflection.getMethod(Reflection.ver.isAbove(ServerVersion.v1_9) ? cl : baseCl, "getY");
    private static final Method getZ = Reflection.getMethod(Reflection.ver.isAbove(ServerVersion.v1_9) ? cl : baseCl, "getZ");
    public int x;
    public int y;
    public int z;

    public BlockLocation() {

    }

    public BlockLocation(String in) {
        String[] d = in.split(" ", 3);
        x = Integer.valueOf(d[0]);
        y = Integer.valueOf(d[1]);
        z = Integer.valueOf(d[2]);
    }

    public BlockLocation(Block bl) {
        this(bl.getX(), bl.getY(), bl.getZ());
    }

    public BlockLocation(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockLocation(Location loc) {
        this(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public BlockLocation(LocationData loc) {
        this.x = (int) loc.x;
        this.y = (int) loc.y;
        this.z = (int) loc.z;
    }

    public BlockLocation(Object nmsData) {
        try {
            x = (int) getX.invoke(nmsData);
            y = (int) getY.invoke(nmsData);
            z = (int) getZ.invoke(nmsData);
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
    }

    public BlockLocation add(BlockLocation bl) {
        return add(bl.x, bl.y, bl.z);
    }

    public BlockLocation add(Vector v) {
        return add(v.getBlockX(), v.getBlockY(), v.getBlockZ());
    }

    public BlockLocation add(int x, int y, int z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public BlockLocation subtract(Vector v) {
        return add(v.clone().multiply(-1));
    }

    public BlockLocation subtract(BlockLocation bl) {
        return add(-bl.x, -bl.y, -bl.z);
    }

    public BlockLocation subtract(int x, int y, int z) {
        return add(-x, -y, -z);
    }

    public Block getBlock(World w) {
        return w.getBlockAt(x, y, z);
    }

    public Location getLocation(World w) {
        return new Location(w, x, y, z);
    }

    @Override
    public int hashCode() {
        return (x << 20) + (y << 12) + z;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BlockLocation))
            return false;
        BlockLocation bl = (BlockLocation) obj;
        return bl.x == x && bl.y == y && bl.z == z;
    }

    public BlockLocation clone() {
        return new BlockLocation(x, y, z);
    }

    @Override
    public String toString() {
        return x + " " + y + ' ' + z;
    }

    public boolean isDefined() {
        return x != 0 || y != 0 || z != 0;
    }

    @Override
    public Object toNMS() {
        try {
            return con.newInstance(x, y, z);
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
        return null;
    }
}

