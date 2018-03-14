package gyurix.protocol.wrappers.inpackets;

import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketInType;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotutils.ServerVersion;

public class PacketPlayInFlying extends WrappedPacket {
    public boolean hasLook;
    public boolean hasPos;
    public double headY;
    public boolean onGround;
    public float pitch;
    public double x;
    public double y;
    public float yaw;
    public double z;

    @Override
    public Object getVanillaPacket() {
        return Reflection.ver.isAbove(ServerVersion.v1_8) ?
                PacketInType.Flying.newPacket(x, y, z, yaw, pitch, onGround, hasPos, hasLook) :
                PacketInType.Flying.newPacket(x, y, z, headY, yaw, pitch, onGround, hasPos, hasLook);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketInType.Flying.getPacketData(packet);
        x = (Double) data[0];
        y = (Double) data[1];
        z = (Double) data[2];
        int start = 2;
        if (Reflection.ver.isBellow(ServerVersion.v1_7)) {
            headY = (Double) data[++start];
        }
        yaw = (Float) data[start + 1];
        pitch = (Float) data[start + 2];
        onGround = (Boolean) data[start + 3];
        hasPos = (Boolean) data[start + 4];
        hasLook = (Boolean) data[start + 5];
    }
}

