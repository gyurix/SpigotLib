package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotlib.SU;

import java.lang.reflect.Method;

public class PacketPlayOutWorldParticles extends WrappedPacket {
    private static final Method enumParticleValueOf = Reflection.getMethod(Reflection.getNMSClass("EnumParticle"), "valueOf", String.class);
    public int count;
    public int[] extraData;
    public boolean longDistance;
    public String particle;
    public float x, y, z, offsetX, offsetY, offsetZ, data;

    public PacketPlayOutWorldParticles() {

    }

    public PacketPlayOutWorldParticles(String particle, float x, float y, float z, float offsetX, float offsetY, float offsetZ, int count, float data, int[] extraData, boolean longDistance) {
        this.particle = particle;
        this.count = count;
        this.extraData = extraData;
        this.longDistance = longDistance;
        this.x = x;
        this.y = y;
        this.z = z;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.data = data;
    }


    @Override
    public Object getVanillaPacket() {
        try {
            return PacketOutType.WorldParticles.newPacket(enumParticleValueOf.invoke(null, particle), x, y, z, offsetX, offsetY, offsetZ, data, count, longDistance, extraData);
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
            return null;
        }
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = PacketOutType.WorldParticles.getPacketData(packet);
        particle = d[0].toString();
        x = (float) d[1];
        y = (float) d[2];
        z = (float) d[3];
        offsetX = (float) d[4];
        offsetY = (float) d[5];
        offsetZ = (float) d[6];
        data = (float) d[7];
        count = (int) d[8];
        longDistance = (boolean) d[9];
        extraData = (int[]) d[10];
    }
}
