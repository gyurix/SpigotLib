package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;

/**
 * Created by GyuriX on 2016.03.08..
 */
public class PacketPlayOutEntity extends WrappedPacket {
    public int entityId;

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.Entity.newPacket(entityId);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        entityId = (int) PacketOutType.Entity.getPacketData(packet)[0];
    }
}
