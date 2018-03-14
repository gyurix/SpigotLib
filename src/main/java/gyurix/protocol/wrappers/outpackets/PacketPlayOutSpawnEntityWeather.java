package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;

/**
 * Created by GyuriX on 2016.03.08..
 */
public class PacketPlayOutSpawnEntityWeather extends WrappedPacket {
    public int entityId;
    /**
     * 1 - thunderbolt
     */
    public int type;
    public double x;
    public double y;
    public double z;

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.SpawnEntityWeather.newPacket(entityId, x, y, z, type);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = PacketOutType.SpawnEntityWeather.getPacketData(packet);
        entityId = (int) d[0];
        x = (double) d[1];
        y = (double) d[2];
        z = (double) d[3];
        type = (int) d[4];
    }
}
