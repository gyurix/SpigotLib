package gyurix.protocol.utils;

import gyurix.protocol.Reflection;
import gyurix.spigotlib.SU;
import gyurix.spigotutils.BlockUtils;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import java.lang.reflect.Method;

public enum Direction implements WrappedData {
    DOWN(new Vector(0, -1, 0)), UP(new Vector(0, 1, 0)), SOUTH(new Vector(0, 0, 1)), WEST(new Vector(-1, 0, 0)), NORTH(new Vector(0, 0, -1)), EAST(new Vector(1, 0, 0));
    private static final Method valueOf = Reflection.getMethod(Reflection.getNMSClass("EnumDirection"), "valueOf", String.class);
    private Vector v;
    private float yaw, pitch;

    Direction(Vector v) {
        this.v = v;
        yaw = BlockUtils.getYaw(v);
        pitch = BlockUtils.getPitch(v);
    }

    public static Direction get(int id) {
        if (id >= 0 && id < 6)
            return Direction.values()[id];
        return null;
    }

    public static Direction get(BlockFace face) {
        return valueOf(face.name());
    }

    public static Direction get(Vector v) {
        return get(BlockUtils.getYaw(v), BlockUtils.getPitch(v));
    }

    private static Direction get(float yaw, float pitch) {
        yaw = (yaw + 405) % 360;
        return pitch > 45 ? DOWN : pitch < -45 ? UP : yaw < 90 ? SOUTH : yaw < 180 ? WEST : yaw < 270 ? NORTH : EAST;
    }

    public static void main(String[] args) {
    }


    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public BlockFace toBlockFace() {
        return BlockFace.valueOf(name());
    }

    @Override
    public Object toNMS() {
        try {
            return valueOf.invoke(null, name());
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
            return null;
        }
    }

    public Vector toVector() {
        return v.clone();
    }
}

