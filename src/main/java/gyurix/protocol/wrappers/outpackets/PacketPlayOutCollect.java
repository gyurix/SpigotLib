package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;

/**
 * Created by gyurix on 25/11/2015.
 */
public class PacketPlayOutCollect extends WrappedPacket {
    public int collectedID, collectorID;

    public PacketPlayOutCollect() {
    }

    public PacketPlayOutCollect(int collectedID, int collectorID) {
        this.collectedID = collectedID;
        this.collectorID = collectorID;
    }

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.Collect.newPacket(collectedID, collectorID);
    }

    @Override
    public void loadVanillaPacket(Object obj) {
        Object[] data = PacketOutType.Collect.getPacketData(obj);
        collectedID = (int) data[0];
        collectorID = (int) data[1];
    }
}
