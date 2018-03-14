package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.utils.BlockLocation;
import gyurix.protocol.wrappers.WrappedPacket;

/**
 * Created by gyurix on 25/11/2015.
 */
public class PacketPlayOutBed extends WrappedPacket {
    public BlockLocation bed;
    public int entityId;

    public PacketPlayOutBed() {
    }

    public PacketPlayOutBed(int entityId, BlockLocation bed) {
        this.entityId = entityId;
        this.bed = bed;
    }

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.Bed.newPacket(entityId, bed.toNMS());
    }

    @Override
    public void loadVanillaPacket(Object obj) {
        Object[] data = PacketOutType.Bed.getPacketData(obj);
        entityId = (int) data[0];
        bed = new BlockLocation(data[1]);
    }
}
