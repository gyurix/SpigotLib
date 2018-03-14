package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.utils.WrappedData;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotlib.SU;
import gyurix.spigotutils.ServerVersion;
import org.bukkit.Location;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import static gyurix.protocol.wrappers.outpackets.PacketPlayOutPosition.PlayerTeleportFlag.*;

/**
 * Created by GyuriX on 2016.08.23..
 */
public class PacketPlayOutPosition extends WrappedPacket {
    public static final HashSet<PlayerTeleportFlag> allFlags = new HashSet<>();
    public static final HashSet<PlayerTeleportFlag> coordFlags = new HashSet<>();
    public static final HashSet<PlayerTeleportFlag> rotationFlags = new HashSet<>();

    static {
        coordFlags.add(X);
        coordFlags.add(Y);
        coordFlags.add(Z);
        rotationFlags.add(X_ROT);
        rotationFlags.add(Y_ROT);
        allFlags.addAll(coordFlags);
        allFlags.addAll(rotationFlags);
    }

    public HashSet<PlayerTeleportFlag> flags;
    public float pitch;
    public int teleportId = 2147483647;
    public double x;
    public double y;
    public float yaw;
    public double z;

    public PacketPlayOutPosition() {
    }

    public PacketPlayOutPosition(double x, double y, double z) {
        flags = coordFlags;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public PacketPlayOutPosition(float yaw, float pitch) {
        flags = rotationFlags;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public PacketPlayOutPosition(Location loc) {
        setLocation(loc);
    }

    public PacketPlayOutPosition(Object packet) {
        loadVanillaPacket(packet);
    }

    public HashSet<Object> getVanillaFlags() {
        HashSet<Object> out = new HashSet<>();
        for (PlayerTeleportFlag f : flags)
            out.add(f.toNMS());
        return out;
    }

    private void setVanillaFlags(Set set) {
        flags = new HashSet<>();
        for (Object o : set)
            flags.add(fromVanillaPTF(o));
    }

    @Override
    public Object getVanillaPacket() {
        return Reflection.ver.isAbove(ServerVersion.v1_9) ? PacketOutType.Position.newPacket(x, y, z, yaw, pitch, getVanillaFlags(), teleportId) :
                PacketOutType.Position.newPacket(x, y, z, yaw, pitch, getVanillaFlags());
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = PacketOutType.Position.getPacketData(packet);
        x = (double) d[0];
        y = (double) d[1];
        z = (double) d[2];
        yaw = (float) d[3];
        pitch = (float) d[4];
        setVanillaFlags((Set) d[5]);
        if (Reflection.ver.isAbove(ServerVersion.v1_9))
            teleportId = (int) d[6];
    }

    public void setLocation(Location loc) {
        flags = allFlags;
        x = loc.getX();
        y = loc.getY();
        z = loc.getZ();
        yaw = loc.getYaw();
        pitch = loc.getPitch();
    }

    public enum PlayerTeleportFlag implements WrappedData {
        X,
        Y,
        Z,
        Y_ROT,
        X_ROT;
        private static final Class cl = Reflection.getNMSClass("PacketPlayOutPosition$EnumPlayerTeleportFlags");
        private static final Method valueOf = Reflection.getMethod(cl, "valueOf", String.class);

        public static PlayerTeleportFlag fromVanillaPTF(Object nms) {
            return valueOf(nms.toString());
        }

        @Override
        public Object toNMS() {
            try {
                return valueOf.invoke(null, name());
            } catch (Throwable e) {
                SU.error(SU.cs, e, "SpigotLib", "gyurix");
            }
            return null;
        }
    }
}
