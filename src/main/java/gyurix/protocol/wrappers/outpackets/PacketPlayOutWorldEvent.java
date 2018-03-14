package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.utils.BlockLocation;
import gyurix.protocol.wrappers.WrappedPacket;

/**
 * Created by gyurix on 25/11/2015.
 */
public class PacketPlayOutWorldEvent extends WrappedPacket {
    public int data;
    public boolean disableRelVolume;
    public int effectId;
    public BlockLocation loc;

    public PacketPlayOutWorldEvent() {

    }

    public PacketPlayOutWorldEvent(int effectId, BlockLocation loc, int data, boolean disableRelVolume) {
        this.effectId = effectId;
        this.loc = loc;
        this.data = data;
        this.disableRelVolume = disableRelVolume;
    }


    @Override
    public Object getVanillaPacket() {
        return PacketOutType.WorldEvent.newPacket(effectId, loc.toNMS(), data, disableRelVolume);
    }

    @Override
    public void loadVanillaPacket(Object obj) {
        Object[] d = PacketOutType.WorldEvent.getPacketData(obj);
        effectId = (int) d[0];
        loc = new BlockLocation(d[1]);
        data = (int) d[2];
        disableRelVolume = (boolean) d[3];
    }
}
