package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotutils.ServerVersion;

/**
 * Created by GyuriX on 2016.03.08..
 */
public class PacketPlayOutSpawnEntityExperienceOrb extends WrappedPacket {
    public int count;
    public int entityId;
    public double x;
    public double y;
    public double z;

    @Override
    public Object getVanillaPacket() {
        return Reflection.ver.isAbove(ServerVersion.v1_9) ?
                PacketOutType.SpawnEntityExperienceOrb.newPacket(entityId, x, y, z, count) :
                PacketOutType.SpawnEntityExperienceOrb.newPacket(entityId, (int) (x * 32), (int) (y * 32), (int) (z * 32), count);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = PacketOutType.SpawnEntityExperienceOrb.getPacketData(packet);
        entityId = (int) d[0];
        if (Reflection.ver.isAbove(ServerVersion.v1_9)) {
            x = (double) d[1];
            y = (double) d[2];
            z = (double) d[3];
        } else {
            x = ((int) d[1]) / 32.0;
            y = ((int) d[2]) / 32.0;
            z = ((int) d[3]) / 32.0;
        }
        count = (int) d[4];

    }
}
