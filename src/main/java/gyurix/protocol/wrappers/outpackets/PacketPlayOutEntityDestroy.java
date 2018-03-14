package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;

/**
 * Created by GyuriX on 2016.03.08..
 */
public class PacketPlayOutEntityDestroy extends WrappedPacket {
    public int[] entityIds;

    public PacketPlayOutEntityDestroy() {

    }

    public PacketPlayOutEntityDestroy(int... eids) {
        entityIds = eids;
    }

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.EntityDestroy.newPacket(entityIds);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        entityIds = (int[]) PacketOutType.EntityDestroy.getPacketData(packet)[0];
    }
}
