package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.utils.BlockLocation;
import gyurix.protocol.wrappers.WrappedPacket;

/**
 * Created by GyuriX on 2016.02.28..
 */
public class PacketPlayOutOpenSignEditor extends WrappedPacket {
    public BlockLocation loc;

    public PacketPlayOutOpenSignEditor() {

    }

    public PacketPlayOutOpenSignEditor(BlockLocation loc) {
        this.loc = loc;
    }

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.OpenSignEditor.newPacket(loc.toNMS());
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = PacketOutType.OpenSignEditor.getPacketData(packet);
        loc = new BlockLocation(d[0]);
    }
}
